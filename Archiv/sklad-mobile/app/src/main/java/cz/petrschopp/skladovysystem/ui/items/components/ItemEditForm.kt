package cz.petrschopp.skladovysystem.ui.items.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cz.petrschopp.skladovysystem.ui.items.ItemEditUiState

@Composable
fun ItemEditForm(
    uiState: ItemEditUiState,
    unit: String,
    onNameChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onMinQuantityChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            label = { Text("Název položky") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = unit,
            onValueChange = {},
            label = { Text("Jednotka") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = false
        )

        OutlinedTextField(
            value = uiState.location,
            onValueChange = onLocationChange,
            label = { Text("Umístění") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.minQuantity,
            onValueChange = onMinQuantityChange,
            label = { Text("Minimální množství") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.note,
            onValueChange = onNoteChange,
            label = { Text("Poznámka") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
    }
}