package com.buildabear.tracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PinkPrimary = Color(0xFFE91E8C)
private val PinkDark = Color(0xFFB7156F)

private val LightColors = lightColorScheme(
    primary = PinkPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD8EC),
    secondary = PinkDark,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF8BC8),
    onPrimary = Color(0xFF3D0024),
    primaryContainer = PinkDark,
    secondary = Color(0xFFFFB3DA),
)

@Composable
fun BearTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
