package com.exa.android.reflekt.loopit.presentation.auth

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import com.exa.android.reflekt.loopit.data.feature.authentication.vm.AuthVM
import com.exa.android.reflekt.loopit.presentation.auth.components.AnimatedAuthBackground
import com.exa.android.reflekt.loopit.presentation.auth.components.GlassCard
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors

@Composable
fun VerificationEmailScreen(
    viewModel: AuthVM = hiltViewModel(),
    onVerificationComplete: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state = viewModel.verificationState.value
    val currentUser = viewModel.repository.getCurrentUser()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val logoAlpha = remember { Animatable(0f) }
    val formAlpha = remember { Animatable(0f) }
    val formSlide = remember { Animatable(40f) }

    LaunchedEffect(Unit) {
        viewModel.checkEmailVerification {
            onVerificationComplete()
        }
        logoAlpha.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        kotlinx.coroutines.delay(150)
        formAlpha.animateTo(1f, tween(500))
        formSlide.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    AnimatedAuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // ── Branding / Icon ──────────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer { alpha = logoAlpha.value }
            ) {
                // Icon box
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF13B6EC), Color(0xFF1E5FFF))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Verify Email",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "Verify Your Email",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LiquidColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "You're almost there! We've sent an\nemail to confirm your account.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = LiquidColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── Glass Card ────────────────────────────────────────────────────
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = formAlpha.value
                        translationY = formSlide.value
                    },
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentUser?.email ?: "",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiquidColors.TextPrimary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Message about Inbox and Spam with icons
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = "Inbox",
                            tint = LiquidColors.TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Check Inbox",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = LiquidColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "or",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = LiquidColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Spam Folder",
                            tint = LiquidColors.TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Spam folder",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = LiquidColors.TextSecondary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_APP_EMAIL)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(
                                    context,
                                    "No email app found. Please install one to verify your email.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        enabled = !state.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LiquidColors.Primary,
                            contentColor = Color.White,
                            disabledContainerColor = LiquidColors.Primary.copy(alpha = 0.4f),
                            disabledContentColor = Color.White.copy(alpha = 0.6f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 0.dp
                        )
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text("Open Email App", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        modifier = Modifier
                            .clickableWithNoIndication { viewModel.resendVerificationEmail() }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = "Resend Email",
                            tint = LiquidColors.Primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Resend Verification Email",
                            fontSize = 14.sp,
                            color = LiquidColors.Primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            Text(
                "Back to Login",
                fontSize = 14.sp,
                color = LiquidColors.Primary,
                modifier = Modifier
                    .graphicsLayer { alpha = formAlpha.value }
                    .clickableWithNoIndication { onNavigateBack() }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}