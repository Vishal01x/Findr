package com.exa.android.reflekt.loopit.presentation.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.loopit.data.feature.authentication.vm.AuthVM
import com.exa.android.reflekt.loopit.presentation.auth.components.AnimatedAuthBackground

@Composable
fun SplashScreen(
    viewModel: AuthVM = hiltViewModel(),
    onLoggedIn: () -> Unit,
    onNeedsProfileSetup: () -> Unit,
    onNotLoggedIn: () -> Unit
) {
    val state = viewModel.loginState.value

    // Once the auth check is done, navigate immediately — no delay
    LaunchedEffect(state.isCheckingAuth) {
        if (!state.isCheckingAuth) {
            when {
                state.loginSuccess -> onLoggedIn()
                state.needsProfileSetup -> onNeedsProfileSetup()
                else -> onNotLoggedIn()
            }
        }
    }

    // ─── Branded Splash UI ───
    AnimatedAuthBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val pulse = rememberInfiniteTransition("splash_pulse")
                val scale by pulse.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(
                        tween(1200, easing = FastOutSlowInEasing),
                        RepeatMode.Reverse
                    ),
                    label = "scale"
                )

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎓", fontSize = 52.sp)
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    "Findr",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    "Your campus. Your network.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}
