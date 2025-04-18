package com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.image

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.video.VideoDownloadViewModel
import com.exa.android.reflekt.loopit.util.model.UploadStatus
import com.exa.android.reflekt.loopit.util.showToast

@Composable
fun ImageMessageContent(
    imageUrl: String,
    fileName: String,
    isSentByCurUser : Boolean,
    uploadStatus: UploadStatus,
    onRetry : () -> Unit,
    onDownloadClick: () -> Unit,
    onImageClick: () -> Unit,
    viewModel: VideoDownloadViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        onDispose {
            viewModel.cleanUpTempFiles(context)
        }
    }

    Box(modifier = Modifier
        .clip(RoundedCornerShape(16.dp))
        .background(Color.LightGray)
        .clickable { onImageClick() /*viewModel.openFile(context, fileName)*/ }) {

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .placeholder(R.drawable.placeholder) // shown while loading
                //.error(R.drawable.chat_img3) // shown on load failure
                .build(),
            contentDescription = "Image message",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Crop
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

                IconButton(
                    onClick = { onDownloadClick() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        tint = Color.White
                    )
                }
            }

            UploadStatus.NOTSUPPORTED -> {
                showToast(context, "This file type is not supported")
            }
        }
    }
}