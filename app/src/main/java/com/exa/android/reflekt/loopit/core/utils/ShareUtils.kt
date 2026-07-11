package com.exa.android.reflekt.loopit.core.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.ProfileData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

// Pre-built share payload held by the ViewModel between sheet-open and send.
data class ShareData(val text: String, val imagePath: String?)

object ShareUtils {

    // ── FDL URL helpers ───────────────────────────────────────────────────────

    fun buildFdlUrl(postId: String, scrollTo: String = ""): String {
        val inner = buildString {
            append("https://reflekt.com/project/$postId")
            if (scrollTo.isNotBlank()) append("?scrollTo=$scrollTo")
        }
        val encoded = java.net.URLEncoder.encode(inner, "UTF-8")
        return "https://reflekt.page.link/?link=$encoded&apn=com.exa.android.reflekt&efr=1"
    }

    suspend fun buildShortFdlUrl(context: Context, postId: String): String {
        val longLink = buildFdlUrl(postId)
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = context.getString(R.string.google_api_key)
                val conn = (URL(
                    "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=$apiKey"
                ).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = 5_000
                    readTimeout = 5_000
                    doOutput = true
                }
                val body = """{"longDynamicLink":"$longLink","suffix":{"option":"SHORT"}}"""
                conn.outputStream.use { it.write(body.toByteArray()) }
                if (conn.responseCode == 200) {
                    val json = conn.inputStream.bufferedReader().readText()
                    Regex(""""shortLink"\s*:\s*"([^"]+)"""").find(json)
                        ?.groupValues?.get(1) ?: longLink
                } else longLink
            } catch (_: Exception) {
                longLink
            }
        }
    }

    fun buildCompanyFdlUrl(companyId: String): String {
        val inner = "https://reflekt.com/company/$companyId"
        val encoded = java.net.URLEncoder.encode(inner, "UTF-8")
        return "https://reflekt.page.link/?link=$encoded&apn=com.exa.android.reflekt&efr=1"
    }

    suspend fun buildShortCompanyFdlUrl(context: Context, companyId: String): String {
        val longLink = buildCompanyFdlUrl(companyId)
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = context.getString(R.string.google_api_key)
                val conn = (URL(
                    "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=$apiKey"
                ).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = 5_000
                    readTimeout = 5_000
                    doOutput = true
                }
                val body = """{"longDynamicLink":"$longLink","suffix":{"option":"SHORT"}}"""
                conn.outputStream.use { it.write(body.toByteArray()) }
                if (conn.responseCode == 200) {
                    val json = conn.inputStream.bufferedReader().readText()
                    Regex(""""shortLink"\s*:\s*"([^"]+)"""").find(json)
                        ?.groupValues?.get(1) ?: longLink
                } else longLink
            } catch (_: Exception) {
                longLink
            }
        }
    }

    fun buildProfileFdlUrl(userId: String): String {
        val inner = "https://reflekt.com/profile/$userId"
        val encoded = java.net.URLEncoder.encode(inner, "UTF-8")
        return "https://reflekt.page.link/?link=$encoded&apn=com.exa.android.reflekt&efr=1"
    }

    suspend fun buildShortProfileFdlUrl(context: Context, userId: String): String {
        val longLink = buildProfileFdlUrl(userId)
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = context.getString(R.string.google_api_key)
                val conn = (URL(
                    "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=$apiKey"
                ).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = 5_000
                    readTimeout = 5_000
                    doOutput = true
                }
                val body = """{"longDynamicLink":"$longLink","suffix":{"option":"SHORT"}}"""
                conn.outputStream.use { it.write(body.toByteArray()) }
                if (conn.responseCode == 200) {
                    val json = conn.inputStream.bufferedReader().readText()
                    Regex(""""shortLink"\s*:\s*"([^"]+)"""").find(json)
                        ?.groupValues?.get(1) ?: longLink
                } else longLink
            } catch (_: Exception) {
                longLink
            }
        }
    }

    // ── Pre-build share payload ───────────────────────────────────────────────
    // Called as soon as the share sheet opens.  By the time the user taps
    // "Share via…" the network calls and bitmap work are already done.

    suspend fun prepareShareData(context: Context, post: Post): ShareData {
        return withContext(Dispatchers.IO) {
            val shortLink = buildShortFdlUrl(context, post.id)
            val text = buildShareText(post, shortLink)

            if (post.imageUrls.isNotEmpty()) {
                val bitmaps = downloadImages(post.imageUrls.take(4))
                if (bitmaps.isNotEmpty()) {
                    val collage = createShareBitmap(
                        images = bitmaps,
                        totalCount = post.imageUrls.size,
                        title = post.title,
                        description = post.description
                    )
                    // Delegate all file I/O to ShareFileUtils
                    val file = ShareFileUtils.saveBitmapForShare(context, collage)
                    if (file != null) return@withContext ShareData(text, file.absolutePath)
                }
            }
            ShareData(text, null)
        }
    }

    suspend fun prepareCompanyShareData(context: Context, company: com.exa.android.reflekt.loopit.presentation.main.placement.model.CompanyDetail): ShareData {
        return withContext(Dispatchers.IO) {
            val shortLink = buildShortCompanyFdlUrl(context, company.id)
            val text = buildCompanyShareText(company, shortLink)
            // Skip image collage for companies for now
            ShareData(text, null)
        }
    }

    private fun buildCompanyShareText(company: com.exa.android.reflekt.loopit.presentation.main.placement.model.CompanyDetail, link: String): String = buildString {
        appendLine("*${company.name}*")
        appendLine()
        if (company.description.isNotBlank()) {
            val clean = company.description.replace("\n", " ").trim()
            appendLine(clean.take(80))
            if (clean.length > 80) appendLine(clean.substring(80).take(77) + "…")
            appendLine()
        }
        append(link)
    }

    suspend fun prepareProfileShareData(context: Context, profileData: ProfileData, userId: String): ShareData {
        return withContext(Dispatchers.IO) {
            val shortLink = buildShortProfileFdlUrl(context, userId)
            val text = buildProfileShareText(profileData, shortLink)
            ShareData(text, null)
        }
    }

    private fun buildProfileShareText(profileData: ProfileData, link: String): String = buildString {
        appendLine("*${profileData.profileHeader.name}*")
        if (profileData.profileHeader.headline.isNotBlank()) {
            appendLine(profileData.profileHeader.headline)
        }
        appendLine()
        append(link)
    }

    // ── Fire intent — instant after prepareShareData ──────────────────────────

    suspend fun executeShare(context: Context, data: ShareData) {
        if (data.imagePath != null) {
            val file = java.io.File(data.imagePath)
            if (file.exists()) {
                val uri = ShareFileUtils.getUriForShare(context, file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, data.text)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                withContext(Dispatchers.Main) {
                    context.startActivity(
                        Intent.createChooser(intent, "Share Post")
                            .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    )
                }
                // Give the receiving app ~60 s to read the file, then delete it.
                // We stay on the IO dispatcher so this doesn't block the main thread.
                withContext(Dispatchers.IO) {
                    kotlinx.coroutines.delay(60_000)
                    file.delete()
                }
                return
            }
        }
        withContext(Dispatchers.Main) { shareTextOnly(context, data.text) }
    }

    // ── Share text ────────────────────────────────────────────────────────────

    private fun buildShareText(post: Post, link: String): String = buildString {
        if (post.title.isNotBlank()) {
            appendLine("*${post.title}*")   // bold in WhatsApp / Telegram / Signal
            appendLine()
        }
        if (post.description.isNotBlank()) {
            val clean = post.description.replace("\n", " ").trim()
            appendLine(clean.take(80))
            if (clean.length > 80) appendLine(clean.substring(80).take(77) + "…")
            appendLine()
        }
        append(link)
    }

    // ── Collage bitmap ────────────────────────────────────────────────────────

    private suspend fun downloadImages(urls: List<String>): List<Bitmap> = coroutineScope {
        urls.map { url ->
            async(Dispatchers.IO) {
                runCatching {
                    val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 8_000
                        readTimeout = 8_000
                    }
                    BitmapFactory.decodeStream(conn.inputStream)
                }.getOrNull()
            }
        }.awaitAll().filterNotNull()
    }

    private fun createShareBitmap(
        images: List<Bitmap>,
        totalCount: Int,
        title: String,
        description: String
    ): Bitmap {
        val W = 1080; val imgH = 900; val H = imgH + 220; val gap = 4

        val result = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(Color.parseColor("#0F0F14"))

        drawCollage(canvas, images, totalCount, W, imgH, gap)

        // Gradient into text section
        Paint().also {
            it.shader = LinearGradient(
                0f, (imgH - 200).toFloat(), 0f, imgH.toFloat(),
                intArrayOf(Color.TRANSPARENT, Color.parseColor("#EE0F0F14")),
                null, Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, (imgH - 200).toFloat(), W.toFloat(), imgH.toFloat(), it)
        }

        if (title.isNotBlank()) {
            canvas.drawText(
                if (title.length > 44) title.take(41) + "…" else title,
                48f, (imgH + 76).toFloat(),
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE; textSize = 54f; typeface = Typeface.DEFAULT_BOLD
                }
            )
        }

        if (description.isNotBlank()) {
            val clean = description.replace("\n", " ").trim()
            val descPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#AAFFFFFF"); textSize = 38f
            }
            canvas.drawText(clean.take(56), 48f, (imgH + 138).toFloat(), descPaint)
            if (clean.length > 56)
                canvas.drawText(clean.substring(56).take(53) + "…", 48f, (imgH + 190).toFloat(), descPaint)
        }

        canvas.drawText(
            "Findr", (W - 120).toFloat(), (H - 20).toFloat(),
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#556366f1"); textSize = 28f }
        )
        return result
    }

    // Mirrors MediaContent.kt: 1 / 2 / 3 / 4+ grid layouts
    private fun drawCollage(
        canvas: Canvas, images: List<Bitmap>, totalCount: Int,
        W: Int, H: Int, gap: Int
    ) {
        when (images.size) {
            1 -> canvas.drawBitmap(scale(images[0], W, H), 0f, 0f, null)

            2 -> {
                val half = (W - gap) / 2
                canvas.drawBitmap(scale(images[0], half, H), 0f, 0f, null)
                canvas.drawBitmap(scale(images[1], half, H), (half + gap).toFloat(), 0f, null)
            }

            3 -> {
                val lW = W * 2 / 3 - gap / 2; val rW = W - lW - gap; val hH = (H - gap) / 2
                canvas.drawBitmap(scale(images[0], lW, H), 0f, 0f, null)
                canvas.drawBitmap(scale(images[1], rW, hH), (lW + gap).toFloat(), 0f, null)
                canvas.drawBitmap(scale(images[2], rW, hH), (lW + gap).toFloat(), (hH + gap).toFloat(), null)
            }

            else -> {
                val half = (W - gap) / 2; val hH = (H - gap) / 2
                canvas.drawBitmap(scale(images[0], half, hH), 0f, 0f, null)
                canvas.drawBitmap(scale(images[1], half, hH), (half + gap).toFloat(), 0f, null)
                canvas.drawBitmap(scale(images[2], half, hH), 0f, (hH + gap).toFloat(), null)
                canvas.drawBitmap(scale(images[3], half, hH), (half + gap).toFloat(), (hH + gap).toFloat(), null)
                val remaining = totalCount - 4
                if (remaining > 0) {
                    canvas.drawRect(
                        (half + gap).toFloat(), (hH + gap).toFloat(), W.toFloat(), H.toFloat(),
                        Paint().apply { color = Color.parseColor("#99000000") }
                    )
                    canvas.drawText(
                        "+$remaining",
                        (half + gap + half / 2).toFloat(), (hH + gap + hH / 2 + 36).toFloat(),
                        Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.WHITE; textSize = 96f
                            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
                        }
                    )
                }
            }
        }
    }

    // Centre-crop then scale to exact target dimensions
    private fun scale(src: Bitmap, targetW: Int, targetH: Int): Bitmap {
        val srcR = src.width.toFloat() / src.height
        val dstR = targetW.toFloat() / targetH
        val (cropW, cropH) = if (srcR > dstR)
            (src.height * dstR).toInt() to src.height
        else
            src.width to (src.width / dstR).toInt()
        val x = ((src.width - cropW) / 2).coerceAtLeast(0)
        val y = ((src.height - cropH) / 2).coerceAtLeast(0)
        return Bitmap.createScaledBitmap(
            Bitmap.createBitmap(src, x, y, cropW, cropH), targetW, targetH, true
        )
    }

    // ── Fallback ──────────────────────────────────────────────────────────────

    private fun shareTextOnly(context: Context, text: String) {
        context.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) },
                "Share Post"
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        )
    }
}
