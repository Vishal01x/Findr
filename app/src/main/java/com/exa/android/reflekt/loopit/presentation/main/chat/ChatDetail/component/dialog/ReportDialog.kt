package com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.dialog

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.exa.android.reflekt.loopit.presentation.main.component.textfield.AppTextField


@Composable
fun ReportUserDialog(
    onDismiss: () -> Unit,
    onReportSubmit: (reason: String, proofText: String?, proofImageUri: Uri?) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var proofText by remember { mutableStateOf("") }
    var proofImageUri by remember { mutableStateOf<Uri?>(null) }
    var showError by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        proofImageUri = uri
        showError = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Report User",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column {
                Text(
                    text = "Please provide a valid reason for report",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = reason,
                    onValueChange = {
                        reason = it
                        showError = false
                    },
                    label = "Reason for reporting",
                    placeholder = "e.g., Harassment, Spam, Fake profile",
                    leadingIcon = Icons.Default.Flag,
                    isError = showError && reason.isBlank(),
                    errorMessage = if (showError && reason.isBlank()) "Reason is required" else null,
                    singleLine = false,
                    maxLines = 3,
                )

                Spacer(modifier = Modifier.height(10.dp))

                AppTextField(
                    value = proofText,
                    onValueChange = {
                        proofText = it
                        showError = false
                    },
                    label = "Describe the incident",
                    placeholder = "Proof description (optional)",
                    leadingIcon = Icons.Default.TextSnippet,
                    singleLine = false,
                    maxLines = 3,
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Attach Screenshot Button
                OutlinedButton(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add Screenshot")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (proofImageUri != null) "Change Screenshot" else "Attach Screenshot")
                }

                // Show image preview if selected
                proofImageUri?.let { uri ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "Attached Proof Screenshot",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                if (showError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Reason and at least a text proof or screenshot is required!",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (reason.isBlank() || (proofText.isBlank() && proofImageUri == null)) {
                        showError = true
                    } else {
                        onReportSubmit(reason.trim(), proofText.trim().ifEmpty { null }, proofImageUri)
                        onDismiss()
                    }
                }
            ) {
                Text(
                    "Submit Report",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancel",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
