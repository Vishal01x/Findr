package com.exa.android.reflekt.loopit.data.remote.main.worker


import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.exa.android.reflekt.loopit.data.remote.main.Repository.MediaSharingRepository
import com.exa.android.reflekt.loopit.util.model.Media
import com.exa.android.reflekt.loopit.util.model.UploadStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

@HiltWorker
class MediaUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: MediaSharingRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val uriString = inputData.getString("uri") ?: return Result.failure()
        //val userId = inputData.getString("userId") ?: return Result.failure()
        val otherUserId = inputData.getString("otherUserId") ?: return Result.failure()
        val mediaTypeStr = inputData.getString("mediaType") ?: return Result.failure()
        val messageId = inputData.getString("messageId") ?: return Result.failure()

        val uri = Uri.parse(uriString)
        val mediaType = getMediaType( mediaTypeStr)

        return try {
            val tempFile = createTempFileFromUri(uri)
            val uploadedMedia = repository.uploadFileToCloudinary(tempFile)
                ?: throw Exception("Upload failed")

            val media = Media(mediaType, uploadedMedia, uriString, UploadStatus.SUCCESS)
            Log.d("MediaUploadWorker", "Upload success: $uploadedMedia")

            val output = workDataOf(
                "mediaUrl" to uploadedMedia,
                "mediaType" to mediaType.name,
                "otherUserId" to otherUserId,
                "messageId" to messageId
            )
            Result.success(output)
        } catch (e: Exception) {
            Log.e("MediaUploadWorker", "Upload failed", e)
            Result.failure()
        }
    }

    private suspend fun createTempFileFromUri(uri: Uri): File {
        val inputStream = applicationContext.contentResolver.openInputStream(uri)
        val file = File.createTempFile("upload", null, applicationContext.cacheDir)
        inputStream?.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
        return file
    }
}
