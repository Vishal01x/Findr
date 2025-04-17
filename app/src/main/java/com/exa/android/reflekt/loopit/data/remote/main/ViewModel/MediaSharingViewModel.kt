package com.exa.android.reflekt.loopit.data.remote.main.ViewModel

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.remote.main.Repository.MediaSharingRepository
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.getMediaTypeFromUrl
import com.exa.android.reflekt.loopit.util.model.Media
import com.exa.android.reflekt.loopit.util.model.MediaType

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MediaSharingViewModel @Inject constructor(
    private val mediaSharingRepository: MediaSharingRepository
) : ViewModel() {

    var downloadProgress by mutableStateOf(0f)
        private set

    var isDownloading by mutableStateOf(false)
        private set

    var downloadFailed by mutableStateOf(false)
        private set


    suspend fun uploadFileToCloudinary(context: Context, uri: Uri): Media? {
        return withContext(Dispatchers.IO) {
            try {
                val file = createTempFileFromUri(context, uri)
                val uploadedUrl = mediaSharingRepository.uploadFileToCloudinary(file)
                if (uploadedUrl != null) {
                    val type = getMediaTypeFromUrl(uploadedUrl)
                    Log.d("Storage Cloudinary", "${uploadedUrl} , type : ${type}")
                    Media(mediaType = type, mediaUrl = uploadedUrl)
                } else null
            } catch (e: Exception) {
                Log.e("Storage Cloudinary", "Upload failed", e)
                null
            }
        }
    }



    fun downloadMedia(context: Context, url: String, fileName: String, onSuccess: (File) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                isDownloading = true
                downloadFailed = false

                val file =
                    File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
                if (file.exists()) file.delete()

                val request = DownloadManager.Request(Uri.parse(url)).apply {
                    setDestinationUri(Uri.fromFile(file))
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                }

                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)

                // Simulate progress (for actual download tracking use BroadcastReceiver or ContentObserver)
                for (i in 1..100) {
                    downloadProgress = i / 100f
                    kotlinx.coroutines.delay(20)
                }

                isDownloading = false
                onSuccess(file)
            } catch (e: Exception) {
                downloadFailed = true
                isDownloading = false
            }
        }
    }

    suspend fun createTempFileFromUri(context: Context, uri: Uri): File =
        withContext(Dispatchers.IO) {
            Log.d("Storage Cloudinary", "Creating temp file from URI: $uri")
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileExtension = MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(context.contentResolver.getType(uri)) ?: "tmp"
            val file = File.createTempFile("upload", ".$fileExtension", context.cacheDir)
            file.outputStream().use { output ->
                inputStream?.copyTo(output)
            }
            Log.d("Storage Cloudinary", "Temp file created: ${file.absolutePath}")
            file
        }

//    fun downloadFile(context: Context, url: String, fileName: String) {
//        Log.d("Storage Cloudinary", "Starting download from $url")
//
//        val request = DownloadManager.Request(Uri.parse(url))
//            .setTitle(fileName)
//            .setDescription("Downloading file...")
//            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//            .setDestinationInExternalPublicDir(
//                Environment.DIRECTORY_DOWNLOADS,
//                "$fileName.${getFileExtensionFromUrl(url)}"
//            )
//            .setAllowedOverMetered(true)
//            .setAllowedOverRoaming(true)
//
//        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//        dm.enqueue(request)
//
//        Log.d("Storage Cloudinary", "Download enqueued for $fileName")
//    }
//
//    fun retryDownload(context: Context, url: String, fileName: String, onSuccess: (File) -> Unit) {
//        downloadMedia(context, url, fileName, onSuccess)
//    }

    //fun getFileExtensionFromUrl(url: String): String {
    //    return Uri.parse(url).lastPathSegment?.substringAfterLast('.', "pdf") ?: "pdf"
    //}


}