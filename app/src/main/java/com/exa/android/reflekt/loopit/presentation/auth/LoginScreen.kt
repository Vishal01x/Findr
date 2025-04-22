package com.exa.android.reflekt.loopit.presentation.auth

import com.exa.android.reflekt.loopit.data.remote.authentication.vm.AuthVM
import com.exa.android.reflekt.loopit.presentation.navigation.component.AuthRoute
import com.exa.android.reflekt.loopit.util.Response
import com.exa.android.reflekt.loopit.util.showToast
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.exa.android.reflekt.loopit.data.remote.authentication.vm.LoginEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.exa.android.reflekt.R
import io.getstream.meeting.room.compose.ui.AppColors

/*
// auth/LoginScreen.kt
@Composable
fun LoginScreen(
    viewModel: AuthVM = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val state = viewModel.loginState.value
    val context = LocalContext.current

    LaunchedEffect(state.loginSuccess) {
        if (state.loginSuccess) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.onLoginEvent(LoginEvent.EmailChanged(state.email)) // Reset error
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Image - Replace with your actual image resource
            Image(
                painter = painterResource(id = R.drawable.chat_img3),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(width = 200.dp, height = 200.dp)
                    .clip(RoundedCornerShape(30.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Tech Connect",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Email Input
            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.onLoginEvent(LoginEvent.EmailChanged(it)) },
                label = { Text("Email ID") },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = "Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Input
            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.onLoginEvent(LoginEvent.PasswordChanged(it)) },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "Password") },
                trailingIcon = {
                    IconButton(onClick = {
                        viewModel.onLoginEvent(LoginEvent.TogglePasswordVisibility)
                    }) {
                        Icon(
                            if (state.passwordVisible) Icons.Outlined.VisibilityOff
                            else Icons.Outlined.Visibility,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (state.passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Forgot Password?",
                color = Color.Blue,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 16.dp)
                    .clickable {
                        onNavigateToForgotPassword()
                    }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Login Button
            Button(
                onClick = { viewModel.onLoginEvent(LoginEvent.Submit) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7987CB))
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text("Login", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // OR Divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    color = Color.Gray,
                    thickness = 1.dp
                )
                Text(
                    text = "OR",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = Color.Gray
                )
                Divider(
                    modifier = Modifier.weight(1f),
                    color = Color.Gray,
                    thickness = 1.dp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Register Link
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("New User? ")
                Text(
                    text = "Register",
                    color = Color.Blue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        onNavigateToRegister()
                    }
                )
            }
        }
    }
}*/

@Composable
fun LoginScreen(
    viewModel: AuthVM = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val state = viewModel.loginState.value
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    LaunchedEffect(state.loginSuccess) {
        if (state.loginSuccess) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.onLoginEvent(LoginEvent.EmailChanged(state.email)) // Reset error
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_app),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(width = 200.dp, height = 200.dp)
                    .clip(RoundedCornerShape(30.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome!",
                style = typography.displayLarge,
                color = MaterialTheme.colorScheme.onTertiary
            )

            Text(
                text = "Findr",
                style = typography.titleLarge,
                color = MaterialTheme.colorScheme.onTertiary
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.onLoginEvent(LoginEvent.EmailChanged(it)) },
                label = { Text("Email ID") },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = "Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.onSurface.copy(alpha = 0.4f),
                    focusedLabelColor = colorScheme.primary,
                    cursorColor = colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onTertiary,
                    unfocusedTextColor = MaterialTheme.colorScheme.onTertiary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.onLoginEvent(LoginEvent.PasswordChanged(it)) },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "Password") },
                trailingIcon = {
                    IconButton(onClick = {
                        viewModel.onLoginEvent(LoginEvent.TogglePasswordVisibility)
                    }) {
                        Icon(
                            if (state.passwordVisible) Icons.Outlined.VisibilityOff
                            else Icons.Outlined.Visibility,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (state.passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.onSurface.copy(alpha = 0.4f),
                    focusedLabelColor = colorScheme.primary,
                    cursorColor = colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Forgot Password?",
                color = colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 16.dp)
                    .clickable { onNavigateToForgotPassword() }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.onLoginEvent(LoginEvent.Submit) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text("Login", fontWeight = FontWeight.Bold, color = colorScheme.onPrimary, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    color = AppColors.DividerColor,
                    thickness = 1.dp
                )
                Text(
                    text = "OR",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = colorScheme.onSurface
                )
                Divider(
                    modifier = Modifier.weight(1f),
                    color = AppColors.DividerColor,
                    thickness = 1.dp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("New User? ", style = typography.bodyLarge)
                Text(
                    text = "Register",
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }
        }
    }
}