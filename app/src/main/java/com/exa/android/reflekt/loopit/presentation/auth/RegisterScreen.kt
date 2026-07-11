package com.exa.android.reflekt.loopit.presentation.auth

import android.util.Patterns
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.loopit.data.feature.authentication.vm.AuthVM
import com.exa.android.reflekt.loopit.data.feature.authentication.vm.SignUpEvent
import com.exa.android.reflekt.loopit.data.feature.authentication.vm.SignUpState
import kotlinx.coroutines.delay
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidMeshBackground

// ─── Entry ────────────────────────────────────────────────────────────────────

@Composable
fun SignUpScreen(
    viewModel: AuthVM = hiltViewModel(),
    onSignUpSuccess: () -> Unit,
    onTermsClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state   by viewModel.signUpState

    LaunchedEffect(state.signUpSuccess) { if (state.signUpSuccess) onSignUpSuccess() }

    SignUpScreenContent(
        state          = state,
        onEvent        = viewModel::onSignUpEvent,
        onTermsClick   = onTermsClick,
        onNavigateBack = onNavigateBack
    )
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SignUpScreenContent(
    state: SignUpState,
    onEvent: (SignUpEvent) -> Unit,
    onTermsClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps  = 2

    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarIsError by remember { mutableStateOf(false) }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarIsError = true
            snackbarHostState.showSnackbar(it)
            onEvent(SignUpEvent.ClearMessages)
        }
    }
    LaunchedEffect(state.infoMessage) {
        state.infoMessage?.let {
            snackbarIsError = false
            snackbarHostState.showSnackbar(it)
            onEvent(SignUpEvent.ClearMessages)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LiquidColors.BgBase)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {

            // ── Progress header ────────────────────────────────────────────
            RegistrationHeader(
                currentStep  = currentStep,
                totalSteps   = totalSteps,
                onSkip       = onNavigateBack
            )

            // ── Step content ───────────────────────────────────────────────
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState)
                        slideInHorizontally { it / 2 } + fadeIn() togetherWith
                                slideOutHorizontally { -it / 2 } + fadeOut()
                    else
                        slideInHorizontally { -it / 2 } + fadeIn() togetherWith
                                slideOutHorizontally { it / 2 } + fadeOut()
                },
                modifier = Modifier.weight(1f),
                label    = "step"
            ) { step ->
                when (step) {
                    0 -> PersonalStep(onEvent, state, focusManager)
                    1 -> DetailsStep(onEvent, state, focusManager, onTermsClick)
                }
            }

            // ── Bottom navigation ──────────────────────────────────────────
            RegistrationFooter(
                currentStep = currentStep,
                totalSteps  = totalSteps,
                isLoading   = state.isLoading,
                canProceed  = when (currentStep) {
                    0 -> state.fullName.isNotBlank() &&
                            state.email.isNotBlank() &&
                            state.password.length >= 8
                    1 -> if (state.isStudent) state.collegeName.isNotBlank()
                         else state.companyName.isNotBlank()
                    else -> true
                },
                onBack   = { if (currentStep > 0) currentStep-- else onNavigateBack() },
                onNext   = {
                    if (currentStep == 0 && !Patterns.EMAIL_ADDRESS.matcher(state.email.trim()).matches()) {
                        onEvent(SignUpEvent.EmailValidationFailed)
                    } else {
                        currentStep++
                    }
                },
                onSubmit = {
                    focusManager.clearFocus()
                    onEvent(SignUpEvent.Submit)
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) { data ->
            ThemedSnackbar(message = data.visuals.message, isError = snackbarIsError)
        }
    }
}

// ─── Themed Snackbar ────────────────────────────────────────────────────────

@Composable
private fun ThemedSnackbar(message: String, isError: Boolean) {
    val accent = if (isError) LiquidColors.Red else LiquidColors.Primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LiquidColors.GlassSurface)
            .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isError) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            message,
            fontSize = 14.sp,
            color = LiquidColors.TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─── Header ───────────────────────────────────────────────────────────────────

@Composable
private fun RegistrationHeader(
    currentStep: Int,
    totalSteps: Int,
    onSkip: () -> Unit
) {
    val fraction by animateFloatAsState(
        targetValue      = (currentStep + 1f) / totalSteps.toFloat(),
        animationSpec    = tween(400, easing = FastOutSlowInEasing),
        label            = "progress"
    )

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
        Row(
            modifier            = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment   = Alignment.CenterVertically
        ) {
            Text(
                "Step ${currentStep + 1} of $totalSteps",
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color      = LiquidColors.Primary
            )
            Text(
                "Skip",
                fontSize = 13.sp,
                color    = LiquidColors.TextSecondary,
                modifier = Modifier.clickableWithNoIndication { onSkip() }
            )
        }

        Spacer(Modifier.height(10.dp))

        // Full-width progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(CircleShape)
                .background(LiquidColors.Border)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .clip(CircleShape)
                    .background(LiquidColors.Primary)
            )
        }
    }
}

// ─── Step 1 – Personal Info ───────────────────────────────────────────────────

@Composable
private fun PersonalStep(
    onEvent: (SignUpEvent) -> Unit,
    state: SignUpState,
    fm: androidx.compose.ui.focus.FocusManager
) {
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = 24.dp)
            .imePadding()
    ) {
        Spacer(Modifier.height(8.dp))

        // Title — always visible, no stagger (prevents overlap)
        Text(
            "Create Account",
            fontSize   = 30.sp,
            fontWeight = FontWeight.Bold,
            color      = LiquidColors.TextPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Fill in your details to get started with Findr",
            fontSize   = 15.sp,
            color      = LiquidColors.TextSecondary,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(32.dp))

        // All fields in one Column — spacing handled here, not via stagger
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

            // Full Name
            Column {
                FieldLabel("Full Name")
                Spacer(Modifier.height(8.dp))
                RegTextField(
                    value         = state.fullName,
                    onValueChange = { onEvent(SignUpEvent.FullNameChanged(it)) },
                    placeholder   = "Your full name",
                    leadingIcon   = Icons.Default.Person,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction      = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { fm.moveFocus(FocusDirection.Down) })
                )
            }

            // Location
            Column {
                FieldLabel("Location")
                Spacer(Modifier.height(8.dp))
                RegTextField(
                    value         = state.location,
                    onValueChange = { onEvent(SignUpEvent.LocationChanged(it)) },
                    placeholder   = "City, Country",
                    leadingIcon   = Icons.Outlined.LocationOn,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction      = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { fm.moveFocus(FocusDirection.Down) })
                )
            }

            // Email
            Column {
                FieldLabel("Email")
                Spacer(Modifier.height(8.dp))
                RegTextField(
                    value         = state.email,
                    onValueChange = { onEvent(SignUpEvent.EmailChanged(it)) },
                    placeholder   = "you@google.com",
                    leadingIcon   = Icons.Default.Email,
                    isError       = state.emailError != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction    = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { fm.moveFocus(FocusDirection.Down) })
                )
                if (state.emailError != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        state.emailError,
                        fontSize = 12.sp,
                        color    = Color(0xFFEF4444)
                    )
                }
            }

            // Password
            Column {
                FieldLabel("Password")
                Spacer(Modifier.height(8.dp))
                RegTextField(
                    value         = state.password,
                    onValueChange = { onEvent(SignUpEvent.PasswordChanged(it)) },
                    placeholder   = "Min. 8 characters",
                    leadingIcon   = Icons.Default.Lock,
                    visualTransformation = if (state.passwordVisible) VisualTransformation.None
                                           else PasswordVisualTransformation(),
                    trailingIcon  = if (state.passwordVisible) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                    onTrailingClick = { onEvent(SignUpEvent.TogglePasswordVisibility) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction    = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { fm.clearFocus() })
                )
                // Strength bar always directly below the password field
                if (state.password.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    PasswordStrengthBar(state.password)
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ─── Step 2 – Academic / Professional ─────────────────────────────────────────

@Composable
private fun DetailsStep(
    onEvent: (SignUpEvent) -> Unit,
    state: SignUpState,
    fm: androidx.compose.ui.focus.FocusManager,
    onTermsClick: () -> Unit
) {
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = 24.dp)
            .imePadding()
    ) {
        Spacer(Modifier.height(8.dp))

        // Title — always visible, no stagger (prevents overlap)
        Text(
            if (state.isStudent) "Academic Info" else "Professional Info",
            fontSize   = 30.sp,
            fontWeight = FontWeight.Bold,
            color      = LiquidColors.TextPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            if (state.isStudent)
                "Tell us where you study so we can connect you with your peers."
            else
                "Tell us about your professional background.",
            fontSize   = 15.sp,
            color      = LiquidColors.TextSecondary,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(28.dp))

        // Student / Professional toggle card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(LiquidColors.GlassSurface)
                .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(14.dp))
                .clickable { onEvent(SignUpEvent.ToggleStudentStatus) }
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
                Row(
                    verticalAlignment      = Alignment.CenterVertically,
                    horizontalArrangement  = Arrangement.SpaceBetween,
                    modifier               = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(LiquidColors.Primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (state.isStudent) "🎓" else "💼", fontSize = 20.sp)
                        }
                        Spacer(Modifier.width(14.dp))
                        Text(
                            if (state.isStudent) "I'm a Student" else "I'm a Professional",
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 16.sp,
                            color      = LiquidColors.TextPrimary
                        )
                    }
                    Switch(
                        checked         = state.isStudent,
                        onCheckedChange = { onEvent(SignUpEvent.ToggleStudentStatus) },
                        colors          = SwitchDefaults.colors(
                            checkedThumbColor  = Color.White,
                            checkedTrackColor  = LiquidColors.Primary,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = LiquidColors.Border
                        )
                    )
                }
            }

        // Conditional fields
        AnimatedContent(
            targetState  = state.isStudent,
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
            label        = "fields"
        ) { isStudent ->
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                if (isStudent) StudentFields(onEvent, state, fm, startIdx = 2)
                else ProfessionalFields(onEvent, state, fm, startIdx = 2)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Terms
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "By creating an account, you agree to our ",
                fontSize = 12.sp,
                color    = LiquidColors.TextSecondary
            )
            Text(
                "Terms",
                fontSize   = 12.sp,
                color      = LiquidColors.Primary,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.clickableWithNoIndication { onTermsClick() }
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ─── Student fields ───────────────────────────────────────────────────────────

@Composable
private fun StudentFields(
    onEvent: (SignUpEvent) -> Unit,
    state: SignUpState,
    fm: androidx.compose.ui.focus.FocusManager,
    startIdx: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column {
            FieldLabel("College Name")
            Spacer(Modifier.height(8.dp))
            RegTextField(
                value         = state.collegeName,
                onValueChange = { onEvent(SignUpEvent.CollegeNameChanged(it)) },
                placeholder   = "Select your college",
                leadingIcon   = Icons.Outlined.School,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction      = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { fm.moveFocus(FocusDirection.Down) })
            )
        }

        Column {
            FieldLabel("Branch / Major")
            Spacer(Modifier.height(8.dp))
            RegTextField(
                value         = state.branch,
                onValueChange = { onEvent(SignUpEvent.BranchChanged(it)) },
                placeholder   = "Select your major",
                leadingIcon   = Icons.Outlined.AccountTree,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction      = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { fm.moveFocus(FocusDirection.Down) })
            )
        }

        Column {
            FieldLabel("Graduation Year")
            Spacer(Modifier.height(8.dp))
            RegTextField(
                value         = state.year,
                onValueChange = { onEvent(SignUpEvent.YearChanged(it)) },
                placeholder   = "Year",
                leadingIcon   = Icons.Outlined.DateRange,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { fm.clearFocus() })
            )
        }
    }
}

// ─── Professional fields ──────────────────────────────────────────────────────

@Composable
private fun ProfessionalFields(
    onEvent: (SignUpEvent) -> Unit,
    state: SignUpState,
    fm: androidx.compose.ui.focus.FocusManager,
    startIdx: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column {
            FieldLabel("Company Name")
            Spacer(Modifier.height(8.dp))
            RegTextField(
                value         = state.companyName,
                onValueChange = { onEvent(SignUpEvent.CompanyNameChanged(it)) },
                placeholder   = "Where do you work?",
                leadingIcon   = Icons.Outlined.Business,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction      = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { fm.moveFocus(FocusDirection.Down) })
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                FieldLabel("Experience (yrs)")
                Spacer(Modifier.height(8.dp))
                RegTextField(
                    value         = state.experience,
                    onValueChange = { onEvent(SignUpEvent.ExperienceChanged(it)) },
                    placeholder   = "e.g. 2",
                    leadingIcon   = Icons.Outlined.Timeline,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction    = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { fm.moveFocus(FocusDirection.Right) })
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                FieldLabel("CTC (LPA)")
                Spacer(Modifier.height(8.dp))
                RegTextField(
                    value         = state.ctc,
                    onValueChange = { onEvent(SignUpEvent.CtcChanged(it)) },
                    placeholder   = "e.g. 12",
                    leadingIcon   = Icons.Outlined.CurrencyRupee,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction    = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { fm.clearFocus() })
                )
            }
        }
    }
}

// ─── Reusable field ───────────────────────────────────────────────────────────

@Composable
private fun FieldLabel(text: String) {
    Text(
        text       = text,
        fontSize   = 14.sp,
        fontWeight = FontWeight.Medium,
        color      = LiquidColors.TextPrimary
    )
}

@Composable
private fun RegTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: ImageVector? = null,
    onTrailingClick: (() -> Unit)? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue   = if (isError) Color(0xFFEF4444) else if (isFocused) LiquidColors.Primary else LiquidColors.Border,
        animationSpec = tween(200),
        label         = "border"
    )

    BasicTextField(
        value                = value,
        onValueChange        = onValueChange,
        modifier             = modifier.fillMaxWidth(),
        singleLine           = true,
        textStyle            = androidx.compose.ui.text.TextStyle(
            color    = LiquidColors.TextPrimary,
            fontSize = 15.sp
        ),
        visualTransformation = visualTransformation,
        keyboardOptions      = keyboardOptions,
        keyboardActions      = keyboardActions,
        interactionSource    = interactionSource,
        cursorBrush          = androidx.compose.ui.graphics.SolidColor(LiquidColors.Primary),
        decorationBox        = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(LiquidColors.GlassSurface)
                    .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector     = leadingIcon,
                    contentDescription = null,
                    tint            = if (isFocused) LiquidColors.Primary else LiquidColors.TextSecondary,
                    modifier        = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(placeholder, color = LiquidColors.TextSecondary, fontSize = 15.sp)
                    }
                    innerTextField()
                }
                if (trailingIcon != null) {
                    IconButton(
                        onClick  = { onTrailingClick?.invoke() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector     = trailingIcon,
                            contentDescription = null,
                            tint            = LiquidColors.TextSecondary,
                            modifier        = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    )
}

// ─── Password strength bar ────────────────────────────────────────────────────

@Composable
private fun PasswordStrengthBar(password: String) {
    val strength = when {
        password.length >= 12 && password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() } -> 3
        password.length >= 8  && password.any { it.isDigit() } -> 2
        password.length >= 6 -> 1
        else -> 0
    }
    val label = arrayOf("Weak", "Fair", "Good", "Strong")[strength]
    val color = arrayOf(Color(0xFFEF4444), Color(0xFFFF9800), LiquidColors.Primary, Color(0xFF22C55E))[strength]

    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(4) { i ->
            Box(
                Modifier
                    .weight(1f)
                    .height(4.dp)
                    .padding(horizontal = 2.dp)
                    .clip(CircleShape)
                    .background(if (i <= strength) color else LiquidColors.Border)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(label, fontSize = 12.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}

// ─── Staggered appear ─────────────────────────────────────────────────────────

@Composable
private fun StaggeredItem(index: Int, content: @Composable () -> Unit) {
    val visible = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 60L)
        visible.value = true
    }
    AnimatedVisibility(
        visible = visible.value,
        enter   = slideInVertically { it / 3 } + fadeIn(),
        exit    = fadeOut()
    ) { content() }
}

// ─── Footer ───────────────────────────────────────────────────────────────────

@Composable
private fun RegistrationFooter(
    currentStep: Int,
    totalSteps: Int,
    isLoading: Boolean,
    canProceed: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit
) {
    val isLast = currentStep == totalSteps - 1

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(LiquidColors.GlassSurface)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // Back button — outlined
            OutlinedButton(
                onClick  = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape    = RoundedCornerShape(14.dp),
                border   = BorderStroke(1.dp, LiquidColors.Border),
                colors   = ButtonDefaults.outlinedButtonColors(
                    contentColor = LiquidColors.TextPrimary
                )
            ) {
                Text("Back", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }

            // Next / Complete — cyan
            Button(
                onClick  = { if (isLast) onSubmit() else onNext() },
                enabled  = canProceed && !isLoading,
                modifier = Modifier
                    .weight(2f)
                    .height(54.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = LiquidColors.Primary,
                    contentColor           = Color.White,
                    disabledContainerColor = LiquidColors.Primary.copy(alpha = 0.4f),
                    disabledContentColor   = Color.White.copy(alpha = 0.6f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color       = Color.White
                    )
                } else {
                    Text(
                        if (isLast) "Complete Setup" else "Next Step",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 15.sp
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector     = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier        = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF101D22, widthDp = 390, heightDp = 844)
@Composable
fun PreviewSignUpScreenContent() {
    MaterialTheme {
        SignUpScreenContent(
            state          = SignUpState(),
            onEvent        = {},
            onTermsClick   = {},
            onNavigateBack = {}
        )
    }
}
