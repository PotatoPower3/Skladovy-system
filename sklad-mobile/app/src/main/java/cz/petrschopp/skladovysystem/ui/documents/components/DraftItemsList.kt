package cz.petrschopp.skladovysystem.ui.documents.components

import androidx.compose.runtime.Composable
import cz.petrschopp.skladovysystem.ui.documents.model.DraftDocumentItem
import cz.petrschopp.skladovysystem.ui.common.AppListCard

@Composable
fun DraftItemsList(
    draftItems: List<DraftDocumentItem>,
    onEdit: (DraftDocumentItem) -> Unit,
    onRemove: (DraftDocumentItem) -> Unit
) {
    AppListCard(
        items = draftItems,
        emptyText = "Doklad zatím nemá žádné položky.",
        maxHeightDp = 320
    ) { draftItem ->
        DraftItemRow(
            draftItem = draftItem,
            onClick = { onEdit(draftItem) },
            onRemove = { onRemove(draftItem) }
        )
    }
}