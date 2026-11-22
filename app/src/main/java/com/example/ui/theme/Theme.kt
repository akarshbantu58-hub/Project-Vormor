package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val WildHorizonDarkColorScheme = darkColorScheme(
    primary = NeonCoral,
    onPrimary = MidnightObsidian,
    primaryContainer = NeonCoralDark,
    onPrimaryContainer = TextPrimary,
    secondary = CoastalCyan,
    onSecondary = MidnightObsidian,
    secondaryContainer = CoastalCyanDark,
    onSecondaryContainer = TextPrimary,
    tertiary = SunsetGold,
    onTertiary = MidnightObsidian,
    background = MidnightObsidian,
    onBackground = TextPrimary,
    surface = DeepNavySurface,
    onSurface = TextPrimary,
    surfaceVariant = ElevatedCardNavy,
    onSurfaceVariant = TextSecondary,
    outline = SlateBorder,
    error = CoralAlert,
    onError = MidnightObsidian
)

@Composable
fun WildHorizonTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WildHorizonDarkColorScheme,
        typography = WildHorizonTypography,
        content = content
    )
}
