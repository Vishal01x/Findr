package com.exa.android.reflekt.loopit.presentation.main.profile.feedback


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.EditProfileViewModel


@Composable
fun ProfileViews(userId: String?, viewModel: EditProfileViewModel) {
    // Observe the profile views from the ViewModel
    val profileViews by viewModel.profileViews.observeAsState(0)

    // Call the ViewModel to fetch the profile views
    LaunchedEffect(userId) {
        viewModel.getProfileView(userId)
    }

    if(profileViews > 0) {

        Card(
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiary)
        ) {
            Column(Modifier.padding(16.dp)) {
                // Title + Edit Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Profile Views",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onTertiary
                    )


                    Spacer(Modifier.weight(1f))

                    Icon(
                        imageVector = Icons.Default.RemoveRedEye,
                        contentDescription = "seen",
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.width(2.dp))

                    Text(
                        text = profileViews.toString(),
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/*
@Composable
fun ProfileViews(userId: String?, viewModel: EditProfileViewModel) {
    val profileViews by viewModel.profileViews.observeAsState(0)
    var previousViews by remember { mutableStateOf(0) }
    val animatedCount = animateIntAsState(targetValue = profileViews, label = "viewCounter")
    val infiniteTransition = rememberInfiniteTransition()

    LaunchedEffect(userId) {
        userId?.let { viewModel.getProfileView(it) }
    }

    // Bouncing flame animation
    val bounce by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "bounceAnimation"
    )

    // Progress animation
    val progress by animateFloatAsState(
        targetValue = (profileViews % 1000) / 1000f,
        label = "progressAnimation",
        animationSpec = tween(500)
    )

    Card(
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            // Animated Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Bouncing flame icon
                    Icon(
                        painter = painterResource(R.drawable.ic_fire),
                        contentDescription = "Views",
                        tint = Color.Red,
                        modifier = Modifier
                            .size(32.dp)
                            .scale(bounce)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Scaling number animation
                    AnimatedContent(
                        targetState = animatedCount.value,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInVertically { height -> height } +
                                        fadeIn() with slideOutVertically { height -> -height } +
                                        fadeOut()
                            } else {
                                slideInVertically { height -> -height } +
                                        fadeIn() with slideOutVertically { height -> height } +
                                        fadeOut()
                            }.using(SizeTransform(clip = false))
                        }, label = "viewCountAnimation"
                    ) { targetCount ->
                        Text(
                            text = "$targetCount",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                background = Brush.horizontal(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            ),
                            color = Color.White,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .graphicsLayer {
                                    scaleX = 1 + (progress * 0.2f).coerceIn(0f, 0.2f)
                                    scaleY = 1 + (progress * 0.2f).coerceIn(0f, 0.2f)
                                }
                        )
                    }
                }

                // Rotating star
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing)
                    ), label = "starRotation"
                )

                Icon(
                    painter = painterResource(R.drawable.ic_star),
                    contentDescription = "Achievement",
                    tint = Color.Yellow,
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(rotation)
                )
            }

            // Progress bar with sparkles
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                // Animated progress
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontal(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clip(RoundedCornerShape(8.dp))
                )

                // Floating sparkles animation
                if (progress > 0.7f) {
                    val sparkleOffset by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = LinearEasing)
                        ), label = "sparkleAnimation"
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color.Yellow.copy(alpha = 0.7f),
                            radius = 4.dp.toPx(),
                            center = Offset(
                                x = size.width * sparkleOffset,
                                y = size.height / 2
                            )
                        )
                    }
                }
            }

            // Achievement text
            if (profileViews > previousViews) {
                LaunchedEffect(profileViews) {
                    previousViews = profileViews
                }

                Text(
                    text = when {
                        profileViews % 1000 == 0 -> "🔥 FIRE! ${profileViews / 1000}k views! 🔥"
                        profileViews % 500 == 0 -> "🚀 Halfway to next milestone!"
                        profileViews % 100 == 0 -> "🎉 100 Views Reached!"
                        else -> "👀 People are watching!"
                    },
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }
    }
}
*/





/*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.EditProfileViewModel
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.with
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color.Companion.Yellow
import androidx.compose.ui.graphics.Color.Companion.Red

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProfileViews(userId: String?, viewModel: EditProfileViewModel) {
    val profileViews by viewModel.profileViews.observeAsState(0)
    var previousViews by remember { mutableStateOf(20) }
    val animatedCount = animateIntAsState(targetValue = profileViews, label = "viewCounter")
    val infiniteTransition = rememberInfiniteTransition()

    LaunchedEffect(userId) {
        userId?.let { viewModel.getProfileView(it) }
    }

    // Bouncing flame animation
    val bounce by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "bounceAnimation"
    )

    // Progress animation
    val progress by animateFloatAsState(
        targetValue = (profileViews % 1000) / 1000f,
        label = "progressAnimation",
        animationSpec = tween(500)
    )

    Card(
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Animated Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Bouncing flame icon
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Views",
                        tint = Red,
                        modifier = Modifier
                            .size(32.dp)
                            .scale(bounce)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Scaling number animation
                    AnimatedContent(
                        targetState = animatedCount.value,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInVertically { height -> height } +
                                        fadeIn() with slideOutVertically { height -> -height } +
                                        fadeOut()
                            } else {
                                slideInVertically { height -> -height } +
                                        fadeIn() with slideOutVertically { height -> height } +
                                        fadeOut()
                            }.using(SizeTransform(clip = false))
                        }, label = "viewCountAnimation"
                    ) { targetCount ->
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                )
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "$targetCount",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color.White,
                                modifier = Modifier
                                    .graphicsLayer {
                                        scaleX = 1 + (progress * 0.2f).coerceIn(0f, 0.2f)
                                        scaleY = 1 + (progress * 0.2f).coerceIn(0f, 0.2f)
                                    }
                            )
                        }
                    }
                }

                // Rotating star
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing)
                    ), label = "starRotation"
                )

                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Achievement",
                    tint = Yellow,
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(rotation)
                )
            }

            // Progress bar with sparkles
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                // Animated progress
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clip(RoundedCornerShape(8.dp))
                )

                // Floating sparkles animation
                if (progress > 0.7f) {
                    val sparkleOffset by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = LinearEasing)
                        ), label = "sparkleAnimation"
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Yellow.copy(alpha = 0.7f),
                            radius = 4.dp.toPx(),
                            center = Offset(
                                x = size.width * sparkleOffset,
                                y = size.height / 2
                            )
                        )
                    }
                }
            }

            // Achievement text
            if (profileViews > previousViews) {
                LaunchedEffect(profileViews) {
                    previousViews = profileViews
                }

                Text(
                    text = when {
                        profileViews % 1000 == 0 -> "🔥 FIRE! ${profileViews / 1000}k views! 🔥"
                        profileViews % 500 == 0 -> "🚀 Halfway to next milestone!"
                        profileViews % 100 == 0 -> "🎉 100 Views Reached!"
                        else -> "👀 People are watching!"
                    },
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }
    }
}
*/