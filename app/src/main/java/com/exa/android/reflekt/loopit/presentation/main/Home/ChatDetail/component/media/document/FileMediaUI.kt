package com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.media.docs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.video.VideoDownloadViewModel
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.video.DownloadState
import com.exa.android.reflekt.loopit.util.model.UploadStatus
import com.exa.android.reflekt.loopit.util.showToast

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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(enabled = uploadStatus == UploadStatus.SUCCESS) {
                when (downloadState) {
                    is DownloadState.Completed -> viewModel.openFile(context, fileName)
                    is DownloadState.Failed, is DownloadState.NotStarted -> viewModel.downloadFile(context, fileUrl, fileName)
                    else -> Unit
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .background(Color(0xFFF1F1F1))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "📄", fontSize = 30.sp)

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fileName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )

                when (uploadStatus) {
                    UploadStatus.FAILED -> Text(
                        text = "Upload Failed. Tap to Retry",
                        color = Color.Red,
                        fontSize = 12.sp
                    )

                    UploadStatus.UPLOADING -> {
                        Text(
                            text = "Uploading...",
                            color = Color.Blue,
                            fontSize = 12.sp
                        )
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        )
                    }

                    UploadStatus.NOTSUPPORTED -> showToast(
                        context,
                        "This file type is not supported"
                    )

                    UploadStatus.SUCCESS -> {
                        when (val state = downloadState) {
                            is DownloadState.NotStarted -> Text(
                                text = "Tap to download",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )

                            is DownloadState.Downloading -> Text(
                                //text = "${(downloadState as DownloadState.Downloading).progress.times(100).toInt()}% downloaded",
                                text = "${(state.progress * 100).toInt()}% downloaded",
                                color = Color.Blue,
                                fontSize = 12.sp
                            )

                            is DownloadState.Completed -> Text(
                                text = "Downloaded",
                                color = Color.Green,
                                fontSize = 12.sp
                            )

                            is DownloadState.Failed -> Text(
                                text = "Download failed. Tap to Retry.",
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                        if (downloadState is DownloadState.Downloading) {
                            LinearProgressIndicator(
                                progress = {
                                    (downloadState as DownloadState.Downloading).progress // Directly access progress
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                            )
                        }

                    }

                    else -> {}
                }
            }

            Spacer(modifier = Modifier.width(10.dp))
            Card(
                elevation = CardDefaults.cardElevation(4.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                when {
                    uploadStatus == UploadStatus.FAILED -> {
                        IconButton(onClick = { onRetry() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry Upload",
                                tint = Color.Red
                            )
                        }
                    }

                    uploadStatus == UploadStatus.SUCCESS -> {
                        when (downloadState) {
                            is DownloadState.NotStarted -> {
                                IconButton(onClick = { viewModel.downloadFile(context,fileUrl,fileName) }) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Download",
                                        tint = Color.DarkGray
                                    )
                                }
                            }

                            is DownloadState.Failed -> {
                                IconButton(onClick = {
                                    viewModel.downloadFile(
                                        context,
                                        fileUrl,
                                        fileName
                                    )
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Download or Retry",
                                        tint = Color.Red
                                    )
                                }
                            }

                            is DownloadState.Completed -> {
                                IconButton(onClick = { viewModel.openFile(context, fileName) }) {
                                    Text("📂", fontSize = 18.sp)
                                }
                            }

                            is DownloadState.Downloading -> {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(24.dp),
                                    color = Color.Blue
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
