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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.loopit.data.feature.authentication.vm.AuthVM
import com.exa.android.reflekt.loopit.data.feature.authentication.vm.GoogleProfileSetupEvent
import com.exa.android.reflekt.loopit.data.feature.authentication.vm.GoogleProfileSetupState
import com.exa.android.reflekt.loopit.presentation.auth.components.AnimatedAuthBackground
import com.exa.android.reflekt.loopit.presentation.auth.components.AuthTextField
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GoogleProfileSetupScreen(
    viewModel: AuthVM = hiltViewModel(),
    onSetupComplete: () -> Unit
) {
    val state = viewModel.googleProfileSetupState.value
    val loginState = viewModel.loginState.value
    val roleSuggestions = viewModel.roleSuggestions
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(loginState.loginSuccess) {
        if (loginState.loginSuccess) onSetupComplete()
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
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
                Spacer(Modifier.width(48.dp))
                Spacer(Modifier.weight(1f))
                Text(
                    "Complete Your Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.width(48.dp))
            }

            // ─── Content ───
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp)
                    .imePadding()
            ) {
                Spacer(Modifier.height(8.dp))

                SetupItem(0) {
                    Text(
                        "Almost there! 🎉",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Tell us a bit more so we can personalise your experience",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ─── Role / Skills ───
                SetupItem(1) {
                    Text(
                        "What describes you best?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    AuthTextField(
                        value = state.roleQuery,
                        onValueChange = { viewModel.onGoogleProfileSetupEvent(GoogleProfileSetupEvent.RoleChanged(it)) },
                        label = "Search roles (Developer, Designer…)",
                        leadingIcon = Icons.Default.Search,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                }

                if (roleSuggestions.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        roleSuggestions.take(8).forEach { role ->
                            val sel = role in state.selectedRoles
                            FilterChip(
                                selected = sel,
                                onClick = { viewModel.onGoogleProfileSetupEvent(GoogleProfileSetupEvent.SelectRole(role)) },
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
                                onClick = { viewModel.onGoogleProfileSetupEvent(GoogleProfileSetupEvent.SelectRole(role)) },
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

                Spacer(Modifier.height(24.dp))

                // ─── Student / Professional Toggle ───
                SetupItem(2) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .clickable { viewModel.onGoogleProfileSetupEvent(GoogleProfileSetupEvent.ToggleStudentStatus) }
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
                            onCheckedChange = { viewModel.onGoogleProfileSetupEvent(GoogleProfileSetupEvent.ToggleStudentStatus) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ─── Student / Professional Fields ───
                AnimatedContent(
                    targetState = state.isStudent,
                    transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(180)) },
                    label = "toggle"
                ) { isStudent ->
                    if (isStudent) GoogleStudentFields(viewModel, state, focusManager)
                    else GoogleProfessionalFields(viewModel, state, focusManager)
                }

                Spacer(Modifier.height(32.dp))
            }

            // ─── Bottom Action ───
            val canSubmit = if (state.isStudent) state.collegeName.isNotBlank()
            else state.companyName.isNotBlank()

            Button(
                onClick = { viewModel.onGoogleProfileSetupEvent(GoogleProfileSetupEvent.Submit) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 12.dp)
                    .navigationBarsPadding()
                    .height(52.dp),
                enabled = canSubmit && !state.isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Get Started", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ─── Student Fields ───
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun GoogleStudentFields(
    vm: AuthVM,
    state: GoogleProfileSetupState,
    fm: androidx.compose.ui.focus.FocusManager
) {
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
        AuthTextField(
            value = state.collegeName,
            onValueChange = { vm.onGoogleProfileSetupEvent(GoogleProfileSetupEvent.CollegeNameChanged(it)) },
            label = "College / University",
            leadingIcon = Icons.Outlined.School,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { fm.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) })
        )

        GoogleChipSelector(
            label = "Branch",
            options = branches,
            selected = state.branch,
            onSelected = { vm.onGoogleProfileSetupEvent(GoogleProfileSetupEvent.BranchChanged(it)) }
        )

        GoogleChipSelector(
            label = "Course",
            options = courses,
            selected = state.course,
            onSelected = { vm.onGoogleProfileSetupEvent(GoogleProfileSetupEvent.CourseChanged(it)) }
        )

        GoogleChipSelector(
            label = "Year",
            options = years,
            selected = state.year,
            onSelected = { vm.onGoogleProfileSetupEvent(GoogleProfileSetupEvent.YearChanged(it)) }
        )

        AuthTextField(
            value = state.location,
            onValueChange = { vm.onGoogleProfileSetupEvent(GoogleProfileSetupEvent.LocationChanged(it)) },
            label = "City / Location",
            leadingIcon = Icons.Outlined.LocationOn,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { fm.clearFocus() })
        )
    }
}

// ─── Professional Fields ───
@Composable
private fun GoogleProfessionalFields(
    vm: AuthVM,
    state: GoogleProfileSetupState,
    fm: androidx.compose.ui.focus.FocusManager
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AuthTextField(
            value = state.companyName,
            onValueChange = { vm.onGoogleProfileSetupEvent(GoogleProfileSetupEvent.CompanyNameChanged(it)) },
            label = "Company Name",
            leadingIcon = Icons.Outlined.Business,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { fm.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) })
        )

        AuthTextField(
            value = state.location,
            onValueChange = { vm.onGoogleProfileSetupEvent(GoogleProfileSetupEvent.LocationChanged(it)) },
            label = "Location",
            leadingIcon = Icons.Outlined.LocationOn,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { fm.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) })
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AuthTextField(
                value = state.experience,
                onValueChange = { vm.onGoogleProfileSetupEvent(GoogleProfileSetupEvent.ExperienceChanged(it)) },
                label = "Experience (yrs)",
                leadingIcon = Icons.Outlined.Timeline,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { fm.moveFocus(androidx.compose.ui.focus.FocusDirection.Right) }),
                modifier = Modifier.weight(1f)
            )
            AuthTextField(
                value = state.ctc,
                onValueChange = { vm.onGoogleProfileSetupEvent(GoogleProfileSetupEvent.CtcChanged(it)) },
                label = "CTC (LPA)",
                leadingIcon = Icons.Outlined.CurrencyRupee,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { fm.clearFocus() }),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ─── Chip Selector ───
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GoogleChipSelector(
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
                        Icons.Default.Edit, "Change",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                options.forEach { opt ->
                    val isSel = opt == selected
                    FilterChip(
                        selected = isSel,
                        onClick = { onSelected(opt); expanded = false },
                        label = { Text(opt, fontSize = 12.sp) },
                        leadingIcon = if (isSel) {
                            { Icon(Icons.Default.Check, null, Modifier.size(14.dp)) }
                        } else null,
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true, selected = isSel,
                            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                            selectedBorderColor = androidx.compose.ui.graphics.Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupItem(index: Int, content: @Composable ColumnScope.() -> Unit) {
    val visible = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 80L)
        visible.value = true
    }
    AnimatedVisibility(
        visible = visible.value,
        enter = fadeIn(tween(300)),
        exit = fadeOut()
    ) {
        Column(content = content)
    }
}
