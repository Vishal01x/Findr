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
import kotlinx.coroutines.sync.withPermit
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
    private val downloadSemaphore =
        kotlinx.coroutines.sync.Semaphore(permits = 3) // Max 3 at a time

    init {
        cleanUpTempFiles(context)
    }

    fun getDownloadState(fileName: String): StateFlow<DownloadState> {
        return _downloadStates.getOrPut(fileName) {
            MutableStateFlow(getInitialDownloadState(fileName))
        }
    }

    fun isFileDownloaded(context: Context, fileName: String): File? {
        val folder = File(context.getExternalFilesDir(null), APP_NAME)
        val file = File(folder, fileName)
        val isValid = file.exists() && (
                file.extension != "txt" && file.length() > 1024 ||  // For large files
                        file.extension == "txt"                              // For small text files
                )
        return if (isValid) file else null
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
        val state = getDownloadState(fileName) as MutableStateFlow<DownloadState>

        // Check if final file exists
        val finalFile = File(context.getExternalFilesDir(null), "$APP_NAME/$fileName")
        if (finalFile.exists() && finalFile.length() > 1024) {
            downloadedFilePaths[fileName] = finalFile.absolutePath
            state.value = DownloadState.Completed
            return
        }

        val tempFile = File(context.getExternalFilesDir(null), "$APP_NAME/$fileName.temp")
        tempFile.delete() // Clean up any previous failed attempt

        viewModelScope.launch {
            downloadSemaphore.withPermit {
                state.value = DownloadState.Downloading(0f)

                fileDownloader.downloadFile(
                    context = context,
                    fileUrl = url,
                    fileName = "$fileName.temp", // Save as temp file
                    onProgress = { progress ->
                        state.value = DownloadState.Downloading(progress)
                    },
                    onSuccess = { _ ->
                        val renamed = tempFile.renameTo(finalFile)
                        if (renamed) {
                            downloadedFilePaths[fileName] = finalFile.absolutePath
                            state.value = DownloadState.Completed
                        } else {
                            tempFile.delete()
                            state.value = DownloadState.Failed
                        }
                    },
                    onError = {
                        tempFile.delete()
                        state.value = DownloadState.Failed
                    }
                )
            }
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

    fun cleanUpTempFiles(context: Context) {
        val folder = File(context.getExternalFilesDir(null), APP_NAME)
        folder.listFiles()?.forEach { file ->
            if (file.name.endsWith(".temp")) {
                file.delete()
            }
        }
    }


}
