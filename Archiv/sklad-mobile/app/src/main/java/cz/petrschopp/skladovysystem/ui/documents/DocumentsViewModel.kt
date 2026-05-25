package cz.petrschopp.skladovysystem.ui.documents

import cz.petrschopp.skladovysystem.core.base.BaseViewModel
import cz.petrschopp.skladovysystem.core.base.UiState
import cz.petrschopp.skladovysystem.data.model.DocumentDetailDto
import cz.petrschopp.skladovysystem.data.model.DocumentDto
import cz.petrschopp.skladovysystem.data.remote.ApiClient
import cz.petrschopp.skladovysystem.utils.toUserMessage

data class DocumentsUiState(
    override val isLoading: Boolean = false,
    override val errorMessage: String? = null,
    override val isNetworkError: Boolean = false,
    val documents: List<DocumentDto> = emptyList(),
    val selectedDocument: DocumentDetailDto? = null,
    val isDetailLoading: Boolean = false
) : UiState

class DocumentsViewModel : BaseViewModel<DocumentsUiState>(DocumentsUiState()) {

    fun loadDocuments(movementTypeCode: String? = null) = launchWithHandler("Nepodařilo se načíst doklady.") {
        updateState { copy(isLoading = documents.isEmpty(), errorMessage = null) }

        val docs = ApiClient.api.getDocuments(
            warehouseId = 1,
            limit = 20,
            movementTypeCode = movementTypeCode
        )

        updateState {
            copy(
                isLoading = false,
                documents = docs,
                isNetworkError = false
            )
        }
    }

    fun loadDocumentDetail(documentId: Int) = launchWithHandler("Nepodařilo se načíst detail dokladu.") {
        updateState { copy(isDetailLoading = true, errorMessage = null) }
        val document = ApiClient.api.getDocumentDetail(documentId)
        updateState { copy(isDetailLoading = false, selectedDocument = document) }
    }

    fun closeDocumentDetail() {
        updateState { copy(selectedDocument = null, isDetailLoading = false, errorMessage = null) }
    }

    override fun DocumentsUiState.handleException(e: Exception?, message: String, isNetwork: Boolean) =
        copy(
            isLoading = false,
            isDetailLoading = false,
            errorMessage = e?.toUserMessage(message) ?: "",
            isNetworkError = isNetwork,
            documents = if (isNetwork) emptyList() else documents
        )
}
