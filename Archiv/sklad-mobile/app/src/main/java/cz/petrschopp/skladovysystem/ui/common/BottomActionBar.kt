package cz.petrschopp.skladovysystem.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.petrschopp.skladovysystem.ui.theme.WarehousePrimary
import cz.petrschopp.skladovysystem.ui.theme.WarehouseSurface

@Composable
fun BottomActionBar(
    primaryText: String,
    secondaryText: String,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryEnabled: Boolean = true,
    secondaryEnabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onPrimaryClick,
            enabled = primaryEnabled,
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
        ) {
            Text(primaryText)
        }

        OutlinedButton(
            onClick = onSecondaryClick,
            enabled = secondaryEnabled,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = WarehouseSurface,
                contentColor = WarehousePrimary,
                disabledContainerColor = WarehouseSurface,
                disabledContentColor = MaterialTheme.colorScheme.outline
            ),
            border = BorderStroke(
                width = 1.5.dp,
                color = if (secondaryEnabled) {
                    WarehousePrimary
                } else {
                    MaterialTheme.colorScheme.outline
                }
            ),
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
        ) {
            Text(secondaryText)
        }
    }
}