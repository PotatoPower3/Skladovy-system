package cz.petrschopp.skladovysystem.ui.items.components

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import cz.petrschopp.skladovysystem.data.remote.ApiUrls
import cz.petrschopp.skladovysystem.ui.common.AppCard

@Composable
fun EditableProductImage(
    currentImageFilename: String?,
    selectedImageUri: Uri?,
    onPickImage: () -> Unit,
    onTakePhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageModel: Any? = selectedImageUri ?: ApiUrls.itemImageUrl(currentImageFilename)

    AppCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            if (imageModel == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.77f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Fotka produktu není dostupná")
                }
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Fotka produktu",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.77f),
                    contentScale = ContentScale.Crop
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPickImage,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Galerie")
                }

                Button(
                    onClick = onTakePhoto,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Vyfotit")
                }
            }
        }
    }
}