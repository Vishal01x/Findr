package com.exa.android.reflekt.loopit.presentation.main.post.create

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Removed parseMarkdown import
import com.exa.android.reflekt.loopit.presentation.main.component.dialog.DiscardDialog

@Composable
fun MarkdownEditorScreen(
    initialText: String,
    screenTitle: String = "Description",
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(initialText, TextRange(initialText.length)))
    }
    var previewMode by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges = textFieldValue.text != initialText

    val handleCancel = {
        if (hasUnsavedChanges) {
            showDiscardDialog = true
        } else {
            onCancel()
        }
    }

    BackHandler { handleCancel() }

    if (showDiscardDialog) {
        DiscardDialog(
            title = "Discard description?",
            message = "You have unsaved changes. Are you sure you want to discard them?",
            onConfirm = {
                showDiscardDialog = false
                onCancel()
            },
            onDismiss = {
                showDiscardDialog = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // ── Top bar: Cancel | Title | Preview-toggle + Save ────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = handleCancel) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
                }
                Text(
                    text = screenTitle,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Preview / Edit toggle
                    IconButton(onClick = { previewMode = !previewMode }) {
                        Icon(
                            imageVector = if (previewMode) Icons.Outlined.Edit else Icons.Outlined.Visibility,
                            contentDescription = if (previewMode) "Switch to edit" else "Preview",
                            tint = if (previewMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    TextButton(onClick = { onSave(textFieldValue.text) }) {
                        Text(
                            "Save",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

            if (previewMode) {
                // ── Rendered preview ───────────────────────────────────────
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    if (textFieldValue.text.isBlank()) {
                        Text("Nothing to preview yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    } else {
                        com.exa.android.reflekt.loopit.presentation.main.post.components.MarkdownText(
                            text = textFieldValue.text,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = TextStyle(fontSize = 15.sp),
                            lineHeight = 24.sp
                        )
                    }
                }
            } else {
                // ── Text editor ────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .imePadding()
                ) {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { textFieldValue = it },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            lineHeight = 24.sp
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        maxLines = Int.MAX_VALUE,
                        decorationBox = { innerTextField ->
                            Box {
                                if (textFieldValue.text.isEmpty()) {
                                    Text(
                                        "Start writing your description…\n\n" +
                                                "Formatting tips:\n" +
                                                "  **text** → bold\n" +
                                                "  *text* → italic\n" +
                                                "  # Heading",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 14.sp,
                                        lineHeight = 22.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                // ── Formatting toolbar ─────────────────────────────────────
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FormatButton(
                        label = "B",
                        weight = FontWeight.Bold
                    ) { textFieldValue = textFieldValue.applyBold() }

                    FormatButton(
                        label = "I",
                        style = FontStyle.Italic
                    ) { textFieldValue = textFieldValue.applyItalic() }

                    Box(
                        modifier = Modifier
                            .width(1.dp).height(20.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    FormatButton(label = "H1") { textFieldValue = textFieldValue.applyHeading(1) }
                    FormatButton(label = "H2") { textFieldValue = textFieldValue.applyHeading(2) }

                    Box(
                        modifier = Modifier
                            .width(1.dp).height(20.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    FormatButton(label = "Normal") { textFieldValue = textFieldValue.clearFormatting() }

                    Spacer(Modifier.width(12.dp))

                    Text(
                        "${textFieldValue.text.length} chars",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// ── Format toolbar button ─────────────────────────────────────────────────────

@Composable
private fun FormatButton(
    label: String,
    weight: FontWeight? = null,
    style: FontStyle? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.background)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
            .clickableWithNoIndication(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = weight ?: FontWeight.Normal,
            fontStyle = style ?: FontStyle.Normal
        )
    }
}

// ── TextFieldValue formatting extensions ──────────────────────────────────────

private fun TextFieldValue.applyBold(): TextFieldValue {
    val start = selection.min
    val end   = selection.max
    return if (selection.collapsed) {
        val newText = text.substring(0, start) + "****" + text.substring(start)
        copy(text = newText, selection = TextRange(start + 2))
    } else {
        val selected = text.substring(start, end)
        val newText  = text.substring(0, start) + "**$selected**" + text.substring(end)
        copy(text = newText, selection = TextRange(start + 2, end + 2))
    }
}

private fun TextFieldValue.applyItalic(): TextFieldValue {
    val start = selection.min
    val end   = selection.max
    return if (selection.collapsed) {
        val newText = text.substring(0, start) + "**" + text.substring(start)
        copy(text = newText, selection = TextRange(start + 1))
    } else {
        val selected = text.substring(start, end)
        val newText  = text.substring(0, start) + "*$selected*" + text.substring(end)
        copy(text = newText, selection = TextRange(start + 1, end + 1))
    }
}

private fun TextFieldValue.applyHeading(level: Int): TextFieldValue {
    val prefix    = "#".repeat(level) + " "
    val lineStart = text.lastIndexOf('\n', selection.min - 1) + 1
    val newText   = text.substring(0, lineStart) + prefix + text.substring(lineStart)
    return copy(
        text = newText,
        selection = TextRange(selection.min + prefix.length, selection.max + prefix.length)
    )
}

private fun TextFieldValue.clearFormatting(): TextFieldValue {
    if (selection.collapsed) return this
    val start    = selection.min
    val end      = selection.max
    val selected = text.substring(start, end)
    val cleaned  = selected
        .replace(Regex("\\*{1,3}"), "")
        .replace(Regex("^#{1,6}\\s*", RegexOption.MULTILINE), "")
    val newText  = text.substring(0, start) + cleaned + text.substring(end)
    return copy(text = newText, selection = TextRange(start, start + cleaned.length))
}

