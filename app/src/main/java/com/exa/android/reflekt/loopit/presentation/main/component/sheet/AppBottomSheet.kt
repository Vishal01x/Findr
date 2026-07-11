package com.exa.android.reflekt.loopit.presentation.main.component.sheet

import androidx.compose.foundation.background
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors

data class BottomSheetOption(
    val icon: ImageVector,
    val label: String,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

data class MediaPickerOption(
    val icon: ImageVector,
    val label: String,
    val tint: Color,
    val bg: Color,
    val onClick: () -> Unit
)

private val HandleShape = RoundedCornerShape(2.dp)
private val IconShape = RoundedCornerShape(12.dp)

@Composable
private fun DragHandle() {
    Box(
        modifier = Modifier
            .padding(top = 8.dp, bottom = 4.dp)
            .width(48.dp)
            .height(4.dp)
            .clip(HandleShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppOptionsSheet(
    title: String = "Options",
    options: List<BottomSheetOption>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = LiquidColors.TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            options.forEach { option ->
                val iconBg = if (option.isDestructive)
                    LiquidColors.Red.copy(alpha = 0.08f)
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                val iconTint = if (option.isDestructive) LiquidColors.Red else LiquidColors.TextPrimary.copy(alpha = 0.7f)
                val labelColor = if (option.isDestructive) LiquidColors.Red else LiquidColors.TextPrimary

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickableWithNoIndication {
                            option.onClick()
                            onDismiss()
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(IconShape)
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = option.label,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = labelColor
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppMediaPickerSheet(
    title: String = "Add to your post",
    options: List<MediaPickerOption>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = LiquidColors.TextPrimary,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            val rows = options.chunked(4)
            rows.forEachIndexed { rowIdx, rowItems ->
                if (rowIdx > 0) Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowItems.forEach { option ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickableWithNoIndication {
                                    option.onClick()
                                    onDismiss()
                                }
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(option.bg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = option.icon,
                                    contentDescription = option.label,
                                    tint = option.tint,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = LiquidColors.TextPrimary.copy(alpha = 0.7f)
                            )
                        }
                    }
                    repeat(4 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShareSheet(
    onCopyLink: () -> Unit,
    onSystemShare: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Share",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = LiquidColors.TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            ShareRow(
                icon = Icons.Rounded.ContentCopy,
                label = "Copy Link",
                iconTint = LiquidColors.TextPrimary.copy(alpha = 0.7f),
                iconBg = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                onClick = { onCopyLink(); onDismiss() }
            )

            ShareRow(
                icon = Icons.Rounded.Share,
                label = "Share via...",
                iconTint = LiquidColors.Primary,
                iconBg = LiquidColors.Primary.copy(alpha = 0.1f),
                onClick = { onSystemShare(); onDismiss() }
            )
        }
    }
}

@Composable
private fun ShareRow(
    icon: ImageVector,
    label: String,
    iconTint: Color,
    iconBg: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableWithNoIndication(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(IconShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = LiquidColors.TextPrimary
        )
    }
}
