package com.exa.android.reflekt.uicompose.enums

import androidx.annotation.IntDef

object Position {
    const val Top = 0
    const val Bottom = 1
    const val Start = 2
    const val End = 3
    const val Center = 4

    @IntDef(Top, Bottom, Center)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Vertical

    @IntDef(Start, End, Center)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Horizontal
}
