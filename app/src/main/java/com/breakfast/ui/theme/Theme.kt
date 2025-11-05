package com.breakfast.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define a light colour scheme for the app. You can customise these values to align with
// your branding or desired aesthetics. These defaults provide a simple palette.
private val LightColors = lightColorScheme(
    primary = Color(0xFF0058FF),
    onPrimary = Color.White,
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    tertiary = Color(0xFF018786),
    onTertiary = Color.White,
)

// Define a dark colour scheme for the app. Colours are adjusted for contrast on dark backgrounds.
private val DarkColors = darkColorScheme(
    primary = Color(0xFF0058FF),
    onPrimary = Color.Black,
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    tertiary = Color(0xFF03DAC6),
    onTertiary = Color.Black,
)

/**
 * Custom Material3 theme for the Breakfast app. Wraps [MaterialTheme] and automatically
 * chooses between the light and dark colour schemes based on the system setting.
 *
 * @param useDarkTheme When true, uses the dark colour palette; otherwise uses the light palette.
 * @param content Composable content that will have access to the theming values.
 */
@Composable
fun BreakfastTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}