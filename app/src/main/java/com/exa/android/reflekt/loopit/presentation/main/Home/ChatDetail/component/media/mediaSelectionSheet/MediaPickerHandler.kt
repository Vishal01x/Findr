package com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.mediaSelectionSheet

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.ChatViewModel
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.MediaSharingViewModel
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.getMediaTypeFromUri
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.getMediaTypeFromUrl
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.launchCameraWithPermission
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.openCamera
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.launchMediaUpload
import com.exa.android.reflekt.loopit.util.model.MediaType
import kotlinx.coroutines.CoroutineScope
import java.io.File

@Composable
fun MediaPickerHandler(
    otherUserId: String,
    fcmToken: String?,
    chatViewModel: ChatViewModel = hiltViewModel(),
    mediaSharingViewModel: MediaSharingViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val activity = LocalActivity.current
    val context = LocalContext.current
    val showBottomSheet = remember {
        derivedStateOf {
            mediaSharingViewModel.showMediaPickerSheet
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            // Default to image, determine type later if needed
            launchMediaUpload(
                context = context,
                uri = uri,
                mediaType = getMediaTypeFromUri(context,uri),
                otherUserId = otherUserId,
                fcmToken = fcmToken,
                chatViewModel = chatViewModel,
                coroutineScope = coroutineScope,
                messageId = null,
                mediaSharingViewModel = mediaSharingViewModel
            )
        }
    }

    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        cameraImageUri.value?.let { uri ->
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val byteArray = inputStream?.readBytes()

                if (byteArray != null && byteArray.isNotEmpty()) {
                    launchMediaUpload(
                        context = context,
                        uri = uri,
                        mediaType = MediaType.IMAGE,
                        otherUserId = otherUserId,
                        fcmToken = fcmToken,
                        chatViewModel = chatViewModel,
                        coroutineScope = coroutineScope,
                        messageId = null,
                        mediaSharingViewModel = mediaSharingViewModel
                    )
                } else {
                    Log.e("Upload", "Captured image is empty")
                }
            } catch (e: Exception) {
                Log.e("Upload", "Error reading image: ${e.message}")
            }
        }
    }


    if (showBottomSheet.value) {
        MediaPickerBottomSheet(
            onImageClick = {
                mediaSharingViewModel.showMediaPickerSheet = false
                launcher.launch("image/*")
            },
            onVideoClick = {
                mediaSharingViewModel.showMediaPickerSheet = false
                launcher.launch("video/*")
            },
            onDocumentClick = {
                mediaSharingViewModel.showMediaPickerSheet = false
                launcher.launch("*/*")
            },
            onCameraClick = {
                mediaSharingViewModel.showMediaPickerSheet = false
                // TODO: Add camera capture logic here with permission check
                if (activity != null) {
                    launchCameraWithPermission(
                        activity = activity,
                        onPermissionDenied = { },
                        onImageUriReady = { uri -> cameraImageUri.value = uri },
                        launchCamera = { uri -> takePictureLauncher.launch(uri) }
                    )
                }

            },
            onDismiss = { mediaSharingViewModel.showMediaPickerSheet = false }
        )
    }
}
