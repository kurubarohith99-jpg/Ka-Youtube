package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DjConsoleColorScheme = darkColorScheme(
    primary = DeckACyan,
    onPrimary = Color.Black,
    primaryContainer = DeckADarkBlue,
    onPrimaryContainer = Color.White,
    secondary = DeckBRed,
    onSecondary = Color.White,
    secondaryContainer = DeckBDarkRed,
    onSecondaryContainer = Color.White,
    tertiary = DisplayGold,
    onTertiary = Color.Black,
    background = ConsoleBackground,
    onBackground = TextWhite,
    surface = ConsoleSurface,
    onSurface = TextWhite,
    surfaceVariant = ConsoleSurfaceElevated,
    onSurfaceVariant = TextGray,
    outline = ConsoleBorder
)

@Composable
fun RohithDjProTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DjConsoleColorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    RohithDjProTheme(content = content)
}
