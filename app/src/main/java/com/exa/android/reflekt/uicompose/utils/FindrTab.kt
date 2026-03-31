package com.exa.android.reflekt.uicompose.utils

import androidx.annotation.Keep
import androidx.compose.ui.graphics.vector.ImageVector

@Keep
data class FindrTab<T:Any>(
    val label: String,
    val name: T
)