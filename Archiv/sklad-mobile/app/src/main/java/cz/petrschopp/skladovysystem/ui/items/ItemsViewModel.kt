package cz.petrschopp.skladovysystem.ui.items

import cz.petrschopp.skladovysystem.core.base.BaseViewModel
import cz.petrschopp.skladovysystem.core.base.UiState
import cz.petrschopp.skladovysystem.data.model.ItemDto
import cz.petrschopp.skladovysystem.data.model.MovementDto
import cz.petrschopp.skladovysystem.data.remote.ApiClient
import cz.petrschopp.skladovysystem.utils.toUserMessage

data class ItemsUiState(
    override val isLoading: Boolean = false,
    override val errorMessage: String? = null,
    override val isNetworkError: Boolean = false,
    val items: List<ItemDto> = emptyList(),
    val movements: List<MovementDto> = emptyList()
) : UiState

class ItemsViewModel : BaseViewModel<ItemsUiState>(ItemsUiState()) {

    fun loadItems() = launchWithHandler("Nepodařilo se načíst položky.") {
        updateState { copy(isLoading = items.isEmpty(), errorMessage = null) }

        val items = ApiClient.api.getItems(warehouseId = 1)
        val movements = ApiClient.api.getMovements(warehouseId = 1, limit = 20)

        updateState {
            copy(
                isLoading = false,
                items = items,
                movements = movements,
                isNetworkError = false
            )
        }
    }

    override fun ItemsUiState.handleException(e: Exception?, message: String, isNetwork: Boolean) =
        copy(
            isLoading = false,
            errorMessage = e?.toUserMessage(message) ?: "",
            isNetworkError = isNetwork,
            items = if (isNetwork) emptyList() else items,
            movements = if (isNetwork) emptyList() else movements
        )
}
