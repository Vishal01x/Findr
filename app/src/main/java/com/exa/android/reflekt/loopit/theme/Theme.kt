package com.exa.android.reflekt.loopit.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Light scheme uses LiquidUI design tokens — dark scheme is unchanged
private val LightColorScheme = lightColorScheme(
    primary                = PrimaryLight,
    onPrimary              = OnPrimaryLight,
    primaryContainer       = PrimaryVariantLight,   // sky-tinted light container
    onPrimaryContainer     = Color(0xFF003D56),

    secondary              = SecondaryLight,         // slate-600 for secondary text/buttons
    onSecondary            = OnSecondaryLight,
    secondaryContainer     = SecondaryVariantLight,
    onSecondaryContainer   = OnBackgroundLight,

    tertiary               = Color(0xFF8B5CF6),      // accent purple
    onTertiary             = Color(0xFFFFFFFF),
    tertiaryContainer      = Color(0xFFEDE9FE),
    onTertiaryContainer    = Color(0xFF2E1065),

    background             = BackgroundLight,        // slate-100 (LiquidColors.BgBase light)
    onBackground           = OnBackgroundLight,      // slate-900 (LiquidColors.TextPrimary light)

    surface                = SurfaceLight,           // pure white cards
    onSurface              = OnSurfaceLight,
    surfaceVariant         = SurfaceVariantLight,    // off-white nav/elevated surfaces
    onSurfaceVariant       = TextSecondaryLight,     // slate-600 (LiquidColors.TextSecondary light)

    outline                = DividerLight,           // slate-300 — glass border
    outlineVariant         = OutlineVariantLight,    // slate-200 — subtle

    error                  = ErrorLight,
    onError                = OnErrorLight,
    errorContainer         = Color(0xFFFEE2E2),
    onErrorContainer       = Color(0xFF7F1D1D),

    inverseSurface         = Color(0xFF0F172A),
    inverseOnSurface       = BackgroundLight,
    inversePrimary         = Color(0xFF7DD3F0),

    scrim                  = Color(0xFF000000),
    surfaceTint            = PrimaryLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryVariantDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryVariantDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    error = ErrorDark,
    onError = OnErrorDark,
    outline = DividerDark,
    surfaceTint = PrimaryDark
)

@Composable
fun FindrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Enable edge-to-edge display
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // Make status bar transparent
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()

            // Set status bar icons color based on theme
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
