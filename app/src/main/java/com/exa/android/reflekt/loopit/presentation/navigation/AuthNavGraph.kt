package com.exa.android.reflekt.loopit.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.exa.android.reflekt.loopit.presentation.auth.ForgetPasswordScreen
import com.exa.android.reflekt.loopit.presentation.auth.LoginScreen
import com.exa.android.reflekt.loopit.presentation.auth.RegisterScreen
import com.exa.android.reflekt.loopit.presentation.navigation.component.AuthRoute


fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation(startDestination = AuthRoute.Login.route, route = "auth") {
        composable(AuthRoute.Login.route) { LoginScreen(navController) }
        composable(AuthRoute.Register.route) { RegisterScreen(navController) }
        composable(AuthRoute.ForgetPassword.route) { ForgetPasswordScreen(navController) }
    }
}

