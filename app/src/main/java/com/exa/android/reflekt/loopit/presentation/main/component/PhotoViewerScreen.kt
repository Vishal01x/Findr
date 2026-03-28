package com.exa.android.reflekt.loopit.presentation.main.component

import android.app.Activity
import android.net.Uri
import android.view.WindowManager
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.launch
import java.net.URLDecoder
import kotlin.math.abs


@Composable
fun PhotoViewerScreen(
    imageUrl: String?,
    onBack: () -> Unit
) {
    var isImmersive by remember { mutableStateOf(false) }
    val backgroundColor = if (isImmersive) Color.Black else Color.White
    val iconColor = if (isImmersive) Color.White else Color.Black

    val context = LocalContext.current
    val activity = context as? Activity

    DisposableEffect(Unit) {
        activity?.window?.setFlags( // to block screenshot
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    isImmersive = !isImmersive
                })
            },
        contentAlignment = Alignment.Center
    ) {
        // Image with fixed height and crop scaling
//        SubcomposeAsyncImage(
//            model = imageUrl,
//            contentDescription = null,
//            contentScale = ContentScale.Crop,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(400.dp)  // Fixed height
//        )

        if(imageUrl != null) {
            ZoomableImage(imageUrl, onSwipeDown = onBack,
                onTap = { isImmersive = !isImmersive})
        }

        // Back button (only in non-immersive mode)
        if (!isImmersive) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = iconColor
                )
            }
        }
    }
}



@Composable
fun ZoomableImage( // animated double tap, two finger no pager
    imageUrl: String,
    onSwipeDown: () -> Unit,
    onZoomChanged: (Boolean) -> Unit = {},
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val minScale = 1f
    val maxScale = 4f

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var swipeOffset by remember { mutableStateOf(0f) }

    var velocityTracker = remember { VelocityTracker() }

    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = ""
    )


    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer(
                scaleX = animatedScale,
                scaleY = animatedScale,
                translationX = offset.x,
                translationY = offset.y + swipeOffset
            )

            // PINCH + PAN (unchanged)
            .pointerInput(scale) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(minScale, maxScale)

                    if (newScale != scale) {
                        scale = newScale
                        onZoomChanged(scale > 1f)
                    }

                    if (scale > 1f) {
                        val maxX = (size.width * scale - size.width) / 2
                        val maxY = (size.height * scale - size.height) / 2

                        offset = Offset(
                            (offset.x + pan.x).coerceIn(-maxX, maxX),
                            (offset.y + pan.y).coerceIn(-maxY, maxY)
                        )
                    }
                }
            }

            // DOUBLE TAP ZOOM
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1f) {
                            scale = 1f
                            offset = Offset.Zero
                            onZoomChanged(false)
                        } else {
                            scale = 2f
                            onZoomChanged(true)
                        }
                    },
                    onTap = {
                        onTap()
                    }
                )
            }

            // SWIPE DOWN TO DISMISS
            .pointerInput(scale) {
                detectVerticalDragGestures(
                    onDragStart = { velocityTracker = VelocityTracker() },
                    onVerticalDrag = { change, drag ->
                        if (scale == 1f) {
                            swipeOffset += drag
                            velocityTracker.addPosition(
                                change.uptimeMillis,
                                change.position
                            )
                            change.consume()
                        }
                    },
                    onDragEnd = {
                        if (scale == 1f) {
                            val velocity = velocityTracker.calculateVelocity().y
                            if (abs(swipeOffset) > 200 || abs(velocity) > 1200f) {
                                onSwipeDown()
                            } else {
                                swipeOffset = 0f
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.height(400.dp),
            contentScale = ContentScale.Crop
        )
    }
}