package com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.image

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.util.Log

fun downloadMedia(context: Context, mediaUrl: String, fileName: String) {
    Log.d("Storage Cloudinary", "Starting download from $mediaUrl")

    val request = DownloadManager.Request(Uri.parse(mediaUrl))
        .setTitle(fileName)
        .setDescription("Downloading file...")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalFilesDir(
            context, "Let's Talk", fileName
        )
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)

    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    dm.enqueue(request)

    Log.d("Storage Cloudinary", "Download enqueued for $fileName")
}