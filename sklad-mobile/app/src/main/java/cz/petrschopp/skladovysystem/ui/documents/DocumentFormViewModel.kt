package cz.petrschopp.skladovysystem.ui.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.petrschopp.skladovysystem.data.model.CreateDocumentItemRequest
import cz.petrschopp.skladovysystem.data.model.CreateDocumentRequest
import cz.petrschopp.skladovysystem.data.model.ItemDto
import cz.petrschopp.skladovysystem.data.remote.ApiClient
import cz.petrschopp.skladovysystem.data.model.DocumentDetailDto
import cz.petrschopp.skladovysystem.ui.documents.model.DraftDocumentItem
import cz.petrschopp.skladovysystem.utils.isNetworkError
import cz.petrschopp.skladovysystem.utils.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DocumentFormUiState(
    val note: String = "",
    val searchQuery: String = "",
    val searchResults: List<ItemDto> = emptyList(),
    val draftItems: List<DraftDocumentItem> = emptyList(),

    val selectedItem: ItemDto? = null,
    val editingItemId: Int? = null,
    val editingDocumentId: Int? = null,

    val quantityText: String = "",
    val itemNote: String = "",

    val isSearching: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isNetworkError: Boolean = false
)

class DocumentFormViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DocumentFormUiState())
    val uiState: StateFlow<DocumentFormUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun updateNote(value: String) = updateState { copy(note = value) }

    fun updateSearchQuery(value: String) {
        updateState { copy(searchQuery = value, errorMessage = null) }

        searchJob?.cancel()

        if (value.trim().isEmpty()) {
            updateState {
                copy(
                    searchResults = emptyList(),
                    isSearching = false
                )
            }
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)

            updateState { copy(isSearching = true) }

            try {
                val results = ApiClient.api.searchItems(
                    warehouseId = 1,
                    query = value.trim()
                )

                updateState {
                    copy(
                        searchResults = results,
                        isSearching = false,
                        isNetworkError = false
                    )
                }
            } catch (e: Exception) {
                val isNetwork = e.isNetworkError()
                updateState {
                    copy(
                        isSearching = false,
                        errorMessage = e.toUserMessage("Nepodařilo se vyhledat položky."),
                        isNetworkError = isNetwork,
                        searchResults = if (isNetwork) emptyList() else searchResults
                    )
                }
            }
        }
    }

    fun selectItem(item: ItemDto) {
        updateState {
            copy(
                selectedItem = item,
                editingItemId = null,
                searchQuery = item.name,
                searchResults = emptyList(),
                quantityText = "1",
                itemNote = "",
                errorMessage = null
            )
        }
    }

    fun editDraftItem(itemId: Int) {
        val draftItem = _uiState.value.draftItems.firstOrNull { it.item.id == itemId }
            ?: return

        updateState {
            copy(
                selectedItem = draftItem.item,
                editingItemId = itemId,
                searchQuery = draftItem.item.name,
                searchResults = emptyList(),
                quantityText = draftItem.quantity.toString(),
                itemNote = draftItem.note.orEmpty(),
                errorMessage = null
            )
        }
    }

    fun updateQuantity(value: String) = updateState { copy(quantityText = value.filter { it.isDigit() }) }

    fun updateItemNote(value: String) = updateState { copy(itemNote = value) }

    fun addSelectedItem(movementTypeCode: String): Boolean {
        val state = _uiState.value
        val item = state.selectedItem

        if (item == null) {
            updateState { copy(errorMessage = "Nejdřív vyber položku.") }
            return false
        }

        val quantity = state.quantityText.trim().toIntOrNull()

        if (quantity == null || quantity <= 0) {
            updateState { copy(errorMessage = "Zadej celé kladné množství v kusech.") }
            return false
        }

        if (movementTypeCode == "OUT") {
            val stockQuantity = item.quantity.replace(",", ".").toDoubleOrNull()?.toInt() ?: 0

            val alreadyInDraft = state.draftItems
                .filter { it.item.id == item.id && it.item.id != state.editingItemId }
                .sumOf { it.quantity }

            if (alreadyInDraft + quantity > stockQuantity) {
                updateState {
                    copy(errorMessage = "Nelze vydat více kusů, než je aktuálně na skladě!")
                }
                return false
            }
        }

        val newNote = state.itemNote.ifBlank { null }

        val newItems = when {
            state.editingItemId != null -> {
                state.draftItems.map { draftItem ->
                    if (draftItem.item.id == state.editingItemId) {
                        draftItem.copy(
                            quantity = quantity,
                            note = newNote
                        )
                    } else {
                        draftItem
                    }
                }
            }

            state.draftItems.any { it.item.id == item.id } -> {
                state.draftItems.map { draftItem ->
                    if (draftItem.item.id == item.id) {
                        draftItem.copy(
                            quantity = draftItem.quantity + quantity,
                            note = newNote ?: draftItem.note
                        )
                    } else {
                        draftItem
                    }
                }
            }

            else -> {
                state.draftItems + DraftDocumentItem(
                    item = item,
                    quantity = quantity,
                    note = newNote
                )
            }
        }

        updateState {
            copy(
                draftItems = newItems,
                selectedItem = null,
                editingItemId = null,
                searchQuery = "",
                quantityText = "",
                itemNote = "",
                searchResults = emptyList(),
                errorMessage = null
            )
        }

        return true
    }

    fun removeDraftItem(itemId: Int) {
        updateState {
            copy(draftItems = draftItems.filterNot { it.item.id == itemId })
        }
    }

    fun saveDocument(
        movementTypeCode: String,
        userId: Int,
        onSaved: () -> Unit
    ) {
        val state = _uiState.value

        if (state.draftItems.isEmpty()) {
            updateState { copy(errorMessage = "Doklad musí obsahovat alespoň jednu položku.") }
            return
        }

        viewModelScope.launch {
            updateState {
                copy(
                    isSaving = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            try {
                val request = CreateDocumentRequest(
                    warehouseId = 1,
                    userId = userId,
                    note = state.note.ifBlank { null },
                    items = state.draftItems.map {
                        CreateDocumentItemRequest(
                            itemId = it.item.id,
                            quantity = it.quantity.toDouble(),
                            note = it.note
                        )
                    }
                )

                if (state.editingDocumentId != null) {
                    ApiClient.api.updateDocument(
                        documentId = state.editingDocumentId,
                        request = request
                    )
                } else {
                    if (movementTypeCode == "IN") {
                        ApiClient.api.createIncomeDocument(request)
                    } else {
                        ApiClient.api.createOutcomeDocument(request)
                    }
                }

                resetForm()
                onSaved()
            } catch (e: Exception) {
                updateState {
                    copy(
                        isSaving = false,
                        errorMessage = e.toUserMessage("Nepodařilo se uložit doklad.")
                    )
                }
            }
        }
    }

    fun handleScannedCode(
        code: String,
        onNotFound: (String) -> Unit
    ) {
        viewModelScope.launch {
            updateState {
                copy(
                    isSearching = true,
                    errorMessage = null
                )
            }

            try {
                val results = ApiClient.api.searchItems(
                    warehouseId = 1,
                    query = code.trim()
                )

                if (results.isEmpty()) {
                    updateState {
                        copy(
                            isSearching = false,
                            searchQuery = code,
                            searchResults = emptyList(),
                            selectedItem = null,
                            editingItemId = null
                        )
                    }

                    onNotFound(code)
                } else {
                    val item = results.first()

                    updateState {
                        copy(
                            isSearching = false,
                            searchQuery = item.name,
                            searchResults = emptyList(),
                            selectedItem = item,
                            editingItemId = null,
                            quantityText = "1",
                            itemNote = ""
                        )
                    }
                }
            } catch (e: Exception) {
                updateState {
                    copy(
                        isSearching = false,
                        errorMessage = e.toUserMessage("Nepodařilo se vyhledat položku podle kódu.")
                    )
                }
            }
        }
    }

    fun increaseQuantity() {
        val current = _uiState.value.quantityText.toIntOrNull() ?: 0
        updateState {
            copy(quantityText = (current + 1).toString())
        }
    }

    fun decreaseQuantity() {
        val current = _uiState.value.quantityText.toIntOrNull() ?: 1
        val newValue = (current - 1).coerceAtLeast(1)

        updateState {
            copy(quantityText = newValue.toString())
        }
    }

    fun clearSelectedItem() {
        updateState {
            copy(
                selectedItem = null,
                editingItemId = null,
                quantityText = "",
                itemNote = "",
                errorMessage = null
            )
        }
    }

    fun resetForm() {
        searchJob?.cancel()
        _uiState.value = DocumentFormUiState()
    }

    fun hasUnsavedChanges(): Boolean {
        val state = _uiState.value

        return state.note.isNotBlank() ||
                state.draftItems.isNotEmpty() ||
                state.selectedItem != null ||
                state.searchQuery.isNotBlank() ||
                state.quantityText.isNotBlank() ||
                state.itemNote.isNotBlank()
    }

    fun loadExistingDocumentForEdit(document: DocumentDetailDto) {
        val currentState = _uiState.value

        if (currentState.editingDocumentId == document.id) {
            return
        }

        viewModelScope.launch {
            updateState {
                copy(
                    isSearching = true,
                    errorMessage = null
                )
            }

            try {
                val allItems = ApiClient.api.getItems(warehouseId = document.warehouseId)

                val draftItems = document.items.mapNotNull { documentItem ->
                    val item = allItems.firstOrNull { it.id == documentItem.itemId }

                    item?.let {
                        DraftDocumentItem(
                            item = it,
                            quantity = documentItem.quantity.replace(",", ".").toDoubleOrNull()?.toInt() ?: 0,
                            note = documentItem.note
                        )
                    }
                }

                _uiState.value = DocumentFormUiState(
                    editingDocumentId = document.id,
                    note = document.note.orEmpty(),
                    draftItems = draftItems,
                    isSearching = false
                )
            } catch (e: Exception) {
                updateState {
                    copy(
                        isSearching = false,
                        errorMessage = e.toUserMessage("Nepodařilo se načíst doklad pro editaci.")
                    )
                }
            }
        }
    }

    fun clearError() {
        updateState { copy(errorMessage = null, isNetworkError = false) }
    }

    private fun updateState(updater: DocumentFormUiState.() -> DocumentFormUiState) {
        _uiState.value = _uiState.value.updater()
    }
}