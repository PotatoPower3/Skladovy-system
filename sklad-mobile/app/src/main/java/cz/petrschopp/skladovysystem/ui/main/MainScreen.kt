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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.petrschopp.skladovysystem.data.session.LoggedUser
import cz.petrschopp.skladovysystem.ui.history.HistoryScreen
import cz.petrschopp.skladovysystem.ui.issuing.IssuingScreen
import cz.petrschopp.skladovysystem.ui.items.ItemsScreen
import cz.petrschopp.skladovysystem.ui.receiving.ReceivingScreen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment

private val AppBackground = Color(0xFF828286)
private val HeaderBackground = Color(0xFF1D2A36)
private val HeaderAccent = Color(0xFF454648)
private val MenuBackground = Color(0xFF5F5F62)
private val SelectedTab = Color(0xFF204B73)
private val UnselectedTab = Color(0xFFD7DDE3)
private val SelectedText = Color.White
private val UnselectedText = Color(0xFF1F2D3A)

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

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Odhlášení") },
            text = { Text("Opravdu se chcete odhlásit?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Ano")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Ne")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        WarehouseHeader(
            warehouseName = "Hlavní sklad",
            loggedUser = loggedUser,
            onLogout = { showLogoutDialog = true },
            showLogout = !isFormOpen && !isDetailOpen && selectedTab == MainTab.HISTORY
        )

        if (!isFormOpen) {
            MainMenu(
                selectedTab = selectedTab,
                onTabSelected = {
                    selectedTab = it
                    isFormOpen = false
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground)
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

@Composable
private fun WarehouseHeader(
    warehouseName: String,
    loggedUser: LoggedUser,
    onLogout: () -> Unit,
    showLogout: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderBackground)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Sklad",
                color = Color(0xFFB8C7D6),
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = warehouseName,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${loggedUser.firstName} ${loggedUser.lastName}",
                color = Color(0xFFB8C7D6),
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (showLogout) {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HeaderAccent,
                    contentColor = Color.White
                )
            ) {
                Text("Odhlásit")
            }
        }
    }
}

@Composable
private fun MainMenu(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    Surface(
        color = MenuBackground,
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
                        containerColor = if (isSelected) SelectedTab else UnselectedTab,
                        contentColor = if (isSelected) SelectedText else UnselectedText
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = if (isSelected) 2.dp else 0.dp
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = tab.title,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}