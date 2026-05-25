package cz.petrschopp.skladovysystem.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.petrschopp.skladovysystem.data.model.LoginRequest
import cz.petrschopp.skladovysystem.data.remote.ApiClient
import cz.petrschopp.skladovysystem.data.session.LoggedUser
import cz.petrschopp.skladovysystem.data.session.SavedCredentials
import cz.petrschopp.skladovysystem.data.session.SessionManager
import cz.petrschopp.skladovysystem.utils.isNetworkError
import cz.petrschopp.skladovysystem.utils.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import cz.petrschopp.skladovysystem.data.session.ServerConfigManager

data class AuthUiState(
    val username: String = "",
    val password: String = "",
    val serverIp: String = "",
    val serverPort: String = "3000",
    val isCheckingServerConfig: Boolean = false,
    val loggedUser: LoggedUser? = null,
    val isCheckingSession: Boolean = true,
    val isLoggingIn: Boolean = false,
    val errorMessage: String? = null,
    val isNetworkError: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    private var serverConfigManager: ServerConfigManager? = null

    private var sessionManager: SessionManager? = null

    fun initialize(context: Context) {
        if (sessionManager != null) {
            return
        }

        val appContext = context.applicationContext

        sessionManager = SessionManager(appContext)
        serverConfigManager = ServerConfigManager(appContext)

        val serverConfig = serverConfigManager!!.getConfig()
        ApiClient.configure(serverConfig.baseUrl)

        updateState {
            copy(
                serverIp = serverConfig.serverIp,
                serverPort = serverConfig.serverPort
            )
        }

        viewModelScope.launch {
            autoLogin()
        }
    }

    private suspend fun autoLogin() {
        val manager = sessionManager ?: return

        val savedCredentials: SavedCredentials? = manager.savedCredentialsFlow.first()

        if (savedCredentials == null) {
            updateState {
                copy(
                    isCheckingSession = false,
                    loggedUser = null
                )
            }
            return
        }

        updateState {
            copy(
                username = savedCredentials.username,
                password = savedCredentials.password,
                isCheckingSession = true,
                errorMessage = null
            )
        }

        try {
            val user = ApiClient.api.login(
                LoginRequest(
                    username = savedCredentials.username,
                    password = savedCredentials.password
                )
            )

            manager.saveLogin(
                username = savedCredentials.username,
                password = savedCredentials.password,
                user = user
            )

            updateState {
                copy(
                    loggedUser = LoggedUser(
                        id = user.id,
                        username = user.username,
                        firstName = user.firstName,
                        lastName = user.lastName,
                        roleName = user.roleName
                    ),
                    isCheckingSession = false,
                    errorMessage = null
                )
            }
        } catch (e: Exception) {
            manager.clearSession()

            updateState {
                copy(
                    loggedUser = null,
                    username = savedCredentials.username,
                    password = "",
                    isCheckingSession = false,
                    errorMessage = "Přihlášení vypršelo nebo údaje už neplatí."
                )
            }
        }
    }

    fun updateUsername(value: String) = updateState {
        copy(username = value, errorMessage = null)
    }

    fun updatePassword(value: String) = updateState {
        copy(password = value, errorMessage = null)
    }

    fun login() {
        val state = _uiState.value
        val username = state.username.trim()
        val password = state.password

        if (username.isBlank() || password.isBlank()) {
            updateState { copy(errorMessage = "Zadej uživatelské jméno a heslo.") }
            return
        }

        val manager = sessionManager
        if (manager == null) {
            updateState { copy(errorMessage = "Přihlášení není připravené.") }
            return
        }

        viewModelScope.launch {
            updateState { copy(isLoggingIn = true, errorMessage = null) }

            try {
                val user = ApiClient.api.login(
                    LoginRequest(
                        username = username,
                        password = password
                    )
                )

                manager.saveLogin(
                    username = username,
                    password = password,
                    user = user
                )

                updateState {
                    copy(
                        loggedUser = LoggedUser(
                            id = user.id,
                            username = user.username,
                            firstName = user.firstName,
                            lastName = user.lastName,
                            roleName = user.roleName
                        ),
                        isLoggingIn = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoggingIn = false,
                        errorMessage = e.toUserMessage("Přihlášení se nepodařilo."),
                        isNetworkError = e.isNetworkError()
                    )
                }
            }
        }
    }

    fun updateServerIp(value: String) = updateState {
        copy(serverIp = value.trim(), errorMessage = null)
    }

    fun updateServerPort(value: String) {
        val filteredValue = value.filter { it.isDigit() }

        updateState {
            copy(serverPort = filteredValue, errorMessage = null)
        }
    }

    fun saveServerConfig() {
        val state = _uiState.value
        val ip = state.serverIp.trim()
        val port = state.serverPort.ifBlank { "3000" }

        if (!isValidServerHost(ip)) {
            updateState {
                copy(errorMessage = "Zadejte platnou IP adresu nebo název serveru bez http://.")
            }
            return
        }

        if (!isValidServerPort(port)) {
            updateState {
                copy(errorMessage = "Port musí být číslo v rozsahu 1 až 65535.")
            }
            return
        }

        val baseUrl = ApiClient.buildBaseUrl(
            serverIp = ip,
            serverPort = port
        )

        viewModelScope.launch {
            try {
                updateState {
                    copy(
                        isCheckingServerConfig = true,
                        errorMessage = null,
                        isNetworkError = false
                    )
                }

                ApiClient.configure(baseUrl)
                ApiClient.api.healthCheck()

                serverConfigManager?.saveConfig(
                    serverIp = ip,
                    serverPort = port
                )

                updateState {
                    copy(
                        serverIp = ip,
                        serverPort = port,
                        isCheckingServerConfig = false,
                        errorMessage = null,
                        isNetworkError = false
                    )
                }
            } catch (e: Exception) {
                val currentConfig = serverConfigManager?.getConfig()

                if (currentConfig != null) {
                    ApiClient.configure(currentConfig.baseUrl)
                }

                updateState {
                    copy(
                        isCheckingServerConfig = false,
                        errorMessage = e.toUserMessage("Na zadané adrese nebyl nalezen server."),
                        isNetworkError = e.isNetworkError()
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager?.clearSession()

            val serverConfig = serverConfigManager?.getConfig()

            updateState {
                AuthUiState(
                    serverIp = serverConfig?.serverIp ?: serverIp,
                    serverPort = serverConfig?.serverPort ?: serverPort,
                    isCheckingSession = false
                )
            }
        }
    }

    private fun isValidServerHost(value: String): Boolean {
        val host = value.trim()

        if (host.isBlank()) return false
        if (host.startsWith("http://") || host.startsWith("https://")) return false
        if (host.contains("/")) return false
        if (host.contains(" ")) return false

        return true
    }

    private fun isValidServerPort(value: String): Boolean {
        val port = value.toIntOrNull() ?: return false
        return port in 1..65535
    }

    fun resetServerConfigInput() {
        val serverConfig = serverConfigManager?.getConfig()

        updateState {
            copy(
                serverIp = serverConfig?.serverIp ?: serverIp,
                serverPort = serverConfig?.serverPort ?: serverPort,
                errorMessage = null,
                isNetworkError = false,
                isCheckingServerConfig = false
            )
        }
    }

    fun clearError() = updateState { copy(errorMessage = null, isNetworkError = false) }

    private fun updateState(updater: AuthUiState.() -> AuthUiState) {
        _uiState.value = _uiState.value.updater()
    }
}