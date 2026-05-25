package cz.petrschopp.skladovysystem.ui.issuing

import androidx.compose.runtime.Composable
import cz.petrschopp.skladovysystem.ui.documents.DocumentsScreen

@Composable
fun IssuingScreen(
    currentUserId: Int,
    onFormVisibleChange: (Boolean) -> Unit = {},
    onDetailVisibleChange: (Boolean) -> Unit = {}
) {
    DocumentsScreen(
        title = "Výdej",
        movementTypeCode = "OUT",
        currentUserId = currentUserId,
        showAddButton = true,
        addButtonText = "Přidat výdej",
        onFormVisibleChange = onFormVisibleChange,
        onDetailVisibleChange = onDetailVisibleChange
    )
}