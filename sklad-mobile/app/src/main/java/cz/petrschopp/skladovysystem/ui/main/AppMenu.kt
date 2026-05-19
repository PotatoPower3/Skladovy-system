package cz.petrschopp.skladovysystem.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun AppMenu(
    onAboutClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(
        onClick = { expanded = true }
    ) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Menu"
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text("O aplikaci") },
            onClick = {
                expanded = false
                onAboutClick()
            }
        )

        DropdownMenuItem(
            text = { Text("Odhlásit") },
            onClick = {
                expanded = false
                onLogoutClick()
            }
        )
    }
}