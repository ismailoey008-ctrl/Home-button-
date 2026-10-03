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

private val DarkColorScheme = darkColorScheme(
    primary = TealPrimaryDark,
    onPrimary = Color(0xFF003734),
    primaryContainer = TealContainer,
    onPrimaryContainer = Color(0xFFA5F3EB),
    secondary = SlateLight,
    onSecondary = Color.White,
    background = DarkBackground,
    onBackground = Color(0xFFE4E9F0),
    surface = DarkSurface,
    onSurface = Color(0xFFE4E9F0),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFB0BAC8),
    tertiary = AccentGreen
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6F6F3),
    onPrimaryContainer = Color(0xFF004D47),
    secondary = SlateSecondary,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = Color(0xFF191D23),
    surface = LightSurface,
    onSurface = Color(0xFF191D23),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF4A5568),
    tertiary = AccentBlue
)

@Composable
fun HomeCircleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep intentional brand design
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
