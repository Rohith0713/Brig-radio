package com.campuswave.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.campuswave.app.data.theme.ThemeManager
import com.campuswave.app.data.theme.ThemeMode

// Dark Color Scheme with Premium Colors
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryPurple,
    onPrimaryContainer = Color.White,
    secondary = AccentCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1E3A5F),
    onSecondaryContainer = AccentCyan,
    tertiary = AccentPink,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF5C1A2A),
    onTertiaryContainer = AccentPink,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = DarkGrey,
    outline = DarkBorder,
    outlineVariant = Color(0xFF363636),
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFF5C1A2A),
    onErrorContainer = AccentPink,
    inverseSurface = LightSurface,
    inverseOnSurface = LightOnSurface,
    inversePrimary = PrimaryBlue
)

// Light Color Scheme with Premium Colors
private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8EAFF),
    onPrimaryContainer = PrimaryPurple,
    secondary = AccentCyan,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD4F4FF),
    onSecondaryContainer = Color(0xFF0D5670),
    tertiary = AccentPink,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE5E9),
    onTertiaryContainer = Color(0xFF8B1A3A),
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = Color(0xFFF0F2F5),
    onSurfaceVariant = LightGrey,
    outline = LightBorder,
    outlineVariant = Color(0xFFE8E8E8),
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFE5E9),
    onErrorContainer = Color(0xFF8B1A3A),
    inverseSurface = DarkSurface,
    inverseOnSurface = DarkOnSurface,
    inversePrimary = Color(0xFFB4C5FF)
)

/**
 * Composition local to access current dark mode state throughout the app
 */
val LocalIsDarkTheme = compositionLocalOf { false }

/**
 * Composition local to access theme change callback
 */
val LocalThemeManager = compositionLocalOf<ThemeManager?> { null }

@Composable
fun BRIG_RADIOTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled by default to use our custom colors
    content: @Composable () -> Unit
) {
    // Determine if we should use dark theme based on theme mode
    val systemIsDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> systemIsDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val context = LocalContext.current
    val themeManager = ThemeManager.getInstance(context)

    CompositionLocalProvider(
        LocalIsDarkTheme provides darkTheme,
        LocalThemeManager provides themeManager
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

/**
 * Wrapper composable that automatically reads theme from ThemeManager
 */
@Composable
fun BRIG_RADIOThemeWithManager(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeManager = ThemeManager.getInstance(context)
    val themeMode by themeManager.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    
    BRIG_RADIOTheme(
        themeMode = themeMode,
        content = content
    )
}