package com.exa.android.reflekt.loopit.presentation.auth.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    errorMessage: String? = null,
    supportingText: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> colorScheme.error
            isFocused -> colorScheme.primary
            else -> Color.Transparent
        },
        animationSpec = tween(250),
        label = "border_color"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isFocused) colorScheme.surfaceContainerHigh
        else colorScheme.surfaceContainerLow,
        animationSpec = tween(250),
        label = "container_color"
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (isFocused) 2.dp else 0.dp,
        animationSpec = tween(250),
        label = "shadow"
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(label, color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
        leadingIcon = {
            Icon(
                leadingIcon,
                contentDescription = label,
                tint = if (isFocused) colorScheme.primary
                else colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        isError = isError,
        singleLine = singleLine,
        enabled = enabled,
        supportingText = supportingText ?: errorMessage?.let {
            { Text(it, color = colorScheme.error) }
        },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = borderColor,
            unfocusedBorderColor = Color.Transparent,
            focusedLabelColor = colorScheme.primary,
            unfocusedLabelColor = colorScheme.onSurfaceVariant,
            cursorColor = colorScheme.primary,
            errorBorderColor = colorScheme.error,
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor
        ),
        modifier = modifier
            .fillMaxWidth()
            .shadow(shadowElevation, RoundedCornerShape(16.dp), clip = false)
            .onFocusChanged { isFocused = it.isFocused }
    )
}
