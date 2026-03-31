package com.exa.android.reflekt.uicompose.elements.buttons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.uicompose.theme.FindrTheme
import com.exa.android.reflekt.uicompose.theme.LocalFindrDimens
import com.exa.android.reflekt.uicompose.theme.LocalFindrShapes
import com.exa.android.reflekt.uicompose.theme.LocalFindrTypography

@Composable
fun FindrFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    text: String? = null,
    expanded: Boolean = true,
    containerColor: Color = FindrTheme.colorScheme.primary,
    contentColor: Color = FindrTheme.colorScheme.content.onPrimary
) {
    val dimens = LocalFindrDimens.current
    val shapes = LocalFindrShapes.current
    val typography = LocalFindrTypography.current

    if (text != null && expanded) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            shape = shapes.button.pill,
            containerColor = containerColor,
            contentColor = contentColor,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = dimens.elevation.medium)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(dimens.icon.medium)
                )
                Spacer(modifier = Modifier.width(dimens.margin.small))
            }
            Text(
                text = text,
                style = typography.labelLarge
            )
        }
    } else {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            shape = shapes.button.rounded,
            containerColor = containerColor,
            contentColor = contentColor,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = dimens.elevation.medium)
        ) {
            val displayIcon = icon
            if (displayIcon != null) {
                Icon(
                    imageVector = displayIcon,
                    contentDescription = text,
                    modifier = Modifier.size(dimens.icon.medium)
                )
            } else if (text != null) {
                 Text(
                    text = text.take(1),
                    style = typography.titleMedium
                 )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FindrFloatingActionButtonPreview() {
    FindrTheme {
        Row {
            FindrFloatingActionButton(onClick = {}, text = "Compose", icon = androidx.compose.material.icons.Icons.Default.Add)
            Spacer(modifier = Modifier.width(16.dp))
            FindrFloatingActionButton(onClick = {}, icon = androidx.compose.material.icons.Icons.Default.Add)
        }
    }
}
