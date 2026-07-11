package com.exa.android.reflekt.loopit.presentation.main.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors

private val DialogShape = RoundedCornerShape(24.dp)

/** Shared glass-morphism surface used by every liquid dialog. */
@Composable
private fun LiquidDialogSurface(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(DialogShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, LiquidColors.GlassBorder, DialogShape)
    ) {
        // Subtle cyan glow at the top of every dialog
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            LiquidColors.Primary.copy(alpha = 0.07f),
                            Color.Transparent
                        )
                    )
                )
        )
        content()
    }
}

@Composable
fun AppDialog(
    onDismissRequest: () -> Unit = {},
    title: String,
    content: @Composable () -> Unit,
    confirmText: String = "Confirm",
    onConfirm: () -> Unit,
    dismissText: String = "Cancel",
    onDismiss: (() -> Unit)? = null,
    showDismissButton: Boolean = true,
    isLoading: Boolean = false
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        LiquidDialogSurface {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = LiquidColors.TextPrimary
                )
                content()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showDismissButton) {
                        TextButton(onClick = onDismiss ?: onDismissRequest) {
                            Text(dismissText, color = LiquidColors.TextSecondary)
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    Button(
                        onClick = onConfirm,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = LiquidColors.Primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(confirmText, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmDialog(
    onDismissRequest: () -> Unit = {},
    title: String,
    message: String,
    confirmText: String = "Confirm",
    onConfirm: () -> Unit,
    dismissText: String = "Cancel",
    onDismiss: (() -> Unit)? = null,
    showDismissButton: Boolean = true,
    isLoading: Boolean = false
) {
    AppDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        content = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = LiquidColors.TextSecondary
            )
        },
        confirmText = confirmText,
        onConfirm = onConfirm,
        dismissText = dismissText,
        onDismiss = onDismiss,
        showDismissButton = showDismissButton,
        isLoading = isLoading
    )
}

@Composable
fun DestructiveDialog(
    onDismissRequest: () -> Unit = {},
    title: String,
    message: String? = null,
    confirmText: String = "Delete",
    onConfirm: () -> Unit,
    dismissText: String = "Cancel",
    onDismiss: (() -> Unit)? = null,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    extraContent: @Composable (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        LiquidDialogSurface {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    icon?.let {
                        Icon(it, contentDescription = null, tint = LiquidColors.Red)
                        Spacer(Modifier.width(10.dp))
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = LiquidColors.TextPrimary
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    message?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LiquidColors.TextSecondary
                        )
                    }
                    extraContent?.invoke()
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss ?: onDismissRequest) {
                        Text(dismissText, color = LiquidColors.TextSecondary)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = LiquidColors.Red),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(confirmText, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiscardDialog(
    onDismissRequest: () -> Unit = {},
    title: String = "Discard Changes?",
    message: String = "Are you sure you want to discard your changes?",
    discardText: String = "Discard",
    keepText: String = "Keep Editing",
    onDiscard: () -> Unit = {},
    onKeep: () -> Unit = {},
    onConfirm: () -> Unit = onDiscard,
    onDismiss: () -> Unit = onKeep
) {
    ConfirmDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        message = message,
        confirmText = discardText,
        onConfirm = onConfirm,
        dismissText = keepText,
        onDismiss = onDismiss
    )
}

@Composable
fun InputDialog(
    onDismissRequest: () -> Unit = {},
    title: String,
    label: String = "",
    placeholder: String = "",
    value: String = "",
    onValueChange: (String) -> Unit = {},
    onConfirm: () -> Unit = {},
    initialValue: String = value,
    onConfirmWithText: (String) -> Unit = { onConfirm() },
    onDismiss: () -> Unit = {},
    confirmText: String = "Save",
    dismissText: String = "Cancel",
    isLoading: Boolean = false,
    maxLines: Int = 1,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(initialValue) }

    AppDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        content = {
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    onValueChange(it)
                },
                label = if (label.isNotBlank()) ({ Text(label, color = LiquidColors.TextSecondary) }) else null,
                placeholder = if (placeholder.isNotBlank()) ({ Text(placeholder, color = LiquidColors.TextMuted) }) else null,
                leadingIcon = icon?.let { { Icon(it, null, tint = LiquidColors.Primary) } },
                modifier = Modifier.fillMaxWidth().then(modifier),
                maxLines = maxLines,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = LiquidColors.TextPrimary,
                    unfocusedTextColor = LiquidColors.TextPrimary,
                    focusedBorderColor = LiquidColors.Primary,
                    unfocusedBorderColor = LiquidColors.GlassBorder,
                    cursorColor = LiquidColors.Primary
                ),
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmText = confirmText,
        onConfirm = { onConfirmWithText(text) },
        dismissText = dismissText,
        onDismiss = onDismiss,
        isLoading = isLoading
    )
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun DialogsPreview() {
    MaterialTheme {
        ConfirmDialog(
            title = "Confirm Delete",
            message = "Are you sure you want to delete this?",
            onConfirm = {},
            onDismissRequest = {}
        )
    }
}

// ─── Avatar Confirmation Dialog (e.g. Unfollow) ───────────────────────────────

@Composable
fun AvatarConfirmationDialog(
    onDismissRequest: () -> Unit = {},
    avatarContent: @Composable () -> Unit,
    title: String,
    message: String,
    confirmText: String,
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    dismissText: String = "Cancel",
    onDismiss: (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(DialogShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, LiquidColors.GlassBorder, DialogShape)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                ) {
                    avatarContent()
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = LiquidColors.TextPrimary
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = LiquidColors.TextMuted
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss ?: onDismissRequest,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = LiquidColors.TextPrimary
                    )
                ) {
                    Text(dismissText, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDestructive) LiquidColors.Red else LiquidColors.Primary
                    )
                ) {
                    Text(confirmText, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── Success Dialog ───────────────────────────────────────────────────────────

@Composable
fun SuccessDialog(
    onDismissRequest: () -> Unit = {},
    title: String,
    message: String,
    actionText: String = "Done",
    onAction: () -> Unit = onDismissRequest
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(DialogShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, LiquidColors.GlassBorder, DialogShape)
        ) {
            // Close button
            IconButton(
                onClick = onDismissRequest,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = LiquidColors.TextMuted
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(LiquidColors.Green.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = LiquidColors.Green,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = LiquidColors.TextPrimary
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LiquidColors.TextMuted
                    )
                }
                Button(
                    onClick = onAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LiquidColors.Primary)
                ) {
                    Text(actionText, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── Report Content Dialog ────────────────────────────────────────────────────

@Composable
fun ReportContentDialog(
    onDismissRequest: () -> Unit = {},
    title: String = "Report Post",
    placeholder: String = "Reason for reporting...",
    onSubmit: (reason: String) -> Unit,
    isLoading: Boolean = false
) {
    var text by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(DialogShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, LiquidColors.GlassBorder, DialogShape)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.Flag,
                        contentDescription = null,
                        tint = LiquidColors.Red,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LiquidColors.TextPrimary
                    )
                }
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = LiquidColors.TextMuted)
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Please provide details on why you are reporting this content.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LiquidColors.TextMuted
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 120.dp),
                    placeholder = {
                        Text(
                            placeholder,
                            color = LiquidColors.TextMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    minLines = 4,
                    maxLines = 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LiquidColors.TextPrimary,
                        unfocusedTextColor = LiquidColors.TextPrimary,
                        focusedBorderColor = LiquidColors.Primary,
                        unfocusedBorderColor = LiquidColors.GlassBorder,
                        cursorColor = LiquidColors.Primary,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Footer
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LiquidColors.TextPrimary)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onSubmit(text.trim()) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading && text.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = LiquidColors.Primary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text("Submit", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
