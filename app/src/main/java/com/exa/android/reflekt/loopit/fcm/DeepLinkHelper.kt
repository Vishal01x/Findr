package com.exa.android.reflekt.loopit.fcm

import android.net.Uri
import androidx.navigation.NavController

object DeepLinkHelper {
    fun handleDeepLink(navController: NavController, uri: Uri) {
        // Handle both custom scheme (findr://) and HTTP scheme (https://findr.app/)
        val path = when {
            uri.scheme == "findr" -> uri.pathSegments.firstOrNull()
            uri.host == "findr.app" -> uri.pathSegments.firstOrNull()
            else -> null
        }
        
        when (path) {
            "chat" -> navigateToChat(uri, navController)
            "project" -> navigateToProject(uri, navController)
            "post" -> navigateToPost(uri, navController)
//            "profile" -> navigateToProfile(uri, navController)
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

    private fun navigateToPost(uri: Uri, navController: NavController) {
        // Extract postId from either findr://post/{id} or https://findr.app/post/{id}
        val postId = uri.pathSegments.lastOrNull() ?: return
        navController.navigate("project_detail/$postId")
    }

    // Add other navigation handlers
}