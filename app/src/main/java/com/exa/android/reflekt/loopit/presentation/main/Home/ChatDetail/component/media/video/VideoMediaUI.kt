package com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.video


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.video.DownloadState
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.video.VideoDownloadViewModel
import com.exa.android.reflekt.loopit.util.model.UploadStatus
import com.exa.android.reflekt.loopit.util.showToast
import kotlinx.coroutines.delay

@Composable
fun VideoMessageItem(
    videoUrl: String,
    fileName: String,
    isSendByCurUser: Boolean,
    uploadStatus: UploadStatus,
    onRetry : () -> Unit,
    viewModel: VideoDownloadViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val downloadState by viewModel.getDownloadState(fileName).collectAsState()

    LaunchedEffect(fileName, uploadStatus) {
        if (isSendByCurUser && uploadStatus == UploadStatus.SUCCESS) {
           // viewModel.downloadFile(context, videoUrl, fileName)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.cleanUpTempFiles(context)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(videoUrl)
                .crossfade(true)
                .placeholder(R.drawable.placeholder)
                .build(),
            contentDescription = "Video Thumbnail",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        when (uploadStatus) {
            UploadStatus.UPLOADING -> {
                Column(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        //progress = { (downloadState as DownloadState.Downloading).progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )
                    Text(
                        text = "Uploading...",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            UploadStatus.FAILED -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable { onRetry() },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry Upload",
                        tint = Color.Red
                    )
                    Text(
                        text = "Upload Failed. Tap to Retry",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
            }

            UploadStatus.SUCCESS -> {

                when (downloadState) {
                    is DownloadState.NotStarted -> {
                        IconButton(
                            onClick = { viewModel.downloadFile(context, videoUrl, fileName) },
                            modifier = Modifier
                                .align(Alignment.Center)
                                .background(Color.White.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download",
                                tint = Color.Black
                            )
                        }
                    }

                    is DownloadState.Downloading -> {
                        val progressPercent =
                            ((downloadState as DownloadState.Downloading).progress * 100).toInt()
                        Column(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LinearProgressIndicator(
                                progress = { (downloadState as DownloadState.Downloading).progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                            )
                            Text(
                                text = "$progressPercent%",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }

                    is DownloadState.Completed -> {
                        IconButton(
                            onClick = {
                                viewModel.openFile(context, fileName)
                            },
                            modifier = Modifier
                                .align(Alignment.Center)
                                .background(Color.White.copy(alpha = 0.8f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.Black
                            )
                        }
                    }

                    is DownloadState.Failed -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clickable { viewModel.downloadFile(context, videoUrl, fileName) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry Download",
                                tint = Color.Red
                            )
                            Text(
                                text = "Download Failed. Tap to Retry",
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                    }

                    else -> {
                        // Optional: catch unexpected state
                        Text(
                            text = "Unknown State",
                            color = Color.LightGray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            else -> {
                showToast(context, "This file type is not supported")
            }
        }
    }
}
