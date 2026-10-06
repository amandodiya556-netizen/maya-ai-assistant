package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF002026),
    primaryContainer = Color(0xFF003842),
    onPrimaryContainer = Color(0xFF8CF4FF),

    secondary = NeonMagenta,
    onSecondary = Color(0xFF3B001B),
    secondaryContainer = Color(0xFF5E002E),
    onSecondaryContainer = Color(0xFFFFB0D0),

    tertiary = NeonPurple,
    onTertiary = Color(0xFF2A004C),
    tertiaryContainer = Color(0xFF45007A),
    onTertiaryContainer = Color(0xFFE5B8FF),

    background = CyberBackground,
    onBackground = TextPrimary,

    surface = CyberSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = TextSecondary,

    outline = Color(0xFF263859),
    outlineVariant = Color(0xFF18243A),
    error = CyberRed,
    onError = Color.White
)

@Composable
fun MayaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberColorScheme,
        typography = Typography,
        content = content
    )
}
