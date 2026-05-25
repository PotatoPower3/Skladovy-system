package cz.petrschopp.skladovysystem.ui.documents.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cz.petrschopp.skladovysystem.ui.documents.model.DraftDocumentItem
import cz.petrschopp.skladovysystem.utils.formatQuantity
import cz.petrschopp.skladovysystem.utils.formatWeight

@Composable
fun DraftItemRow(
    draftItem: DraftDocumentItem,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val item = draftItem.item

    val totalWeight = draftItem.quantity * (
            item.weightPerUnit
                ?.replace(",", ".")
                ?.toDoubleOrNull()
                ?: 0.0
            )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = item.name,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${formatQuantity(draftItem.quantity.toString())} ${item.unit}",
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Hmotnost: ${formatWeight(totalWeight.toString())} kg",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = draftItem.note
                        ?.takeIf { it.isNotBlank() }
                        ?: "Bez poznámky",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Odebrat položku",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onRemove() }
            )
        }
    }
}