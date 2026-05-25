package cz.petrschopp.skladovysystem.ui.items

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import cz.petrschopp.skladovysystem.data.model.ItemDto
import cz.petrschopp.skladovysystem.ui.common.ErrorDialog
import cz.petrschopp.skladovysystem.ui.common.AppListCard
import cz.petrschopp.skladovysystem.ui.common.AutoRefreshEffect
import cz.petrschopp.skladovysystem.ui.common.SearchableListScreen
import cz.petrschopp.skladovysystem.ui.items.components.ItemRow
import java.text.Normalizer

@Composable
fun ItemsScreen(
    modifier: Modifier = Modifier,
    onDetailVisibleChange: (Boolean) -> Unit = {},
    viewModel: ItemsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedItem by remember { mutableStateOf<ItemDto?>(null) }
    var editingItem by remember { mutableStateOf<ItemDto?>(null) }
    var scannerVisible by remember { mutableStateOf(false) }

    val autoRefreshEnabled = selectedItem == null && !scannerVisible

    AutoRefreshEffect(
        enabled = autoRefreshEnabled,
        onRefresh = viewModel::loadItems
    )

    BackHandler(enabled = selectedItem != null || editingItem != null || scannerVisible) {
        when {
            editingItem != null -> editingItem = null
            selectedItem != null -> selectedItem = null
            scannerVisible -> scannerVisible = false
        }
    }

    LaunchedEffect(editingItem, selectedItem, scannerVisible) {
        onDetailVisibleChange(editingItem != null || selectedItem != null || scannerVisible)
    }

    when {
        editingItem != null -> {
            ItemEditScreen(
                item = editingItem!!,
                onBack = { editingItem = null },
                onSaved = { updatedItem ->
                    selectedItem = updatedItem
                    editingItem = null
                    viewModel.loadItems()
                }
            )
        }
        selectedItem != null -> {
            val itemMovements = uiState.movements.filter { it.itemId == selectedItem!!.id }
            ItemDetailScreen(
                item = selectedItem!!,
                movements = itemMovements,
                onBack = { selectedItem = null },
                onEdit = { editingItem = it },
                modifier = modifier
            )
        }
        scannerVisible -> {
            ItemScannerScreen(
                onBack = { scannerVisible = false },
                onItemFound = { item ->
                    selectedItem = item
                    scannerVisible = false
                    viewModel.loadItems()
                }
            )
        }
        else -> {
            ItemsListContent(
                uiState = uiState,
                onRefresh = { viewModel.loadItems() },
                onScanClick = { scannerVisible = true },
                onItemClick = { selectedItem = it },
                modifier = modifier
            )
        }
    }

    ErrorDialog(
        message = if (uiState.isNetworkError) null else uiState.errorMessage,
        onDismiss = { viewModel.clearError() }
    )
}

@Composable
private fun ItemsListContent(
    uiState: ItemsUiState,
    onRefresh: () -> Unit,
    onScanClick: () -> Unit,
    onItemClick: (ItemDto) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems by remember(uiState.items, searchQuery) {
        derivedStateOf {
            val query = normalizeSearchText(searchQuery.trim())

            if (query.isBlank()) {
                uiState.items
            } else {
                uiState.items.filter { item ->
                    normalizeSearchText(item.name).contains(query) ||
                            item.codes.any { code ->
                                normalizeSearchText(code.code).contains(query)
                            }
                }
            }
        }
    }

    SearchableListScreen(
        title = "Položky",
        uiState = uiState,
        data = filteredItems,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        searchLabel = "Hledat položku",
        emptyMessage = if (searchQuery.isBlank()) {
            "Ve skladu nejsou žádné položky."
        } else {
            "Nic nebylo nalezeno."
        },
        onRefresh = onRefresh,
        bottomButtonText = "Scan",
        onBottomButtonClick = onScanClick,
        modifier = modifier
    ) { items ->
        AppListCard(
            items = items,
            emptyText = "Nic nebylo nalezeno."
        ) { item ->
            ItemRow(
                item = item,
                onClick = { onItemClick(item) }
            )
        }
    }
}

private fun normalizeSearchText(value: String): String {
    return Normalizer.normalize(value.lowercase(), Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
}