package cz.petrschopp.skladovysystem.ui.receiving

import androidx.compose.runtime.Composable
import cz.petrschopp.skladovysystem.ui.documents.DocumentsScreen

@Composable
fun ReceivingScreen(
    currentUserId: Int,
    onFormVisibleChange: (Boolean) -> Unit = {},
    onDetailVisibleChange: (Boolean) -> Unit = {}
) {
    DocumentsScreen(
        title = "Příjem",
        movementTypeCode = "IN",
        currentUserId = currentUserId,
        showAddButton = true,
        addButtonText = "Přidat příjem",
        onFormVisibleChange = onFormVisibleChange,
        onDetailVisibleChange = onDetailVisibleChange
    )
}