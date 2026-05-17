package cz.petrschopp.skladovysystem.core.base

interface UiState {
    val isLoading: Boolean
    val errorMessage: String?
    val isNetworkError: Boolean
}
