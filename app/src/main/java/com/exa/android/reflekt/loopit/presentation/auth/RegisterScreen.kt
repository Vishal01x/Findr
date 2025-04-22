package com.exa.android.reflekt.loopit.presentation.auth

import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.reflekt.loopit.data.remote.authentication.vm.AuthVM
import com.exa.android.reflekt.loopit.data.remote.authentication.vm.SignUpEvent
import com.exa.android.reflekt.loopit.presentation.navigation.component.AuthRoute
import com.exa.android.reflekt.loopit.util.Response
import com.exa.android.reflekt.loopit.util.showToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.remote.authentication.vm.SignUpState

// auth/SignUpScreen.kt
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SignUpScreen(
    viewModel: AuthVM = hiltViewModel(),
    onSignUpSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state = viewModel.signUpState.value
    val roleSuggestions = viewModel.roleSuggestions
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.signUpSuccess) {
        if (state.signUpSuccess) {
            onSignUpSuccess()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }
    BackHandler {
        if (state.selectedAccountType == "Professional") {
            viewModel.onSignUpEvent(SignUpEvent.AccountTypeSelected("Personal"))
        } else {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account", color = colorScheme.onTertiary) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.selectedAccountType == "Professional") {
                            viewModel.onSignUpEvent(SignUpEvent.AccountTypeSelected("Personal"))
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onTertiary)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(rememberNestedScrollInteropConnection())
                .verticalScroll(scrollState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp)
                    .imePadding()
                    .padding(WindowInsets.ime.asPaddingValues())
            ) {
                // Header Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app),
                        contentDescription = "Profile Creation",
                        modifier = Modifier.size(80.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Join Our Community",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Complete your profile to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
                // Add this in the Basic Info Section where you want the buttons


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Personal", "Professional").forEach { type ->
                        FilterChip(
                            selected = state.selectedAccountType == type,
                            onClick = {

                            },
                            label = {
                                Text(
                                    type,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (state.selectedAccountType == type) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline
                                },
                                selectedBorderColor = MaterialTheme.colorScheme.primary,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                selectedBorderWidth = 1.dp,
                                enabled = true,
                                selected = state.selectedAccountType == type

                            ),
                            enabled = true,
                            elevation = FilterChipDefaults.elevatedFilterChipElevation(
                                elevation = if (state.selectedAccountType == type) 4.dp else 0.dp
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .animateContentSize()
                        )
                    }
                }


                if (state.selectedAccountType == "Personal") {
                    // Basic Info Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Full Name
                        OutlinedTextField(
                            value = state.fullName,
                            onValueChange = {
                                viewModel.onSignUpEvent(
                                    SignUpEvent.FullNameChanged(
                                        it
                                    )
                                )
                            },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Person,
                                    contentDescription = "Name"
                                )
                            },
                            shape = MaterialTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorScheme.primary,
                                unfocusedBorderColor = colorScheme.onSurface.copy(alpha = 0.4f),
                                focusedLabelColor = colorScheme.primary,
                                cursorColor = colorScheme.primary,
                                focusedTextColor = colorScheme.onTertiary
                            ),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next,  // This enables the Next button
                                capitalization = KeyboardCapitalization.Words,
                                autoCorrect = true
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    // Move focus to the next field (you'll need to add focus references)
                                    focusManager.moveFocus(FocusDirection.Down)
                                }
                            )
                        )

                        // Email
                        OutlinedTextField(
                            value = state.email,
                            onValueChange = {
                                viewModel.onSignUpEvent(
                                    SignUpEvent.EmailChanged(
                                        it
                                    )
                                )
                            },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Email,
                                    contentDescription = "Email"
                                )
                            },
                            shape = MaterialTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorScheme.primary,
                                unfocusedBorderColor = colorScheme.onSurface.copy(alpha = 0.4f),
                                focusedLabelColor = colorScheme.primary,
                                cursorColor = colorScheme.primary
                            ),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next,  // This enables the Next button
                                capitalization = KeyboardCapitalization.Words,
                                keyboardType = KeyboardType.Email,
                                autoCorrect = true
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    // Move focus to the next field (you'll need to add focus references)
                                    focusManager.moveFocus(FocusDirection.Down)
                                }
                            )
                        )

                        // Password
                        OutlinedTextField(
                            value = state.password,
                            onValueChange = {
                                viewModel.onSignUpEvent(
                                    SignUpEvent.PasswordChanged(
                                        it
                                    )
                                )
                            },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Lock,
                                    contentDescription = "Password"
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    viewModel.onSignUpEvent(SignUpEvent.TogglePasswordVisibility)
                                }) {
                                    Icon(
                                        if (state.passwordVisible) Icons.Outlined.Visibility
                                        else Icons.Outlined.VisibilityOff,
                                        contentDescription = "Toggle password visibility"
                                    )
                                }
                            },
                            visualTransformation = if (state.passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            shape = MaterialTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorScheme.primary,
                                unfocusedBorderColor = colorScheme.onSurface.copy(alpha = 0.4f),
                                focusedLabelColor = colorScheme.primary,
                                cursorColor = colorScheme.primary
                            ),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next,
                                keyboardType = KeyboardType.Password,
                                autoCorrect = false
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    // Move focus to the next field (you'll need to add focus references)
                                    focusManager.moveFocus(FocusDirection.Down)
                                }
                            )
                        )
                        // Role with suggestions
                        Column {
                            // Selected Roles Display
                            if (state.selectedRoles.isNotEmpty()) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    state.selectedRoles.forEach { role ->
                                        InputChip(
                                            selected = true,
                                            onClick = {
                                                viewModel.onSignUpEvent(
                                                    SignUpEvent.SelectRole(
                                                        role
                                                    )
                                                )
                                            },
                                            label = { Text(role) },
                                            trailingIcon = {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            colors = InputChipDefaults.inputChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Role Suggestions
                            if (roleSuggestions.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp)
                                        .padding(top = 4.dp)
                                ) {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 200.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        items(roleSuggestions.size) {
                                            val role = roleSuggestions[it]
                                            SuggestionChip(
                                                onClick = {
                                                    viewModel.onSignUpEvent(
                                                        SignUpEvent.SelectRole(
                                                            role
                                                        )
                                                    )
                                                    focusManager.clearFocus()
                                                },
                                                label = { Text(role) },
                                                modifier = Modifier.fillMaxWidth(),
                                                border = BorderStroke(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                                ),
                                                colors = SuggestionChipDefaults.suggestionChipColors(
                                                    containerColor = MaterialTheme.colorScheme.surface,
                                                    labelColor = MaterialTheme.colorScheme.primary
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                            // Role Search Field
                            OutlinedTextField(
                                value = state.roleQuery,
                                onValueChange = {
                                    viewModel.onSignUpEvent(
                                        SignUpEvent.RoleChanged(
                                            it
                                        )
                                    )
                                },
                                label = { Text("Your Role (e.g. Developer, Designer)") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Work,
                                        contentDescription = "Role"
                                    )
                                },
                                trailingIcon = {
                                    if (state.roleQuery.isNotBlank()) {
                                        IconButton(onClick = {
                                            // Add the custom role if it's not in suggestions
                                            if (state.roleQuery.isNotBlank() &&
                                                !roleSuggestions.contains(state.roleQuery)) {
                                                viewModel.onSignUpEvent(SignUpEvent.SelectRole(state.roleQuery))
                                                viewModel.addRole(state.roleQuery)
                                            }
                                            focusManager.clearFocus()
                                        }) {
                                            Icon(
                                                Icons.Default.Done,
                                                contentDescription = "Add role",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (state.roleQuery.isNotBlank() &&
                                            !roleSuggestions.contains(state.roleQuery)) {
                                            viewModel.onSignUpEvent(SignUpEvent.SelectRole(state.roleQuery))
                                            viewModel.addRole(state.roleQuery)
                                        }
                                        focusManager.clearFocus()
                                    }
                                ),
                                shape = MaterialTheme.shapes.medium,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colorScheme.primary,
                                    unfocusedBorderColor = colorScheme.onSurface.copy(alpha = 0.4f),
                                    focusedLabelColor = colorScheme.primary,
                                    cursorColor = colorScheme.primary
                                ),
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Continue Button
                        Button(
                            onClick = {
                                viewModel.onSignUpEvent(SignUpEvent.Continue)
                                focusManager.clearFocus()
                                viewModel.onSignUpEvent(SignUpEvent.AccountTypeSelected("Professional"))

                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = state.email.isNotBlank() &&
                                    state.fullName.isNotBlank() &&
                                    state.selectedRoles.isNotEmpty() &&
                                    state.password.length >= 8,
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 2.dp
                            )
                        ) {
                            Text(
                                "Continue",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                else {
                    // Profile Details Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Tell us more about yourself",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Student/Professional Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "I'm a student",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Switch(
                                checked = state.isStudent,
                                onCheckedChange = {
                                    viewModel.onSignUpEvent(SignUpEvent.ToggleStudentStatus)
                                },
                                thumbContent = if (state.isStudent) {
                                    {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = "Student",
                                            modifier = Modifier.size(SwitchDefaults.IconSize)
                                        )
                                    }
                                } else {
                                    null
                                }
                            )
                        }

                        if (state.isStudent) {
                            // Student fields
                            OutlinedTextField(
                                value = state.collegeName,
                                onValueChange = {
                                    viewModel.onSignUpEvent(
                                        SignUpEvent.CollegeNameChanged(
                                            it
                                        )
                                    )
                                },
                                label = { Text("College/University") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.School,
                                        contentDescription = "College"
                                    )
                                },
                                shape = MaterialTheme.shapes.medium,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colorScheme.primary,
                                    unfocusedBorderColor = colorScheme.onSurface.copy(alpha = 0.4f),
                                    focusedLabelColor = colorScheme.primary,
                                    cursorColor = colorScheme.primary
                                ),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Next,  // This enables the Next button
                                    capitalization = KeyboardCapitalization.Words,
                                    autoCorrect = true
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = {
                                        // Move focus to the next field (you'll need to add focus references)
                                        focusManager.moveFocus(FocusDirection.Down)
                                    }
                                )
                            )

                            OutlinedTextField(
                                value = state.year,
                                onValueChange = {
                                    viewModel.onSignUpEvent(
                                        SignUpEvent.YearChanged(
                                            it
                                        )
                                    )
                                },
                                label = { Text("Current Year") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.CalendarToday,
                                        contentDescription = "Year"
                                    )
                                },
                                shape = MaterialTheme.shapes.medium,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colorScheme.primary,
                                    unfocusedBorderColor = colorScheme.onSurface.copy(alpha = 0.4f),
                                    focusedLabelColor = colorScheme.primary,
                                    cursorColor = colorScheme.primary
                                ),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Next,  // This enables the Next button
                                    capitalization = KeyboardCapitalization.Words,
                                    autoCorrect = true,
                                    keyboardType = KeyboardType.Number
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = {
                                        // Move focus to the next field (you'll need to add focus references)
                                        focusManager.moveFocus(FocusDirection.Down)
                                    }
                                )
                            )
                        }
                        else {
                            // Professional fields
                            OutlinedTextField(
                                value = state.location,
                                onValueChange = {
                                    viewModel.onSignUpEvent(
                                        SignUpEvent.LocationChanged(
                                            it
                                        )
                                    )
                                },
                                label = { Text("Location (City, Country)") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.LocationOn,
                                        contentDescription = "Location"
                                    )
                                },
                                shape = MaterialTheme.shapes.medium,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colorScheme.primary,
                                    unfocusedBorderColor = colorScheme.onSurface.copy(alpha = 0.4f),
                                    focusedLabelColor = colorScheme.primary,
                                    cursorColor = colorScheme.primary
                                ),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Next,  // This enables the Next button
                                    capitalization = KeyboardCapitalization.Words,
                                    autoCorrect = true
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = {
                                        // Move focus to the next field (you'll need to add focus references)
                                        focusManager.moveFocus(FocusDirection.Down)
                                    }
                                )
                            )

                            OutlinedTextField(
                                value = state.companyName,
                                onValueChange = {
                                    viewModel.onSignUpEvent(
                                        SignUpEvent.CompanyNameChanged(
                                            it
                                        )
                                    )
                                },
                                label = { Text("Current/Most Recent Company") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Business,
                                        contentDescription = "Company"
                                    )
                                },
                                shape = MaterialTheme.shapes.medium,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colorScheme.primary,
                                    unfocusedBorderColor = colorScheme.onSurface.copy(alpha = 0.4f),
                                    focusedLabelColor = colorScheme.primary,
                                    cursorColor = colorScheme.primary
                                ),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Next,  // This enables the Next button
                                    capitalization = KeyboardCapitalization.Words,
                                    autoCorrect = true
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = {
                                        // Move focus to the next field (you'll need to add focus references)
                                        focusManager.moveFocus(FocusDirection.Down)
                                    }
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = state.experience,
                                    onValueChange = {
                                        viewModel.onSignUpEvent(
                                            SignUpEvent.ExperienceChanged(
                                                it
                                            )
                                        )
                                    },
                                    label = { Text("Experience (years)") },
                                    modifier = Modifier.weight(1f),
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.Timeline,
                                            contentDescription = "Experience"
                                        )
                                    },
                                    shape = MaterialTheme.shapes.medium,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colorScheme.primary,
                                        unfocusedBorderColor = colorScheme.onSurface.copy(alpha = 0.4f),
                                        focusedLabelColor = colorScheme.primary,
                                        cursorColor = colorScheme.primary
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        imeAction = ImeAction.Next,  // This enables the Next button
                                        capitalization = KeyboardCapitalization.Words,
                                        keyboardType = KeyboardType.Number,
                                        autoCorrect = true
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = {
                                            focusManager.moveFocus(FocusDirection.Down)
                                        }
                                    )
                                )

                                OutlinedTextField(
                                    value = state.ctc,
                                    onValueChange = {
                                        viewModel.onSignUpEvent(
                                            SignUpEvent.CtcChanged(
                                                it
                                            )
                                        )
                                    },
                                    label = { Text("CTC (LPA)") },
                                    modifier = Modifier.weight(1f),
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.AttachMoney,
                                            contentDescription = "CTC"
                                        )
                                    },
                                    shape = MaterialTheme.shapes.medium,

                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colorScheme.primary,
                                        unfocusedBorderColor = colorScheme.onSurface.copy(alpha = 0.4f),
                                        focusedLabelColor = colorScheme.primary,
                                        cursorColor = colorScheme.primary
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))


                        Button(
                            onClick = {
                                viewModel.onSignUpEvent(SignUpEvent.Submit)
                                focusManager.clearFocus()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = isFormComplete(state),
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 2.dp
                            )
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Complete Registration",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
private fun isFormComplete(state: SignUpState): Boolean {
    return if (state.selectedAccountType == "Personal") {
        state.email.isNotBlank() &&
                state.fullName.isNotBlank() &&
                state.selectedRoles.isNotEmpty() &&
                state.password.length >= 8
    } else {
        if (state.isStudent) {
            state.collegeName.isNotBlank() && state.year.isNotBlank()
        } else {
            state.location.isNotBlank() &&
                    state.companyName.isNotBlank() &&
                    state.experience.isNotBlank() &&
                    state.ctc.isNotBlank()
        }
    }
}