package com.exa.android.reflekt.loopit.presentation.main.component.textfield

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors

// ─── Shared Liquid text-field colour token ─────────────────────────────────
/**
 * Returns [OutlinedTextFieldDefaults.colors] styled with Liquid design tokens
 * in dark mode, and stock Material 3 colours in light mode.
 *
 * Drop this into any raw [OutlinedTextField] call:
 *   colors = liquidTextFieldColors()
 */
@Composable
fun liquidTextFieldColors(): TextFieldColors {
    val isDark = isSystemInDarkTheme()
    return if (isDark) {
        OutlinedTextFieldDefaults.colors(
            // Container
            focusedContainerColor   = Color(0xFF13B6EC).copy(alpha = 0.06f),
            unfocusedContainerColor = Color(0xFF0D1B21),
            disabledContainerColor  = Color(0xFF0D1B21).copy(alpha = 0.5f),

            // Border / outline
            focusedBorderColor   = LiquidColors.Primary,                        // cyan
            unfocusedBorderColor = Color(0xFFFFFFFF).copy(alpha = 0.12f),       // subtle white
            disabledBorderColor  = Color(0xFFFFFFFF).copy(alpha = 0.06f),
            errorBorderColor     = Color(0xFFEF4444),

            // Label
            focusedLabelColor   = LiquidColors.Primary,
            unfocusedLabelColor = Color(0xFFFFFFFF).copy(alpha = 0.45f),
            disabledLabelColor  = Color(0xFFFFFFFF).copy(alpha = 0.25f),
            errorLabelColor     = Color(0xFFEF4444),

            // Placeholder
            focusedPlaceholderColor   = Color(0xFFFFFFFF).copy(alpha = 0.30f),
            unfocusedPlaceholderColor = Color(0xFFFFFFFF).copy(alpha = 0.30f),

            // Text
            focusedTextColor   = Color(0xFFF8FAFC),
            unfocusedTextColor = Color(0xFFCBD5E1),
            disabledTextColor  = Color(0xFF6B7280),

            // Cursor
            cursorColor      = LiquidColors.Primary,
            errorCursorColor = Color(0xFFEF4444),

            // Leading / trailing icons
            focusedLeadingIconColor    = LiquidColors.Primary,
            unfocusedLeadingIconColor  = Color(0xFFFFFFFF).copy(alpha = 0.40f),
            focusedTrailingIconColor   = LiquidColors.Primary,
            unfocusedTrailingIconColor = Color(0xFFFFFFFF).copy(alpha = 0.40f),

            // Error
            errorLeadingIconColor  = Color(0xFFEF4444),
            errorTrailingIconColor = Color(0xFFEF4444),
        )
    } else {
        // Light mode — keep stock Material3 defaults
        OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    }
}

// ─── AppTextField ──────────────────────────────────────────────────────────
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    errorMessage: String? = null,
    supportingText: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    enabled: Boolean = true,
    readOnly: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
        trailingIcon = trailingIcon,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        isError = isError,
        supportingText = errorMessage?.let { { Text(it) } } ?: supportingText,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        enabled = enabled,
        readOnly = readOnly,
        shape = MaterialTheme.shapes.medium,
        colors = liquidTextFieldColors()
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AppTextFieldPreview() {
    MaterialTheme {
        AppTextField(
            value = "Sample text",
            onValueChange = {},
            label = "Sample Label",
            leadingIcon = Icons.Default.Person
        )
    }
}
