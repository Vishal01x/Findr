package com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.video

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.getMimeType
import com.exa.android.reflekt.loopit.util.Constants.APP_NAME
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class VideoDownloadViewModel @Inject constructor(
    private val fileDownloader: FileDownloader,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _downloadStates = mutableMapOf<String, MutableStateFlow<DownloadState>>()
    val downloadStates: Map<String, StateFlow<DownloadState>> get() = _downloadStates

    private val downloadedFilePaths = mutableMapOf<String, String>()

    fun getDownloadState(fileName: String): StateFlow<DownloadState> {
        return _downloadStates.getOrPut(fileName) {
            MutableStateFlow(getInitialDownloadState(fileName))
        }
    }

    fun isFileDownloaded(context: Context, fileName: String): File? {
        val folder = File(context.getExternalFilesDir(null), APP_NAME)
        val file = File(folder, fileName)
        return if (file.exists()) file else null
    }


    private fun getInitialDownloadState(fileName: String): DownloadState {
        val file = isFileDownloaded(context, fileName)
        if (file != null) {
            return if (file.exists()) {
                downloadedFilePaths[fileName] = file.absolutePath
                DownloadState.Completed
            } else DownloadState.NotStarted
        }
        return DownloadState.NotStarted
    }

    fun downloadFile(context: Context, url: String, fileName: String) {
        val state = getDownloadState(fileName) as MutableStateFlow

        viewModelScope.launch {
            state.value = DownloadState.Downloading(0f)

            fileDownloader.downloadFile(
                context = context,
                fileUrl = url,
                fileName = fileName,
                onProgress = { progress ->
                    state.value = DownloadState.Downloading(progress)
                },
                onSuccess = { file ->
                    downloadedFilePaths[fileName] = file.absolutePath
                    state.value = DownloadState.Completed
                },
                onError = {
                    state.value = DownloadState.Failed
                }
            )
        }
    }

    fun openFile(context: Context, fileName: String) {
        downloadedFilePaths[fileName]?.let { path ->
            val file = File(path)
            val mimeType = getMimeType(file)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }

            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    context,
                    "No app found to open this file type.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}
