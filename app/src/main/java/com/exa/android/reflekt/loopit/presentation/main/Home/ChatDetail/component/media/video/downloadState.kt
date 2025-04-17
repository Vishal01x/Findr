package com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.video

sealed class DownloadState {
    object NotStarted : DownloadState()
    data class Downloading(val progress: Float) : DownloadState()
    object Completed : DownloadState()
    object Failed : DownloadState()
}
