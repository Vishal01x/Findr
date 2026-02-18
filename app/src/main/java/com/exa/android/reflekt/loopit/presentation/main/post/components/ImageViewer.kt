package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.User
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostUserInfo
import com.exa.android.reflekt.loopit.theme.Spacing
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Enhanced full-screen image viewer with zoom, pan, and swipe-to-close
 */
/*
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageViewer(
    images: List<String>,
    initialPage: Int = 0,
    user: PostUserInfo? = null,
    caption: String? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDetails by remember { mutableStateOf(true) }

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { images.size }
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { showDetails = !showDetails }
                    )
                }
        ) {


            // Image Pager with Zoom
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                ZoomableImage(
                    imageUrl = images[page],
                    onSwipeDown = onDismiss
                )
            }

            // Top Bar with close and actions
            AnimatedVisibility(
                visible = showDetails,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                ImageViewerTopBar(
                    currentPage = pagerState.currentPage + 1,
                    totalPages = images.size,
                    onClose = onDismiss
                )
            }

            // Page Indicator
            /*if (images.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (user != null || caption != null) 100.dp else Spacing.large),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    repeat(images.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (pagerState.currentPage == index) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage == index)
                                        Color.White
                                    else
                                        Color.White.copy(alpha = 0.5f)
                                )
                        )
                    }
                }
            }*/

            // Bottom Info
            AnimatedVisibility(
                visible = showDetails && (user != null || caption != null),
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                ImageViewerBottomInfo(
                    user = user,
                    caption = caption
                )
            }
        }
    }
}


/*
@Composable   // 2 finger zoom but pager doesn't work
fun ZoomableImage(
    imageUrl: String,
    onSwipeDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val minScale = 1f
    val maxScale = 4f

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var swipeOffsetY by remember { mutableStateOf(0f) }

    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = ""
    )

    val animatedOffset by animateOffsetAsState(
        targetValue = offset + Offset(0f, swipeOffsetY),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = ""
    )

    val velocityTracker = remember { VelocityTracker() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)

            // TRANSFORMS
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                translationX = animatedOffset.x
                translationY = animatedOffset.y
            }

            // PINCH + PAN
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->

                    val newScale = (scale * zoom).coerceIn(minScale, maxScale)
                    val resistance = if (newScale == minScale) 0.25f else 1f

                    scale = newScale

                    if (scale > 1f) {
                        offset += pan * resistance
                    }
                }
            }

            // DOUBLE TAP ZOOM (TO FINGER)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tap ->
                        if (scale > 1f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                            offset = -tap * (scale - 1f)
                        }
                    }
                )
            }

            // FLING + SWIPE DOWN TO CLOSE
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, drag ->
                        velocityTracker.addPosition(
                            change.uptimeMillis,
                            change.position
                        )

                        if (scale == 1f) {
                            swipeOffsetY += drag.y
                        } else {
                            offset += drag
                        }

                        change.consume()
                    },
                    onDragEnd = {
                        if (scale == 1f && swipeOffsetY > 250f) {
                            onSwipeDown()
                        } else {
                            swipeOffsetY = 0f
                        }
                    }
                )
            }
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}
*/
*/

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageViewer(
    images: List<String>,
    initialPage: Int = 0,
    user: PostUserInfo? = null,
    caption: String? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDetails by remember { mutableStateOf(true) }
    var isZoomed by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { images.size }
    )

    // Reset zoom state when page changes
    LaunchedEffect(pagerState.currentPage) {
        isZoomed = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {

            // ✅ TAP HANDLING (single tap only)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isZoomed) {
                        detectTapGestures(
                            onTap = {
                                // Toggle UI ONLY when not zooming
                                showDetails = !showDetails
                            }
                        )
                    }
            )

            // ✅ HORIZONTAL PAGER (correct ownership)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true //!isZoomed
            ) { page ->
                ZoomableImage(
                    imageUrl = images[page],
                    onSwipeDown = onDismiss,
                    onTap = {// Toggle UI ONLY when not zooming
                        showDetails = !showDetails}
                )
            }

            // TOP BAR
            AnimatedVisibility(
                visible = showDetails,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                ImageViewerTopBar(
                    currentPage = pagerState.currentPage + 1,
                    totalPages = images.size,
                    onClose = onDismiss
                )
            }

            // BOTTOM INFO
            AnimatedVisibility(
                visible = showDetails && (user != null || caption != null),
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                ImageViewerBottomInfo(
                    user = user,
                    caption = caption
                )
            }
        }
    }
}

@Composable
private fun ImageViewerTopBar(
    currentPage: Int,
    totalPages: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars),
        color = Color.Black.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Page counter
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "$currentPage / $totalPages",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            // Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
//                IconButton(
//                    onClick = { /* Share */ },
//                    modifier = Modifier
//                        .size(40.dp)
//                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
//                ) {
//                    Icon(
//                        imageVector = Icons.Rounded.Share,
//                        contentDescription = "Share",
//                        tint = Color.White,
//                        modifier = Modifier.size(20.dp)
//                    )
//                }
//
//                Spacing.small

//                IconButton(
//                    onClick = { /* Download */ },
//                    modifier = Modifier
//                        .size(40.dp)
//                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
//                ) {
//                    Icon(
//                        imageVector = Icons.Rounded.Download,
//                        contentDescription = "Download",
//                        tint = Color.White,
//                        modifier = Modifier.size(20.dp)
//                    )
//                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageViewerBottomInfo(
    user: PostUserInfo?,
    caption: String?,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        val maxExpandedHeight = maxHeight * 0.4f

        Surface(
            color = Color.Black.copy(alpha = 0.7f),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = if (expanded) maxExpandedHeight else Dp.Unspecified)
                .animateContentSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.medium)
            ) {

                /* ---------- USER INFO ---------- */
                if (user != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = if (caption != null) Spacing.small else 0.dp)
                    ) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = "${user.name}'s avatar",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(Spacing.small))

                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                    }
                }

                /* ---------- CAPTION ---------- */
                if (!caption.isNullOrBlank()) {
                    val scrollState = rememberScrollState()

                    Column(
                        modifier = if (expanded) {
                            Modifier
                                .fillMaxWidth()
                                .heightIn(max = maxExpandedHeight)
                                .verticalScroll(scrollState)
                        } else Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = caption,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = if (expanded) Int.MAX_VALUE else 4,
                            overflow = TextOverflow.Ellipsis
                        )

                        // More / Less
                        Text(
                            text = if (expanded) "less" else "more",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .clickable { expanded = !expanded }
                        )
                    }
                }
            }
        }
    }
}



@Composable // on double tap zoom, pager works fine
fun ZoomableImage(
    imageUrl: String,
    onSwipeDown: () -> Unit,
    onTap: () -> Unit
) {
    val minScale = 1f
    val maxScale = 4f

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var swipeOffset by remember { mutableStateOf(0f) }

    val state = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(minScale, maxScale)
        scale = newScale

        if (scale > 1f) {
            offset += panChange
        }
    }

    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(
                scaleX = animatedScale,
                scaleY = animatedScale,
                translationX = offset.x,
                translationY = offset.y + swipeOffset
            )

            // ✅ TRANSFORM ONLY WHEN ZOOMED
            .transformable(
                state = state,
                enabled = scale > 1f
            )

            // DOUBLE TAP
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 1.5f
                        }
                    },
                    onTap = {
                        onTap()
                    }
                )
            }

            // SWIPE DOWN TO CLOSE
            .pointerInput(scale) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, drag ->
                        if (scale == 1f) {
                            swipeOffset += drag
                            change.consume()
                        }
                    },
                    onDragEnd = {
                        if (scale == 1f && abs(swipeOffset) > 200) {
                            onSwipeDown()
                        } else {
                            swipeOffset = 0f
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = if (scale > 1f) ContentScale.Crop else ContentScale.Fit
        )
    }
}