package cz.petrschopp.skladovysystem.ui.documents.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.petrschopp.skladovysystem.data.model.DocumentDetailDto
import cz.petrschopp.skladovysystem.utils.formatDateTime

@Composable
fun DocumentInfoCard(
    document: DocumentDetailDto,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = document.documentNumber ?: "Bez čísla dokladu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Stav: ${document.status}")
            Text("Sklad: ${document.warehouseName}")
            Text("Vytvořil: ${document.createdByFirstName} ${document.createdByLastName}")
            Text("Vytvořeno: ${formatDateTime(document.createdAt)}")

            if (document.wasUpdated()) {
                Text("Změnil: ${document.updatedByFirstName} ${document.updatedByLastName}")
                Text("Změněno: ${formatDateTime(document.updatedAt)}")
            }

            if (!document.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Poznámka:",
                    fontWeight = FontWeight.SemiBold
                )
                Text(document.note)
            }
        }
    }
}