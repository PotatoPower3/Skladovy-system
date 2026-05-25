package cz.petrschopp.skladovysystem.ui.items

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
import cz.petrschopp.skladovysystem.data.model.MovementDto
import cz.petrschopp.skladovysystem.ui.common.AppListCard
import cz.petrschopp.skladovysystem.ui.common.MovementIndicatorRow
import cz.petrschopp.skladovysystem.utils.formatDateTime
import cz.petrschopp.skladovysystem.utils.formatQuantity

@Composable
fun MovementHistory(
    movements: List<MovementDto>,
    maxVisibleHeightDp: Int = 320
) {
    AppListCard(
        items = movements,
        emptyText = "U této položky zatím není žádný pohyb.",
        maxVisibleItems = 20,
        maxHeightDp = maxVisibleHeightDp
    ) { movement ->
        MovementRow(movement = movement)
    }
}

@Composable
private fun MovementRow(
    movement: MovementDto
) {
    val quantityPrefix = if (movement.type == "OUT") "-" else "+"
    val movementName = movement.typeName ?: movementTypeLabel(movement.type)

    MovementIndicatorRow(
        direction = movement.type
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = movementName,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "$quantityPrefix${formatQuantity(movement.quantity)} ks",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatDateTime(movement.createdAt),
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "Vytvořil: ${movement.firstName} ${movement.lastName}",
                style = MaterialTheme.typography.bodySmall
            )

            if (!movement.note.isNullOrBlank()) {
                Text(
                    text = movement.note,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun movementTypeLabel(type: String): String {
    return when (type) {
        "IN" -> "Příjem"
        "OUT" -> "Výdej"
        else -> type
    }
}