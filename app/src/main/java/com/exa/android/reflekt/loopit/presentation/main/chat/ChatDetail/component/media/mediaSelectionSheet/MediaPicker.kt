package com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.media.mediaSelectionSheet

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.exa.android.reflekt.loopit.presentation.main.component.sheet.AppMediaPickerSheet
import com.exa.android.reflekt.loopit.presentation.main.component.sheet.MediaPickerOption

@Composable
fun MediaPickerBottomSheet(
    showAll: Boolean = true,
    onImageClick: () -> Unit,
    onVideoClick: () -> Unit,
    onDocumentClick: () -> Unit,
    onCameraClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val options = buildList {
        add(
            MediaPickerOption(
                icon = Icons.Default.PhotoCamera,
                label = "Camera",
                tint = Color(0xFFEC5B13),
                bg = Color(0xFFEC5B13).copy(alpha = 0.1f),
                onClick = onCameraClick
            )
        )
        add(
            MediaPickerOption(
                icon = Icons.Default.Image,
                label = "Gallery",
                tint = Color(0xFF3B82F6),
                bg = Color(0xFF3B82F6).copy(alpha = 0.1f),
                onClick = onImageClick
            )
        )
        if (showAll) {
            add(
                MediaPickerOption(
                    icon = Icons.Default.Videocam,
                    label = "Video",
                    tint = Color(0xFF8B5CF6),
                    bg = Color(0xFF8B5CF6).copy(alpha = 0.1f),
                    onClick = onVideoClick
                )
            )
            add(
                MediaPickerOption(
                    icon = Icons.AutoMirrored.Filled.InsertDriveFile,
                    label = "File",
                    tint = Color(0xFF10B981),
                    bg = Color(0xFF10B981).copy(alpha = 0.1f),
                    onClick = onDocumentClick
                )
            )
        }
    }

    AppMediaPickerSheet(
        title = "Add to your post",
        options = options,
        onDismiss = onDismiss
    )
}
