package com.exa.android.reflekt.loopit.util

import androidx.compose.material3.MaterialTheme
import com.exa.android.reflekt.loopit.data.local.domain.LinkMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.regex.Pattern

object LinkUtils {

    private val URL_REGEX = Pattern.compile(
        "(https?://)?(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)"
    )


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

    fun findLinksInText(text: String): List<LinkSpan> {
        val matcher = URL_REGEX.matcher(text)
        val links = mutableListOf<LinkSpan>()

        while (matcher.find()) {
            links.add(
                LinkSpan(
                    url = matcher.group(),
                    start = matcher.start(),
                    end = matcher.end()
                )
            )
        }
        return links
    }

    fun isValidUrl(text: String): Boolean {
        return try {
            val url = URL(text)
            url.toURI() // Ensures it's a valid URI
            true
        } catch (e: Exception) {
            false
        }
    }
    data class LinkSpan(val url: String, val start: Int, val end: Int)
}
