package com.exa.android.reflekt.loopit.presentation.auth

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.feature.authentication.vm.AuthVM
import com.exa.android.reflekt.loopit.data.feature.authentication.vm.LoginEvent
import com.exa.android.reflekt.loopit.presentation.auth.components.AnimatedAuthBackground
import com.exa.android.reflekt.loopit.presentation.auth.components.AuthTextField
import com.exa.android.reflekt.loopit.presentation.auth.components.GoogleSignInButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    viewModel: AuthVM = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onNeedsProfileSetup: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val state = viewModel.loginState.value
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()

    // Google Sign-In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { viewModel.onLoginEvent(LoginEvent.GoogleSignIn(it)) }
            } catch (e: ApiException) {
                Toast.makeText(context, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(state.loginSuccess) { if (state.loginSuccess) onLoginSuccess() }
    LaunchedEffect(state.needsProfileSetup) { if (state.needsProfileSetup) onNeedsProfileSetup() }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.onLoginEvent(LoginEvent.ClearError)
        }
    }

    // Staggered entrance
    val logoAlpha = remember { Animatable(0f) }
    val formAlpha = remember { Animatable(0f) }
    val formSlide = remember { Animatable(40f) }
    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(250)
        formAlpha.animateTo(1f, tween(500))
        formSlide.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
    }

    AnimatedAuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 28.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.12f))

            // ─── Branding ───
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer { alpha = logoAlpha.value }
            ) {
                val pulse = rememberInfiniteTransition("pulse")
                val s by pulse.animateFloat(
                    1f, 1.06f,
                    infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                    "s"
                )
                // Gradient circle behind emoji
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .scale(s)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎓", fontSize = 44.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Findr",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Your campus. Your network.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    letterSpacing = 0.3.sp
                )
            }

            Spacer(modifier = Modifier.weight(0.08f))

            // ─── Sign-In Section ───
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = formAlpha.value
                        translationY = formSlide.value
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Social sign-in first (primary action)
                GoogleSignInButton(
                    onClick = {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(context.getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build()
                        val client = GoogleSignIn.getClient(context, gso)
                        client.signOut().addOnCompleteListener {
                            googleSignInLauncher.launch(client.signInIntent)
                        }
                    },
                    isLoading = state.isGoogleLoading
                )

                Spacer(modifier = Modifier.height(24.dp))

                // OR divider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        "  or sign in with email  ",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Email
                AuthTextField(
                    value = state.email,
                    onValueChange = { viewModel.onLoginEvent(LoginEvent.EmailChanged(it)) },
                    label = "Email address",
                    leadingIcon = Icons.Default.Email,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password
                AuthTextField(
                    value = state.password,
                    onValueChange = { viewModel.onLoginEvent(LoginEvent.PasswordChanged(it)) },
                    label = "Password",
                    leadingIcon = Icons.Default.Lock,
                    visualTransformation = if (state.passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = {
                            viewModel.onLoginEvent(LoginEvent.TogglePasswordVisibility)
                        }) {
                            Icon(
                                if (state.passwordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.onLoginEvent(LoginEvent.Submit)
                        }
                    )
                )

                // Forgot password
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, end = 4.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        "Forgot password?",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onNavigateToForgotPassword() }
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Sign-In button
                val canLogin = state.email.isNotBlank() && state.password.isNotBlank()
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.onLoginEvent(LoginEvent.Submit)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = canLogin && !state.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Sign In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.06f))

            // ─── Register link ───
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .graphicsLayer { alpha = formAlpha.value }
            ) {
                Text(
                    "Don't have an account? ",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    "Sign Up",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onNavigateToRegister() }
                )
            }
        }
    }
}