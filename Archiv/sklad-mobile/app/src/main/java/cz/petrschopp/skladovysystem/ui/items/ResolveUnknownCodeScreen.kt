package cz.petrschopp.skladovysystem.ui.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.runtime.LaunchedEffect
import cz.petrschopp.skladovysystem.data.model.ItemDto
import cz.petrschopp.skladovysystem.ui.common.AppCard
import cz.petrschopp.skladovysystem.ui.common.AppListCard
import cz.petrschopp.skladovysystem.ui.common.ErrorDialog
import cz.petrschopp.skladovysystem.ui.items.components.ItemRow

@Composable
fun ResolveUnknownCodeScreen(
    code: String,
    allowCreateNew: Boolean,
    onBack: () -> Unit,
    onItemReady: (ItemDto) -> Unit,
    viewModel: ResolveUnknownCodeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(allowCreateNew) {
        if (!allowCreateNew && uiState.mode == ResolveUnknownCodeMode.CREATE_NEW) {
            viewModel.setMode(ResolveUnknownCodeMode.ASSIGN_TO_EXISTING)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                text = if (code.isBlank()) "Založit nový produkt" else "Neznámý kód",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (code.isNotBlank()) {
            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text("Kód")
                        Text(
                            text = code,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (allowCreateNew) {
            item {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = uiState.mode == ResolveUnknownCodeMode.CREATE_NEW,
                        onClick = {
                            viewModel.setMode(ResolveUnknownCodeMode.CREATE_NEW)
                        },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = 0,
                            count = 2
                        ),
                        icon = {}
                    ) {
                        Text("Nový produkt")
                    }

                    SegmentedButton(
                        selected = uiState.mode == ResolveUnknownCodeMode.ASSIGN_TO_EXISTING,
                        onClick = {
                            viewModel.setMode(ResolveUnknownCodeMode.ASSIGN_TO_EXISTING)
                        },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = 1,
                            count = 2
                        ),
                        icon = {}
                    ) {
                        Text(if (code.isBlank()) "Vybrat produkt" else "Přiřadit kód")
                    }
                }
            }
        }

        when (uiState.mode) {
            ResolveUnknownCodeMode.CREATE_NEW -> {
                if (allowCreateNew) {
                    item {
                        CreateNewItemForm(
                            uiState = uiState,
                            onNameChange = viewModel::updateName,
                            onUnitChange = viewModel::updateUnit,
                            onLocationChange = viewModel::updateLocation,
                            onMinQuantityChange = viewModel::updateMinQuantity,
                            onNoteChange = viewModel::updateNote
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                viewModel.createNewItem(
                                    code = code,
                                    onCreated = onItemReady
                                )
                            },
                            enabled = !uiState.isSaving,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (uiState.isSaving) {
                                    "Ukládám..."
                                } else {
                                    "Založit produkt"
                                }
                            )
                        }
                    }
                }
            }

            ResolveUnknownCodeMode.ASSIGN_TO_EXISTING -> {
                item {
                    AssignCodeForm(
                        uiState = uiState,
                        onSearchChange = viewModel::updateSearchQuery
                    )
                }

                if (uiState.isSearching) {
                    item {
                        CircularProgressIndicator()
                    }
                }

                if (uiState.searchResults.isNotEmpty()) {
                    item {
                        AppListCard(
                            items = uiState.searchResults,
                            emptyText = "Nic nebylo nalezeno.",
                            maxHeightDp = 320
                        ) { item ->
                            ItemRow(
                                item = item,
                                selected = uiState.selectedExistingItem?.id == item.id,
                                onClick = {
                                    viewModel.selectExistingItem(item)
                                }
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            if (code.isBlank()) {
                                uiState.selectedExistingItem?.let(onItemReady)
                            } else {
                                viewModel.assignCodeToExistingItem(
                                    code = code,
                                    onAssigned = onItemReady
                                )
                            }
                        },
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (uiState.isSaving) {
                                "Ukládám..."
                            } else {
                                if (code.isBlank()) "Vybrat produkt" else "Přiřadit kód"
                            }
                        )
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Zpět")
            }
        }
    }

    ErrorDialog(
        message = if (uiState.isNetworkError) null else uiState.errorMessage,
        onDismiss = viewModel::clearError
    )
}

@Composable
private fun CreateNewItemForm(
    uiState: ResolveUnknownCodeUiState,
    onNameChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onMinQuantityChange: (String) -> Unit,
    onNoteChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Založit nový produkt",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            label = { Text("Název produktu") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.unit,
            onValueChange = {},
            label = { Text("Jednotka") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = false
        )

        OutlinedTextField(
            value = uiState.location,
            onValueChange = onLocationChange,
            label = { Text("Umístění") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.minQuantity,
            onValueChange = onMinQuantityChange,
            label = { Text("Minimální množství") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.note,
            onValueChange = onNoteChange,
            label = { Text("Poznámka") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
    }
}

@Composable
private fun AssignCodeForm(
    uiState: ResolveUnknownCodeUiState,
    onSearchChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Přiřadit kód k existujícímu produktu",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchChange,
            label = { Text("Hledat existující produkt") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (uiState.selectedExistingItem != null) {
            Text(
                text = "Vybráno: ${uiState.selectedExistingItem.name}",
                fontWeight = FontWeight.Bold
            )
        }
    }
}