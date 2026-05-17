package cz.petrschopp.skladovysystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import cz.petrschopp.skladovysystem.ui.auth.AuthViewModel
import cz.petrschopp.skladovysystem.ui.auth.LoginScreen
import cz.petrschopp.skladovysystem.ui.common.LoadingContent
import cz.petrschopp.skladovysystem.ui.main.MainScreen
import cz.petrschopp.skladovysystem.ui.theme.SkladovySystemTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SkladovySystemTheme {
                val authViewModel: AuthViewModel = viewModel()
                val authUiState by authViewModel.uiState.collectAsState()

                LaunchedEffect(Unit) {
                    authViewModel.initialize(applicationContext)
                }

                when {
                    authUiState.isCheckingSession -> {
                        LoadingContent(text = "Ověřuji přihlášení...")
                    }

                    authUiState.loggedUser != null -> {
                        MainScreen(
                            loggedUser = authUiState.loggedUser!!,
                            onLogout = authViewModel::logout
                        )
                    }

                    else -> {
                        LoginScreen(
                            uiState = authUiState,
                            onUsernameChange = authViewModel::updateUsername,
                            onPasswordChange = authViewModel::updatePassword,
                            onLoginClick = authViewModel::login,
                            onClearError = authViewModel::clearError
                        )
                    }
                }
            }
        }
    }
}