package com.exa.android.reflekt.uicompose.enums

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.exa.android.reflekt.uicompose.theme.FindrColorScheme
import com.exa.android.reflekt.uicompose.theme.FindrTheme

/**
 * Interface defining the color structure for buttons
 */
@Immutable
abstract class FindrButtonColors {
    abstract val container: Color
    abstract val content: Color
    abstract val border: Color
}

/**
 * Predefined button color variants associated with semantic meanings
 */
object FindrButtonDefaults {
    
    @Immutable
    data object Primary : FindrButtonColors() {
        override val container: Color @Composable get() = FindrTheme.colorScheme.primary
        override val content: Color @Composable get() = FindrTheme.colorScheme.content.onPrimary
        override val border: Color @Composable get() = Color.Transparent
    }

    @Immutable
    data object Secondary : FindrButtonColors() {
        override val container: Color @Composable get() = FindrTheme.colorScheme.secondary
        override val content: Color @Composable get() = FindrTheme.colorScheme.content.onSecondary
        override val border: Color @Composable get() = Color.Transparent
    }

    @Immutable
    data object Success : FindrButtonColors() {
        override val container: Color @Composable get() = FindrTheme.colorScheme.success
        override val content: Color @Composable get() = FindrTheme.colorScheme.content.onSuccess
        override val border: Color @Composable get() = Color.Transparent
    }

    @Immutable
    data object Warning : FindrButtonColors() {
        override val container: Color @Composable get() = FindrTheme.colorScheme.warning
        override val content: Color @Composable get() = FindrTheme.colorScheme.content.onWarning
        override val border: Color @Composable get() = Color.Transparent
    }

    @Immutable
    data object Critical : FindrButtonColors() {
        override val container: Color @Composable get() = FindrTheme.colorScheme.critical
        override val content: Color @Composable get() = FindrTheme.colorScheme.content.onCritical
        override val border: Color @Composable get() = Color.Transparent
    }
}

/**
 * Button Type Hierarchy
 */
enum class ButtonType {
    PRIMARY,
    SECONDARY,
    TERTIARY,
    LINK,
    NEUTRAL;

    @Composable
    fun getContainerColor(
        baseColors: FindrButtonColors = FindrButtonDefaults.Primary
    ): Color {
        val colors = FindrTheme.colorScheme
        return when (this) {
            PRIMARY -> baseColors.container
            SECONDARY -> colors.surface.primary // Often outlined or lighter
            TERTIARY -> Color.Transparent
            LINK -> Color.Transparent
            NEUTRAL -> colors.surface.secondary
        }
    }

    @Composable
    fun getContentColor(
        baseColors: FindrButtonColors = FindrButtonDefaults.Primary
    ): Color {
        val colors = FindrTheme.colorScheme
        return when (this) {
            PRIMARY -> baseColors.content
            SECONDARY -> baseColors.container // Text matches primary color usually
            TERTIARY -> baseColors.container
            LINK -> colors.primary // Links usually primary color
            NEUTRAL -> colors.content.primary
        }
    }

    @Composable
    fun getBorderColor(
        baseColors: FindrButtonColors = FindrButtonDefaults.Primary
    ): Color {
        val colors = FindrTheme.colorScheme
        return when (this) {
            PRIMARY -> Color.Transparent
            SECONDARY -> baseColors.container // Border matches primary
            TERTIARY -> Color.Transparent
            LINK -> Color.Transparent
            NEUTRAL -> Color.Transparent
        }
    }
}
