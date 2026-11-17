package com.dsl21.cryptopulse.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CryptoPulseColors = darkColorScheme(
    primary = Color(0xFF62E6B5),
    onPrimary = Color(0xFF06251A),
    secondary = Color(0xFF8BAEFF),
    tertiary = Color(0xFFF3B96C),
    background = Color(0xFF07111F),
    surface = Color(0xFF101C2D),
    surfaceVariant = Color(0xFF1A2A40),
    onBackground = Color(0xFFEAF1FA),
    onSurface = Color(0xFFEAF1FA),
    onSurfaceVariant = Color(0xFFA8B7CA),
    outline = Color(0xFF35465D),
    error = Color(0xFFFF7185)
)

@Composable
fun CryptoPulseTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CryptoPulseColors, content = content)
}
