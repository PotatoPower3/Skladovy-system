package cz.petrschopp.skladovysystem.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.petrschopp.skladovysystem.data.model.AddCodeRequest
import cz.petrschopp.skladovysystem.data.model.CreateItemRequest
import cz.petrschopp.skladovysystem.data.model.ItemDto
import cz.petrschopp.skladovysystem.data.model.WarehouseLocationDto
import cz.petrschopp.skladovysystem.data.remote.ApiClient
import cz.petrschopp.skladovysystem.utils.isNetworkError
import cz.petrschopp.skladovysystem.utils.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ResolveUnknownCodeMode {
    CREATE_NEW,
    ASSIGN_TO_EXISTING
}

data class ResolveUnknownCodeUiState(
    val mode: ResolveUnknownCodeMode = ResolveUnknownCodeMode.CREATE_NEW,

    val name: String = "",
    val unit: String = "ks",
    val location: String = "",
    val minQuantity: String = "0",
    val note: String = "",

    val searchQuery: String = "",
    val searchResults: List<ItemDto> = emptyList(),
    val selectedExistingItem: ItemDto? = null,

    val warehouseLocations: List<WarehouseLocationDto> = emptyList(),
    val isLoadingLocations: Boolean = false,

    val isSearching: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isNetworkError: Boolean = false
)

class ResolveUnknownCodeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ResolveUnknownCodeUiState())
    val uiState: StateFlow<ResolveUnknownCodeUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun setMode(mode: ResolveUnknownCodeMode) {
        updateState {
            copy(
                mode = mode,
                errorMessage = null
            )
        }
    }

    fun updateName(value: String) = updateState { copy(name = value, errorMessage = null) }

    fun updateUnit(value: String) = updateState { copy(unit = value) }

    fun updateLocation(value: String) = updateState { copy(location = value) }

    fun updateMinQuantity(value: String) {
        val filteredValue = value.filter { it.isDigit() }

        updateState {
            copy(
                minQuantity = filteredValue,
                errorMessage = null
            )
        }
    }

    fun loadWarehouseLocations() {
        if (_uiState.value.warehouseLocations.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            updateState {
                copy(
                    isLoadingLocations = true,
                    errorMessage = null
                )
            }

            try {
                val locations = ApiClient.api.getWarehouseLocations(warehouseId = 1)

                updateState {
                    copy(
                        warehouseLocations = locations,
                        isLoadingLocations = false,
                        isNetworkError = false
                    )
                }
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoadingLocations = false,
                        errorMessage = e.toUserMessage("Nepodařilo se načíst umístění."),
                        isNetworkError = e.isNetworkError()
                    )
                }
            }
        }
    }

    fun updateNote(value: String) = updateState { copy(note = value) }

    fun clearError() = updateState { copy(errorMessage = null, isNetworkError = false) }

    private fun updateState(updater: ResolveUnknownCodeUiState.() -> ResolveUnknownCodeUiState) {
        _uiState.value = _uiState.value.updater()
    }

    fun updateSearchQuery(value: String) {
        updateState {
            copy(
                searchQuery = value,
                selectedExistingItem = null,
                errorMessage = null
            )
        }

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

    fun selectExistingItem(item: ItemDto) {
        updateState {
            copy(
                selectedExistingItem = item,
                searchQuery = item.name,
                searchResults = emptyList(),
                errorMessage = null
            )
        }
    }

    fun createNewItem(
        code: String,
        onCreated: (ItemDto) -> Unit
    ) {
        val state = _uiState.value

        if (state.name.isBlank()) {
            updateState { copy(errorMessage = "Název produktu je povinný.") }
            return
        }

        val minQuantity = state.minQuantity.toDoubleOrNull()

        if (minQuantity == null || minQuantity < 0.0) {
            updateState {
                copy(errorMessage = "Minimální množství musí být číslo 0 nebo větší.")
            }
            return
        }

        viewModelScope.launch {
            updateState {
                copy(
                    isSaving = true,
                    errorMessage = null
                )
            }

            try {
                val createdItem = ApiClient.api.createItem(
                    CreateItemRequest(
                        name = state.name.trim(),
                        code = code.trim().ifBlank { null },
                        codeType = "UNKNOWN",
                        unit = state.unit.ifBlank { "ks" },
                        imageFilename = null,
                        note = state.note.ifBlank { null },
                        warehouseId = 1,
                        quantity = 0.0,
                        location = state.location.ifBlank { null },
                        minQuantity = minQuantity
                    )
                )

                updateState { ResolveUnknownCodeUiState() }
                onCreated(createdItem)
            } catch (e: Exception) {
                updateState {
                    copy(
                        isSaving = false,
                        errorMessage = e.toUserMessage("Nepodařilo se založit produkt.")
                    )
                }
            }
        }
    }

    fun assignCodeToExistingItem(
        code: String,
        onAssigned: (ItemDto) -> Unit
    ) {
        val state = _uiState.value
        val selectedItem = state.selectedExistingItem

        if (selectedItem == null) {
            updateState { copy(errorMessage = "Nejdřív vyber existující produkt.") }
            return
        }

        viewModelScope.launch {
            updateState {
                copy(
                    isSaving = true,
                    errorMessage = null
                )
            }

            try {
                ApiClient.api.addCodeToItem(
                    itemId = selectedItem.id,
                    request = AddCodeRequest(
                        code = code.trim(),
                        codeType = "UNKNOWN"
                    )
                )

                val refreshedItem = ApiClient.api.getItemDetail(
                    id = selectedItem.id,
                    warehouseId = 1
                )

                updateState { ResolveUnknownCodeUiState() }
                onAssigned(refreshedItem)
            } catch (e: Exception) {
                updateState {
                    copy(
                        isSaving = false,
                        errorMessage = e.toUserMessage("Nepodařilo se přiřadit kód.")
                    )
                }
            }
        }
    }
}