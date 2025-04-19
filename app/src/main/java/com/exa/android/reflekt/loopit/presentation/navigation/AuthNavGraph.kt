package com.exa.android.reflekt.loopit.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.exa.android.reflekt.loopit.presentation.auth.ForgotPasswordScreen
import com.exa.android.reflekt.loopit.presentation.auth.LoginScreen
import com.exa.android.reflekt.loopit.presentation.auth.SignUpScreen
import com.exa.android.reflekt.loopit.presentation.auth.VerificationEmailScreen
import com.exa.android.reflekt.loopit.presentation.navigation.component.AuthRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.MainRoute

fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation(
        startDestination = AuthRoute.Login.route,
        route = AuthRoute.ROOT
    ) {
        composable(AuthRoute.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(MainRoute.ROOT) {
                        popUpTo(AuthRoute.ROOT) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(AuthRoute.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(AuthRoute.ForgetPassword.route)
                }
            )
        }

        composable(AuthRoute.Register.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(AuthRoute.Verification.route) {
                        popUpTo(AuthRoute.Register.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(AuthRoute.Verification.route) {
            VerificationEmailScreen(
                onVerificationComplete = {
                    navController.navigate(MainRoute.ROOT) {
                        popUpTo(AuthRoute.ROOT) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(AuthRoute.ForgetPassword.route) {
            ForgotPasswordScreen(
                onSuccess = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
