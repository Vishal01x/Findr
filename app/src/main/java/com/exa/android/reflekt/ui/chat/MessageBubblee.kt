package com.exa.android.reflekt.ui.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.model.Message
import com.exa.android.reflekt.ui.components.LinkPreviewCard
import com.exa.android.reflekt.ui.viewmodel.ChatViewModel
import com.exa.android.reflekt.ui.viewmodel.LinkState

@Composable
fun MessageBubblee(message: Message) {
    val isSentByMe = message.isSent
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val viewModel: ChatViewModel = hiltViewModel()

    val linkState by viewModel.getLinkState(message.text).collectAsState()


    // State for storing link metadata
    var linkMetadata by remember { mutableStateOf<LinkMetadata?>(null) }

    // Detect URLs in message
    val annotatedString = buildAnnotatedString {
        append(message.text)

        LinkUtils.findUrls(message.text).forEach { urlSpan ->
            addStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (isSentByMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {

        Column(modifier = Modifier.padding(12.dp)) {
            when (val state = linkState) {
                is LinkState.Loading -> LinkPreviewLoading()
                is LinkState.Success -> state.metadata?.let {
                    LinkPreviewCard(
                        metadata = it,
                        onRetry = { viewModel.refreshMetadata(message.text) }
                    )
                }

                is LinkState.Error -> LinkPreviewError(
                    message = state.message,
                    onRetry = { viewModel.refreshMetadata(message.text) }
                )
            }

            //Text(
//                    text = message.text,
//                    style = MaterialTheme.typography.bodyLarge.copy(
//                        color = if (isSentByMe) Color.White else MaterialTheme.colorScheme.onSurface
//                    )
//                )
            ClickableText(
                text = annotatedString,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = if (isSentByMe) Color.White else MaterialTheme.colorScheme.onSurface
                ),
                onClick = { offset ->
                    annotatedString.getStringAnnotations("URL", offset, offset)
                        .firstOrNull()
                        ?.let { annotation ->
                            uriHandler.openUri(annotation.item)
                        }
                }
            )

            Text(
                text = message.timestamp,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isSentByMe) Color.White.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ),
                modifier = Modifier.align(Alignment.End)
            )
        }

//        // Show link preview if metadata exists
//        linkMetadata?.let { metadata ->
//            LinkPreviewCard(
//                metadata = metadata,
//                modifier = Modifier.padding(top = 4.dp)
//            )
//        }
    }
}

@Composable
private fun LinkPreviewLoading() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Loading preview...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LinkPreviewError(message: String, onRetry: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Preview unavailable",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}