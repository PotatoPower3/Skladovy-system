package cz.petrschopp.skladovysystem.ui.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import cz.petrschopp.skladovysystem.data.remote.ApiUrls

@Composable
fun ProductImage(imageFilename: String?) {
    val fullImageUrl = ApiUrls.itemImageUrl(imageFilename)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (fullImageUrl.isNullOrBlank()) {
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
            Image(
                painter = rememberAsyncImagePainter(fullImageUrl),
                contentDescription = "Fotka produktu",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.77f),
                contentScale = ContentScale.Crop
            )
        }
    }
}