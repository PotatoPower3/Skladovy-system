package cz.petrschopp.skladovysystem.ui.documents.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.petrschopp.skladovysystem.data.model.DocumentDto
import cz.petrschopp.skladovysystem.utils.formatDateTime
import cz.petrschopp.skladovysystem.ui.common.MovementIndicatorRow

@Composable
fun DocumentHistoryRow(
    document: DocumentDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    MovementIndicatorRow(
        direction = document.movementTypeDirection,
        onClick = onClick,
        modifier = modifier
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = document.movementTypeName,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = formatDateTime(document.createdAt),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = document.documentNumber ?: "Bez čísla dokladu",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Vytvořil: ${document.createdByFirstName} ${document.createdByLastName}",
                style = MaterialTheme.typography.bodySmall
            )

            if (document.wasUpdated()) {
                Text(
                    text = "Změnil: ${document.updatedByFirstName} ${document.updatedByLastName} • ${formatDateTime(document.updatedAt)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "Položek: ${document.itemsCount}",
                style = MaterialTheme.typography.bodySmall
            )

            if (!document.note.isNullOrBlank()) {
                Text(
                    text = document.note,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}