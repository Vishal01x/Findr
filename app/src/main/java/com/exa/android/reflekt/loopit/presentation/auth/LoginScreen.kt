package com.exa.android.reflekt.loopit.presentation.auth

import com.exa.android.reflekt.loopit.authentication.vm.AuthVM
import com.exa.android.reflekt.loopit.presentation.navigation.component.AuthRoute
import com.exa.android.reflekt.loopit.util.Response
import com.exa.android.reflekt.loopit.util.showToast
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    val viewModel : AuthVM = hiltViewModel()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val authStatus by viewModel.authStatus.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(authStatus) {
        when (authStatus) {
            is Response.Success -> {
                isLoading = false

                showToast(context, "User Successfully Login")
                navController.navigate("main_app") {
                    popUpTo("auth") { inclusive = true }
                }
            }
            is Response.Error -> {
                isLoading = false
                errorMessage = (authStatus as Response.Error).message
            }
            is Response.Loading -> {
                isLoading = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Login", fontSize = 24.sp, modifier = Modifier.padding(bottom = 24.dp))

        // Email Input Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Input Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display error message if present
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Login Button with loading indicator
        Button(
            onClick = {
                if (email.isBlank()) {
                    errorMessage = "Please enter an email."
                } else if(password.length < 8){
                    errorMessage = "Please enter least 8 characters password"
                }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    errorMessage = "Enter correct email"
                } else {
                    // Start loading and perform login operation
                    viewModel.loginUser(email, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(text = "Login")
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row {
            Text("Create New Account", style = MaterialTheme.typography.labelSmall,
                color = Color.Blue, modifier = Modifier.clickable {
                    navController.navigate(AuthRoute.Register.route)
                })

            Text(" ? Forget Password", style = MaterialTheme.typography.labelSmall,
                color = Color.Blue, modifier = Modifier.clickable {
                    navController.navigate(AuthRoute.ForgetPassword.route)
                })
        }
    }
}