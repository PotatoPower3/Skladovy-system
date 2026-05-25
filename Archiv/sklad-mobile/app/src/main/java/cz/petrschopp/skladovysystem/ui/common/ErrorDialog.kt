package cz.petrschopp.skladovysystem.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ErrorDialog(
    message: String?,
    onDismiss: () -> Unit,
    title: String = "Chyba"
) {
    if (!message.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(title)
            },
            text = {
                Text(message)
            },
            confirmButton = {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("OK")
                }
            }
        )
    }
}