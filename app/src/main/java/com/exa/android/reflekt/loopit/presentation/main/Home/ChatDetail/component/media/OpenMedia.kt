package com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.exa.android.reflekt.loopit.util.Constants.APP_NAME
import java.io.File

fun checkAndOpenOrDownloadMedia(
    context: Context,
    mediaUrl: String,
    mimeType: String,
    onDownloadRequired: (fileName: String) -> Unit
) {
    val fileName = getFileNameFromUrl(mediaUrl)
    val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val myAppFolder = File(downloadsFolder, APP_NAME)
    val file = File(myAppFolder, fileName)

    if (file.exists()) {
        // File already downloaded, open it
        context.openFileWithIntent(file, mimeType)
    } else {
        // File not found, trigger download
        onDownloadRequired(fileName)
    }
}


fun Context.openFileWithIntent(file: File, mimeType: String) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider", // Make sure this matches your authority in Manifest
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, "No app found to open this file.", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(this, "Error opening file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}
