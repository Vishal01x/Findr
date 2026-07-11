package com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.media.docs

import androidx.compose.foundation.background
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.media.video.VideoDownloadViewModel
import com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.media.video.DownloadState
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.UploadStatus
import com.exa.android.reflekt.loopit.core.utils.showToast
import com.exa.android.reflekt.loopit.theme.SuccessColor

@Composable
fun DocumentMessageItem(
    fileUrl: String,
    fileName: String,
    isSendByCurUser: Boolean,
    uploadStatus: UploadStatus,
    onRetry: () -> Unit,
    viewModel: VideoDownloadViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val downloadState by viewModel.getDownloadState(fileName).collectAsState()

    LaunchedEffect(fileName, uploadStatus) {
        if (isSendByCurUser && uploadStatus == UploadStatus.SUCCESS && downloadState !is DownloadState.Completed) {
           // viewModel.downloadFile(context, fileUrl, fileName)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.cleanUpTempFiles(context)
        }
    }

    val iconBoxColor = if (isSendByCurUser) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    val iconTint = if (isSendByCurUser) Color.White else MaterialTheme.colorScheme.primary
    val titleColor = if (isSendByCurUser) Color.White else MaterialTheme.colorScheme.onSurface
    val subtitleColor = if (isSendByCurUser) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickableWithNoIndication(enabled = uploadStatus == UploadStatus.SUCCESS) {
                when (downloadState) {
                    is DownloadState.Completed -> viewModel.openFile(context, fileName)
                    is DownloadState.Failed, is DownloadState.NotStarted -> viewModel.downloadFile(context, fileUrl, fileName)
                    else -> Unit
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // File icon box
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBoxColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        // File name + status
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fileName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = titleColor
            )

            val statusText = when (uploadStatus) {
                UploadStatus.UPLOADING -> "Uploading…"
                UploadStatus.FAILED -> "Upload failed"
                UploadStatus.NOTSUPPORTED -> "Not supported"
                UploadStatus.SUCCESS -> when (downloadState) {
                    is DownloadState.NotStarted -> "Tap to download"
                    is DownloadState.Downloading -> "${((downloadState as DownloadState.Downloading).progress * 100).toInt()}%"
                    is DownloadState.Completed -> "Downloaded"
                    is DownloadState.Failed -> "Download failed"
                    else -> ""
                }
                else -> ""
            }
            Text(
                text = statusText,
                fontSize = 12.sp,
                color = subtitleColor
            )

            if (uploadStatus == UploadStatus.UPLOADING ||
                (uploadStatus == UploadStatus.SUCCESS && downloadState is DownloadState.Downloading)) {
                val progress = if (downloadState is DownloadState.Downloading)
                    (downloadState as DownloadState.Downloading).progress else null
                if (progress != null) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        color = iconTint
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        color = iconTint
                    )
                }
            }
        }

        // Action icon (retry / download indicator)
        when {
            uploadStatus == UploadStatus.FAILED -> {
                IconButton(onClick = onRetry, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = subtitleColor)
                }
            }
            uploadStatus == UploadStatus.SUCCESS && downloadState is DownloadState.NotStarted -> {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download",
                    tint = subtitleColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            uploadStatus == UploadStatus.SUCCESS && downloadState is DownloadState.Downloading -> {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp),
                    color = iconTint
                )
            }
        }
    }
}
