package cz.petrschopp.skladovysystem.ui.history

import androidx.compose.runtime.Composable
import cz.petrschopp.skladovysystem.ui.documents.DocumentsScreen

@Composable
fun HistoryScreen(
    currentUserId: Int,
    onFormVisibleChange: (Boolean) -> Unit = {},
    onDetailVisibleChange: (Boolean) -> Unit = {}
) {
    DocumentsScreen(
        title = "Historie skladu",
        movementTypeCode = null,
        currentUserId = currentUserId,
        showAddButton = false,
        onFormVisibleChange = onFormVisibleChange,
        onDetailVisibleChange = onDetailVisibleChange
    )
}