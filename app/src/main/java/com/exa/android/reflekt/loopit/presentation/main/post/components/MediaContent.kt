package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.theme.Spacing
import kotlinx.coroutines.delay

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
    compactSingleImage: Boolean = false,
    modifier: Modifier = Modifier
) {
    when {
        images.isNotEmpty() -> {
            if (compactSingleImage && images.size == 1) {
                AsyncImage(
                    model = images[0],
                    contentDescription = "Post image",
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .height(200.dp)
                        .clickableWithNoIndication { onImageClick(0) },
                    contentScale = ContentScale.Crop
                )
            } else {
                DynamicImageGrid(
                    images = images,
                    onImageClick = onImageClick,
                    modifier = modifier
                )
            }
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
            .clickableWithNoIndication(onClick = onClick),
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
                    .clickableWithNoIndication { onImageClick(index) },
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
                .clickableWithNoIndication { onImageClick(0) },
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
                    .clickableWithNoIndication { onImageClick(1) },
                contentScale = ContentScale.Crop
            )
            
            AsyncImage(
                model = images[2],
                contentDescription = "Image 3",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickableWithNoIndication { onImageClick(2) },
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
                    .clickableWithNoIndication { onImageClick(0) },
                contentScale = ContentScale.Crop
            )

            AsyncImage(
                model = images[1],
                contentDescription = "Image 2",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickableWithNoIndication { onImageClick(1) },
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
                    .clickableWithNoIndication { onImageClick(2) },
                contentScale = ContentScale.Crop
            )

            AsyncImage(
                model = images[3],
                contentDescription = "Image 4",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickableWithNoIndication { onImageClick(3) },
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
                    .clickableWithNoIndication { onImageClick(0) },
                contentScale = ContentScale.Crop
            )
            
            AsyncImage(
                model = images[1],
                contentDescription = "Image 2",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickableWithNoIndication { onImageClick(1) },
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
                    .clickableWithNoIndication { onImageClick(2) },
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
                        .clickableWithNoIndication { onImageClick(3) },
                    contentScale = ContentScale.Crop
                )
                
                // Overlay with +N
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickableWithNoIndication { onImageClick(3) },
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

/**
 * Hero image display for PostDetailScreen.
 * - No images: caller is responsible for not calling this.
 * - 1 image: full-width single image.
 * - 2+ images: HorizontalPager carousel with dot indicators.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeroCarousel(
    images: List<String>,
    onImageClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (images.isEmpty()) return
    if (images.size == 1) {
        AsyncImage(
            model = images[0],
            contentDescription = null,
            modifier = modifier
                .fillMaxWidth()
                .clickableWithNoIndication { onImageClick(0) },
            contentScale = ContentScale.Crop
        )
        return
    }
    val pagerState = rememberPagerState { images.size }
    // Continuous auto-advance: loops every 2s independently of manual swipes
    LaunchedEffect(Unit) {
        while (true) {
            delay(2_000L)
            val next = (pagerState.currentPage + 1) % images.size
            pagerState.animateScrollToPage(next)
        }
    }
    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            AsyncImage(
                model = images[page],
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clickableWithNoIndication { onImageClick(page) },
                contentScale = ContentScale.Crop
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(images.size) { index ->
                Box(
                    modifier = Modifier
                        .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (pagerState.currentPage == index) Color.White
                            else Color.White.copy(alpha = 0.5f)
                        )
                )
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
            .clickableWithNoIndication(onClick = onVideoClick),
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
