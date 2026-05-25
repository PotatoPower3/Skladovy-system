package cz.petrschopp.skladovysystem.ui.documents.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cz.petrschopp.skladovysystem.data.model.ItemDto
import cz.petrschopp.skladovysystem.ui.common.BottomActionBar
import cz.petrschopp.skladovysystem.ui.items.ProductImage
import cz.petrschopp.skladovysystem.utils.formatQuantity
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import cz.petrschopp.skladovysystem.ui.common.AppCard

@Composable
fun SelectedItemFullScreen(
    item: ItemDto,
    movementTypeCode: String,
    isEditing: Boolean,
    quantityText: String,
    itemNote: String,
    onQuantityChange: (String) -> Unit,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit,
    onNoteChange: (String) -> Unit,
    onAdd: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                ProductImage(imageFilename = item.imageFilename)
            }

            item {
                SelectedItemInfoCard(item = item)
            }

            item {
                QuantityEditor(
                    quantityText = quantityText,
                    onQuantityChange = onQuantityChange,
                    onIncreaseQuantity = onIncreaseQuantity,
                    onDecreaseQuantity = onDecreaseQuantity
                )
            }

            item {
                OutlinedTextField(
                    value = itemNote,
                    onValueChange = onNoteChange,
                    label = { Text("Poznámka k položce") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    )
                )
            }
        }

        BottomActionBar(
            primaryText = if (isEditing) {
                "Uložit změnu"
            } else if (movementTypeCode == "IN") {
                "Přidat do příjmu"
            } else {
                "Přidat do výdeje"
            },
            secondaryText = "Zpět",
            onPrimaryClick = onAdd,
            onSecondaryClick = onBack,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun SelectedItemInfoCard(
    item: ItemDto
) {
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text("Aktuálně: ${formatQuantity(item.quantity)} ${item.unit}")
            Text("Umístění: ${item.location ?: "neuvedeno"}")

            val mainCode = item.codes.firstOrNull()?.code
            if (!mainCode.isNullOrBlank()) {
                Text("Kód: $mainCode")
            }
        }
    }
}

@Composable
private fun QuantityEditor(
    quantityText: String,
    onQuantityChange: (String) -> Unit,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit
) {
    var quantityFieldValue by remember(quantityText) {
        mutableStateOf(
            TextFieldValue(
                text = quantityText,
                selection = TextRange(quantityText.length)
            )
        )
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) {
        if (isFocused) {
            quantityFieldValue = quantityFieldValue.copy(
                selection = TextRange(0, quantityFieldValue.text.length)
            )
        }
    }

    Text(
        text = "Množství",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(6.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onDecreaseQuantity,
            modifier = Modifier.weight(0.8f)
        ) {
            Text("-1")
        }

        OutlinedTextField(
            value = quantityFieldValue,
            onValueChange = { newValue ->
                quantityFieldValue = newValue
                onQuantityChange(newValue.text)
            },
            label = { Text("Množství") },
            singleLine = true,
            interactionSource = interactionSource,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.weight(1.4f)
        )

        OutlinedButton(
            onClick = onIncreaseQuantity,
            modifier = Modifier.weight(0.8f)
        ) {
            Text("+1")
        }
    }
}