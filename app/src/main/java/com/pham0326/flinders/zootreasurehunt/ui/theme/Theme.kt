package com.pham0326.flinders.zootreasurehunt.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val NocturnalDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB74D),
    onPrimary = Color(0xFF1B1B2F),
    primaryContainer = Color(0xFF3E3E5C),
    onPrimaryContainer = Color(0xFFFFE0B2),
    secondary = Color(0xFF9FA8DA),
    onSecondary = Color(0xFF1B1B2F),
    surface = Color(0xFF1B1B2F),
    onSurface = Color(0xFFE8E8F0),
    surfaceVariant = Color(0xFF2A2A3E),
    onSurfaceVariant = Color(0xFFBDBDD0),
    background = Color(0xFF0F0F1A),
    onBackground = Color(0xFFE8E8F0),
    error = Color(0xFFCF6679),
    onError = Color(0xFF1B1B2F)
)
private val SafariLightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)
@Composable
fun ZooTreasureHuntTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) NocturnalDarkColorScheme else SafariLightColorScheme

    CompositionLocalProvider(
        LocalZooSpacing provides ZooSpacing()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}