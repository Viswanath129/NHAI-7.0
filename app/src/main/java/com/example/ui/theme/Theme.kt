package com.example.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DeepSpace = Color(0xFF0A0E14)
val NeonAccent = Color(0xFF00E5FF)
val SlateGray = Color(0xFF1C252E)
val AlertRed = Color(0xFFFF3D00)
val SuccessGreen = Color(0xFF00E676)

private val DarkColorScheme = darkColorScheme(
    primary = NeonAccent,
    background = DeepSpace,
    surface = SlateGray,
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    error = AlertRed
)

val ColorScheme.successGreen: Color get() = Color(0xFF00E676)
val ColorScheme.deepSpace: Color get() = Color(0xFF0A0E14)

@Composable
fun NHAiAuthTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
