package org.dastakvani.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CivicColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = BackgroundWhite,
    primaryContainer = SurfaceLightBlue,
    onPrimaryContainer = PrimaryDarkNavy,
    secondary = PrimaryLightBlue,
    onSecondary = BackgroundWhite,
    background = BackgroundWhite,
    onBackground = TextDark,
    surface = SurfaceCard,
    onSurface = TextDark,
    outline = BorderSubtle
)

@Composable
fun DastakVaniTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CivicColorScheme,
        content = content
    )
}
