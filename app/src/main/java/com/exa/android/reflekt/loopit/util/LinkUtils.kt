package com.exa.android.reflekt.loopit.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import com.exa.android.reflekt.ui.chat.LinkMetadata
import com.exa.android.reflekt.ui.chat.LinkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.regex.Pattern

object LinkUtils {
    // Existing containsLink function
    fun containsLink(text: String): Boolean {
        return findUrls(text).isNotEmpty()
    }

    // New URL detection function
     fun findUrls(text: String): List<LinkSpan> {
        val pattern = Pattern.compile("(https?://\\S+)")
        val matcher = pattern.matcher(text)
        val matches = mutableListOf<LinkSpan>()
        while (matcher.find()) {
            matches.add(LinkSpan(
                url = matcher.group(),
                start = matcher.start(),
                end = matcher.end()
            ))
        }
        return matches
    }

    fun isValidUrl(text: String): Boolean {
        return try {
            val url = java.net.URL(text)
            url.toURI() // Ensures it's a valid URI
            true
        } catch (e: Exception) {
            false
        }
    }
    data class LinkSpan(val url: String, val start: Int, val end: Int)
}


// Mock metadata fetch function
private suspend fun fetchLinkMetadata(url: String): LinkMetadata? {
    return try {
        val connection =
            withContext(Dispatchers.IO) {
                URL(url).openConnection()
            }

        withContext(Dispatchers.IO) {
            connection.connect()
        }

        // Parse HTML meta tags here
        LinkMetadata(
            url = url,
            title = connection.getHeaderField("og:title") ?: "Untitled",
            description = connection.getHeaderField("og:description"),
            imageUrl = connection.getHeaderField("og:image")
        )
    } catch (e: Exception) {
        null
    }
}