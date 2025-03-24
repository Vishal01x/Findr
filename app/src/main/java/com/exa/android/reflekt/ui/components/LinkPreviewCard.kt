package com.exa.android.reflekt.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.local.domain.LinkMetadata

@Composable
fun LinkPreviewCard(metadata: LinkMetadata, message : String, onRetry: () -> Unit) {
    val context = LocalContext.current

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            metadata.imageUrl?.let { url ->
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(url)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Preview image",
                    modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium),
                    placeholder = painterResource(R.drawable.ic_launcher_foreground),
                    error = painterResource(R.drawable.ic_launcher_foreground),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(8.dp))
            }

            Text(
                text = metadata.domain,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary
            )

            Text(
                text = metadata.title ?: "No title available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )

            metadata.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 3
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LinkPreviewCardPreview() {
    val link = LinkMetadata(
        url = "https://www.example.com",
        imageUrl = "https://via.placeholder.com/300", // Placeholder image
        domain = "example.com",
        title = "Example Article: Understanding Jetpack Compose",
        description = "A deep dive into Jetpack Compose and its powerful UI toolkit.",
//        favicon = "https://via.placeholder.com/32" // Example favicon URL
    )

//    LinkPreviewCard(
//        metadata = link,
//        modifier = Modifier,
//        onLinkClicked = { /* Handle click event */ }
//    )
}