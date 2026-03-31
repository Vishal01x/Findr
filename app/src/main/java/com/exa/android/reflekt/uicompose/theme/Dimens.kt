package com.exa.android.reflekt.uicompose.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed class FindrDimenVariants {
    
    @Immutable
    data class Margin(
        val zero: Dp,
        val xxs: Dp,
        val xs: Dp,
        val small: Dp,
        val smallSecondary: Dp,
        val medium: Dp,
        val mediumSecondary: Dp,
        val large: Dp,
        val largeSecondary: Dp,
        val xl: Dp,
        val xxl: Dp,
        val xxxl: Dp
    )
    
    @Immutable
    data class Stroke(
        val zero: Dp,
        val thin: Dp,
        val regular: Dp,
        val medium: Dp,
        val thick: Dp,
        val extraThick: Dp
    )
    
    @Immutable
    data class Image(
        val xxs: Dp,
        val xs: Dp,
        val small: Dp,
        val medium: Dp,
        val large: Dp,
        val xl: Dp,
        val xxl: Dp,
        val thumbnail: Dp,
        val avatar: Dp,
        val hero: Dp
    )
    
    @Immutable
    data class Icon(
        val xxs: Dp,
        val xs: Dp,
        val small: Dp,
        val medium: Dp,
        val large: Dp,
        val xl: Dp,
        val xxl: Dp
    )
    
    @Immutable
    data class Card(
        val minHeight: Dp,
        val smallHeight: Dp,
        val mediumHeight: Dp,
        val largeHeight: Dp,
        val padding: Dp,
        val paddingSmall: Dp,
        val paddingLarge: Dp,
        val spacing: Dp
    )
    
    @Immutable
    data class Text(
        val lineHeightSmall: Dp,
        val lineHeightMedium: Dp,
        val lineHeightLarge: Dp,
        val letterSpacingTight: Dp,
        val letterSpacingNormal: Dp,
        val letterSpacingWide: Dp
    )
    
    @Immutable
    data class Elevation(
        val zero: Dp,
        val xs: Dp,
        val small: Dp,
        val medium: Dp,
        val large: Dp,
        val xl: Dp,
        val xxl: Dp
    )
}

@Immutable
data class FindrDimens(
    val margin: FindrDimenVariants.Margin,
    val stroke: FindrDimenVariants.Stroke,
    val image: FindrDimenVariants.Image,
    val icon: FindrDimenVariants.Icon,
    val card: FindrDimenVariants.Card,
    val text: FindrDimenVariants.Text,
    val elevation: FindrDimenVariants.Elevation
)

val DefaultFindrDimens = FindrDimens(
    margin = FindrDimenVariants.Margin(
        zero = 0.dp,
        xxs = 2.dp,
        xs = 4.dp,
        small = 8.dp,
        smallSecondary = 12.dp,
        medium = 16.dp,
        mediumSecondary = 20.dp,
        large = 24.dp,
        largeSecondary = 28.dp,
        xl = 32.dp,
        xxl = 48.dp,
        xxxl = 64.dp
    ),
    stroke = FindrDimenVariants.Stroke(
        zero = 0.dp,
        thin = 0.5.dp,
        regular = 1.dp,
        medium = 2.dp,
        thick = 3.dp,
        extraThick = 4.dp
    ),
    image = FindrDimenVariants.Image(
        xxs = 24.dp,
        xs = 32.dp,
        small = 48.dp,
        medium = 64.dp,
        large = 96.dp,
        xl = 128.dp,
        xxl = 192.dp,
        thumbnail = 80.dp,
        avatar = 40.dp,
        hero = 240.dp
    ),
    icon = FindrDimenVariants.Icon(
        xxs = 12.dp,
        xs = 16.dp,
        small = 20.dp,
        medium = 24.dp,
        large = 32.dp,
        xl = 40.dp,
        xxl = 48.dp
    ),
    card = FindrDimenVariants.Card(
        minHeight = 56.dp,
        smallHeight = 80.dp,
        mediumHeight = 120.dp,
        largeHeight = 160.dp,
        padding = 16.dp,
        paddingSmall = 12.dp,
        paddingLarge = 24.dp,
        spacing = 8.dp
    ),
    text = FindrDimenVariants.Text(
        lineHeightSmall = 16.dp,
        lineHeightMedium = 20.dp,
        lineHeightLarge = 24.dp,
        letterSpacingTight = (-0.5).dp,
        letterSpacingNormal = 0.dp,
        letterSpacingWide = 0.5.dp
    ),
    elevation = FindrDimenVariants.Elevation(
        zero = 0.dp,
        xs = 1.dp,
        small = 2.dp,
        medium = 4.dp,
        large = 8.dp,
        xl = 12.dp,
        xxl = 16.dp
    )
)

val CompactFindrDimens = FindrDimens(
    margin = FindrDimenVariants.Margin(
        zero = 0.dp,
        xxs = 2.dp,
        xs = 4.dp,
        small = 6.dp,
        smallSecondary = 10.dp,
        medium = 12.dp,
        mediumSecondary = 16.dp,
        large = 20.dp,
        largeSecondary = 24.dp,
        xl = 28.dp,
        xxl = 40.dp,
        xxxl = 56.dp
    ),
    stroke = FindrDimenVariants.Stroke(
        zero = 0.dp,
        thin = 0.5.dp,
        regular = 1.dp,
        medium = 1.5.dp,
        thick = 2.dp,
        extraThick = 3.dp
    ),
    image = FindrDimenVariants.Image(
        xxs = 20.dp,
        xs = 28.dp,
        small = 40.dp,
        medium = 56.dp,
        large = 80.dp,
        xl = 112.dp,
        xxl = 160.dp,
        thumbnail = 64.dp,
        avatar = 36.dp,
        hero = 200.dp
    ),
    icon = FindrDimenVariants.Icon(
        xxs = 10.dp,
        xs = 14.dp,
        small = 18.dp,
        medium = 22.dp,
        large = 28.dp,
        xl = 36.dp,
        xxl = 44.dp
    ),
    card = FindrDimenVariants.Card(
        minHeight = 48.dp,
        smallHeight = 72.dp,
        mediumHeight = 104.dp,
        largeHeight = 144.dp,
        padding = 12.dp,
        paddingSmall = 8.dp,
        paddingLarge = 20.dp,
        spacing = 6.dp
    ),
    text = FindrDimenVariants.Text(
        lineHeightSmall = 14.dp,
        lineHeightMedium = 18.dp,
        lineHeightLarge = 22.dp,
        letterSpacingTight = (-0.5).dp,
        letterSpacingNormal = 0.dp,
        letterSpacingWide = 0.5.dp
    ),
    elevation = FindrDimenVariants.Elevation(
        zero = 0.dp,
        xs = 1.dp,
        small = 2.dp,
        medium = 3.dp,
        large = 6.dp,
        xl = 10.dp,
        xxl = 14.dp
    )
)

val ExpandedFindrDimens = FindrDimens(
    margin = FindrDimenVariants.Margin(
        zero = 0.dp,
        xxs = 4.dp,
        xs = 6.dp,
        small = 12.dp,
        smallSecondary = 16.dp,
        medium = 20.dp,
        mediumSecondary = 24.dp,
        large = 32.dp,
        largeSecondary = 36.dp,
        xl = 40.dp,
        xxl = 56.dp,
        xxxl = 72.dp
    ),
    stroke = FindrDimenVariants.Stroke(
        zero = 0.dp,
        thin = 0.5.dp,
        regular = 1.dp,
        medium = 2.dp,
        thick = 4.dp,
        extraThick = 6.dp
    ),
    image = FindrDimenVariants.Image(
        xxs = 28.dp,
        xs = 40.dp,
        small = 56.dp,
        medium = 80.dp,
        large = 112.dp,
        xl = 160.dp,
        xxl = 224.dp,
        thumbnail = 96.dp,
        avatar = 48.dp,
        hero = 320.dp
    ),
    icon = FindrDimenVariants.Icon(
        xxs = 16.dp,
        xs = 20.dp,
        small = 24.dp,
        medium = 28.dp,
        large = 36.dp,
        xl = 48.dp,
        xxl = 56.dp
    ),
    card = FindrDimenVariants.Card(
        minHeight = 64.dp,
        smallHeight = 96.dp,
        mediumHeight = 144.dp,
        largeHeight = 192.dp,
        padding = 20.dp,
        paddingSmall = 16.dp,
        paddingLarge = 32.dp,
        spacing = 12.dp
    ),
    text = FindrDimenVariants.Text(
        lineHeightSmall = 18.dp,
        lineHeightMedium = 24.dp,
        lineHeightLarge = 28.dp,
        letterSpacingTight = (-0.5).dp,
        letterSpacingNormal = 0.dp,
        letterSpacingWide = 0.5.dp
    ),
    elevation = FindrDimenVariants.Elevation(
        zero = 0.dp,
        xs = 2.dp,
        small = 4.dp,
        medium = 6.dp,
        large = 10.dp,
        xl = 16.dp,
        xxl = 20.dp
    )
)

val LocalFindrDimens = staticCompositionLocalOf { DefaultFindrDimens }