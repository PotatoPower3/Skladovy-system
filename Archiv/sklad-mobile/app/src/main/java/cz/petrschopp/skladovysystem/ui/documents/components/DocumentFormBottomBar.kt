package cz.petrschopp.skladovysystem.ui.documents.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.petrschopp.skladovysystem.ui.theme.WarehousePrimary
import cz.petrschopp.skladovysystem.ui.theme.WarehouseSurface

@Composable
fun DocumentFormBottomBar(
    saveText: String,
    saveEnabled: Boolean,
    onSaveClick: () -> Unit,
    onAddItemClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onSaveClick,
            enabled = saveEnabled,
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
        ) {
            Text(saveText)
        }

        FloatingActionButton(
            onClick = onAddItemClick,
            modifier = Modifier.size(72.dp)
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }

        OutlinedButton(
            onClick = onBackClick,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = WarehouseSurface,
                contentColor = WarehousePrimary
            ),
            border = BorderStroke(
                width = 1.5.dp,
                color = WarehousePrimary
            ),
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
        ) {
            Text("Zpět")
        }
    }
}