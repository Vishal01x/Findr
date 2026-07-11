package com.exa.android.reflekt.loopit.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Reusable file utilities for sharing bitmaps.
 *
 * Cache strategy (mirrors Carousell FileUtils.getImageCacheDir):
 *   • External cache + Pictures sub-dir when external storage is mounted
 *   • Internal cache + Pictures sub-dir as fallback
 *
 * Cleanup strategy (mirrors FileUtils.deleteOldCacheFile):
 *   • Before writing a new file, delete every existing file whose name starts
 *     with the share prefix so the cache never accumulates stale bitmaps.
 */
object ShareFileUtils {

    private const val SHARE_PREFIX = "reflekt_share"
    private const val COMPRESS_QUALITY = 92

    // ── Directory ─────────────────────────────────────────────────────────────

    fun getShareCacheDir(context: Context): File {
        val base = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED)
            context.externalCacheDir
        else
            context.cacheDir
        return File(base, Environment.DIRECTORY_PICTURES).also { it.mkdirs() }
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    fun deleteOldShareFiles(context: Context) {
        try {
            val dir = getShareCacheDir(context)
            if (!dir.exists()) return
            dir.listFiles()
                ?.filter { it.name.startsWith(SHARE_PREFIX) }
                ?.forEach { it.delete() }
        } catch (_: Exception) {
        }
    }

    // ── Save ─────────────────────────────────────────────────────────────────

    suspend fun saveBitmapForShare(context: Context, bitmap: Bitmap): File? =
        withContext(Dispatchers.IO) {
            try {
                deleteOldShareFiles(context)
                val file = File(getShareCacheDir(context), "${SHARE_PREFIX}_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, out)
                }
                file
            } catch (_: Exception) {
                null
            }
        }

    // ── URI for sharing via FileProvider ─────────────────────────────────────

    fun getUriForShare(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}
