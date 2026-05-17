package cz.petrschopp.skladovysystem.ui.documents

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import cz.petrschopp.skladovysystem.data.model.DocumentDetailDto
import cz.petrschopp.skladovysystem.ui.common.ErrorDialog
import cz.petrschopp.skladovysystem.ui.common.LoadingContent
import cz.petrschopp.skladovysystem.ui.documents.components.AddItemToDocumentScreen
import cz.petrschopp.skladovysystem.ui.documents.components.DocumentFormBottomBar
import cz.petrschopp.skladovysystem.ui.documents.components.DraftItemsList

@Composable
fun DocumentFormScreen(
    title: String,
    movementTypeCode: String,
    currentUserId: Int,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    editingDocument: DocumentDetailDto? = null,
    viewModel: DocumentFormViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showAddItemScreen by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(editingDocument?.id) {
        if (editingDocument != null) {
            viewModel.loadExistingDocumentForEdit(editingDocument)
        }
    }

    fun requestCloseForm() {
        if (viewModel.hasUnsavedChanges()) {
            showDiscardDialog = true
        } else {
            onBack()
        }
    }

    BackHandler {
        when {
            showAddItemScreen -> showAddItemScreen = false
            else -> requestCloseForm()
        }
    }

    if (showAddItemScreen) {
        AddItemToDocumentScreen(
            movementTypeCode = movementTypeCode,
            uiState = uiState,
            onBack = { showAddItemScreen = false },
            onSearchChange = viewModel::updateSearchQuery,
            onScannedCode = { code, onNotFound ->
                viewModel.handleScannedCode(
                    code = code,
                    onNotFound = onNotFound
                )
            },
            onSelectItem = viewModel::selectItem,
            onClearSelectedItem = viewModel::clearSelectedItem,
            onQuantityChange = viewModel::updateQuantity,
            onIncreaseQuantity = viewModel::increaseQuantity,
            onDecreaseQuantity = viewModel::decreaseQuantity,
            onItemNoteChange = viewModel::updateItemNote,
            onAddSelectedItem = {
                val added = viewModel.addSelectedItem(
                    movementTypeCode = movementTypeCode
                )

                if (added) {
                    showAddItemScreen = false
                }
            },
            onShowToast = { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            },
            onEditDraftItem = viewModel::editDraftItem,
            onCloseAddItemScreen = { showAddItemScreen = false }
        )
        return
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.isSearching && uiState.editingDocumentId != null && uiState.draftItems.isEmpty()) {
                item {
                    LoadingContent(text = "Načítám doklad pro editaci...")
                }
            }

            item {
                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = viewModel::updateNote,
                    label = { Text("Poznámka dokladu") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            item {
                Text(
                    text = "Položky v dokladu",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.draftItems.isEmpty()) {
                item {
                    Text("Zatím nejsou přidané žádné položky.")
                }
            } else {
                item {
                    DraftItemsList(
                        draftItems = uiState.draftItems,
                        onEdit = { draftItem ->
                            viewModel.editDraftItem(draftItem.item.id)
                            showAddItemScreen = true
                        },
                        onRemove = { draftItem ->
                            viewModel.removeDraftItem(draftItem.item.id)
                        }
                    )
                }
            }

        }

        DocumentFormBottomBar(
            saveText = when {
                uiState.isSaving -> "Ukládám..."
                uiState.editingDocumentId != null -> "Uložit změny"
                movementTypeCode == "IN" -> "Uložit příjem"
                else -> "Uložit výdej"
            },
            saveEnabled = !uiState.isSaving,
            onSaveClick = {
                viewModel.saveDocument(
                    movementTypeCode = movementTypeCode,
                    userId = currentUserId,
                    onSaved = onSaved
                )
            },
            onAddItemClick = {
                showAddItemScreen = true
            },
            onBackClick = {
                requestCloseForm()
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    ErrorDialog(
        message = if (uiState.isNetworkError) null else uiState.errorMessage,
        onDismiss = viewModel::clearError
    )

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = {
                showDiscardDialog = false
            },
            title = {
                Text("Zahodit rozpracovaný doklad?")
            },
            text = {
                Text("Pokud se vrátíš zpět, neuložené změny se smažou.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        viewModel.resetForm()
                        onBack()
                    }
                ) {
                    Text("Zahodit")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                    }
                ) {
                    Text("Pokračovat")
                }
            }
        )
    }
}