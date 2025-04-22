package com.exa.android.reflekt.loopit.data.remote.main.ViewModel

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.remote.main.Repository.MediaSharingRepository
import com.exa.android.reflekt.loopit.data.remote.main.worker.enqueueUploadWorker
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.getMediaTypeFromUrl
import com.exa.android.reflekt.loopit.util.isNetworkAvailable
import com.exa.android.reflekt.loopit.util.model.Media
import com.exa.android.reflekt.loopit.util.model.MediaType
import com.exa.android.reflekt.loopit.util.model.UploadStatus

import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class MediaSharingViewModel @Inject constructor(
    private val mediaSharingRepository: MediaSharingRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    var downloadProgress by mutableStateOf(0f)
        private set

    var isDownloading by mutableStateOf(false)
        private set

    var downloadFailed by mutableStateOf(false)
        private set

    var showMediaPickerSheet by mutableStateOf(false)

    init {
        cleanMediaTempFiles(context = context)
    }

    suspend fun uploadAndSendMediaMessage(
        context: Context,
        uri: Uri?,
        otherUserId: String,
        fcmToken: String?,
        mediaType: MediaType = MediaType.IMAGE,
        chatViewModel: ChatViewModel,
        messageId: String? = null,
        onError: ((Exception) -> Unit)? = null,
        onProgress: ((UploadStatus) -> Unit)? = null
    ) {
        withContext(Dispatchers.IO) {
            var messageId: String? = messageId
            try {
                val isNetworkAvailable = isNetworkAvailable(context)
                val mediaPreview = createMediaPreview(mediaType, uri, isNetworkAvailable)

                onProgress?.invoke(UploadStatus.UPLOADING)
                if (messageId == null)
                    messageId =
                        sendInitialMediaMessage(chatViewModel, otherUserId, null, mediaPreview)
                else chatViewModel.createChatAndSendMessage(
                    otherUserId,
                    "",
                    null,
                    mediaPreview,
                    messageId
                )

                if (messageId == null) throw Exception("Message ID generation failed")
                if (!isNetworkAvailable) throw UnknownHostException("No Internet Available")

                if (messageId != null && uri != null) {
                    retryUploadFileAndSend(
                        context = context,
                        uri = uri,
                        mediaType = mediaType,
                        otherUserId = otherUserId,
                        fcmToken = fcmToken,
                        messageId = messageId,
                        chatViewModel = chatViewModel,
                        onProgress = onProgress
                    )
//                    if (messageId != null && uri != null) {
//                        enqueueUploadWorker(
//                            context, uri, mediaType, chatViewModel, otherUserId, messageId
//                        )
//                    }

                }

            } catch (e: UnknownHostException) {
                Log.e("UploadFlow", "No internet connection", e)
                handleUploadFailure(chatViewModel, messageId, otherUserId, mediaType,uri, e, onError, onProgress)

            } catch (e: SocketTimeoutException) {
                Log.e("UploadFlow", "Upload timed out", e)
                handleUploadFailure(chatViewModel, messageId, otherUserId, mediaType, uri,e, onError, onProgress)

            } catch (e: IOException) {
                Log.e("UploadFlow", "IO error during upload", e)
                handleUploadFailure(chatViewModel, messageId, otherUserId, mediaType, uri, e, onError, onProgress)

            } catch (e: Exception) {
                Log.e("UploadFlow", "Unexpected error", e)
                handleUploadFailure(chatViewModel, messageId, otherUserId, mediaType, uri,e, onError, onProgress)
            }
        }
    }

    private fun createMediaPreview(
        mediaType: MediaType,
        uri: Uri?,
        isNetworkAvailable: Boolean
    ): Media {
        return Media(
            mediaType,
            "",
            uri.toString(),
            if (isNetworkAvailable) UploadStatus.UPLOADING else UploadStatus.FAILED
        )
    }

    private suspend fun sendInitialMediaMessage(
        chatViewModel: ChatViewModel,
        otherUserId: String,
        fcmToken: String?,
        mediaPreview: Media
    ): String? {
        chatViewModel.createChatAndSendMessage(
            otherUserId,
            "",
            fcmToken,
            mediaPreview
        )
        return chatViewModel.messageIdFlow.first()
    }

    private suspend fun tryUploadFileToCloudinary(file: File, mediaType: MediaType): Media {
        return uploadFileToCloudinary(file) ?: throw IOException("Upload File Failed").also {
            Log.e("Upload", "uploadFileToCloudinary returned null")
        }
    }

    suspend fun retryUploadFileAndSend(
        context: Context,
        uri: Uri,
        mediaType: MediaType,
        otherUserId: String,
        fcmToken: String?,
        messageId: String,
        chatViewModel: ChatViewModel,
        onProgress: ((UploadStatus) -> Unit)? = null
    ) {
        val tempFile = createTempFileFromUri(context, uri)
        val uploadedMedia = tryUploadFileToCloudinary(tempFile, mediaType)
        uploadedMedia.uploadStatus = UploadStatus.SUCCESS
        onProgress?.invoke(UploadStatus.SUCCESS)

        chatViewModel.createChatAndSendMessage(
            otherUserId,
            "",
            fcmToken,
            uploadedMedia,
            messageId
        )
    }


    private suspend fun handleUploadFailure(
        chatViewModel: ChatViewModel,
        messageId: String?,
        otherUserId: String,
        mediaType: MediaType,
        uri: Uri?,
        exception: Exception,
        onError: ((Exception) -> Unit)?,
        onProgress: ((UploadStatus) -> Unit)?
    ) {
        val failedMedia = Media(mediaType, "", uri.toString(), UploadStatus.FAILED)

        // Update the already-created message if messageId exists
        if (!messageId.isNullOrEmpty()) {
            chatViewModel.updateMediaMessage(messageId, otherUserId, failedMedia)
        } else {
            // Create a failed message if one was never created
            chatViewModel.createChatAndSendMessage(
                otherUserId,
                "",
                null,
                failedMedia
            )
        }

        onProgress?.invoke(UploadStatus.FAILED)
        onError?.invoke(exception)
    }

    /*
        suspend fun uploadAndSendMediaMessage(
            context: Context,
            uri: Uri,
            otherUserId: String,
            fcmToken: String?,
            mediaType: MediaType = MediaType.VIDEO,
            chatViewModel: ChatViewModel,
            onError: ((Exception) -> Unit)? = null,
            onProgress: ((UploadStatus) -> Unit)? = null
        ) = withContext(Dispatchers.Main) { // Use Main to access ViewModel scope safely
            try {
                val previewMedia = Media(mediaType, "", UploadStatus.UPLOADING)

                chatViewModel.createChatAndSendMessage(
                    userId = otherUserId,
                    message = "",
                    media = previewMedia,
                    receiverToken = fcmToken
                ) { messageId ->

                    if (messageId == null) {
                        Log.e("UploadFlow", "Message ID was null. Upload skipped.")
                        onError?.invoke(Exception("Message ID generation failed"))
                        onProgress?.invoke(UploadStatus.FAILED)
                        return@createChatAndSendMessage
                    }

                    onProgress?.invoke(UploadStatus.UPLOADING)

                    // Start upload in IO thread
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val tempFile = createTempFileFromUri(context, uri)
                            val uploadedMedia = uploadFileToCloudinary(tempFile)

                            withContext(Dispatchers.Main) {
                                if (uploadedMedia != null) {
                                    uploadedMedia.uploadStatus = UploadStatus.SUCCESS
                                    chatViewModel.updateMediaMessage(messageId, otherUserId, uploadedMedia)
                                    onProgress?.invoke(UploadStatus.SUCCESS)
                                } else {
                                    val failedMedia = Media(mediaType, "", UploadStatus.FAILED)
                                    chatViewModel.updateMediaMessage(messageId, otherUserId, failedMedia)
                                    onProgress?.invoke(UploadStatus.FAILED)
                                    onError?.invoke(Exception("Cloudinary upload failed"))
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("UploadFlow", "Exception in background upload", e)
                            withContext(Dispatchers.Main) {
                                onProgress?.invoke(UploadStatus.FAILED)
                                onError?.invoke(e)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("UploadFlow", "Outer Exception", e)
                onProgress?.invoke(UploadStatus.FAILED)
                onError?.invoke(e)
            }
        }

    */


    suspend fun uploadFileToCloudinary(file: File): Media? {
        return withContext(Dispatchers.IO) {
            try {
                // val file = createTempFileFromUri(context, uri)
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
                    delay(20)
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
            Log.d("Storage Cloudinary", "Creating file from URI: $uri")

            val contentResolver = context.contentResolver
            var fileName = "default_file"

            // Step 1: Try to extract the original file name
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    fileName = cursor.getString(nameIndex)
                }
            }

            // Step 2: Use file extension if not in original file name
            val fileExtension = MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(contentResolver.getType(uri)) ?: "tmp"

            if (!fileName.contains(".")) {
                fileName += ".$fileExtension"
            }

            // Step 3: Create a new file in cache directory using original file name
            val file = File(context.cacheDir, fileName)

            // Step 4: Copy the URI content to the new file
            contentResolver.openInputStream(uri)?.use { inputStream ->
                file.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
            }

            Log.d("Storage Cloudinary", "File created with original name: ${file.absolutePath}")
            file
        }



    // Companion cleanup function
    fun cleanMediaTempFiles(context: Context) {
        val cacheDir = context.cacheDir
        val tempFiles = cacheDir.listFiles { file ->
            file.name.startsWith("temp_media_") && file.isFile
        }

        tempFiles?.forEach { file ->
            try {
                if (file.delete()) {
                    Log.d("TempCleanup", "Deleted: ${file.name}")
                } else {
                    Log.w("TempCleanup", "Failed to delete: ${file.name}")
                }
            } catch (e: SecurityException) {
                Log.e("TempCleanup", "Security error deleting ${file.name}: ${e.message}")
            } catch (e: Exception) {
                Log.e("TempCleanup", "Error deleting ${file.name}: ${e.message}")
            }
        }
    }

    fun getUploadErrorMessage(e: Exception): String {
        return when (e) {
            is UnknownHostException -> "No internet connection. Please check your network."
            is SocketTimeoutException -> "Connection timed out. Try again later."
            is IOException -> "File upload failed due to network or file error."
            is IllegalStateException -> "Failed to prepare upload. Please try again."
            else -> e.message ?: "Unknown error occurred."
        }
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