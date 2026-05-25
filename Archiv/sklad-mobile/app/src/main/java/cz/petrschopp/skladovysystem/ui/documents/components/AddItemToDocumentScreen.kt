package cz.petrschopp.skladovysystem.ui.documents.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.petrschopp.skladovysystem.data.model.ItemDto
import cz.petrschopp.skladovysystem.ui.documents.DocumentFormUiState
import cz.petrschopp.skladovysystem.ui.items.ResolveUnknownCodeScreen
import cz.petrschopp.skladovysystem.ui.items.components.ItemSearchSheet
import cz.petrschopp.skladovysystem.ui.scanner.BarcodeScanPanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemToDocumentScreen(
    movementTypeCode: String,
    uiState: DocumentFormUiState,
    onBack: () -> Unit,
    onSearchChange: (String) -> Unit,
    onScannedCode: (String, (String) -> Unit) -> Unit,
    onSelectItem: (ItemDto) -> Unit,
    onClearSelectedItem: () -> Unit,
    onQuantityChange: (String) -> Unit,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit,
    onItemNoteChange: (String) -> Unit,
    onAddSelectedItem: () -> Unit,
    onShowToast: (String) -> Unit,
    onEditDraftItem: (Int) -> Unit,
    onCloseAddItemScreen: () -> Unit
) {
    var manualInputVisible by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var unknownCode by remember { mutableStateOf<String?>(null) }

    BackHandler(
        enabled = uiState.selectedItem != null || manualInputVisible || unknownCode != null
    ) {
        when {
            unknownCode != null -> unknownCode = null
            manualInputVisible -> manualInputVisible = false
            uiState.selectedItem != null -> {
                val isEditing = uiState.editingItemId != null

                onClearSelectedItem()

                if (isEditing) {
                    onCloseAddItemScreen()
                }
            }
        }
    }

    if (unknownCode != null) {
        ResolveUnknownCodeScreen(
            code = unknownCode!!,
            allowCreateNew = movementTypeCode == "IN",
            onBack = { unknownCode = null },
            onItemReady = { item ->
                onSelectItem(item)
                unknownCode = null
            }
        )
        return
    }

    if (uiState.selectedItem != null) {
        val isEditing = uiState.editingItemId != null

        SelectedItemFullScreen(
            item = uiState.selectedItem,
            movementTypeCode = movementTypeCode,
            isEditing = isEditing,
            quantityText = uiState.quantityText,
            itemNote = uiState.itemNote,
            onQuantityChange = onQuantityChange,
            onIncreaseQuantity = onIncreaseQuantity,
            onDecreaseQuantity = onDecreaseQuantity,
            onNoteChange = onItemNoteChange,
            onAdd = {
                if (movementTypeCode == "OUT") {
                    val quantity = uiState.quantityText.toIntOrNull() ?: 0
                    val stockQuantity = uiState.selectedItem.quantity
                        .replace(",", ".")
                        .toDoubleOrNull()
                        ?.toInt()
                        ?: 0

                    val alreadyInDraft = uiState.draftItems
                        .filter { it.item.id == uiState.selectedItem.id && it.item.id != uiState.editingItemId }
                        .sumOf { it.quantity }

                    if (alreadyInDraft + quantity > stockQuantity) {
                        onShowToast("Nelze vydat více kusů, než je aktuálně na skladě!")
                        return@SelectedItemFullScreen
                    }
                }

                onAddSelectedItem()
            },
            onBack = {
                onClearSelectedItem()

                if (isEditing) {
                    onCloseAddItemScreen()
                }
            }
        )
        return
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        BarcodeScanPanel(
            title = "Přidat položku",
            isSearching = uiState.isSearching,
            cameraHeightDp = 420,
            errorMessage = null,
            onCodeScanned = { code ->
                onScannedCode(code) { notFoundCode ->
                    unknownCode = notFoundCode
                }
            },
            onManualClick = {
                manualInputVisible = true
            },
            onBackClick = onBack,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )

        if (manualInputVisible) {
            ModalBottomSheet(
                onDismissRequest = {
                    manualInputVisible = false
                },
                sheetState = sheetState
            ) {
                ItemSearchSheet(
                    title = "Ruční zadání",
                    searchQuery = uiState.searchQuery,
                    searchResults = uiState.searchResults,
                    isSearching = uiState.isSearching,
                    actionButtonText = if (movementTypeCode == "IN") {
                        "Založit nový produkt"
                    } else {
                        "Vybrat existující produkt"
                    },
                    onSearchChange = onSearchChange,
                    onItemClick = { item ->
                        onSelectItem(item)
                        manualInputVisible = false
                    },
                    onActionClick = {
                        unknownCode = ""
                        manualInputVisible = false
                    }
                )
            }
        }
    }
}