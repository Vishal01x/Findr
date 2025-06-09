package com.exa.android.reflekt.loopit.fcm

import android.net.Uri
import androidx.navigation.NavController

object DeepLinkHelper {
    fun handleDeepLink(navController: NavController, uri: Uri) {
        when (uri.pathSegments.firstOrNull()) {
            "chat" -> navigateToChat(uri, navController)
            "project" -> navigateToProject(uri, navController)
//            "profile" -> navigateToProfile(uri, navController)
//            "post" -> navigateToPost(uri, navController)
//            "update" -> navigateToUpdate(uri, navController)
        }
    }

    private fun navigateToChat(uri: Uri, navController: NavController) {
        val chatId = uri.lastPathSegment ?: return
        navController.navigate("app://chat/$chatId")
    }

    private fun navigateToProject(uri: Uri, navController: NavController) {
        val projectId = uri.getQueryParameter("id") ?: return
        navController.navigate("app://project?id=$projectId")
    }

    // Add other navigation handlers
}