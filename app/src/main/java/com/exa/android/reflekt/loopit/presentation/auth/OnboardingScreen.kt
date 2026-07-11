package com.exa.android.reflekt.loopit.presentation.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exa.android.reflekt.R
import kotlinx.coroutines.launch
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidMeshBackground

// ─── Data ─────────────────────────────────────────────────────────────────────

data class OnboardingData(
    val titlePlain: String,
    val titleHighlight: String,
    val description: String,
    val illustration: IllustrationKind
)

enum class IllustrationKind { COLLABORATE, CAREER, COMMUNITY }

// ─── Entry point ──────────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(onCompleted: () -> Unit) {
    val pages = listOf(
        OnboardingData(
            titlePlain      = "Collaborate ",
            titleHighlight  = "& Build",
            description     = "Connect with peers to work on projects, solve bugs, and organize campus events together.",
            illustration    = IllustrationKind.COLLABORATE
        ),
        OnboardingData(
            titlePlain      = "Accelerate Your\n",
            titleHighlight  = "Career",
            description     = "Stay updated with placement news, track company timelines, and get AI-powered eligibility insights.",
            illustration    = IllustrationKind.CAREER
        ),
        OnboardingData(
            titlePlain      = "Find Your ",
            titleHighlight  = "Community",
            description     = "Discover students nearby, filter by skills or year, and network within your campus.",
            illustration    = IllustrationKind.COMMUNITY
        )
    )
    OnboardingScreenContent(pages = pages, onCompleted = onCompleted)
}

// ─── Screen content ───────────────────────────────────────────────────────────

@Composable
fun OnboardingScreenContent(
    pages: List<OnboardingData>,
    onCompleted: () -> Unit
) {
    val pagerState  = rememberPagerState(pageCount = { pages.size })
    val scope       = rememberCoroutineScope()
    val isLastPage  = pagerState.currentPage == pages.size - 1
    val currentPage = pages[pagerState.currentPage]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LiquidColors.BgBase)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Skip ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (!isLastPage) {
                    TextButton(onClick = onCompleted) {
                        Text(
                            "Skip",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // ── Illustration pager ────────────────────────────────────────
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                userScrollEnabled = true
            ) { index ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (pages[index].illustration) {
                        IllustrationKind.COLLABORATE -> CollaborateIllustration()
                        IllustrationKind.CAREER      -> CareerIllustration()
                        IllustrationKind.COMMUNITY   -> CommunityIllustration()
                    }
                }
            }

            // ── Text + dots + button ──────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val titleAlpha = remember { Animatable(1f) }
                LaunchedEffect(pagerState.currentPage) {
                    titleAlpha.snapTo(0f)
                    titleAlpha.animateTo(1f, tween(300))
                }

                // Title: white plain + cyan highlight
                Row(modifier = Modifier.graphicsLayer { alpha = titleAlpha.value }) {
                    Text(
                        text = currentPage.titlePlain,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = currentPage.titleHighlight,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = currentPage.description,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { alpha = titleAlpha.value }
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Pill dots
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 28.dp else 8.dp,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "dot_w"
                        )
                        val color by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            animationSpec = tween(300),
                            label = "dot_c"
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

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (isLastPage) onCompleted()
                        else scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor   = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = if (isLastPage) "Get Started" else "Next",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ─── Illustration 1 – Collaborate & Build ─────────────────────────────────────

@Composable
private fun CollaborateIllustration() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Rounded card that holds the image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.onboarding_collaborate),
                contentDescription = "Students collaborating",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        // Gradient fade from transparent → background at the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background)
                    )
                )
        )
    }
}

// ─── Illustration 2 – Accelerate Your Career ──────────────────────────────────

@Composable
private fun CareerIllustration() {
    // Pulsing glow behind the whole composition
    val pulse = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by pulse.animateFloat(
        initialValue = 0.18f, targetValue = 0.45f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background glow blob
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.Center)
                .graphicsLayer { alpha = glowAlpha }
                .background(
                    Brush.radialGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), Color.Transparent)),
                    shape = CircleShape
                )
        )

        // Main student image — rounded, rotated +3°, greyscale+high-contrast
        // with a dark gradient overlay fading from bottom
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.75f)
                .graphicsLayer { rotationZ = 3f }
                .clip(RoundedCornerShape(20.dp))
                .border(
                    2.dp,
                    MaterialTheme.colorScheme.background,
                    RoundedCornerShape(20.dp)
                )
        ) {
            // Grayscale + increased contrast via ColorMatrix
            val grayscaleContrast = ColorMatrix().apply {
                setToSaturation(0f)  // desaturate → greyscale
            }
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.onboarding_career),
                contentDescription = "Career dashboard",
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.colorMatrix(grayscaleContrast),
                modifier = Modifier.fillMaxSize()
            )
            // Dark gradient overlay: transparent → dark from bottom 50%
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.5f to Color.Transparent,
                                1.0f to MaterialTheme.colorScheme.background.copy(alpha = 0.85f)
                            )
                        )
                    )
            )
        }

        // Floating PLACEMENT card — top-left, bounces vertically
        PlacementCard(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 0.dp, y = 24.dp)
        )

        // Floating Eligible card — bottom-right, bounces vertically
        EligibleCard(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 0.dp, y = (-24).dp)
        )
    }
}

@Composable
private fun PlacementCard(modifier: Modifier = Modifier) {
    val bounce = rememberInfiniteTransition(label = "b1")
    val ty by bounce.animateFloat(
        initialValue = 0f, targetValue = -8f,
        animationSpec = infiniteRepeatable(tween(700, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "ty1"
    )
    Box(
        modifier = modifier
            .graphicsLayer { translationY = ty }
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                Spacer(modifier = Modifier.width(6.dp))
                Text("PLACEMENT", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(0.4f to Color(0xFF374151), 0.6f to Color(0xFF4B5563),
                    0.5f to Color(0xFF6B7280), 1.0f to MaterialTheme.colorScheme.primary).forEach { (h, c) ->
                    Box(
                        modifier = Modifier
                            .width(14.dp)
                            .height((h * 32).dp)
                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                            .background(c)
                    )
                }
            }
        }
    }
}

@Composable
private fun EligibleCard(modifier: Modifier = Modifier) {
    val bounce = rememberInfiniteTransition(label = "b2")
    val ty by bounce.animateFloat(
        initialValue = 0f, targetValue = -8f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "ty2"
    )
    Box(
        modifier = modifier
            .graphicsLayer { translationY = ty }
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Verified, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Eligible", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                listOf(Color.White to "G", MaterialTheme.colorScheme.primary to "M", Color(0xFF6366F1) to "A").forEach { (bg, letter) ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(bg)
                            .border(2.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(letter, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                            color = if (bg == Color.White) Color.Black else Color.White)
                    }
                }
            }
        }
    }
}

// ─── Illustration 3 – Find Your Community ─────────────────────────────────────

@Composable
private fun CommunityIllustration() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f),
        contentAlignment = Alignment.Center
    ) {
        val infinite = rememberInfiniteTransition(label = "ring")
        val ringAlpha by infinite.animateFloat(
            initialValue = 0.25f, targetValue = 0.55f,
            animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
            label = "ring_alpha"
        )

        // Concentric rings
        listOf(320.dp, 250.dp, 175.dp).forEachIndexed { i, size ->
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = if (i == 1) MaterialTheme.colorScheme.primary.copy(alpha = ringAlpha)
                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
            )
        }

        val boxColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)

        // Dot pattern fill inside innermost ring
        Box(
            modifier = Modifier
                .size(173.dp)
                .clip(CircleShape)
                .drawBehind {
                    val spacing = 24.dp.toPx()
                    val dotR = 1.5.dp.toPx()
                    val cols = (size.width / spacing).toInt() + 2
                    val rows = (size.height / spacing).toInt() + 2
                    for (r in 0..rows) for (c in 0..cols) {
                        drawCircle(
                            color = boxColor,
                            radius = dotR,
                            center = Offset(c * spacing, r * spacing)
                        )
                    }
                }
        )

        // Centre pin + ripple
        val rippleScale by infinite.animateFloat(
            initialValue = 1f, targetValue = 1.6f,
            animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Restart),
            label = "ripple"
        )
        val rippleAlpha by infinite.animateFloat(
            initialValue = 0.5f, targetValue = 0f,
            animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Restart),
            label = "ripple_a"
        )
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .graphicsLayer { scaleX = rippleScale; scaleY = rippleScale; alpha = rippleAlpha }
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(3.dp, MaterialTheme.colorScheme.background, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(26.dp))
            }
        }

        // Floating avatars
        AvatarPin(color = Color(0xFF3B82F6), badgeColor = Color(0xFF22C55E),
            badgeIcon = BadgeIcon.CODE,   size = 50.dp,
            modifier = Modifier.offset(x = 90.dp, y = (-80).dp), bounceDelay = 0)
        AvatarPin(color = Color(0xFFEC4899), badgeColor = Color(0xFF8B5CF6),
            badgeIcon = BadgeIcon.BRUSH,  size = 46.dp,
            modifier = Modifier.offset(x = (-100).dp, y = 76.dp), bounceDelay = 300)
        AvatarPin(color = Color(0xFF6B7280), badgeColor = Color.Transparent,
            badgeIcon = null,             size = 34.dp,  alpha = 0.7f,
            modifier = Modifier.offset(x = (-90).dp, y = (-58).dp), bounceDelay = 600)
        AvatarPin(color = Color(0xFF059669), badgeColor = Color(0xFFEAB308),
            badgeIcon = BadgeIcon.SCHOOL, size = 42.dp,
            modifier = Modifier.offset(x = 88.dp, y = 70.dp), bounceDelay = 150)
    }
}

private enum class BadgeIcon { CODE, BRUSH, SCHOOL }

@Composable
private fun AvatarPin(
    color: Color,
    badgeColor: Color,
    badgeIcon: BadgeIcon?,
    size: Dp,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    bounceDelay: Int = 0
) {
    val bounce = rememberInfiniteTransition(label = "av_bounce")
    val ty by bounce.animateFloat(
        initialValue = 0f, targetValue = -7f,
        animationSpec = infiniteRepeatable(
            tween(800 + bounceDelay, easing = EaseInOutSine), RepeatMode.Reverse
        ),
        label = "av_ty"
    )
    Box(
        modifier = modifier
            .graphicsLayer { translationY = ty; this.alpha = alpha },
        contentAlignment = Alignment.BottomEnd
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color)
                .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(size * 0.5f))
        }
        if (badgeIcon != null && badgeColor != Color.Transparent) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
                    .border(1.5.dp, MaterialTheme.colorScheme.background, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (badgeIcon) {
                        BadgeIcon.CODE   -> Icons.Default.Code
                        BadgeIcon.BRUSH  -> Icons.Default.Brush
                        BadgeIcon.SCHOOL -> Icons.Default.School
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(8.dp)
                )
            }
        }
    }
}

private val EaseInOutSine = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF101D22, widthDp = 390, heightDp = 844)
@Composable
fun PreviewOnboardingScreenContent() {
    MaterialTheme {
        OnboardingScreen(onCompleted = {})
    }
}
