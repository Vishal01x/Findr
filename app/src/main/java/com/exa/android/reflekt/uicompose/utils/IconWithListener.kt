package com.exa.android.reflekt.uicompose.utils

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@Immutable
data class IconWithListener(
    val icon: ImageVector,
    val modifier: Modifier = Modifier,
    val iconTint: Color = Color.Unspecified,
    val onClick: () -> Unit
)
