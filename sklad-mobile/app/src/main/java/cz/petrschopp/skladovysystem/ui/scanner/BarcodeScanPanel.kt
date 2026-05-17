package cz.petrschopp.skladovysystem.ui.scanner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BarcodeScanPanel(
    title: String? = null,
    isSearching: Boolean,
    onCodeScanned: (String) -> Unit,
    onManualClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    cameraHeightDp: Int? = null,
    errorMessage: String? = null
) {
    var detectedCode by remember { mutableStateOf<String?>(null) }

    fun confirmScan() {
        val code = detectedCode

        if (!code.isNullOrBlank() && !isSearching) {
            onCodeScanned(code)
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!title.isNullOrBlank()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (cameraHeightDp != null) {
                            Modifier.height(cameraHeightDp.dp)
                        } else {
                            Modifier.weight(1f)
                        }
                    )
                    .clickable(
                        enabled = !detectedCode.isNullOrBlank() && !isSearching
                    ) {
                        confirmScan()
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                BarcodeCameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onCodeDetected = { code ->
                        detectedCode = code
                    }
                )
            }

            Text(
                text = if (detectedCode.isNullOrBlank()) {
                    "Kód zatím nebyl rozpoznán"
                } else {
                    "Rozpoznán kód: $detectedCode • klepni na kameru nebo SCAN"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onManualClick,
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp)
            ) {
                Text("Ručně")
            }

            Spacer(modifier = Modifier.weight(0.12f))

            Button(
                onClick = { confirmScan() },
                enabled = !detectedCode.isNullOrBlank() && !isSearching,
                modifier = Modifier
                    .weight(1.45f)
                    .height(68.dp)
            ) {
                Text(
                    text = if (isSearching) "Hledám..." else "SCAN",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.weight(0.12f))

            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp)
            ) {
                Text("Zpět")
            }
        }
    }
}