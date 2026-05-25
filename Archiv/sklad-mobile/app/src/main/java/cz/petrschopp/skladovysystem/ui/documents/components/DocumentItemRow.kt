package cz.petrschopp.skladovysystem.ui.documents.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.petrschopp.skladovysystem.data.model.DocumentItemDto
import cz.petrschopp.skladovysystem.utils.formatQuantity

@Composable
fun DocumentItemRow(
    item: DocumentItemDto,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.itemName,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${formatQuantity(item.quantity)} ${item.unit}",
                fontWeight = FontWeight.Bold
            )
        }

        if (!item.note.isNullOrBlank()) {
            Text(
                text = item.note,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}