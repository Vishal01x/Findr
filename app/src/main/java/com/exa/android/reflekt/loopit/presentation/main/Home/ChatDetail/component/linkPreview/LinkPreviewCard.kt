package com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.linkPreview

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposableTarget
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.data.local.domain.LinkMetadata
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.linkPreview.viewModel.LinkState
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.linkPreview.viewModel.MetaDataViewModel
import com.exa.android.reflekt.loopit.presentation.main.profile.components.extra_card.openUrl
import com.exa.android.reflekt.loopit.util.LinkUtils
import com.exa.android.reflekt.loopit.util.showToast
import kotlinx.coroutines.launch
import java.net.URL


@Composable
fun LinkPreview(message: String, isSentByMe: Boolean, selectedMessagesSize: Int) {

    val metaDataViewModel: MetaDataViewModel = hiltViewModel()

    val linkState by metaDataViewModel.getLinkState(message).collectAsState()
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val annotatedString = getAnnotatedString(message, isSentByMe)

    when (val state = linkState) {
        is LinkState.Loading -> { /*LinkPreviewLoading() */
        }

        is LinkState.Success -> state.metadata?.let { metaData ->
            LinkPreviewCard(
                metadata = metaData,
                isSentByMe = isSentByMe,
                selectedMessagesSize = selectedMessagesSize,
                onClick = { if (selectedMessagesSize <= 0) openUrl(context,metaData.url) }
                // onRetry = { metaDataViewModel.refreshMetadata(message) }
            )
        }

        is LinkState.Error -> { /*LinkPreviewError(
            message = state.message,
            onRetry = { metaDataViewModel.refreshMetadata(message) }
        )*/
        }
    }
    SelectionContainer {
        ClickableText(
            text = annotatedString,
            style = MaterialTheme.typography.bodyLarge.copy(
                // color is set from annotated string
            ),
            onClick = { offset ->
                if (selectedMessagesSize <= 0)
                    openLink(annotatedString, context, offset)
            },
            onTextLayout = { layoutResult ->
                textLayoutResult = layoutResult
            },
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        if (selectedMessagesSize <= 0) {
                            openLink(annotatedString, context, 0)
                        }
                    },
                    onLongPress = { tapOffset ->
                        textLayoutResult?.let { layout ->
                            val offset = layout.getOffsetForPosition(tapOffset)
                            val link = getLinkFromText(annotatedString, offset)
                            link?.let {
                                coroutineScope.launch {
                                    copyToClipboard(context, it)
                                }
                            }
                        }
                    }
                )
            }
        )
    }
}

@Composable
fun LinkPreviewCard(
    metadata: LinkMetadata,
    isSentByMe: Boolean,
    selectedMessagesSize: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
//    Card(
//        modifier = modifier,
//        shape = MaterialTheme.shapes.medium,
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surfaceVariant
//        )
//    ) {

    metadata.imageUrl?.let { imageUrl ->
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(MaterialTheme.shapes.medium)
                .clickable(enabled = selectedMessagesSize <= 0) { onClick() }
        )
        Spacer(modifier = Modifier.height(4.dp))
    }

    Column(
        modifier = Modifier
            .background(
                if (isSentByMe) Color.White.copy(alpha = .1f) else Color.White.copy(
                    alpha = .4f
                )
            )
            .padding(4.dp)
            .clickable(enabled = selectedMessagesSize <= 0) { onClick() }
    ) {

        Text(
            text = URL(metadata.url).host ?: "",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.tertiary
        )
        metadata.title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
        }
        metadata.description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 3
            )
        }
    }
    // }
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

fun openLink(annotatedString: AnnotatedString, context: Context, offset: Int) { // offset to check at click point is there any link
    val annotation = annotatedString.getStringAnnotations(start = offset, end = offset)
        .firstOrNull() ?: return

    try {
        if (Patterns.EMAIL_ADDRESS.matcher(annotation.item).matches()) {
            // It's a valid email
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${annotation.item}")
            }
            context.startActivity(intent)

        } else if (Patterns.WEB_URL.matcher(annotation.item).matches()) {
            // It's a valid URL
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } else {
            showToast(context, "Invalid link or email")
        }

    } catch (e: Exception) {
        e.printStackTrace()
        showToast(context, "Cannot open link")
    }
}


private fun getLinkFromText(annotatedString: AnnotatedString, offset: Int): String? {
    return annotatedString.getStringAnnotations("URL", offset, offset)
        .firstOrNull()?.item
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("Copied Link", text)
    clipboardManager.setPrimaryClip(clipData)
}