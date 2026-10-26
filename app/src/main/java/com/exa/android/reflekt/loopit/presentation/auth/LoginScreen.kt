package com.exa.android.reflekt.loopit.presentation.auth

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.core.env.EnvironmentManager
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.data.feature.authentication.vm.AuthVM
import com.exa.android.reflekt.loopit.data.feature.authentication.vm.LoginEvent
import com.exa.android.reflekt.loopit.data.feature.authentication.vm.LoginState
import com.exa.android.reflekt.loopit.presentation.auth.components.AnimatedAuthBackground
import com.exa.android.reflekt.loopit.presentation.auth.components.AuthTextField
import com.exa.android.reflekt.loopit.presentation.auth.components.GlassCard
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: AuthVM = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onNeedsProfileSetup: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val state by viewModel.loginState
    val context = LocalContext.current

    // ── Notification permission launcher (Android 13+) ──────────────────────
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Proceed to home regardless of whether permission was granted or denied
        onLoginSuccess()
    }

    val scope = rememberCoroutineScope()
    val webClientId = stringResource(R.string.default_web_client_id)

    LaunchedEffect(state.loginSuccess) {
        if (state.loginSuccess) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val isGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (isGranted) {
                    // Permission already granted — go straight to home
                    onLoginSuccess()
                } else {
                    // Ask for push (chat) notification permission
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                // Below Android 13 permissions are granted at install time
                onLoginSuccess()
            }
        }
    }
    LaunchedEffect(state.needsProfileSetup) { if (state.needsProfileSetup) onNeedsProfileSetup() }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.onLoginEvent(LoginEvent.ClearError)
        }
    }

    LoginScreenContent(
        state = state,
        onEvent = viewModel::onLoginEvent,
        showGoogleSignIn = !EnvironmentManager.isStaging,
        onGoogleSignInClick = {
            scope.launch {
                val option = GetSignInWithGoogleOption.Builder(webClientId).build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(option)
                    .build()
                try {
                    val credential = CredentialManager.create(context)
                        .getCredential(context, request)
                        .credential
                    if (credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                        viewModel.onLoginEvent(LoginEvent.GoogleSignIn(idToken))
                    }
                } catch (_: GetCredentialCancellationException) {
                    // User dismissed the account picker
                } catch (e: GetCredentialException) {
                    Toast.makeText(context, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        },
        onNavigateToRegister = onNavigateToRegister,
        onNavigateToForgotPassword = onNavigateToForgotPassword
    )
}

@Composable
fun LoginScreenContent(
    state: LoginState,
    onEvent: (LoginEvent) -> Unit,
    showGoogleSignIn: Boolean = true,
    onGoogleSignInClick: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val logoAlpha = remember { Animatable(0f) }
    val formAlpha = remember { Animatable(0f) }
    val formSlide = remember { Animatable(40f) }

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        kotlinx.coroutines.delay(150)
        formAlpha.animateTo(1f, tween(500))
        formSlide.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
    }

    AnimatedAuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // ── Branding ──────────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer { alpha = logoAlpha.value }
            ) {
                // Logo icon box
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
                        imageVector = Icons.Default.School,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // "CampusConnect"
                Row {
                    Text(
                        "Findr",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LiquidColors.TextPrimary
                    )
//                    Text(
//                        "Connect",
//                        fontSize = 30.sp,
//                        fontWeight = FontWeight.ExtraBold,
//                        color = LiquidColors.Primary
//                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Connect. Collaborate.\nGet Hired.",
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
                    if (showGoogleSignIn) {
                        // ── Google button (top of card) ──────────────────────────
                        Button(
                            onClick = onGoogleSignInClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF1F2937)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            if (state.isGoogleLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color = LiquidColors.Primary
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.google),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Continue with Google",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1F2937)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            Text(
                                "  or continue with email  ",
                                color = LiquidColors.TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // ── Email field ──────────────────────────────────────────
                    AuthTextField(
                        value = state.email,
                        onValueChange = { onEvent(LoginEvent.EmailChanged(it)) },
                        label = "Email",
                        leadingIcon = Icons.Default.Email,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── Password field ───────────────────────────────────────
                    AuthTextField(
                        value = state.password,
                        onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
                        label = "Password",
                        leadingIcon = Icons.Default.Lock,
                        visualTransformation = if (state.passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { onEvent(LoginEvent.TogglePasswordVisibility) }) {
                                Icon(
                                    imageVector = if (state.passwordVisible) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = LiquidColors.TextSecondary
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
                                onEvent(LoginEvent.Submit)
                            }
                        )
                    )

                    // ── Forgot password ──────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "Forgot password?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LiquidColors.Primary,
                            modifier = Modifier.clickableWithNoIndication { onNavigateToForgotPassword() }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Sign In button ───────────────────────────────────────
                    val canLogin = state.email.isNotBlank() && state.password.isNotBlank()
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onEvent(LoginEvent.Submit)
                        },
                        enabled = canLogin && !state.isLoading,
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
                            Text(
                                "Sign In",
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

            Spacer(modifier = Modifier.height(28.dp))

            // ── "New to Findr? Create an account" ─────────────────────
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.graphicsLayer { alpha = formAlpha.value }
            ) {
                Text(
                    "New to Findr? ",
                    color = LiquidColors.TextSecondary
                )
                Text(
                    "Create an account",
                    fontSize = 14.sp,
                    color = LiquidColors.Primary,
                    modifier = Modifier.clickableWithNoIndication { onNavigateToRegister() }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Trust badges ──────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .graphicsLayer { alpha = formAlpha.value * 0.5f }
                    .padding(bottom = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = LiquidColors.TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = LiquidColors.TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = LiquidColors.TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF101D22)
@Composable
fun PreviewLoginScreenContent() {
    MaterialTheme {
        LoginScreenContent(
            state = LoginState(),
            onEvent = {},
            onGoogleSignInClick = {},
            onNavigateToRegister = {},
            onNavigateToForgotPassword = {}
        )
    }
}