package com.exa.android.reflekt.loopit.presentation.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.sin

data class OnboardingPage(
    val title: String,
    val description: String,
    val emoji: String,
    val accentColor: Color
)

@Composable
fun OnboardingScreen(
    onCompleted: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Connect with Campus",
            description = "Find friends, classmates, and study groups from your college. Build your campus network effortlessly.",
            emoji = "🎓",
            accentColor = Color(0xFF6C63FF)
        ),
        OnboardingPage(
            title = "Discover Opportunities",
            description = "Stay updated with events, clubs, hackathons, and internship opportunities curated for you.",
            emoji = "🚀",
            accentColor = Color(0xFF00BFA5)
        ),
        OnboardingPage(
            title = "Collaborate & Grow",
            description = "Team up on projects, share ideas, and build something amazing together with fellow students.",
            emoji = "💡",
            accentColor = Color(0xFFFF6B6B)
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.size - 1

    val infiniteTransition = rememberInfiniteTransition(label = "onboarding_bg")
    val bgPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg_phase"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Animated background blobs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val currentColor = pages[pagerState.currentPage].accentColor

            // Top wave
            val path = Path()
            for (i in 0..100) {
                val x = w * i / 100
                val y = h * 0.12f + sin(Math.toRadians((i * 3.6 + bgPhase).toDouble())).toFloat() * 30f
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.lineTo(w, 0f)
            path.lineTo(0f, 0f)
            path.close()
            drawPath(path, currentColor.copy(alpha = 0.08f))

            // Bottom curved blob
            val path2 = Path()
            for (i in 0..100) {
                val x = w * i / 100
                val y = h * 0.88f + sin(Math.toRadians((i * 3.6 + bgPhase + 180).toDouble())).toFloat() * 25f
                if (i == 0) path2.moveTo(x, y) else path2.lineTo(x, y)
            }
            path2.lineTo(w, h)
            path2.lineTo(0f, h)
            path2.close()
            drawPath(path2, currentColor.copy(alpha = 0.06f))

            // Floating decorative circles
            val dotY = h * 0.2f + sin(Math.toRadians(bgPhase.toDouble())).toFloat() * 20f
            drawCircle(currentColor.copy(alpha = 0.1f), radius = 50f, center = Offset(w * 0.85f, dotY))

            val dotY2 = h * 0.75f + sin(Math.toRadians((bgPhase + 90).toDouble())).toFloat() * 15f
            drawCircle(currentColor.copy(alpha = 0.08f), radius = 35f, center = Offset(w * 0.1f, dotY2))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Skip button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (!isLastPage) {
                    TextButton(onClick = onCompleted) {
                        Text(
                            "Skip",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(0.6f)
            ) { page ->
                OnboardingPageContent(pages[page])
            }

            Spacer(modifier = Modifier.weight(0.05f))

            // Page indicators
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 28.dp else 8.dp,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "indicator_width"
                    )
                    val color by animateColorAsState(
                        targetValue = if (isSelected) pages[pagerState.currentPage].accentColor
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        animationSpec = tween(300),
                        label = "indicator_color"
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.05f))

            // Action button
            val buttonScale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "btn_scale"
            )

            Button(
                onClick = {
                    if (isLastPage) {
                        onCompleted()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .scale(buttonScale),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = pages[pagerState.currentPage].accentColor
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                AnimatedContent(
                    targetState = isLastPage,
                    transitionSpec = {
                        fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                    },
                    label = "btn_content"
                ) { last ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            if (last) "Get Started" else "Next",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    val appear = remember { Animatable(0f) }
    LaunchedEffect(page) {
        appear.snapTo(0f)
        appear.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Emoji illustration
        val emojiScale by animateFloatAsState(
            targetValue = appear.value,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "emoji_scale"
        )

        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(emojiScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            page.accentColor.copy(alpha = 0.15f),
                            page.accentColor.copy(alpha = 0.05f),
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = page.emoji,
                fontSize = 64.sp
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer { alpha = appear.value }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .graphicsLayer { alpha = appear.value }
        )
    }
}
