package cz.petrschopp.skladovysystem.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

@Composable
fun AutoRefreshEffect(
    enabled: Boolean,
    key: Any? = Unit,
    intervalMillis: Long = 30_000,
    refreshImmediately: Boolean = true,
    onRefresh: () -> Unit
) {
    LaunchedEffect(enabled, key) {
        if (!enabled) return@LaunchedEffect

        if (refreshImmediately) {
            onRefresh()
        }

        while (true) {
            delay(intervalMillis)
            onRefresh()
        }
    }
}