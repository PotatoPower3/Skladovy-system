package cz.petrschopp.skladovysystem.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = WarehousePrimaryLight,
    onPrimary = WarehouseTextLight,

    secondary = WarehouseSecondary,
    onSecondary = WarehouseTextLight,

    tertiary = WarehouseAccent,
    onTertiary = WarehouseTextDark,

    background = WarehouseDarkBackground,
    onBackground = WarehouseDarkText,

    surface = WarehouseDarkSurface,
    onSurface = WarehouseDarkText,

    surfaceVariant = WarehouseDarkSurfaceVariant,
    onSurfaceVariant = WarehouseDarkText,

    outline = WarehouseDarkOutline,
    outlineVariant = WarehouseDarkTextMuted,

    error = WarehouseError
)

private val LightColorScheme = lightColorScheme(
    primary = WarehousePrimary,
    onPrimary = WarehouseTextLight,

    secondary = WarehouseSecondary,
    onSecondary = WarehouseTextLight,

    tertiary = WarehouseAccent,
    onTertiary = WarehouseTextDark,

    background = WarehouseBackground,
    onBackground = WarehouseTextDark,

    surface = WarehouseSurface,
    onSurface = WarehouseTextDark,

    surfaceVariant = WarehouseSurfaceVariant,
    onSurfaceVariant = WarehouseTextMuted,

    outline = WarehouseOutline,
    outlineVariant = WarehouseOutlineStrong,

    error = WarehouseError
)

@Composable
fun SkladovySystemTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}