package cz.petrschopp.skladovysystem.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cz.petrschopp.skladovysystem.core.base.UiState
import cz.petrschopp.skladovysystem.ui.common.LoadingContent

@Composable
fun <T> StateWrapper(
    uiState: UiState,
    data: List<T>,
    emptyMessage: String = "Žádné záznamy nebyly nalezeny.",
    content: @Composable (List<T>) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading && data.isEmpty() -> {
                LoadingContent(
                    text = "Načítám...",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            uiState.isNetworkError -> {
                Text(
                    text = uiState.errorMessage ?: "Chyba připojení",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
            data.isEmpty() -> {
                Text(
                    text = emptyMessage,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
            else -> {
                content(data)
            }
        }
    }
}
