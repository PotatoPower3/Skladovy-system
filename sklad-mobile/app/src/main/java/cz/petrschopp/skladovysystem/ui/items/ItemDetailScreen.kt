package cz.petrschopp.skladovysystem.ui.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.petrschopp.skladovysystem.data.model.ItemDto
import cz.petrschopp.skladovysystem.data.model.MovementDto
import cz.petrschopp.skladovysystem.ui.common.AppCard
import cz.petrschopp.skladovysystem.ui.common.BottomActionBar
import cz.petrschopp.skladovysystem.utils.formatQuantity
import cz.petrschopp.skladovysystem.utils.formatWeight

@Composable
fun ItemDetailScreen(
    item: ItemDto,
    movements: List<MovementDto>,
    onBack: () -> Unit,
    onEdit: (ItemDto) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                ProductImage(imageFilename = item.imageFilename)
            }

            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Aktuální množství")
                        Text(
                            text = "${formatQuantity(item.quantity)} ${item.unit}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Umístění: ${item.location ?: "neuvedeno"}")
                        Text("Minimum: ${item.minQuantity ?: "0"} ${item.unit}")
                        Text("Hmotnost 1 ks: ${formatWeight(item.weightPerUnit ?: "0")} kg")

                        if (!item.note.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Poznámka:",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(item.note)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Historie pohybů",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                MovementHistory(movements = movements)
            }
        }

        BottomActionBar(
            primaryText = "Upravit",
            secondaryText = "Zpět",
            onPrimaryClick = { onEdit(item) },
            onSecondaryClick = onBack,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}