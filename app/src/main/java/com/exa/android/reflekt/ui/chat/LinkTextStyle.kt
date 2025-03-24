package com.exa.android.reflekt.ui.chat

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.data.local.domain.LinkMetadata
import java.net.URL

//
//object LinkTextStyle {
//    val linkSpanStyle = SpanStyle(
//        color = MaterialTheme.colorScheme.primary,
//        textDecoration = TextDecoration.Underline,
//        background = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
//    )
//
//    val linkPreviewCardShape = RoundedCornerShape(12.dp)
//    val linkPreviewElevation = 4.dp
//}


@Composable
fun LinkPreviewCard(metadata: LinkMetadata, onRetry: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Card(
        modifier = modifier.clickable {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(metadata.url))
            context.startActivity(intent)
        },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Display preview image if available
            metadata.imageUrl?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Display URL hostname
            Text(
                text = URL(metadata.url).host ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary
            )

            // Display Title
            metadata.title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Display Description
            metadata.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Function to ensure only the URL remains clickable
@Composable
fun NonClickableTextWithClickableLink(text: String) {
    val annotatedString = buildAnnotatedString {
        val urlPattern = "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)".toRegex()
        var lastIndex = 0

        urlPattern.findAll(text).forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1

            // Append non-link text before the URL
            append(text.substring(lastIndex, start))

            // Append clickable URL
            pushStringAnnotation(tag = "URL", annotation = matchResult.value)
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                append(matchResult.value)
            }
            pop()

            lastIndex = end
        }
        // Append any remaining text
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }

    val context = LocalContext.current

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium,
        onClick = { offset ->
            annotatedString.getStringAnnotations("URL", offset, offset).firstOrNull()?.let {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.item))
                context.startActivity(intent)
            }
        }
    )
}
