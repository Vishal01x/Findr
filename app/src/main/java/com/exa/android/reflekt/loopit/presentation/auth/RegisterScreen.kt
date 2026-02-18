package com.exa.android.reflekt.loopit.presentation.auth

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.loopit.data.feature.authentication.vm.AuthVM
import com.exa.android.reflekt.loopit.data.feature.authentication.vm.SignUpEvent
import com.exa.android.reflekt.loopit.data.feature.authentication.vm.SignUpState
import com.exa.android.reflekt.loopit.presentation.auth.components.AnimatedAuthBackground
import com.exa.android.reflekt.loopit.presentation.auth.components.AuthTextField
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SignUpScreen(
    viewModel: AuthVM = hiltViewModel(),
    onSignUpSuccess: () -> Unit,
    onTermsClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state = viewModel.signUpState.value
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isDark = isSystemInDarkTheme()

    // 2 steps only: Personal → Details
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 2

    LaunchedEffect(state.signUpSuccess) { if (state.signUpSuccess) onSignUpSuccess() }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    val enterAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) { enterAlpha.animateTo(1f, tween(500)) }

    AnimatedAuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = enterAlpha.value }
        ) {
            // ─── Top Bar ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (currentStep > 0) currentStep-- else onNavigateBack()
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "Create Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.width(48.dp))
            }

            // ─── Step Progress Bar ───
            MinimalStepBar(currentStep, totalSteps)

            // ─── Step Content ───
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
                label = "step"
            ) { step ->
                when (step) {
                    0 -> PersonalStep(viewModel, state, focusManager)
                    1 -> DetailsStep(viewModel, state, focusManager, onTermsClick)
                }
            }

            // ─── Bottom Action ───
            BottomAction(
                currentStep = currentStep,
                totalSteps = totalSteps,
                isLoading = state.isLoading,
                canProceed = when (currentStep) {
                    0 -> state.fullName.isNotBlank() && state.email.isNotBlank() && state.password.length >= 8
                    1 -> if (state.isStudent) state.collegeName.isNotBlank()
                    else state.companyName.isNotBlank()
                    else -> true
                },
                onBack = { currentStep-- },
                onNext = { currentStep++ },
                onSubmit = {
                    focusManager.clearFocus()
                    viewModel.onSignUpEvent(SignUpEvent.Submit)
                }
            )
        }
    }
}

// ─── Minimal Progress Bar ───
@Composable
private fun MinimalStepBar(current: Int, total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(total) { i ->
            val fraction by animateFloatAsState(
                targetValue = if (i < current) 1f else if (i == current) 0.5f else 0f,
                animationSpec = tween(400, easing = FastOutSlowInEasing),
                label = "step_$i"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

// ════════════════════════════════════════
//  Step 1: Personal Info
// ════════════════════════════════════════
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PersonalStep(
    vm: AuthVM,
    state: SignUpState,
    fm: androidx.compose.ui.focus.FocusManager
) {
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = 28.dp)
            .imePadding()
    ) {
        Spacer(Modifier.height(16.dp))
        StaggeredItem(0) {
            Text("Let's get started 👋", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface)
            Text("Tell us about yourself", fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }

        Spacer(Modifier.height(28.dp))

        StaggeredItem(1) {
            AuthTextField(
                value = state.fullName,
                onValueChange = { vm.onSignUpEvent(SignUpEvent.FullNameChanged(it)) },
                label = "Full Name",
                leadingIcon = Icons.Default.Person,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { fm.moveFocus(FocusDirection.Down) })
            )
        }

        Spacer(Modifier.height(12.dp))

        StaggeredItem(2) {
            AuthTextField(
                value = state.email,
                onValueChange = { vm.onSignUpEvent(SignUpEvent.EmailChanged(it)) },
                label = "Email address",
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email, imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { fm.moveFocus(FocusDirection.Down) })
            )
        }

        Spacer(Modifier.height(12.dp))

        StaggeredItem(3) {
            AuthTextField(
                value = state.password,
                onValueChange = { vm.onSignUpEvent(SignUpEvent.PasswordChanged(it)) },
                label = "Password (min 8 characters)",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = if (state.passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { vm.onSignUpEvent(SignUpEvent.TogglePasswordVisibility) }) {
                        Icon(
                            if (state.passwordVisible) Icons.Default.Visibility
                            else Icons.Default.VisibilityOff,
                            "Toggle",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { fm.clearFocus() })
            )
        }

        // Password strength
        if (state.password.isNotEmpty() && state.password.length < 12) {
            Spacer(Modifier.height(8.dp))
            StaggeredItem(4) { PasswordBar(state.password) }
        }

        Spacer(Modifier.height(24.dp))

        StaggeredItem(5) {
            // Role selection
            Text(
                "What describes you best?",
                fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))

            AuthTextField(
                value = state.roleQuery,
                onValueChange = { vm.onSignUpEvent(SignUpEvent.RoleChanged(it)) },
                label = "Search roles (Developer, Designer…)",
                leadingIcon = Icons.Default.Search,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { fm.clearFocus() })
            )
        }

        if (state.roleSuggestions.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                state.roleSuggestions.take(8).forEach { role ->
                    val sel = role in state.selectedRoles
                    FilterChip(
                        selected = sel,
                        onClick = { vm.onSignUpEvent(SignUpEvent.SelectRole(role)) },
                        label = { Text(role, fontSize = 12.sp) },
                        leadingIcon = if (sel) {
                            { Icon(Icons.Default.Check, null, Modifier.size(14.dp)) }
                        } else null,
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true, selected = sel,
                            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    )
                }
            }
        }

        if (state.selectedRoles.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                state.selectedRoles.forEach { role ->
                    AssistChip(
                        onClick = { vm.onSignUpEvent(SignUpEvent.SelectRole(role)) },
                        label = { Text(role, fontSize = 12.sp) },
                        trailingIcon = { Icon(Icons.Default.Close, "Remove", Modifier.size(12.dp)) },
                        shape = RoundedCornerShape(10.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ════════════════════════════════════════
//  Step 2: Academic / Professional Details
// ════════════════════════════════════════
@Composable
private fun DetailsStep(
    vm: AuthVM,
    state: SignUpState,
    fm: androidx.compose.ui.focus.FocusManager,
    onTermsClick: () -> Unit
) {
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = 28.dp)
            .imePadding()
    ) {
        Spacer(Modifier.height(16.dp))
        StaggeredItem(0) {
            Text(
                if (state.isStudent) "Education Details 🎓" else "Professional Details 💼",
                fontSize = 22.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                if (state.isStudent) "Tell us about your college" else "Where do you work?",
                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

        Spacer(Modifier.height(20.dp))

        // Toggle
        StaggeredItem(1) {
            Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .clickable { vm.onSignUpEvent(SignUpEvent.ToggleStudentStatus) }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (state.isStudent) "🎓" else "💼", fontSize = 20.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    if (state.isStudent) "I'm a student" else "I'm a professional",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Switch(
                checked = state.isStudent,
                onCheckedChange = { vm.onSignUpEvent(SignUpEvent.ToggleStudentStatus) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }

        }

        Spacer(Modifier.height(20.dp))

        AnimatedContent(
            targetState = state.isStudent,
            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(180)) },
            label = "toggle"
        ) { isStudent ->
            if (isStudent) StudentFields(vm, state, fm, startIdx = 2) // Pass startIdx for continuation
            else ProfessionalFields(vm, state, fm, startIdx = 2)
        }

        // Terms text at bottom
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                "By creating an account, you agree to our ",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            )
            Text(
                "Terms",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onTermsClick() }
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ─── Student Fields ───
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentFields(vm: AuthVM, state: SignUpState, fm: androidx.compose.ui.focus.FocusManager, startIdx: Int) {
    val branches = listOf(
        "Computer Science", "Information Technology", "Electronics & Communication",
        "Electrical Engineering", "Mechanical Engineering", "Civil Engineering",
        "Chemical Engineering", "Biotechnology", "Data Science", "AI & ML", "Other"
    )
    val courses = listOf(
        "B.Tech", "B.E.", "BCA", "B.Sc", "MCA", "M.Tech", "MBA", "M.Sc",
        "B.Com", "BBA", "Ph.D", "Diploma", "Other"
    )
    val years = listOf("1st Year", "2nd Year", "3rd Year", "4th Year", "5th Year", "Graduated")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StaggeredItem(startIdx) {
            AuthTextField(
                value = state.collegeName,
                onValueChange = { vm.onSignUpEvent(SignUpEvent.CollegeNameChanged(it)) },
                label = "College / University",
                leadingIcon = Icons.Outlined.School,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { fm.moveFocus(FocusDirection.Down) })
            )
        }

        StaggeredItem(startIdx + 1) {
            ChipSelector(
                label = "Branch",
                options = branches,
                selected = state.branch,
                onSelected = { vm.onSignUpEvent(SignUpEvent.BranchChanged(it)) }
            )
        }

        StaggeredItem(startIdx + 2) {
            ChipSelector(
                label = "Course",
                options = courses,
                selected = state.course,
                onSelected = { vm.onSignUpEvent(SignUpEvent.CourseChanged(it)) }
            )
        }

        StaggeredItem(startIdx + 3) {
            ChipSelector(
                label = "Year",
                options = years,
                selected = state.year,
                onSelected = { vm.onSignUpEvent(SignUpEvent.YearChanged(it)) }
            )
        }

        StaggeredItem(startIdx + 4) {
            AuthTextField(
                value = state.location,
                onValueChange = { vm.onSignUpEvent(SignUpEvent.LocationChanged(it)) },
                label = "City / Location",
                leadingIcon = Icons.Outlined.LocationOn,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { fm.clearFocus() })
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ─── Professional Fields ───
@Composable
private fun ProfessionalFields(vm: AuthVM, state: SignUpState, fm: androidx.compose.ui.focus.FocusManager, startIdx: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StaggeredItem(startIdx) {
            AuthTextField(
                value = state.companyName,
                onValueChange = { vm.onSignUpEvent(SignUpEvent.CompanyNameChanged(it)) },
                label = "Company Name",
                leadingIcon = Icons.Outlined.Business,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { fm.moveFocus(FocusDirection.Down) })
            )
        }

        StaggeredItem(startIdx + 1) {
            AuthTextField(
                value = state.location,
                onValueChange = { vm.onSignUpEvent(SignUpEvent.LocationChanged(it)) },
                label = "Location",
                leadingIcon = Icons.Outlined.LocationOn,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { fm.moveFocus(FocusDirection.Down) })
            )
        }

        StaggeredItem(startIdx + 2) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AuthTextField(
                    value = state.experience,
                    onValueChange = { vm.onSignUpEvent(SignUpEvent.ExperienceChanged(it)) },
                    label = "Experience (yrs)",
                    leadingIcon = Icons.Outlined.Timeline,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { fm.moveFocus(FocusDirection.Right) }),
                    modifier = Modifier.weight(1f)
                )
                AuthTextField(
                    value = state.ctc,
                    onValueChange = { vm.onSignUpEvent(SignUpEvent.CtcChanged(it)) },
                    label = "CTC (LPA)",
                    leadingIcon = Icons.Outlined.CurrencyRupee,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { fm.clearFocus() }),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StaggeredItem(index: Int, content: @Composable () -> Unit) {
    val visible = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 60L) // Stagger delay
        visible.value = true
    }
    AnimatedVisibility(
        visible = visible.value,
        enter = slideInVertically { it / 3 } + fadeIn(),
        exit = fadeOut()
    ) {
        content()
    }
}

// ─── Chip Selector (replaces dropdowns) ───
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipSelector(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )

        if (selected.isNotBlank() && !expanded) {
            // Show selected as a chip with edit option
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.clickable { expanded = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            selected,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Edit,
                            "Change",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            // Show chips grid
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                options.forEach { opt ->
                    val isSel = opt == selected
                    FilterChip(
                        selected = isSel,
                        onClick = {
                            onSelected(opt)
                            expanded = false
                        },
                        label = { Text(opt, fontSize = 12.sp) },
                        leadingIcon = if (isSel) {
                            { Icon(Icons.Default.Check, null, Modifier.size(14.dp)) }
                        } else null,
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true, selected = isSel,
                            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

// ─── Bottom Action ───
@Composable
private fun BottomAction(
    currentStep: Int,
    totalSteps: Int,
    isLoading: Boolean,
    canProceed: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit
) {
    val isLast = currentStep == totalSteps - 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (currentStep > 0) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(18.dp))
            }
        }

        Button(
            onClick = { if (isLast) onSubmit() else onNext() },
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            enabled = canProceed && !isLoading,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    if (isLast) "Create Account" else "Continue",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (!isLast) {
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(18.dp))
                }
            }
        }
    }
}

// ─── Password Strength Bar ───
@Composable
private fun PasswordBar(password: String) {
    val strength = when {
        password.length >= 12 && password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() } -> 3
        password.length >= 8 && password.any { it.isDigit() } -> 2
        password.length >= 6 -> 1
        else -> 0
    }
    val label = arrayOf("Weak", "Fair", "Good", "Strong")[strength]
    val color = arrayOf(
        MaterialTheme.colorScheme.error,
        Color(0xFFFF9800),
        MaterialTheme.colorScheme.tertiary,
        Color(0xFF4CAF50)
    )[strength]

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        repeat(4) { i ->
            Box(
                Modifier
                    .weight(1f)
                    .height(3.dp)
                    .padding(horizontal = 1.dp)
                    .clip(CircleShape)
                    .background(if (i <= strength) color else MaterialTheme.colorScheme.surfaceContainerHigh)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(label, fontSize = 11.sp, color = color, fontWeight = FontWeight.Medium)
    }
}