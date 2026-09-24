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

private val LightColorScheme = lightColorScheme(
    primary = ZenPurple,
    onPrimary = Color.White,
    primaryContainer = ZenPurpleLight.copy(alpha = 0.35f),
    onPrimaryContainer = ZenPurple,
    secondary = ZenTeal,
    onSecondary = Color.White,
    secondaryContainer = ZenTealLight.copy(alpha = 0.35f),
    tertiary = ZenPink,
    onTertiary = Color.White,
    background = SoftBackgroundLight,
    surface = SoftSurfaceLight,
    onBackground = SoftTextPrimaryLight,
    onSurface = SoftTextPrimaryLight,
    surfaceVariant = Color(0xFFEDF2F7),
    onSurfaceVariant = SoftTextSecondaryLight
)

private val DarkColorScheme = darkColorScheme(
    primary = ZenPurpleLight,
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = ZenPurple.copy(alpha = 0.4f),
    onPrimaryContainer = Color.White,
    secondary = ZenTealLight,
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = ZenTeal.copy(alpha = 0.4f),
    tertiary = ZenPinkLight,
    onTertiary = Color(0xFF4C0519),
    background = SoftBackgroundDark,
    surface = SoftSurfaceDark,
    onBackground = SoftTextPrimaryDark,
    onSurface = SoftTextPrimaryDark,
    surfaceVariant = Color(0xFF263050),
    onSurfaceVariant = SoftTextSecondaryDark
)

@Composable
fun ZenPlayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
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
