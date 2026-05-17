package cz.petrschopp.skladovysystem.ui.documents.components

import androidx.compose.runtime.Composable
import cz.petrschopp.skladovysystem.data.model.DocumentItemDto
import cz.petrschopp.skladovysystem.ui.common.AppListCard

@Composable
fun DocumentItemsCard(
    items: List<DocumentItemDto>
) {
    AppListCard(
        items = items,
        emptyText = "Doklad nemá žádné položky.",
        maxHeightDp = 320
    ) { item ->
        DocumentItemRow(item = item)
    }
}