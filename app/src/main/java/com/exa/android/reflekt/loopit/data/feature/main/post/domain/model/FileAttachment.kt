package com.exa.android.reflekt.loopit.data.feature.main.post.domain.model

import android.net.Uri
import com.google.firebase.firestore.Exclude

/**
 * Domain model for file attachments
 * Represents a file attached to a post
 */
data class FileAttachment(
    val id: String = "",
    val fileName: String = "",
    val fileUrl: String = "",                   // Download URL from Firebase Storage
    val fileSize: Long = 0,                     // Size in bytes
    val fileType: String = "",                  // MIME type or extension
    val uploadedAt: Long = System.currentTimeMillis(),
    
    // UI-only field for local file selection
    @Exclude val localUri: Uri? = null
) {
    /**
     * Gets human-readable file size
     */
    fun getFormattedSize(): String {
        val kb = fileSize / 1024.0
        val mb = kb / 1024.0
        
        return when {
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> "$fileSize bytes"
        }
    }
    
    /**
     * Gets file extension from filename
     */
    fun getExtension(): String {
        return fileName.substringAfterLast('.', "")
    }
    
    /**
     * Checks if file is a document type
     */
    fun isDocument(): Boolean {
        val ext = getExtension().lowercase()
        return ext in listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt")
    }
    
    companion object {
        const val MAX_FILE_SIZE = 10 * 1024 * 1024L // 10MB in bytes
        
        val SUPPORTED_TYPES = listOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain"
        )
        
        val SUPPORTED_EXTENSIONS = listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt")
    }
}
