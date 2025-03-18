package com.exa.android.reflekt.util

import java.util.regex.Pattern

object LinkUtils {
    private val URL_REGEX = Pattern.compile(
        "(https?://)?(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)"
    )

    fun containsLink(message: String): Boolean {
        val matcher = URL_REGEX.matcher(message)
        return matcher.find() // Returns true if a URL is found
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

    data class LinkSpan(val url: String, val start: Int, val end: Int)
}