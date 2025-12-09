package com.exa.android.reflekt.loopit.presentation.navigation.nav_graph

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.exa.android.reflekt.loopit.presentation.main.placement.screen.PlacementTimelineScreen
import com.exa.android.reflekt.loopit.presentation.main.placement.screen.ReviewDetailScreen
import com.exa.android.reflekt.loopit.presentation.main.placement.screen.SubmitReviewScreen
import com.exa.android.reflekt.loopit.presentation.navigation.component.PlacementRoute

fun NavGraphBuilder.placementNavGraph(navController: NavHostController) {
    navigation(
        startDestination = PlacementRoute.Timeline.route,
        route = "placement_graph"
    ) {
        // Timeline Screen (Main)
        composable(PlacementRoute.Timeline.route) {
            PlacementTimelineScreen(
                onReviewClick = { reviewId ->
                    navController.navigate(PlacementRoute.ReviewDetail.createRoute(reviewId))
                },
                onAddReviewClick = {
                    navController.navigate(PlacementRoute.SubmitReview.route)
                },
                viewModel = hiltViewModel()
            )
        }

        // Review Detail Screen
        composable(
            route = PlacementRoute.ReviewDetail.route,
            arguments = listOf(
                navArgument("reviewId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val reviewId = backStackEntry.arguments?.getString("reviewId") ?: ""
            ReviewDetailScreen(
                reviewId = reviewId,
                onBackClick = {
                    navController.popBackStack()
                },
                viewModel = hiltViewModel()
            )
        }

        // Submit Review Screen
        composable(PlacementRoute.SubmitReview.route) {
            SubmitReviewScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Company Detail Screen (placeholder for now)
        composable(
            route = PlacementRoute.CompanyDetail.route,
            arguments = listOf(
                navArgument("companyName") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            // TODO: Implement CompanyDetailScreen
            navController.popBackStack()
        }
    }
}
