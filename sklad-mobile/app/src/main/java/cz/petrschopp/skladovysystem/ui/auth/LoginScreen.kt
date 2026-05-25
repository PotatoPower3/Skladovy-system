package cz.petrschopp.skladovysystem.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cz.petrschopp.skladovysystem.ui.common.ErrorDialog

@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onServerIpChange: (String) -> Unit,
    onServerPortChange: (String) -> Unit,
    onSaveServerConfig: () -> Unit,
    onResetServerConfigInput: () -> Unit,
    onLoginClick: () -> Unit,
    onClearError: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var showServerConfigDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isCheckingServerConfig, uiState.errorMessage) {
        if (
            showServerConfigDialog &&
            !uiState.isCheckingServerConfig &&
            uiState.errorMessage == null
        ) {
            showServerConfigDialog = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .imePadding()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Přihlášení",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Skladový systém",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Server: ${uiState.serverIp}:${uiState.serverPort}",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = uiState.username,
                    onValueChange = onUsernameChange,
                    label = { Text("Uživatelské jméno") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isLoggingIn,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    label = { Text("Heslo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isLoggingIn,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onLoginClick()
                        }
                    )
                )

                if (uiState.isNetworkError) {
                    Text(
                        text = uiState.errorMessage ?: "Nelze se připojit k serveru.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onLoginClick()
                    },
                    enabled = !uiState.isLoggingIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (uiState.isLoggingIn) {
                        CircularProgressIndicator()
                    } else {
                        Text("Přihlásit")
                    }
                }

                TextButton(
                    onClick = {
                        onClearError()
                        showServerConfigDialog = true
                    },
                    enabled = !uiState.isLoggingIn,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Nastavení serveru")
                }

                if (uiState.username.isNotBlank() || uiState.password.isNotBlank()) {
                    TextButton(
                        onClick = {
                            onUsernameChange("")
                            onPasswordChange("")
                        },
                        enabled = !uiState.isLoggingIn,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Vymazat")
                    }
                }
            }
        }

        if (showServerConfigDialog) {
            AlertDialog(
                onDismissRequest = {
                    if (!uiState.isCheckingServerConfig) {
                        onResetServerConfigInput()
                        showServerConfigDialog = false
                    }
                },
                title = {
                    Text("Nastavení serveru")
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.serverIp,
                            onValueChange = onServerIpChange,
                            label = { Text("IP adresa serveru") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !uiState.isCheckingServerConfig,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            )
                        )

                        OutlinedTextField(
                            value = uiState.serverPort,
                            onValueChange = onServerPortChange,
                            label = { Text("Port") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !uiState.isCheckingServerConfig,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            )
                        )

                        Text(
                            text = "Zadej adresu a port serveru. Telefon musí být připojený ke stejné síti jako server.",
                            style = MaterialTheme.typography.bodySmall
                        )

                        if (uiState.isCheckingServerConfig) {
                            Text(
                                text = "Ověřuji připojení k serveru...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (!uiState.errorMessage.isNullOrBlank()) {
                            Text(
                                text = uiState.errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onSaveServerConfig()
                        },
                        enabled = !uiState.isCheckingServerConfig
                    ) {
                        Text(if (uiState.isCheckingServerConfig) "Ověřuji..." else "Uložit")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            onResetServerConfigInput()
                            showServerConfigDialog = false
                        },
                        enabled = !uiState.isCheckingServerConfig
                    ) {
                        Text("Zrušit")
                    }
                }
            )
        }

        ErrorDialog(
            message = if (showServerConfigDialog || uiState.isNetworkError) {
                null
            } else {
                uiState.errorMessage
            },
            onDismiss = onClearError
        )
    }
}