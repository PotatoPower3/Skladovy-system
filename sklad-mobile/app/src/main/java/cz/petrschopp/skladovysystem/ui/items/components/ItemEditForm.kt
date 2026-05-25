package cz.petrschopp.skladovysystem.ui.items.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cz.petrschopp.skladovysystem.ui.items.ItemEditUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEditForm(
    uiState: ItemEditUiState,
    unit: String,
    onNameChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onMinQuantityChange: (String) -> Unit,
    onWeightPerUnitChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var locationExpanded by remember { mutableStateOf(false) }

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
            value = uiState.weightPerUnit,
            onValueChange = onWeightPerUnitChange,
            label = { Text("Hmotnost 1 kusu (kg)") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        ExposedDropdownMenuBox(
            expanded = locationExpanded,
            onExpandedChange = {
                if (!uiState.isLoadingLocations && uiState.warehouseLocations.isNotEmpty()) {
                    locationExpanded = !locationExpanded
                }
            }
        ) {
            OutlinedTextField(
                value = uiState.location,
                onValueChange = {},
                readOnly = true,
                label = {
                    Text(
                        if (uiState.isLoadingLocations) {
                            "Načítám umístění..."
                        } else {
                            "Umístění"
                        }
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = locationExpanded
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isLoadingLocations && uiState.warehouseLocations.isNotEmpty()
            )

            ExposedDropdownMenu(
                expanded = locationExpanded,
                onDismissRequest = { locationExpanded = false }
            ) {
                uiState.warehouseLocations.forEach { location ->
                    DropdownMenuItem(
                        text = {
                            Text("${location.name} (${location.code})")
                        },
                        onClick = {
                            onLocationChange(location.name)
                            locationExpanded = false
                        }
                    )
                }
            }
        }

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