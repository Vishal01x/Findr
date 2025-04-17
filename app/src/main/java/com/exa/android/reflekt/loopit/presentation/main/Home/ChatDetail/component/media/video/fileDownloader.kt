package com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.video

import android.content.Context
import com.exa.android.reflekt.loopit.util.Constants.APP_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class FileDownloader @Inject constructor() {

    suspend fun downloadFile(
        context: Context,
        fileUrl: String,
        fileName: String,
        onProgress: (Float) -> Unit,
        onSuccess: (File) -> Unit,
        onError: () -> Unit
    ) {
        try {
            withContext(Dispatchers.IO) {
                val url = URL(fileUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                val totalSize = connection.contentLength
                val inputStream = connection.inputStream

                val folder = File(context.getExternalFilesDir(null), APP_NAME/*"MyChat/Video"*/)
                if (!folder.exists()) folder.mkdirs()

                val file = File(folder, fileName)
                val outputStream = FileOutputStream(file)

                val buffer = ByteArray(1024)
                var downloadedSize = 0
                var read: Int

                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                    downloadedSize += read
                    val progress = downloadedSize.toFloat() / totalSize
                    onProgress(progress)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                onSuccess(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onError()
        }
    }
}
