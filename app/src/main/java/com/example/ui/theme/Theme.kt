package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Professional Polish Theme Color Definitions
val PolishIndigo = Color(0xFF4F46E5)      // Primary brand indigo color
val PolishIndigoDark = Color(0xFF3730A3)
val PolishIndigoLight = Color(0xFF818CF8)

val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val Slate700 = Color(0xFF334155)
val Slate500 = Color(0xFF64748B)
val Slate100 = Color(0xFFF1F5F9)
val PolishBackground = Color(0xFFF3F4F9) // Clean slate bg from HTML

private val DarkColorScheme = darkColorScheme(
    primary = PolishIndigoLight,
    onPrimary = Color.White,
    secondary = Slate500,
    background = Slate900,
    surface = Slate800,
    onBackground = Slate100,
    onSurface = Color.White,
    secondaryContainer = Slate700,
    onSecondaryContainer = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PolishIndigo,
    onPrimary = Color.White,
    secondary = Slate500,
    background = PolishBackground,
    surface = Color.White,
    onBackground = Slate900,
    onSurface = Slate800,
    secondaryContainer = Slate100,
    onSecondaryContainer = Slate900
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to strictly enforce our beautiful design theme!
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
