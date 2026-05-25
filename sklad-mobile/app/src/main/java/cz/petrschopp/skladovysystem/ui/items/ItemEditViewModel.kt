package cz.petrschopp.skladovysystem.ui.items

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.petrschopp.skladovysystem.data.model.ItemDto
import cz.petrschopp.skladovysystem.data.model.UpdateItemRequest
import cz.petrschopp.skladovysystem.data.model.WarehouseLocationDto
import cz.petrschopp.skladovysystem.data.remote.ApiClient
import cz.petrschopp.skladovysystem.utils.isNetworkError
import cz.petrschopp.skladovysystem.utils.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

data class ItemEditUiState(
    val name: String = "",
    val location: String = "",
    val minQuantity: String = "0",
    val weightPerUnit: String = "0",
    val note: String = "",
    val imageFilename: String? = null,
    val selectedImageUri: Uri? = null,
    val warehouseLocations: List<WarehouseLocationDto> = emptyList(),
    val isLoadingLocations: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isNetworkError: Boolean = false
)

class ItemEditViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ItemEditUiState())
    val uiState: StateFlow<ItemEditUiState> = _uiState.asStateFlow()

    private var loadedItemId: Int? = null

    fun loadItem(item: ItemDto) {
        if (loadedItemId == item.id) {
            return
        }

        loadedItemId = item.id

        updateState {
            copy(
                name = item.name,
                location = item.location.orEmpty(),
                minQuantity = item.minQuantity ?: "0",
                weightPerUnit = item.weightPerUnit ?: "0",
                note = item.note.orEmpty(),
                imageFilename = item.imageFilename,
                selectedImageUri = null
            )
        }

        loadWarehouseLocations(item.warehouseId)
    }

    fun updateName(value: String) = updateState {
        copy(name = value, errorMessage = null)
    }

    fun updateLocation(value: String) = updateState {
        copy(location = value)
    }

    fun updateMinQuantity(value: String) {
        val filteredValue = value.filter { it.isDigit() }

        updateState {
            copy(
                minQuantity = filteredValue,
                errorMessage = null
            )
        }
    }

    fun updateWeightPerUnit(value: String) {
        val normalizedInput = value.replace(',', '.')

        val filteredValue = buildString {
            var hasDecimalSeparator = false

            normalizedInput.forEach { char ->
                when {
                    char.isDigit() -> append(char)

                    char == '.' && !hasDecimalSeparator -> {
                        append(char)
                        hasDecimalSeparator = true
                    }
                }
            }
        }

        updateState {
            copy(
                weightPerUnit = filteredValue,
                errorMessage = null
            )
        }
    }

    fun updateNote(value: String) = updateState {
        copy(note = value)
    }

    fun updateSelectedImage(uri: Uri?) = updateState {
        copy(selectedImageUri = uri)
    }

    fun saveItem(
        context: Context,
        item: ItemDto,
        onSaved: (ItemDto) -> Unit
    ) {
        val state = _uiState.value

        if (state.name.isBlank()) {
            updateState { copy(errorMessage = "Název položky je povinný.") }
            return
        }

        val minQuantity = state.minQuantity.toDoubleOrNull()

        if (minQuantity == null || minQuantity < 0.0) {
            updateState {
                copy(errorMessage = "Minimální množství musí být číslo 0 nebo větší.")
            }
            return
        }

        val weightPerUnit = state.weightPerUnit
            .replace(",", ".")
            .toDoubleOrNull()

        if (weightPerUnit == null || weightPerUnit < 0.0) {
            updateState {
                copy(errorMessage = "Hmotnost kusu musí být číslo 0 nebo větší.")
            }
            return
        }

        viewModelScope.launch {
            updateState {
                copy(
                    isSaving = true,
                    errorMessage = null
                )
            }

            try {
                var updatedItem = item

                val itemDataChanged =
                    state.name.trim() != item.name ||
                            state.location.trim() != item.location.orEmpty() ||
                            state.minQuantity.trim() != item.minQuantity.orEmpty() ||
                            state.note.trim() != item.note.orEmpty()

                if (itemDataChanged) {
                    updatedItem = ApiClient.api.updateItem(
                        itemId = item.id,
                        request = UpdateItemRequest(
                            name = state.name.trim(),
                            unit = item.unit,
                            weightPerUnit = weightPerUnit,
                            imageFilename = item.imageFilename,
                            note = state.note.ifBlank { null },
                            active = null,
                            warehouseId = item.warehouseId,
                            location = state.location.ifBlank { null },
                            minQuantity = minQuantity
                        )
                    )
                }

                if (state.selectedImageUri != null) {
                    updatedItem = uploadImage(
                        context = context,
                        itemId = item.id,
                        warehouseId = item.warehouseId,
                        imageUri = state.selectedImageUri
                    )
                }

                updateState {
                    copy(
                        isSaving = false,
                        imageFilename = updatedItem.imageFilename,
                        selectedImageUri = null,
                        errorMessage = null
                    )
                }

                onSaved(updatedItem)

            } catch (e: Exception) {
                updateState {
                    copy(
                        isSaving = false,
                        errorMessage = e.toUserMessage("Nepodařilo se uložit položku."),
                        isNetworkError = e.isNetworkError()
                    )
                }
            }
        }
    }

    private suspend fun uploadImage(
        context: Context,
        itemId: Int,
        warehouseId: Int,
        imageUri: Uri
    ): ItemDto {
        val contentResolver = context.contentResolver
        val mimeType = "image/jpeg"

        val originalBytes = contentResolver.openInputStream(imageUri)?.use { inputStream ->
            inputStream.readBytes()
        } ?: throw IllegalStateException("Nepodařilo se načíst vybraný obrázek.")

        val compressedBytes = compressImage(originalBytes)

        val requestBody = compressedBytes.toRequestBody(mimeType.toMediaTypeOrNull())

        val imagePart = MultipartBody.Part.createFormData(
            name = "image",
            filename = "item-image.jpg",
            body = requestBody
        )

        return ApiClient.api.uploadItemImage(
            itemId = itemId,
            warehouseId = warehouseId,
            image = imagePart
        )
    }

    private fun compressImage(bytes: ByteArray): ByteArray {
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return bytes
        val outputStream = ByteArrayOutputStream()

        var quality = 80
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

        while (outputStream.toByteArray().size > 500 * 1024 && quality > 10) {
            outputStream.reset()
            quality -= 10
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        }

        return outputStream.toByteArray()
    }

    fun clearError() = updateState { copy(errorMessage = null, isNetworkError = false) }

    private fun updateState(updater: ItemEditUiState.() -> ItemEditUiState) {
        _uiState.value = _uiState.value.updater()
    }

    private fun loadWarehouseLocations(warehouseId: Int) {
        viewModelScope.launch {
            updateState {
                copy(isLoadingLocations = true)
            }

            try {
                val locations = ApiClient.api.getWarehouseLocations(warehouseId)

                updateState {
                    copy(
                        warehouseLocations = locations,
                        isLoadingLocations = false
                    )
                }
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoadingLocations = false,
                        errorMessage = e.toUserMessage("Nepodařilo se načíst pozice skladu."),
                        isNetworkError = e.isNetworkError()
                    )
                }
            }
        }
    }
}