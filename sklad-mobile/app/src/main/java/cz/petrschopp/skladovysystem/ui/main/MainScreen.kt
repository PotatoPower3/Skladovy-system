package cz.petrschopp.skladovysystem.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.petrschopp.skladovysystem.data.session.LoggedUser
import cz.petrschopp.skladovysystem.ui.history.HistoryScreen
import cz.petrschopp.skladovysystem.ui.issuing.IssuingScreen
import cz.petrschopp.skladovysystem.ui.items.ItemsScreen
import cz.petrschopp.skladovysystem.ui.receiving.ReceivingScreen
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.window.PopupProperties
import cz.petrschopp.skladovysystem.ui.theme.WarehouseSurface
import cz.petrschopp.skladovysystem.ui.theme.WarehouseBackground
import cz.petrschopp.skladovysystem.ui.theme.WarehouseHeader
import cz.petrschopp.skladovysystem.ui.theme.WarehouseMenu
import cz.petrschopp.skladovysystem.ui.theme.WarehouseSelectedTab
import cz.petrschopp.skladovysystem.ui.theme.WarehouseTextDark
import cz.petrschopp.skladovysystem.ui.theme.WarehouseTextLight
import cz.petrschopp.skladovysystem.ui.theme.WarehouseUnselectedTab

enum class MainTab(
    val title: String
) {
    HISTORY("Historie"),
    ITEMS("Položky"),
    RECEIVING("Příjem"),
    ISSUING("Výdej")
}

@Composable
fun MainScreen(
    loggedUser: LoggedUser,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(MainTab.HISTORY) }
    var isFormOpen by remember { mutableStateOf(false) }
    var isDetailOpen by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Odhlášení") },
            text = { Text("Opravdu se chcete odhlásit?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Ano")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Ne")
                }
            }
        )
    }

    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            MainHeader(
                warehouseName = "Hlavní sklad",
                loggedUser = loggedUser,
                showMenu = !isFormOpen && !isDetailOpen,
                onAboutClick = { showAboutDialog = true },
                onLogoutClick = { showLogoutDialog = true }
            )

            if (!isFormOpen && !isDetailOpen) {
                MainMenu(
                    selectedTab = selectedTab,
                    onTabSelected = {
                        selectedTab = it
                        isFormOpen = false
                        isDetailOpen = false
                    }
                )
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (selectedTab) {
                        MainTab.HISTORY -> {
                            HistoryScreen(
                                currentUserId = loggedUser.id,
                                onFormVisibleChange = { isFormOpen = it },
                                onDetailVisibleChange = { isDetailOpen = it }
                            )
                        }

                        MainTab.ITEMS -> {
                            ItemsScreen(
                                onDetailVisibleChange = { isDetailOpen = it }
                            )
                        }

                        MainTab.RECEIVING -> {
                            ReceivingScreen(
                                currentUserId = loggedUser.id,
                                onFormVisibleChange = { isFormOpen = it },
                                onDetailVisibleChange = { isDetailOpen = it }
                            )
                        }

                        MainTab.ISSUING -> {
                            IssuingScreen(
                                currentUserId = loggedUser.id,
                                onFormVisibleChange = { isFormOpen = it },
                                onDetailVisibleChange = { isDetailOpen = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainHeader(
    warehouseName: String,
    loggedUser: LoggedUser,
    showMenu: Boolean,
    onAboutClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(WarehouseHeader)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Sklad",
                color = WarehouseTextLight,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = warehouseName,
                color = WarehouseTextLight,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${loggedUser.firstName} ${loggedUser.lastName}",
                color = WarehouseTextLight,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (showMenu) {
            HeaderMenu(
                onAboutClick = onAboutClick,
                onLogoutClick = onLogoutClick
            )
        }
    }
}

@Composable
private fun HeaderMenu(
    onAboutClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(
            onClick = { expanded = true }
        ) {
            Text(
                text = "⋮",
                color = WarehouseTextLight,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            properties = PopupProperties(focusable = true),
            modifier = Modifier.widthIn(min = 180.dp)
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = "O aplikaci",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                onClick = {
                    expanded = false
                    onAboutClick()
                }
            )

            DropdownMenuItem(
                text = {
                    Text(
                        text = "Odhlásit",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                onClick = {
                    expanded = false
                    onLogoutClick()
                }
            )
        }
    }
}

@Composable
private fun MainMenu(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            MainTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab

                Button(
                    onClick = { onTabSelected(tab) },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        contentColor = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = if (isSelected) 2.dp else 0.dp
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = tab.title,
                        fontWeight = if (isSelected) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Medium
                        }
                    )
                }
            }
        }
    }
}