package com.exa.android.reflekt.uicompose.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val steel900 = Color(0xFF071424)
private val steel800 = Color(0xFF0E2848)
private val steel700 = Color(0xFF153C6C)
private val steel600 = Color(0xFF1C5090)
private val steel500 = Color(0xFF2364B4)
private val steel400 = Color(0xFF4F83C3)
private val steel300 = Color(0xFF7BA2D2)
private val steel200 = Color(0xFFA7C1E1)
private val steel100 = Color(0xFFD3E0F0)


private val blue900 = Color(0xFF00174D)
private val blue800 = Color(0xFF002E7A)
private val blue700 = Color(0xFF0045A7)
private val blue600 = Color(0xFF005CD4)
private val blue500 = Color(0xFF0073FF)
private val blue400 = Color(0xFF3392FF)
private val blue300 = Color(0xFF66B1FF)
private val blue200 = Color(0xFF99D0FF)
private val blue100 = Color(0xFFCCE8FF)

private val purple900 = Color(0xFF300142)
private val purple800 = Color(0xFF4A0264)
private val purple700 = Color(0xFF640386)
private val purple600 = Color(0xFF7E04A8)
private val purple500 = Color(0xFF9805CA)
private val purple400 = Color(0xFFAD37D5)
private val purple300 = Color(0xFFC269E0)
private val purple200 = Color(0xFFD79BEB)
private val purple100 = Color(0xFFEBCDF5)

private val black900 = Color(0xFF000000)
private val black800 = Color(0xFF1A1A1A)
private val black700 = Color(0xFF333333)
private val black600 = Color(0xFF4D4D4D)
private val black500 = Color(0xFF666666)
private val black400 = Color(0xFF808080)
private val black300 = Color(0xFF999999)
private val black200 = Color(0xFFB3B3B3)
private val black100 = Color(0xFFCCCCCC)

private val white = Color(0xFFFFFFFF)
private val transparent = Color(0x00000000)

private val success900 = Color(0xFF0A4D0A)
private val success500 = Color(0xFF28A745)
private val success100 = Color(0xFFD4EDDA)

private val warning900 = Color(0xFF856404)
private val warning500 = Color(0xFFFFC107)
private val warning100 = Color(0xFFFFF3CD)

private val critical900 = Color(0xFF721C24)
private val critical500 = Color(0xFFDC3545)
private val critical100 = Color(0xFFF8D7DA)

sealed class FindrColors {
    
    @Immutable
    data class Content(
        val primary: Color,
        val secondary: Color,
        val tertiary: Color,
        val inverse: Color,
        val disabled: Color,
        val onPrimary: Color,
        val onSecondary: Color,
        val onCritical: Color,
        val onSuccess: Color,
        val onWarning: Color
    )
    
    @Immutable
    data class Stroke(
        val primary: Color,
        val secondary: Color,
        val tertiary: Color,
        val disabled: Color,
        val focus: Color,
        val error: Color,
        val success: Color
    )
    
    @Immutable
    data class Surface(
        val primary: Color,
        val secondary: Color,
        val tertiary: Color,
        val inverse: Color,
        val overlay: Color,
        val disabled: Color,
        val hover: Color,
        val pressed: Color
    )
    
    @Immutable
    data class Gradient(
        val primaryStart: Color,
        val primaryEnd: Color,
        val secondaryStart: Color,
        val secondaryEnd: Color,
        val accentStart: Color,
        val accentEnd: Color
    )
}

@Immutable
data class FindrColorScheme(
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val secondaryVariant: Color,
    val tertiary: Color,
    val critical: Color,
    val criticalVariant: Color,
    val warning: Color,
    val warningVariant: Color,
    val success: Color,
    val successVariant: Color,
    val white: Color,
    val black: Color,
    val content: FindrColors.Content,
    val stroke: FindrColors.Stroke,
    val surface: FindrColors.Surface,
    val gradient: FindrColors.Gradient,
    val isDark: Boolean
)

val FindrLightColorScheme = FindrColorScheme(
    primary = blue600,
    primaryVariant = blue700,
    secondary = purple600,
    secondaryVariant = purple700,
    tertiary = steel600,
    critical = critical500,
    criticalVariant = critical900,
    warning = warning500,
    warningVariant = warning900,
    success = success500,
    successVariant = success900,
    white = white,
    black = black900,
    content = FindrColors.Content(
        primary = black900,
        secondary = black700,
        tertiary = black500,
        inverse = white,
        disabled = black300,
        onPrimary = white,
        onSecondary = white,
        onCritical = white,
        onSuccess = white,
        onWarning = black900
    ),
    stroke = FindrColors.Stroke(
        primary = black300,
        secondary = black200,
        tertiary = black100,
        disabled = black100,
        focus = blue600,
        error = critical500,
        success = success500
    ),
    surface = FindrColors.Surface(
        primary = white,
        secondary = black100,
        tertiary = steel100,
        inverse = black900,
        overlay = Color(0x80000000),
        disabled = black100,
        hover = black100,
        pressed = black200
    ),
    gradient = FindrColors.Gradient(
        primaryStart = blue600,
        primaryEnd = blue800,
        secondaryStart = purple600,
        secondaryEnd = purple800,
        accentStart = blue500,
        accentEnd = purple500
    ),
    isDark = false
)

val FindrDarkColorScheme = FindrColorScheme(
    primary = blue400,
    primaryVariant = blue500,
    secondary = purple400,
    secondaryVariant = purple500,
    tertiary = steel400,
    critical = critical500,
    criticalVariant = critical100,
    warning = warning500,
    warningVariant = warning100,
    success = success500,
    successVariant = success100,
    white = white,
    black = black900,
    content = FindrColors.Content(
        primary = white,
        secondary = black100,
        tertiary = black300,
        inverse = black900,
        disabled = black500,
        onPrimary = black900,
        onSecondary = black900,
        onCritical = white,
        onSuccess = black900,
        onWarning = black900
    ),
    stroke = FindrColors.Stroke(
        primary = black600,
        secondary = black700,
        tertiary = black800,
        disabled = black700,
        focus = blue400,
        error = critical500,
        success = success500
    ),
    surface = FindrColors.Surface(
        primary = black900,
        secondary = black800,
        tertiary = steel900,
        inverse = white,
        overlay = Color(0x80000000),
        disabled = black800,
        hover = black700,
        pressed = black600
    ),
    gradient = FindrColors.Gradient(
        primaryStart = blue400,
        primaryEnd = blue600,
        secondaryStart = purple400,
        secondaryEnd = purple600,
        accentStart = blue300,
        accentEnd = purple300
    ),
    isDark = true
)

/**
 * Returns a hover color for the given color (slightly lighter/darker based on luminance)
 */
fun Color.hoverColor(): Color {
    val luminance = (0.299 * red + 0.587 * green + 0.114 * blue)
    return if (luminance > 0.5) {
        copy(
            red = (red * 0.9f).coerceIn(0f, 1f),
            green = (green * 0.9f).coerceIn(0f, 1f),
            blue = (blue * 0.9f).coerceIn(0f, 1f)
        )
    } else {
        copy(
            red = (red * 1.1f).coerceIn(0f, 1f),
            green = (green * 1.1f).coerceIn(0f, 1f),
            blue = (blue * 1.1f).coerceIn(0f, 1f)
        )
    }
}

/**
 * Returns appropriate content color for the given background color
 */
fun Color.contentColor(): Color {
    val luminance = (0.299 * red + 0.587 * green + 0.114 * blue)
    return if (luminance > 0.5) black900 else white
}

/**
 * Returns appropriate stroke color for the given color
 */
fun Color.strokeColor(): Color {
    val luminance = (0.299 * red + 0.587 * green + 0.114 * blue)
    return if (luminance > 0.5) {
        copy(
            red = (red * 0.7f).coerceIn(0f, 1f),
            green = (green * 0.7f).coerceIn(0f, 1f),
            blue = (blue * 0.7f).coerceIn(0f, 1f)
        )
    } else {
        copy(
            red = (red * 1.3f).coerceIn(0f, 1f),
            green = (green * 1.3f).coerceIn(0f, 1f),
            blue = (blue * 1.3f).coerceIn(0f, 1f)
        )
    }
}

/**
 * Returns disabled version of the color (reduced opacity)
 */
fun Color.disabledColor(): Color {
    return copy(alpha = 0.38f)
}

/**
 * Returns pressed state color (slightly darker)
 */
fun Color.pressedColor(): Color {
    return copy(
        red = (red * 0.85f).coerceIn(0f, 1f),
        green = (green * 0.85f).coerceIn(0f, 1f),
        blue = (blue * 0.85f).coerceIn(0f, 1f)
    )
}

val LocalFindrColorScheme = staticCompositionLocalOf { FindrLightColorScheme }
