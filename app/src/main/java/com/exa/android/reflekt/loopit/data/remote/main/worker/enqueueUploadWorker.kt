package com.exa.android.reflekt.loopit.data.remote.main.worker

import android.content.Context
import android.net.Uri
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.ChatViewModel
import com.exa.android.reflekt.loopit.util.model.Media
import com.exa.android.reflekt.loopit.util.model.MediaType
import com.exa.android.reflekt.loopit.util.model.UploadStatus
import okhttp3.MediaType.Companion.toMediaType

 fun enqueueUploadWorker(
    context: Context,
    uri: Uri,
    mediaType: MediaType,
    chatViewModel: ChatViewModel,
    otherUserId: String,
    messageId: String
) {
    val inputData = Data.Builder()
        .putString("uri", uri.toString())
        //.putString("userId", userId) // Ensuring userId is passed here
        .putString("otherUserId", otherUserId)
        .putString("mediaType", mediaType.name)
        .putString("messageId", messageId)
        .build()

    val request = OneTimeWorkRequestBuilder<MediaUploadWorker>()
        .setInputData(inputData)
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS, java.util.concurrent.TimeUnit.MILLISECONDS)
        .build()

    WorkManager.getInstance(context).enqueue(request)

    WorkManager.getInstance(context).getWorkInfoByIdLiveData(request.id)
        .observeForever { info ->
            // Check for success or failure of the upload
            if (info?.state == WorkInfo.State.SUCCEEDED) {
                val mediaUrl = info.outputData.getString("mediaUrl") ?: return@observeForever
                val uriString = info.outputData.getString("mediaType") ?: "IMAGE"
                val mediaTypeFromWorker = getMediaType(uriString)

                val uploadedMedia = Media(
                    mediaType = mediaTypeFromWorker,
                    mediaUrl = mediaUrl,
                    uri = uri.toString(),
                    uploadStatus = UploadStatus.SUCCESS
                )

                // Update the message in ViewModel
                chatViewModel.updateMediaMessage(messageId, otherUserId, uploadedMedia)
            } else if (info?.state == WorkInfo.State.FAILED) {
                val failedMedia = Media(mediaType, "", uri.toString(), UploadStatus.FAILED)
                chatViewModel.updateMediaMessage(messageId, otherUserId, failedMedia)
            }
        }
}



fun getMediaType(mediaString : String): MediaType =
    when (mediaString.uppercase()) {
    "IMAGE" -> MediaType.IMAGE
    "VIDEO" -> MediaType.VIDEO
    "AUDIO" -> MediaType.AUDIO
    else -> MediaType.IMAGE
}
