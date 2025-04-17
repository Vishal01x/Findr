package com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media

import android.net.Uri
import com.exa.android.reflekt.loopit.util.model.MediaType
import java.io.File

fun getFileNameFromUrl(url: String): String {
        return Uri.parse(url).lastPathSegment ?: "downloaded_file"
    }

fun getMimeType(file: File): String {
    val extension = file.extension.lowercase()
    return when (extension) {
        "jpg", "jpeg", "png", "gif", "webp" -> "image/*"
        "mp4", "mkv", "webm", "3gp" -> "video/*"
        "mp3", "wav", "m4a" -> "audio/*"
        "pdf" -> "application/pdf"
        "doc", "docx" -> "application/msword"
        "ppt", "pptx" -> "application/vnd.ms-powerpoint"
        "xls", "xlsx" -> "application/vnd.ms-excel"
        "txt", "csv", "json", "xml" -> "text/plain"
        "zip" -> "application/zip"
        else -> "*/*" // fallback
    }
}

fun getMediaTypeFromUrl(url: String): MediaType {
    val extension = url.substringAfterLast('.', "").lowercase()

    return when (extension) {
        "jpg", "jpeg", "png", "webp", "bmp", "gif", "heic" -> MediaType.IMAGE
        "mp4", "mkv", "mov", "avi", "flv", "wmv", "webm" -> MediaType.VIDEO
        "mp3", "wav", "aac", "ogg", "m4a" -> MediaType.AUDIO
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv" -> MediaType.DOCUMENT
        "vcf" -> MediaType.CONTACT
        "geo", "location" -> MediaType.LOCATION // optional or special handling
        else -> MediaType.DOCUMENT // fallback to document for unknown types
    }
}
