package cz.petrschopp.skladovysystem.ui.items

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.petrschopp.skladovysystem.data.model.ItemDto
import cz.petrschopp.skladovysystem.data.remote.ApiClient
import cz.petrschopp.skladovysystem.ui.items.components.ItemSearchSheet
import cz.petrschopp.skladovysystem.ui.scanner.BarcodeScanPanel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScannerScreen(
    onBack: () -> Unit,
    onItemFound: (ItemDto) -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isSearching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var manualInputVisible by remember { mutableStateOf(false) }
    var unknownCode by remember { mutableStateOf<String?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<ItemDto>>(emptyList()) }
    var manualSearching by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    fun searchByCode(code: String) {
        scope.launch {
            isSearching = true
            errorMessage = null

            try {
                val results = ApiClient.api.searchItems(
                    warehouseId = 1,
                    query = code.trim()
                )

                isSearching = false

                if (results.isEmpty()) {
                    unknownCode = code.trim()
                } else {
                    onItemFound(results.first())
                }
            } catch (e: Exception) {
                isSearching = false
                errorMessage = e.message ?: "Nepodařilo se vyhledat položku."
            }
        }
    }

    fun updateManualSearch(value: String) {
        searchQuery = value
        errorMessage = null

        searchJob?.cancel()

        if (value.trim().isEmpty()) {
            searchResults = emptyList()
            manualSearching = false
            return
        }

        searchJob = scope.launch {
            delay(300)
            manualSearching = true

            try {
                searchResults = ApiClient.api.searchItems(
                    warehouseId = 1,
                    query = value.trim()
                )
                manualSearching = false
            } catch (e: Exception) {
                manualSearching = false
                errorMessage = e.message ?: "Nepodařilo se vyhledat položky."
            }
        }
    }

    BackHandler(enabled = unknownCode != null || manualInputVisible) {
        when {
            unknownCode != null -> unknownCode = null
            manualInputVisible -> manualInputVisible = false
        }
    }

    if (unknownCode != null) {
        ResolveUnknownCodeScreen(
            code = unknownCode!!,
            allowCreateNew = true,
            onBack = { unknownCode = null },
            onItemReady = { item ->
                unknownCode = null
                onItemFound(item)
            }
        )
        return
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        BarcodeScanPanel(
            title = "Hledat položku",
            isSearching = isSearching,
            cameraHeightDp = 420,
            errorMessage = errorMessage,
            onCodeScanned = { code ->
                searchByCode(code)
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
                onDismissRequest = { manualInputVisible = false },
                sheetState = sheetState
            ) {
                ItemSearchSheet(
                    title = "Ruční hledání",
                    searchQuery = searchQuery,
                    searchResults = searchResults,
                    isSearching = manualSearching,
                    actionButtonText = "Založit nový produkt",
                    onSearchChange = { updateManualSearch(it) },
                    onItemClick = { item ->
                        manualInputVisible = false
                        onItemFound(item)
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