package cz.petrschopp.skladovysystem.ui.documents.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.petrschopp.skladovysystem.ui.documents.model.DraftDocumentItem
import cz.petrschopp.skladovysystem.utils.formatQuantity

@Composable
fun DraftItemRow(
    draftItem: DraftDocumentItem,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = draftItem.item.name,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${formatQuantity(draftItem.quantity.toString())} ${draftItem.item.unit}",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = draftItem.note
                ?.takeIf { it.isNotBlank() }
                ?: "Bez poznámky",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedButton(
            onClick = onRemove,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Odebrat")
        }
    }
}