package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SpruceDarkPrimary,
    onPrimary = SpruceDarkOnPrimary,
    primaryContainer = SpruceDarkPrimaryContainer,
    secondary = SpruceDarkSecondary,
    onSecondary = SpruceDarkOnSecondary,
    secondaryContainer = SpruceDarkSecondaryContainer,
    background = SpruceDarkBackground,
    onBackground = SpruceDarkOnBackground,
    surface = SpruceDarkSurface,
    onSurface = SpruceDarkOnSurface
)

private val LightColorScheme = lightColorScheme(
    primary = SchoolPrimary,
    onPrimary = SchoolOnPrimary,
    primaryContainer = SchoolPrimaryContainer,
    secondary = SchoolSecondary,
    onSecondary = SchoolOnSecondary,
    secondaryContainer = SchoolSecondaryContainer,
    background = SchoolBackground,
    onBackground = SchoolOnBackground,
    surface = SchoolSurface,
    onSurface = SchoolOnBackground
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
