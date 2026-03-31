package com.exa.android.reflekt.uicompose.elements.buttons

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.uicompose.enums.FindrButtonDefaults
import com.exa.android.reflekt.uicompose.theme.FindrTheme
import com.exa.android.reflekt.uicompose.theme.LocalFindrDimens
import com.exa.android.reflekt.uicompose.theme.LocalFindrShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindrIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
    containerColor: Color = Color.Transparent,
    contentColor: Color = FindrTheme.colorScheme.content.primary,
    badgeContainerColor: Color = FindrTheme.colorScheme.critical,
    badgeContentColor: Color = FindrTheme.colorScheme.content.onCritical,
    enabled: Boolean = true
) {
    val dimens = LocalFindrDimens.current
    
    val scale by animateFloatAsState(if (enabled) 1f else 0.9f, label = "scale")

    IconButton(
        onClick = onClick,
        modifier = modifier.scale(scale),
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        )
    ) {
        if (badgeCount > 0) {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = badgeContainerColor,
                        contentColor = badgeContentColor
                    ) {
                        val badgeText = if (badgeCount > 99) "99+" else badgeCount.toString()
                        Text(text = badgeText)
                    }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(dimens.icon.medium)
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(dimens.icon.medium)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FindrIconButtonPreview() {
    FindrTheme {
        Row {
            FindrIconButton(
                onClick = {},
                icon = androidx.compose.material.icons.Icons.Default.Notifications,
                badgeCount = 5
            )
            FindrIconButton(
                onClick = {},
                icon = androidx.compose.material.icons.Icons.Default.Email,
                badgeCount = 150
            )
        }
    }
}
