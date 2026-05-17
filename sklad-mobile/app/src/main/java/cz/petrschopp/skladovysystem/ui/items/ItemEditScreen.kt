package cz.petrschopp.skladovysystem.ui.items

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cz.petrschopp.skladovysystem.data.model.ItemDto
import cz.petrschopp.skladovysystem.ui.common.BottomActionBar
import cz.petrschopp.skladovysystem.ui.common.ErrorDialog
import cz.petrschopp.skladovysystem.ui.common.LoadingContent
import cz.petrschopp.skladovysystem.ui.items.components.EditableProductImage
import cz.petrschopp.skladovysystem.ui.items.components.ItemEditForm

@Composable
fun ItemEditScreen(
    item: ItemDto,
    onBack: () -> Unit,
    onSaved: (ItemDto) -> Unit,
    viewModel: ItemEditViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.updateSelectedImage(uri)
    }

    var showCamera by remember { mutableStateOf(false) }

    LaunchedEffect(item.id) {
        viewModel.loadItem(item)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
    ) {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Text(
                    text = "Upravit položku",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                EditableProductImage(
                    currentImageFilename = uiState.imageFilename ?: item.imageFilename,
                    selectedImageUri = uiState.selectedImageUri,
                    onPickImage = {
                        imagePickerLauncher.launch("image/*")
                    },
                    onTakePhoto = {
                        showCamera = true
                    }
                )
            }

            item {
                ItemEditForm(
                    uiState = uiState,
                    unit = item.unit,
                    onNameChange = viewModel::updateName,
                    onLocationChange = viewModel::updateLocation,
                    onMinQuantityChange = viewModel::updateMinQuantity,
                    onNoteChange = viewModel::updateNote
                )
            }

            if (uiState.isSaving) {
                item {
                    LoadingContent(text = "Ukládám položku...")
                }
            }
        }

        ErrorDialog(
            message = if (uiState.isNetworkError) null else uiState.errorMessage,
            onDismiss = viewModel::clearError
        )

        BottomActionBar(
            primaryText = if (uiState.isSaving) "Ukládám..." else "Uložit",
            secondaryText = "Zpět",
            onPrimaryClick = {
                viewModel.saveItem(
                    context = context,
                    item = item,
                    onSaved = onSaved
                )
            },
            onSecondaryClick = onBack,
            primaryEnabled = !uiState.isSaving,
            secondaryEnabled = !uiState.isSaving,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (showCamera) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showCamera = false },
                properties = androidx.compose.ui.window.DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            ) {
                CameraCaptureScreen(
                    onImageCaptured = { uri ->
                        viewModel.updateSelectedImage(uri)
                        showCamera = false
                    },
                    onBack = { showCamera = false }
                )
            }
        }
    }
}
