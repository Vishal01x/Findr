package com.exa.android.reflekt.loopit.fcm

import android.net.Uri
import androidx.navigation.NavController

object DeepLinkHelper {
    fun handleDeepLink(navController: NavController, uri: Uri) {
        val host = uri.host ?: return
        val firstSegment = uri.pathSegments.firstOrNull()

        when {
            // Custom scheme from FCM notifications: findr://post/{id}, findr://chat/{id}
            uri.scheme == "findr" -> when (host) {
                "chat" -> navigateToChat(uri, navController)
                "project", "post" -> navigateToPostDetail(uri, navController)
                "profile" -> navigateToProfile(uri, navController)
                "company" -> navigateToCompanyDetail(uri, navController)
            }

            // FDL link= parameter: https://reflekt.com/project/{id}
            host == "reflekt.com" -> when (firstSegment) {
                "project" -> navigateToPostDetail(uri, navController)
                "chat" -> navigateToChat(uri, navController)
                "company" -> navigateToCompanyDetail(uri, navController)
                "profile" -> navigateToProfile(uri, navController)
            }

            // Firebase Hosting / findr.app: https://social-media-ab0cc.web.app/post/{id}
            host == "findr.app" || host == "social-media-ab0cc.web.app" -> when (firstSegment) {
                "post" -> navigateToPostDetail(uri, navController)
                "chat" -> navigateToChat(uri, navController)
                "company" -> navigateToCompanyDetail(uri, navController)
            }
        }
    }

    private fun navigateToPostDetail(uri: Uri, navController: NavController) {
        val postId = uri.lastPathSegment ?: return
        val scrollTo = uri.getQueryParameter("scrollTo") ?: ""
        val route = if (scrollTo.isBlank()) "project_detail/$postId"
                    else "project_detail/$postId?scrollTo=$scrollTo"
        navController.navigate(route) { launchSingleTop = true }
    }

    private fun navigateToChat(uri: Uri, navController: NavController) {
        val userId = uri.lastPathSegment ?: return
        navController.navigate("chat_detail/$userId") { launchSingleTop = true }
    }

    private fun navigateToProfile(uri: Uri, navController: NavController) {
        val userId = uri.lastPathSegment ?: return
        navController.navigate("user_profile?userId=$userId") { launchSingleTop = true }
    }

    private fun navigateToCompanyDetail(uri: Uri, navController: NavController) {
        val companyId = uri.lastPathSegment ?: return
        navController.navigate("placement_company_detail/$companyId") { launchSingleTop = true }
    }
}
