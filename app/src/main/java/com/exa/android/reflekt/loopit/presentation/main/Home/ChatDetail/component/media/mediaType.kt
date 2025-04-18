package com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media

import android.content.Context
import android.net.Uri
import com.exa.android.reflekt.loopit.util.model.MediaType
import java.io.File

fun getFileNameFromUrl(url: String): String {
    if (url.isNullOrEmpty()) return "Uploading..."
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

fun getMediaTypeFromUri(context: Context, uri: Uri?): MediaType {

    if(uri == null)return MediaType.DOCUMENT

    val mimeType = context.contentResolver.getType(uri)
    val uriStr = uri.toString()

    return when {
        mimeType?.startsWith("image/") == true -> MediaType.IMAGE
        mimeType?.startsWith("video/") == true -> MediaType.VIDEO
        mimeType?.startsWith("audio/") == true -> MediaType.AUDIO
        mimeType == "application/pdf" -> MediaType.DOCUMENT
        mimeType?.contains("msword") == true ||
                mimeType?.contains("excel") == true ||
                mimeType?.contains("powerpoint") == true -> MediaType.DOCUMENT

        mimeType?.contains("vcard") == true || uri.authority?.contains("contacts") == true -> MediaType.CONTACT
        uriStr.startsWith("geo:") || uriStr.contains("maps.google.com") -> MediaType.LOCATION
        else -> MediaType.DOCUMENT
    }
}

fun isFileTooLarge(context: Context, uri: Uri, maxSizeMB: Int = 10): Boolean {
    val fileSizeInBytes =
        context.contentResolver.openFileDescriptor(uri, "r")?.statSize ?: return false
    val fileSizeInMB = fileSizeInBytes / (1024 * 1024)
    return fileSizeInMB > maxSizeMB
}

