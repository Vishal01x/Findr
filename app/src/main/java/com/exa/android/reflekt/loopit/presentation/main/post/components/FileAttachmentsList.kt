package com.exa.android.reflekt.loopit.presentation.main.post.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.theme.Spacing

/**
 * Displays a list of file attachments with click-to-open functionality
 */
@Composable
fun FileAttachmentsList(    //this codebase has redundant things of files, make sure to reuse the chat one
    fileUrls: List<String>,
    modifier: Modifier = Modifier
) {
    if (fileUrls.isEmpty()) return
    
    val context = LocalContext.current
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
//        Text(
//            text = "Attachments",
//            style = MaterialTheme.typography.titleSmall,
//            fontWeight = FontWeight.Bold,
//            color = MaterialTheme.colorScheme.onSurface
//        )
        
        fileUrls.forEach { fileUrl ->
            FileAttachmentItem(
                fileUrl = fileUrl,
                onClick = { openFile(context, fileUrl) }
            )
        }
    }
}

/**
 * Individual file attachment item
 */
@Composable
private fun FileAttachmentItem(
    fileUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fileName = extractFileName(fileUrl)
    val fileExtension = extractFileExtension(fileName)
    val fileIcon = getFileIcon(fileExtension)
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = fileIcon,
                    contentDescription = "File",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                
                Column {
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = fileExtension.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Opens a file using Android Intent
 */
private fun openFile(context: Context, fileUrl: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(fileUrl)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            
            // Set MIME type based on file extension
            val mimeType = getMimeType(extractFileExtension(fileUrl))
            setDataAndType(Uri.parse(fileUrl), mimeType)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle error - could show a toast or snackbar
        e.printStackTrace()
    }
}

/**
 * Extracts filename from URL
 */
private fun extractFileName(url: String): String {
    return url.substringAfterLast("/").substringBefore("?")
}

/**
 * Extracts file extension from filename
 */
private fun extractFileExtension(fileName: String): String {
    return fileName.substringAfterLast(".", "")
}

/**
 * Returns appropriate icon based on file extension
 */
private fun getFileIcon(extension: String): ImageVector {
    return when (extension.lowercase()) {
        "pdf" -> Icons.Default.PictureAsPdf
        "doc", "docx" -> Icons.Default.Description
        "xls", "xlsx" -> Icons.Default.TableChart
        "ppt", "pptx" -> Icons.Default.Slideshow
        "zip", "rar" -> Icons.Default.FolderZip
        "jpg", "jpeg", "png", "gif" -> Icons.Default.Image
        else -> Icons.Default.InsertDriveFile
    }
}

/**
 * Returns MIME type based on file extension
 */
private fun getMimeType(extension: String): String {
    return when (extension.lowercase()) {
        "pdf" -> "application/pdf"
        "doc" -> "application/msword"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "xls" -> "application/vnd.ms-excel"
        "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        "ppt" -> "application/vnd.ms-powerpoint"
        "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        "zip" -> "application/zip"
        "rar" -> "application/x-rar-compressed"
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        else -> "*/*"
    }
}
