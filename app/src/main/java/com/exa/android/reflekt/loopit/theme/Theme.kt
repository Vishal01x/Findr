package com.exa.android.reflekt.loopit.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)*/

// Light Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2A5C82),       // Main primary (Deep Navy)
    onPrimary = Color.White,           // Text/icons on primary
    secondary = Color(0xFF4ECDC4),     // Turquoise Teal
    onSecondary = Color.Black,         // Text/icons on secondary
    tertiary = Color(0xFF6C5B7B),      // Muted Purple
    background = Color(0xFFF5F5F5),    // Off-White background
    surface = Color.White,             // Cards/surface elements
    onBackground = Color(0xFF333333),  // Primary text color
    error = Color(0xFFE57373),         // Soft Red
    primaryContainer = Color(0xFF2A5C82), // Container for primary
    secondaryContainer = Color(0xFF4ECDC4) // Container for secondary
)

// Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4ECDC4),       // Teal becomes primary in dark
    onPrimary = Color.Black,
    secondary = Color(0xFF2A5C82),     // Navy becomes secondary
    onSecondary = Color.White,
    tertiary = Color(0xFFB39BC4),      // Lighter muted purple
    background = Color(0xFF121212),    // Dark background
    surface = Color(0xFF242424),       // Dark surface elements
    onBackground = Color(0xFFE0E0E0),  // Light text
    error = Color(0xFFEF5350),
    primaryContainer = Color(0xFF2A5C82),
    secondaryContainer = Color(0xFF4ECDC4)
)

// Extended colors for custom components
object AppColors {
    val CoralAccent = Color(0xFFFF6B6B)
    val GoldAccent = Color(0xFFFFD166)
    val NotificationAlert = Color(0xFFFFD700)
    val DividerColor = Color(0xFFE0E0E0)
}

// Typography Setup
val AppTypography = androidx.compose.material3.Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        color = Color(0xFF333333)
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = Color(0xFF6C5B7B)
    )
)

// Shapes for Components
val AppShapes = androidx.compose.material3.Shapes(
    small = RoundedCornerShape(4.dp),   // For buttons, chips
    medium = RoundedCornerShape(8.dp),  // Cards, dialogs
    large = RoundedCornerShape(16.dp)   // Large containers, sheets
)

@Composable
fun ReflektTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}


