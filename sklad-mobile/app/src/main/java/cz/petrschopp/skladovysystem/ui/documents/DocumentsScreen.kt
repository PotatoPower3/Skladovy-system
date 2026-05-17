package cz.petrschopp.skladovysystem.ui.documents

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import cz.petrschopp.skladovysystem.data.model.DocumentDetailDto
import cz.petrschopp.skladovysystem.data.model.DocumentDto
import cz.petrschopp.skladovysystem.ui.common.AppListCard
import cz.petrschopp.skladovysystem.ui.common.AutoRefreshEffect
import cz.petrschopp.skladovysystem.ui.common.BottomActionBar
import cz.petrschopp.skladovysystem.ui.common.ErrorDialog
import cz.petrschopp.skladovysystem.ui.common.LoadingContent
import cz.petrschopp.skladovysystem.ui.common.SearchableListScreen
import cz.petrschopp.skladovysystem.ui.documents.components.DocumentHistoryRow
import cz.petrschopp.skladovysystem.ui.documents.components.DocumentInfoCard
import cz.petrschopp.skladovysystem.ui.documents.components.DocumentItemsCard
import cz.petrschopp.skladovysystem.utils.SearchUtils
import cz.petrschopp.skladovysystem.utils.formatDateTime

@Composable
fun DocumentsScreen(
    title: String,
    movementTypeCode: String? = null,
    currentUserId: Int,
    showAddButton: Boolean = false,
    addButtonText: String = "",
    onAddClick: () -> Unit = {},
    onFormVisibleChange: (Boolean) -> Unit = {},
    onDetailVisibleChange: (Boolean) -> Unit = {},
    viewModel: DocumentsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    var editingDocument by remember { mutableStateOf<DocumentDetailDto?>(null) }

    val autoRefreshEnabled = !showForm && uiState.selectedDocument == null && !uiState.isDetailLoading

    LaunchedEffect(movementTypeCode) {
        showForm = false
        editingDocument = null
        viewModel.closeDocumentDetail()
        viewModel.loadDocuments(movementTypeCode = movementTypeCode)
    }

    AutoRefreshEffect(
        enabled = autoRefreshEnabled,
        key = movementTypeCode,
        onRefresh = {
            viewModel.loadDocuments(movementTypeCode)
        }
    )

    BackHandler(enabled = uiState.selectedDocument != null || uiState.isDetailLoading || showForm) {
        if (showForm) {
            showForm = false
            editingDocument = null
            onFormVisibleChange(false)
        } else {
            viewModel.closeDocumentDetail()
        }
    }

    LaunchedEffect(showForm, uiState.selectedDocument, uiState.isDetailLoading) {
        onDetailVisibleChange(showForm || uiState.selectedDocument != null || uiState.isDetailLoading)
    }

    if (showForm) {
        DocumentFormScreen(
            title = if (editingDocument != null) "Upravit doklad" else if (movementTypeCode == "OUT") "Nový výdej" else "Nový příjem",
            movementTypeCode = editingDocument?.movementTypeCode ?: movementTypeCode ?: "IN",
            currentUserId = currentUserId,
            editingDocument = editingDocument,
            onBack = {
                showForm = false
                editingDocument = null
                onFormVisibleChange(false)
            },
            onSaved = {
                showForm = false
                editingDocument = null
                onFormVisibleChange(false)
                viewModel.closeDocumentDetail()
                viewModel.loadDocuments(movementTypeCode = movementTypeCode)
            },
            viewModel = viewModel(
                key = if (editingDocument != null) "document_form_edit_${editingDocument!!.id}" else "document_form_${movementTypeCode ?: "ALL"}"
            )
        )
        return
    }

    when {
        uiState.selectedDocument != null -> {
            DocumentDetailScreen(
                document = uiState.selectedDocument!!,
                onBack = { viewModel.closeDocumentDetail() },
                onEdit = { document ->
                    editingDocument = document
                    showForm = true
                    onFormVisibleChange(true)
                }
            )
        }
        uiState.isDetailLoading -> {
            LoadingContent(text = "Načítám detail dokladu...")
        }
        else -> {
            DocumentsListScreen(
                title = title,
                uiState = uiState,
                showAddButton = showAddButton,
                addButtonText = addButtonText,
                onRefresh = { viewModel.loadDocuments(movementTypeCode = movementTypeCode) },
                onDocumentClick = { viewModel.loadDocumentDetail(it.id) },
                onAddClick = {
                    if (movementTypeCode == "IN" || movementTypeCode == "OUT") {
                        editingDocument = null
                        showForm = true
                        onFormVisibleChange(true)
                    } else {
                        onAddClick()
                    }
                },
                onClearError = { viewModel.clearError() }
            )
        }
    }
}

@Composable
private fun DocumentsListScreen(
    title: String,
    uiState: DocumentsUiState,
    showAddButton: Boolean,
    addButtonText: String,
    onRefresh: () -> Unit,
    onDocumentClick: (DocumentDto) -> Unit,
    onAddClick: () -> Unit,
    onClearError: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredDocuments = remember(uiState.documents, searchQuery) {
        SearchUtils.filter(uiState.documents, searchQuery) { doc ->
            listOf(
                doc.documentNumber.orEmpty(),
                doc.note.orEmpty(),
                formatDateTime(doc.createdAt),
                "${doc.createdByFirstName} ${doc.createdByLastName}"
            ).joinToString(" ")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SearchableListScreen(
            title = title,
            uiState = uiState,
            data = filteredDocuments,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            searchLabel = "Hledat podle data nebo poznámky",
            emptyMessage = if (searchQuery.isBlank()) {
                "Žádné doklady k zobrazení."
            } else {
                "Nic nebylo nalezeno."
            },
            onRefresh = onRefresh,
            bottomButtonText = if (showAddButton) addButtonText else null,
            onBottomButtonClick = if (showAddButton) onAddClick else null
        ) { docs ->
            DocumentHistoryList(
                documents = docs,
                onDocumentClick = onDocumentClick
            )
        }

        ErrorDialog(
            message = if (uiState.isNetworkError) null else uiState.errorMessage,
            onDismiss = onClearError
        )
    }
}

@Composable
private fun DocumentHistoryList(
    documents: List<DocumentDto>,
    onDocumentClick: (DocumentDto) -> Unit
) {
    AppListCard(
        items = documents,
        emptyText = "Žádné doklady k zobrazení.",
        maxVisibleItems = 20
    ) { document ->
        DocumentHistoryRow(
            document = document,
            onClick = { onDocumentClick(document) }
        )
    }
}

@Composable
private fun DocumentDetailScreen(
    document: DocumentDetailDto,
    onBack: () -> Unit,
    onEdit: (DocumentDetailDto) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Text(text = document.movementTypeName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            item {
                DocumentInfoCard(document = document)
            }
            item {
                Text(text = "Položky dokladu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            item {
                DocumentItemsCard(items = document.items)
            }
        }

        BottomActionBar(
            primaryText = "Upravit",
            secondaryText = "Zpět",
            onPrimaryClick = { onEdit(document) },
            onSecondaryClick = onBack,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}