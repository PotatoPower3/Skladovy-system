package cz.petrschopp.skladovysystem.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.petrschopp.skladovysystem.core.base.UiState
import cz.petrschopp.skladovysystem.core.ui.components.StateWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@Composable
fun <T> SearchableListScreen(
    title: String? = null,
    uiState: UiState,
    data: List<T>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchLabel: String,
    emptyMessage: String,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    bottomButtonText: String? = null,
    onBottomButtonClick: (() -> Unit)? = null,
    content: @Composable (List<T>) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (bottomButtonText != null) 112.dp else 0.dp)
        ) {
            if (title != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onRefresh
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Obnovit"
                        )
                    }
                }

            } else {
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Obnovit"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text(searchLabel) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            StateWrapper(
                uiState = uiState,
                data = data,
                emptyMessage = emptyMessage,
                content = content
            )
        }

        if (bottomButtonText != null && onBottomButtonClick != null) {
            Button(
                onClick = onBottomButtonClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(bottomButtonText)
            }
        }
    }
}