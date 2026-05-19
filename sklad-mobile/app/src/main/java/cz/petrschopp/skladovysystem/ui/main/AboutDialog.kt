package cz.petrschopp.skladovysystem.ui.main

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("O aplikaci")
        },
        text = {
            Text(
                text = """
                    Skladový systém
                    
                    Autor:
                    Petr Schöpp
                    
                    Účel aplikace:
                    Aplikace slouží pro evidenci skladových položek, příjmů, výdejů a historii dokladů.
                    
                    Hlavní funkce:
                    - správa položek
                    - příjem a výdej ze skladu
                    - skenování čárových a QR kódů
                    - evidence dokladů
                    - nahrávání fotografií produktů
                """.trimIndent()
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Zavřít")
            }
        }
    )
}