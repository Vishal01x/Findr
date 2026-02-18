package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.theme.Spacing

/**
 * Dynamic image grid layout component
 * - 1 image: Full width
 * - 2 images: Side by side
 * - 3 images: 1 large + 2 small grid
 * - 4+ images: Grid with +N overlay on last image
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaContent(
    images: List<String>,
    videoUrl: String? = null,
    onImageClick: (Int) -> Unit = {},
    onVideoClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    when {
        images.isNotEmpty() -> {
            DynamicImageGrid(
                images = images,
                onImageClick = onImageClick,
                modifier = modifier
            )
        }
        videoUrl != null -> {
            VideoThumbnail(
                onVideoClick = onVideoClick,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun DynamicImageGrid(
    images: List<String>,
    onImageClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val imageCount = images.size
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .animateContentSize()
    ) {
        when (imageCount) {
            1 -> SingleImageLayout(
                imageUrl = images[0],
                onClick = { onImageClick(0) }
            )
            2 -> TwoImageLayout(
                images = images,
                onImageClick = onImageClick
            )
            3 -> ThreeImageLayout(
                images = images,
                onImageClick = onImageClick
            )
            4 -> FourImageLayout(
                images = images,
                onImageClick = onImageClick
            )
            else -> MultiImageLayout(
                images = images,
                onImageClick = onImageClick
            )
        }
    }
}

@Composable
private fun SingleImageLayout(
    imageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = "Post image",
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .clickable(onClick = onClick),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun TwoImageLayout(
    images: List<String>,
    onImageClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        images.take(2).forEachIndexed { index, imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = "Image ${index + 1}",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onImageClick(index) },
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun ThreeImageLayout(
    images: List<String>,
    onImageClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // First image takes 2/3 width
        AsyncImage(
            model = images[0],
            contentDescription = "Image 1",
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .clickable { onImageClick(0) },
            contentScale = ContentScale.Crop
        )
        
        // Second and third images stacked vertically
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            AsyncImage(
                model = images[1],
                contentDescription = "Image 2",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable { onImageClick(1) },
                contentScale = ContentScale.Crop
            )
            
            AsyncImage(
                model = images[2],
                contentDescription = "Image 3",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable { onImageClick(2) },
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun FourImageLayout(
    images: List<String>,
    onImageClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val remainingCount = images.size - 3

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // First row with 2 images
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            AsyncImage(
                model = images[0],
                contentDescription = "Image 1",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onImageClick(0) },
                contentScale = ContentScale.Crop
            )

            AsyncImage(
                model = images[1],
                contentDescription = "Image 2",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onImageClick(1) },
                contentScale = ContentScale.Crop
            )
        }

        // Second row with 1 image and +N overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            AsyncImage(
                model = images[2],
                contentDescription = "Image 3",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onImageClick(2) },
                contentScale = ContentScale.Crop
            )

            AsyncImage(
                model = images[3],
                contentDescription = "Image 4",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onImageClick(3) },
                contentScale = ContentScale.Crop
            )
        }
    }
}


@Composable
private fun MultiImageLayout(
    images: List<String>,
    onImageClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val remainingCount = images.size - 3
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // First row with 2 images
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            AsyncImage(
                model = images[0],
                contentDescription = "Image 1",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onImageClick(0) },
                contentScale = ContentScale.Crop
            )
            
            AsyncImage(
                model = images[1],
                contentDescription = "Image 2",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onImageClick(1) },
                contentScale = ContentScale.Crop
            )
        }
        
        // Second row with 1 image and +N overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            AsyncImage(
                model = images[2],
                contentDescription = "Image 3",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onImageClick(2) },
                contentScale = ContentScale.Crop
            )
            
            // Last image with overlay
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                AsyncImage(
                    model = images.getOrNull(3) ?: images[2],
                    contentDescription = "More images",
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onImageClick(3) },
                    contentScale = ContentScale.Crop
                )
                
                // Overlay with +N
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable { onImageClick(3) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+$remainingCount",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoThumbnail(
    onVideoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
            .clickable(onClick = onVideoClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play video",
            tint = Color.White,
            modifier = Modifier.size(64.dp)
        )
    }
}
