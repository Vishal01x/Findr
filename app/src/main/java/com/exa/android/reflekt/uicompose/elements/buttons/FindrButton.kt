package com.exa.android.reflekt.uicompose.elements.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.uicompose.enums.ButtonSize
import com.exa.android.reflekt.uicompose.enums.ButtonType
import com.exa.android.reflekt.uicompose.enums.FindrButtonColors
import com.exa.android.reflekt.uicompose.enums.FindrButtonDefaults
import com.exa.android.reflekt.uicompose.theme.FindrTheme
import com.exa.android.reflekt.uicompose.theme.LocalFindrDimens
import com.exa.android.reflekt.uicompose.theme.LocalFindrShapes
import com.exa.android.reflekt.uicompose.theme.LocalFindrTypography

data class FindrButtonParams(
    val text: String,
    val onClick: () -> Unit,
    val modifier: Modifier = Modifier,
    val type: ButtonType = ButtonType.PRIMARY,
    val size: ButtonSize = ButtonSize.MEDIUM,
    val enabled: Boolean = true,
    val leadingIcon: ImageVector? = null,
    val trailingIcon: ImageVector? = null,
    val isLoading: Boolean = false,
    val colors: FindrButtonColors = FindrButtonDefaults.Primary
)

@Composable
fun FindrButton(
    params: FindrButtonParams
) {
    val dimens = LocalFindrDimens.current
    val shapes = LocalFindrShapes.current
    val typography = LocalFindrTypography.current
    
    val containerColor = params.type.getContainerColor(params.colors)
    val contentColor = params.type.getContentColor(params.colors)
    val borderColor = params.type.getBorderColor(params.colors)

    Button(
        onClick = params.onClick,
        modifier = params.modifier
            .heightIn(min = params.size.minHeight(dimens))
            .widthIn(min = params.size.minWidth(dimens)),
        enabled = params.enabled && !params.isLoading,
        shape = if (params.type == ButtonType.LINK) shapes.button.medium else shapes.button.pill, // Example logic
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(
            horizontal = params.size.horizontalPadding(dimens),
            vertical = params.size.verticalPadding(dimens)
        ),
        border = if (borderColor != Color.Transparent) BorderStroke(dimens.stroke.regular, borderColor) else null,
        elevation = if (params.type == ButtonType.PRIMARY && params.enabled) ButtonDefaults.buttonElevation(defaultElevation = dimens.elevation.small) else ButtonDefaults.buttonElevation(0.dp)
    ) {
        if (params.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(params.size.iconSize(dimens)),
                color = contentColor,
                strokeWidth = dimens.stroke.medium
            )
        } else {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                if (params.leadingIcon != null) {
                    Icon(
                        imageVector = params.leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(params.size.iconSize(dimens))
                    )
                    Spacer(modifier = Modifier.width(params.size.iconPadding(dimens)))
                }

                Text(
                    text = params.text,
                    style = params.size.textStyle(typography)
                )

                if (params.trailingIcon != null) {
                    Spacer(modifier = Modifier.width(params.size.iconPadding(dimens)))
                    Icon(
                        imageVector = params.trailingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(params.size.iconSize(dimens))
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FindrButtonPreview() {
    FindrTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FindrButton(FindrButtonParams(text = "Primary Button", onClick = {}))
            FindrButton(FindrButtonParams(text = "Secondary Button", onClick = {}, type = ButtonType.SECONDARY))
            FindrButton(FindrButtonParams(text = "Loading", onClick = {}, isLoading = true))
        }
    }
}
