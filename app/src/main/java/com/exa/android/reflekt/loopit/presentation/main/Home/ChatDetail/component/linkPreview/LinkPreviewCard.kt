package com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.linkPreview

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.ComposableTarget
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.data.local.domain.LinkMetadata
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.linkPreview.viewModel.LinkState
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.linkPreview.viewModel.MetaDataViewModel
import java.net.URL


@Composable
fun LinkPreview(message: String, isSentByMe: Boolean) {

    val metaDataViewModel: MetaDataViewModel = hiltViewModel()

    val linkState by metaDataViewModel.getLinkState(message).collectAsState()
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val annotatedString = getAnnotatedString(message, isSentByMe)

    when (val state = linkState) {
        is LinkState.Loading -> { /*LinkPreviewLoading() */
        }

        is LinkState.Success -> state.metadata?.let { metaData ->
            LinkPreviewCard(
                metadata = metaData,
                isSentByMe = isSentByMe,
                onRetry = { metaDataViewModel.refreshMetadata(message) }
            )
        }

        is LinkState.Error -> { /*LinkPreviewError(
            message = state.message,
            onRetry = { metaDataViewModel.refreshMetadata(message) }
        )*/
        }
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge.copy(
            // color is set from annotated string
        ),
        onClick = { offset ->
            annotatedString.getStringAnnotations("URL", offset, offset)
                .firstOrNull()
                ?.let { annotation ->
//                    uriHandler.openUri(annotation.item)
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Opens in the same task
                    }
                    context.startActivity(intent)
                }


        }
    )

}

@Composable
fun LinkPreviewCard(
    metadata: LinkMetadata,
    isSentByMe: Boolean,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
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