package com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.linkPreview

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import com.exa.android.reflekt.loopit.util.LinkUtils

/*
@Composable
fun getAnnotatedString(text: String, isSentByMe : Boolean): AnnotatedString {
    val annotatedString = buildAnnotatedString {
        append(text)

        LinkUtils.findUrls(text).forEach { urlSpan ->
            addStyle(
                style = SpanStyle(
                    color = if(isSentByMe) Color.White else MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                ),
                start = urlSpan.start,
                end = urlSpan.end
            )
            addStringAnnotation(
                tag = "URL",
                annotation = urlSpan.url,
                start = urlSpan.start,
                end = urlSpan.end
            )
        }
    }
    return annotatedString
}*/

@Composable
fun getAnnotatedString(text: String, isSentByMe: Boolean): AnnotatedString {
    return buildAnnotatedString {
        val defaultColor = if (isSentByMe) Color.White else Color.Black // Normal text color
        val linkColor = if (isSentByMe) Color.Blue else Color.Blue // Link color

        var lastIndex = 0

        LinkUtils.findUrls(text).forEach { urlSpan ->
            // Append normal text before the link
            if (urlSpan.start > lastIndex) {
                pushStyle(
                    SpanStyle(
                        color = defaultColor
                    )
                )

                append(text.substring(lastIndex, urlSpan.start))
                pop()
            }

            // Append the link with different color
            pushStyle(
                SpanStyle(
                    color = linkColor,
                    fontStyle = FontStyle.Italic,
                    textDecoration = TextDecoration.Underline
                )
            )
            append(text.substring(urlSpan.start, urlSpan.end))
            pop()

            // Add annotation for click handling
            addStringAnnotation(
                tag = "URL",
                annotation = urlSpan.url,
                start = urlSpan.start,
                end = urlSpan.end
            )

            lastIndex = urlSpan.end
        }

        // Append any remaining normal text after the last link
        if (lastIndex < text.length) {
            pushStyle(
                SpanStyle(
                    color = defaultColor
                )
            )
            append(text.substring(lastIndex))
            pop()
        }
    }
}
