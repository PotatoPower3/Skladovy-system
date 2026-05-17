package cz.petrschopp.skladovysystem.ui.items.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.petrschopp.skladovysystem.data.model.ItemDto
import cz.petrschopp.skladovysystem.utils.formatQuantity

@Composable
fun ItemRow(
    item: ItemDto,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val mainCode = item.codes.firstOrNull()?.code

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (selected) "✓ ${item.name}" else item.name,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${formatQuantity(item.quantity)} ${item.unit}",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (!mainCode.isNullOrBlank()) {
            Text(
                text = "Kód: $mainCode",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Text(
            text = "Umístění: ${item.location ?: "neuvedeno"}",
            style = MaterialTheme.typography.bodySmall
        )
    }
}