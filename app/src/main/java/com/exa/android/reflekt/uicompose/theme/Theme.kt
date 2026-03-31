package com.exa.android.reflekt.uicompose.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat

@Immutable
data class FindrThemeConfig(
    val typography: FindrTypography,
    val colorScheme: FindrColorScheme,
    val dimens: FindrDimens,
    val shapes: FindrShapes
)

private fun FindrColorScheme.toMaterial3ColorScheme() = if (isDark) {
    darkColorScheme(
        primary = primary,
        onPrimary = content.onPrimary,
        primaryContainer = primaryVariant,
        onPrimaryContainer = content.onPrimary,
        secondary = secondary,
        onSecondary = content.onSecondary,
        secondaryContainer = secondaryVariant,
        onSecondaryContainer = content.onSecondary,
        tertiary = tertiary,
        error = critical,
        onError = content.onCritical,
        errorContainer = criticalVariant,
        onErrorContainer = content.onCritical,
        background = surface.primary,
        onBackground = content.primary,
        surface = surface.primary,
        onSurface = content.primary,
        surfaceVariant = surface.secondary,
        onSurfaceVariant = content.secondary,
        outline = stroke.primary,
        outlineVariant = stroke.secondary
    )
} else {
    lightColorScheme(
        primary = primary,
        onPrimary = content.onPrimary,
        primaryContainer = primaryVariant,
        onPrimaryContainer = content.onPrimary,
        secondary = secondary,
        onSecondary = content.onSecondary,
        secondaryContainer = secondaryVariant,
        onSecondaryContainer = content.onSecondary,
        tertiary = tertiary,
        error = critical,
        onError = content.onCritical,
        errorContainer = criticalVariant,
        onErrorContainer = content.onCritical,
        background = surface.primary,
        onBackground = content.primary,
        surface = surface.primary,
        onSurface = content.primary,
        surfaceVariant = surface.secondary,
        onSurfaceVariant = content.secondary,
        outline = stroke.primary,
        outlineVariant = stroke.secondary
    )
}

@Composable
fun FindrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    typography: FindrTypography = DefaultFindrTypography,
    dimens: FindrDimens = DefaultFindrDimens,
    shapes: FindrShapes = DefaultFindrShapes,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) FindrDarkColorScheme else FindrLightColorScheme
    
    val material3ColorScheme = colorScheme.toMaterial3ColorScheme()
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.statusBarColor = colorScheme.surface.primary.toArgb()
            window?.navigationBarColor = colorScheme.surface.primary.toArgb()
            
            WindowCompat.getInsetsController(window!!, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    
    CompositionLocalProvider(
        LocalFindrColorScheme provides colorScheme,
        LocalFindrTypography provides typography,
        LocalFindrDimens provides dimens,
        LocalFindrShapes provides shapes
    ) {
        MaterialTheme(
            colorScheme = material3ColorScheme,
            content = content
        )
    }
}

object FindrTheme {
    
    /**
     * Access current color scheme
     */
    val colorScheme: FindrColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalFindrColorScheme.current
    
    /**
     * Access current typography
     */
    val typography: FindrTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalFindrTypography.current
    
    /**
     * Access current dimensions
     */
    val dimens: FindrDimens
        @Composable
        @ReadOnlyComposable
        get() = LocalFindrDimens.current
    
    /**
     * Access current shapes
     */
    val shapes: FindrShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalFindrShapes.current
}


enum class ScreenSize {
    COMPACT,
    MEDIUM,
    EXPANDED
}

@Composable
fun getScreenSize(): ScreenSize {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    
    return when {
        screenWidthDp < 600 -> ScreenSize.COMPACT
        screenWidthDp < 840 -> ScreenSize.MEDIUM
        else -> ScreenSize.EXPANDED
    }
}


/**
 * Responsive FindrTheme that adapts to screen size
 */
@Composable
fun ResponsiveFindrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    screenSize: ScreenSize = getScreenSize(),
    content: @Composable () -> Unit
) {
    val (typography, dimens, shapes) = when (screenSize) {
        ScreenSize.COMPACT -> Triple(
            CompactFindrTypography,
            CompactFindrDimens,
            CompactFindrShapes
        )
        ScreenSize.MEDIUM -> Triple(
            DefaultFindrTypography,
            DefaultFindrDimens,
            DefaultFindrShapes
        )
        ScreenSize.EXPANDED -> Triple(
            ExpandedFindrTypography,
            ExpandedFindrDimens,
            ExpandedFindrShapes
        )
    }
    
    FindrTheme(
        darkTheme = darkTheme,
        typography = typography,
        dimens = dimens,
        shapes = shapes,
        content = content
    )
}


/**
 * Light theme preview wrapper
 */

@Composable
fun FindrLightThemePreview(content: @Composable () -> Unit) {
    FindrTheme(darkTheme = false, content = content)
}

/**
 * Dark theme preview wrapper
 */

@Composable
fun FindrDarkThemePreview(content: @Composable () -> Unit) {
    FindrTheme(darkTheme = true, content = content)
}