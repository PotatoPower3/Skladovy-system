package cz.petrschopp.skladovysystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.viewmodel.compose.viewModel
import cz.petrschopp.skladovysystem.ui.auth.AuthViewModel
import cz.petrschopp.skladovysystem.ui.auth.LoginScreen
import cz.petrschopp.skladovysystem.ui.common.LoadingContent
import cz.petrschopp.skladovysystem.ui.main.MainScreen
import cz.petrschopp.skladovysystem.ui.theme.SkladovySystemTheme
import cz.petrschopp.skladovysystem.ui.theme.WarehouseBackground
import cz.petrschopp.skladovysystem.ui.theme.WarehouseDarkBackground
import android.graphics.Color as AndroidColor
import android.os.Build
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SkladovySystemTheme {
                val isDarkTheme = isSystemInDarkTheme()

                SideEffect {
                    val navigationColor = if (isDarkTheme) {
                        WarehouseDarkBackground
                    } else {
                        WarehouseBackground
                    }

                    window.navigationBarColor = navigationColor.toArgb()
                    window.statusBarColor = AndroidColor.TRANSPARENT

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        window.isNavigationBarContrastEnforced = false
                    }

                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = false
                        isAppearanceLightNavigationBars = !isDarkTheme
                    }
                }

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
                            onServerIpChange = authViewModel::updateServerIp,
                            onServerPortChange = authViewModel::updateServerPort,
                            onSaveServerConfig = authViewModel::saveServerConfig,
                            onResetServerConfigInput = authViewModel::resetServerConfigInput,
                            onLoginClick = authViewModel::login,
                            onClearError = authViewModel::clearError
                        )
                    }
                }
            }
        }
    }
}