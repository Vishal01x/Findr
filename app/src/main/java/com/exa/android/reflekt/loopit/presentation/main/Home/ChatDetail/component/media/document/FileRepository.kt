package com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.media.document

import android.content.Context
import android.os.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class FileRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun downloadFile(url: String, fileName: String, onProgress: (Int) -> Unit): File {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "MyChatApp/$fileName")
        val urlConnection = URL(url).openConnection() as HttpURLConnection
        urlConnection.connect()

        val totalSize = urlConnection.contentLength
        val inputStream = BufferedInputStream(urlConnection.inputStream)
        val outputStream = FileOutputStream(file)

        val data = ByteArray(1024)
        var count: Int
        var downloadedSize = 0

        while (inputStream.read(data).also { count = it } != -1) {
            downloadedSize += count
            outputStream.write(data, 0, count)
            val progress = (downloadedSize * 100 / totalSize)
            onProgress(progress)
        }

        outputStream.flush()
        outputStream.close()
        inputStream.close()
        return file
    }
}
