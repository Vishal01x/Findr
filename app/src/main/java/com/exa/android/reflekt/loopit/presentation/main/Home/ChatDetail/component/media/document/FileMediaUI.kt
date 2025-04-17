package com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.media.docs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun DocumentMessageItem(
    fileUrl: String,
    fileName: String,
    viewModel: VideoDownloadViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val downloadState by viewModel.getDownloadState(fileName).collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .background(Color(0xFFF1F1F1))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File Icon
            Text(
                text = "📄",
                fontSize = 30.sp
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fileName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )

                when (downloadState) {
                    is DownloadState.NotStarted -> {
                        Text(
                            text = "Tap to download",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    is DownloadState.Downloading -> {
                        val progress = (downloadState as DownloadState.Downloading).progress
                        Text(
                            text = "${(progress * 100).toInt()}% downloaded",
                            color = Color.Blue,
                            fontSize = 12.sp
                        )
                    }

                    is DownloadState.Completed -> {
                        Text(
                            text = "Downloaded",
                            color = Color.Green,
                            fontSize = 12.sp
                        )
                    }

                    is DownloadState.Failed -> {
                        Text(
                            text = "Failed. Tap to retry.",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }

                if (downloadState is DownloadState.Downloading) {
                    LinearProgressIndicator(
                        progress = (downloadState as DownloadState.Downloading).progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }
            }

            // Action Button
            when (downloadState) {
                is DownloadState.NotStarted, is DownloadState.Failed -> {
                    Button(
                        onClick = {
                            viewModel.downloadFile(context, fileUrl, fileName)
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                    ) {
                        Text("⬇", fontSize = 18.sp)
                    }
                }

                is DownloadState.Completed -> {
                    Button(
                        onClick = {
                            viewModel.openFile(context, fileName)
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                    ) {
                        Text("📂", fontSize = 18.sp)
                    }
                }

                else -> {
                    // No action needed while downloading
                }
            }
        }
    }
}
