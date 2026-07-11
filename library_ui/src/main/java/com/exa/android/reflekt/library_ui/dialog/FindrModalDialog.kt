package com.exa.android.reflekt.library_ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun FindrModalDialog(
    state: ModalDialogState,
    onEvent: (ModalDialogEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = { onEvent(ModalDialogEvent.OnDismissRequest) },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Optional Icon
                if (state.iconResId != null) {
                    Icon(
                        painter = painterResource(id = state.iconResId),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Title
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                // Subtitle (Optional)
                if (state.subtitle != null) {
                    Text(
                        text = state.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // Input Field (Optional)
                if (state.showInputField) {
                    OutlinedTextField(
                        value = state.inputText,
                        onValueChange = { onEvent(ModalDialogEvent.OnInputTextChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            if (state.inputHint != null) {
                                Text(text = state.inputHint)
                            }
                        },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
                    // Secondary Button (Optional)
                    if (state.secondaryButtonText != null) {
                        OutlinedButton(
                            onClick = { onEvent(ModalDialogEvent.OnSecondaryButtonClicked) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(text = state.secondaryButtonText, style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    // Primary Button
                    Button(
                        onClick = { onEvent(ModalDialogEvent.OnPrimaryButtonClicked) },
                        modifier = Modifier.weight(1f),
                        enabled = state.isPrimaryButtonEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(text = state.primaryButtonText, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFindrModalDialog_Confirm() {
    MaterialTheme {
        FindrModalDialog(
            state = ModalDialogState(
                title = "Unfollow Alex?",
                subtitle = "Their posts will no longer appear in your feed.",
                primaryButtonText = "Unfollow",
                secondaryButtonText = "Cancel"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFindrModalDialog_Feedback() {
    MaterialTheme {
        FindrModalDialog(
            state = ModalDialogState(
                title = "Registration Confirmed!",
                subtitle = "You're all set. We've sent a confirmation email to your student address.",
                primaryButtonText = "Continue"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFindrModalDialog_Input() {
    MaterialTheme {
        FindrModalDialog(
            state = ModalDialogState(
                title = "Report Post",
                subtitle = "Please provide details on why you are reporting this content.",
                primaryButtonText = "Submit",
                secondaryButtonText = "Cancel",
                showInputField = true,
                inputHint = "Additional details..."
            ),
            onEvent = {}
        )
    }
}
