package cz.petrschopp.skladovysystem.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.petrschopp.skladovysystem.utils.isNetworkError
import cz.petrschopp.skladovysystem.utils.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<S : UiState>(initialState: S) : ViewModel() {
    protected val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    protected fun updateState(updater: S.() -> S) {
        _uiState.value = _uiState.value.updater()
    }

    open fun clearError() {
        updateState { handleException(null, "", false) }
    }

    protected fun launchWithHandler(
        errorTitle: String,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                val isNetwork = e.isNetworkError()
                updateState {
                    handleException(e, errorTitle, isNetwork)
                }
            }
        }
    }

    abstract fun S.handleException(e: Exception?, message: String, isNetwork: Boolean): S
}
