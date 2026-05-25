package cz.petrschopp.skladovysystem.ui.documents.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cz.petrschopp.skladovysystem.data.model.DocumentItemDto
import cz.petrschopp.skladovysystem.utils.formatQuantity
import cz.petrschopp.skladovysystem.utils.formatWeight

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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = item.itemName,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${formatQuantity(item.quantity)} ${item.unit}",
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Hmotnost: ${formatWeight(item.totalWeight)} kg",
            style = MaterialTheme.typography.bodySmall
        )

        if (!item.note.isNullOrBlank()) {
            Text(
                text = item.note,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}