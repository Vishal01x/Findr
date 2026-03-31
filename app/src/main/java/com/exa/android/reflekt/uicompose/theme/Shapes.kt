package com.exa.android.reflekt.uicompose.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

sealed interface FindrShape {
    
    @Immutable
    data class Button(
        val small: Shape,
        val medium: Shape,
        val large: Shape,
        val rounded: Shape,
        val pill: Shape
    )
    
    @Immutable
    data class Card(
        val small: Shape,
        val medium: Shape,
        val large: Shape,
        val extraLarge: Shape
    )
    
    @Immutable
    data class Chip(
        val small: Shape,
        val medium: Shape,
        val rounded: Shape
    )
    
    @Immutable
    data class Dialog(
        val small: Shape,
        val medium: Shape,
        val large: Shape
    )
    
    @Immutable
    data class Input(
        val small: Shape,
        val medium: Shape,
        val large: Shape
    )
    
    @Immutable
    data class Image(
        val small: Shape,
        val medium: Shape,
        val large: Shape,
        val circle: Shape
    )
}

@Immutable
data class FindrShapes(
    val button: FindrShape.Button,
    val card: FindrShape.Card,
    val chip: FindrShape.Chip,
    val dialog: FindrShape.Dialog,
    val input: FindrShape.Input,
    val image: FindrShape.Image
)

val DefaultFindrShapes = FindrShapes(
    button = FindrShape.Button(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(8.dp),
        large = RoundedCornerShape(12.dp),
        rounded = RoundedCornerShape(16.dp),
        pill = RoundedCornerShape(50)
    ),
    card = FindrShape.Card(
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
        extraLarge = RoundedCornerShape(24.dp)
    ),
    chip = FindrShape.Chip(
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        rounded = RoundedCornerShape(50)
    ),
    dialog = FindrShape.Dialog(
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(20.dp)
    ),
    input = FindrShape.Input(
        small = RoundedCornerShape(6.dp),
        medium = RoundedCornerShape(8.dp),
        large = RoundedCornerShape(12.dp)
    ),
    image = FindrShape.Image(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(8.dp),
        large = RoundedCornerShape(12.dp),
        circle = RoundedCornerShape(50)
    )
)

val CompactFindrShapes = FindrShapes(
    button = FindrShape.Button(
        small = RoundedCornerShape(3.dp),
        medium = RoundedCornerShape(6.dp),
        large = RoundedCornerShape(10.dp),
        rounded = RoundedCornerShape(14.dp),
        pill = RoundedCornerShape(50)
    ),
    card = FindrShape.Card(
        small = RoundedCornerShape(6.dp),
        medium = RoundedCornerShape(10.dp),
        large = RoundedCornerShape(14.dp),
        extraLarge = RoundedCornerShape(20.dp)
    ),
    chip = FindrShape.Chip(
        small = RoundedCornerShape(6.dp),
        medium = RoundedCornerShape(10.dp),
        rounded = RoundedCornerShape(50)
    ),
    dialog = FindrShape.Dialog(
        small = RoundedCornerShape(10.dp),
        medium = RoundedCornerShape(14.dp),
        large = RoundedCornerShape(18.dp)
    ),
    input = FindrShape.Input(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(6.dp),
        large = RoundedCornerShape(10.dp)
    ),
    image = FindrShape.Image(
        small = RoundedCornerShape(3.dp),
        medium = RoundedCornerShape(6.dp),
        large = RoundedCornerShape(10.dp),
        circle = RoundedCornerShape(50)
    )
)

val ExpandedFindrShapes = FindrShapes(
    button = FindrShape.Button(
        small = RoundedCornerShape(6.dp),
        medium = RoundedCornerShape(10.dp),
        large = RoundedCornerShape(14.dp),
        rounded = RoundedCornerShape(20.dp),
        pill = RoundedCornerShape(50)
    ),
    card = FindrShape.Card(
        small = RoundedCornerShape(10.dp),
        medium = RoundedCornerShape(14.dp),
        large = RoundedCornerShape(20.dp),
        extraLarge = RoundedCornerShape(28.dp)
    ),
    chip = FindrShape.Chip(
        small = RoundedCornerShape(10.dp),
        medium = RoundedCornerShape(14.dp),
        rounded = RoundedCornerShape(50)
    ),
    dialog = FindrShape.Dialog(
        small = RoundedCornerShape(14.dp),
        medium = RoundedCornerShape(20.dp),
        large = RoundedCornerShape(24.dp)
    ),
    input = FindrShape.Input(
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(10.dp),
        large = RoundedCornerShape(14.dp)
    ),
    image = FindrShape.Image(
        small = RoundedCornerShape(6.dp),
        medium = RoundedCornerShape(10.dp),
        large = RoundedCornerShape(14.dp),
        circle = RoundedCornerShape(50)
    )
)


val LocalFindrShapes = staticCompositionLocalOf { DefaultFindrShapes }