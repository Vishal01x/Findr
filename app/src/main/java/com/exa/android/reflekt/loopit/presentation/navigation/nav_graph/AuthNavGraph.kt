package com.exa.android.reflekt.loopit.presentation.navigation.nav_graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.exa.android.reflekt.loopit.presentation.auth.ForgotPasswordScreen
import com.exa.android.reflekt.loopit.presentation.auth.GoogleProfileSetupScreen
import com.exa.android.reflekt.loopit.presentation.auth.LoginScreen
import com.exa.android.reflekt.loopit.presentation.auth.OnboardingScreen
import com.exa.android.reflekt.loopit.presentation.auth.SignUpScreen
import com.exa.android.reflekt.loopit.presentation.auth.SplashScreen
import com.exa.android.reflekt.loopit.presentation.auth.VerificationEmailScreen
import com.exa.android.reflekt.loopit.presentation.main.profile.components.setting.TermsWebView
import com.exa.android.reflekt.loopit.presentation.navigation.component.AuthRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.MainRoute

fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation(
        startDestination = AuthRoute.Splash.route,
        route = AuthRoute.ROOT
    ) {
        // ─── Splash: decides where to go ───
        composable(AuthRoute.Splash.route) {
            SplashScreen(
                onLoggedIn = {
                    navController.navigate(MainRoute.ROOT) {
                        popUpTo(AuthRoute.ROOT) { inclusive = true }
                    }
                },
                onNeedsProfileSetup = {
                    navController.navigate(AuthRoute.GoogleProfileSetup.route) {
                        popUpTo(AuthRoute.Splash.route) { inclusive = true }
                    }
                },
                onNotLoggedIn = {
                    navController.navigate(AuthRoute.Onboarding.route) {
                        popUpTo(AuthRoute.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AuthRoute.Onboarding.route) {
            OnboardingScreen(
                onCompleted = {
                    navController.navigate(AuthRoute.Login.route) {
                        popUpTo(AuthRoute.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AuthRoute.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(MainRoute.ROOT) {
                        popUpTo(AuthRoute.ROOT) { inclusive = true }
                    }
                },
                onNeedsProfileSetup = {
                    navController.navigate(AuthRoute.GoogleProfileSetup.route) {
                        popUpTo(AuthRoute.Login.route) { inclusive = false }
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
                onTermsClick = {
                    navController.navigate(AuthRoute.TermsCondition.route)
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

        composable(AuthRoute.GoogleProfileSetup.route) {
            GoogleProfileSetupScreen(
                onSetupComplete = {
                    navController.navigate(MainRoute.ROOT) {
                        popUpTo(AuthRoute.ROOT) { inclusive = true }
                    }
                }
            )
        }

        composable(AuthRoute.TermsCondition.route) {
            TermsWebView {
                navController.popBackStack()
            }
        }

        composable(AuthRoute.ForgetPassword.route) {
            ForgotPasswordScreen(
                onSuccess = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
