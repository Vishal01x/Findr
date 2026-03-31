package com.exa.android.reflekt.uicompose.enums

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.uicompose.theme.FindrDimens
import com.exa.android.reflekt.uicompose.theme.FindrTypography

@Immutable
enum class ButtonSize {
    SMALL {
        override fun minHeight(dimens: FindrDimens): Dp = 36.dp
        override fun minWidth(dimens: FindrDimens): Dp = 64.dp
        override fun horizontalPadding(dimens: FindrDimens): Dp = dimens.margin.small
        override fun verticalPadding(dimens: FindrDimens): Dp = dimens.margin.xs
        override fun textStyle(typography: FindrTypography): TextStyle = typography.labelMedium
        override fun iconSize(dimens: FindrDimens): Dp = dimens.icon.small
        override fun iconPadding(dimens: FindrDimens): Dp = dimens.margin.xs
    },
    MEDIUM {
        override fun minHeight(dimens: FindrDimens): Dp = 44.dp
        override fun minWidth(dimens: FindrDimens): Dp = 80.dp
        override fun horizontalPadding(dimens: FindrDimens): Dp = dimens.margin.medium
        override fun verticalPadding(dimens: FindrDimens): Dp = dimens.margin.small
        override fun textStyle(typography: FindrTypography): TextStyle = typography.labelLarge
        override fun iconSize(dimens: FindrDimens): Dp = dimens.icon.medium
        override fun iconPadding(dimens: FindrDimens): Dp = dimens.margin.small
    },
    LARGE {
        override fun minHeight(dimens: FindrDimens): Dp = 56.dp
        override fun minWidth(dimens: FindrDimens): Dp = 96.dp
        override fun horizontalPadding(dimens: FindrDimens): Dp = dimens.margin.large
        override fun verticalPadding(dimens: FindrDimens): Dp = dimens.margin.medium
        override fun textStyle(typography: FindrTypography): TextStyle = typography.titleSmall
        override fun iconSize(dimens: FindrDimens): Dp = dimens.icon.large
        override fun iconPadding(dimens: FindrDimens): Dp = dimens.margin.small
    };

    abstract fun minHeight(dimens: FindrDimens): Dp
    abstract fun minWidth(dimens: FindrDimens): Dp
    abstract fun horizontalPadding(dimens: FindrDimens): Dp
    abstract fun verticalPadding(dimens: FindrDimens): Dp
    abstract fun textStyle(typography: FindrTypography): TextStyle
    abstract fun iconSize(dimens: FindrDimens): Dp
    abstract fun iconPadding(dimens: FindrDimens): Dp
}
