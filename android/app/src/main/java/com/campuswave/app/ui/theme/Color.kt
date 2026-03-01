package com.campuswave.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary brand colors - Premium & Vibrant
val PrimaryBlue = Color(0xFF5E72E4)     // Vibrant Blue
val PrimaryPurple = Color(0xFF825EE4)   // Deep Purple
val AccentPink = Color(0xFFF5365C)      // Vibrant Pink
val AccentOrange = Color(0xFFFB6340)    // Energetic Orange
val AccentCyan = Color(0xFF11CDEF)      // Bright Cyan
val SuccessGreen = Color(0xFF2DCE89)    // Fresh Green
val accentPurple = Color(0xFF6366F1)   // Premium Indigo/Purple
val ErrorRed = Color(0xFFF5365C)        // Same as accent pink for errors

// Gradient Brushes
val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(PrimaryBlue, PrimaryPurple)
)

val CardGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF232323), Color(0xFF1A1A1A))
)

val LightCardGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFFFFFFF), Color(0xFFF8F9FA))
)

// ==================== DARK THEME COLORS ====================
val DarkBackground = Color(0xFF121212)      // Dark Background
val DarkSurface = Color(0xFF1E1E1E)         // Dark Surface
val DarkOnBackground = Color(0xFFEDE7F6)    // Light Text on Dark
val DarkOnSurface = Color(0xFFE1E1E1)       // Surface text
val DarkGrey = Color(0xFFA0A0A0)            // Secondary Text
val DarkBorder = Color(0xFF424242)          // Borders/Lines

// ==================== LIGHT THEME COLORS ====================
val LightBackground = Color(0xFFF5F7FA)     // Light gray background
val LightSurface = Color(0xFFFFFFFF)        // Pure white surface
val LightOnBackground = Color(0xFF1A1A2E)   // Dark text on light
val LightOnSurface = Color(0xFF2D2D44)      // Surface text
val LightGrey = Color(0xFF6E6E80)           // Secondary Text
val LightBorder = Color(0xFFE0E0E0)         // Borders/Lines

// ==================== SEMANTIC COLOR HELPERS ====================
// These are now legacy - prefer using ThemeColors or MaterialTheme.colorScheme directly
val CampusDark = DarkOnBackground           // Light Text on Dark (for dark theme)
val CampusGrey = Color(0xFFA0A0A0)          // Standard Grey
val CampusBackground = DarkBackground       // Legacy Dark Background
val CampusSurface = DarkSurface             // Legacy Dark Surface
val CampusLight = LightBorder               // Legacy Light Border

/**
 * Accessor for the current theme's primary background color.
 */
@Composable
fun campusBackground() = if (LocalIsDarkTheme.current) DarkBackground else LightBackground

/**
 * Accessor for the current theme's surface color.
 */
@Composable
fun campusSurface() = if (LocalIsDarkTheme.current) DarkSurface else LightSurface

/**
 * Accessor for the primary text color (on background).
 */
@Composable
fun campusOnBackground() = if (LocalIsDarkTheme.current) DarkOnBackground else LightOnBackground

/**
 * Accessor for the transition/border color.
 */
@Composable
fun campusDivider() = if (LocalIsDarkTheme.current) DarkBorder else LightBorder

/**
 * Accessor for the grey/secondary text color.
 */
@Composable
fun campusGrey() = if (LocalIsDarkTheme.current) DarkGrey else LightGrey

// Status Colors Specific
val StatusLive = Color(0xFFE91E63)
val StatusUpcoming = Color(0xFF5E72E4)
val StatusMissed = Color(0xFF757575)

// Old compatibility values (mapping to new ones to avoid breaking existing code)
val Purple80 = PrimaryPurple
val PurpleGrey80 = CampusGrey
val Pink80 = AccentPink

val Purple40 = PrimaryBlue
val PurpleGrey40 = CampusDark
val Pink40 = AccentPink

val CampusPurple = PrimaryBlue
val CampusLightPurple = Color(0xFF311B92)   // Darker purple for background
val CampusOnSurface = CampusDark

// Semantic Aliases
val LiveRadioColor = StatusLive
val UpcomingRadioColor = StatusUpcoming
val PendingRadioColor = Color(0xFFFF9800)

// ==================== THEME-AWARE COLOR OBJECT ====================
/**
 * Provides theme-aware colors that can be used throughout the app.
 * Use these for components that need to adapt to the current theme.
 */
object ThemeColors {
    // Returns appropriate background color based on dark mode
    fun background(isDark: Boolean) = if (isDark) DarkBackground else LightBackground
    
    // Returns appropriate surface color based on dark mode
    fun surface(isDark: Boolean) = if (isDark) DarkSurface else LightSurface
    
    // Returns appropriate text color for primary content
    fun onBackground(isDark: Boolean) = if (isDark) DarkOnBackground else LightOnBackground
    
    // Returns appropriate text color for surface content
    fun onSurface(isDark: Boolean) = if (isDark) DarkOnSurface else LightOnSurface
    
    // Returns appropriate secondary text color
    fun grey(isDark: Boolean) = if (isDark) DarkGrey else LightGrey
    
    // Returns appropriate border color
    fun border(isDark: Boolean) = if (isDark) DarkBorder else LightBorder
    
    // Returns the card gradient based on theme
    fun cardGradient(isDark: Boolean) = if (isDark) CardGradient else LightCardGradient
}