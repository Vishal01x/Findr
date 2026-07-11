package com.exa.android.reflekt.library_ui.modifier

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

@Stable
fun Modifier.clickableWithNoIndication(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    then(
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
    )
}
