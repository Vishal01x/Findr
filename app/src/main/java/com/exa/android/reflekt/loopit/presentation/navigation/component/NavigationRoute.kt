package com.exa.android.reflekt.loopit.presentation.navigation.component

import android.net.Uri
import com.google.gson.Gson

sealed class AuthRoute(val route: String) {
    object Login : AuthRoute("login")
    object Register : AuthRoute("register")
    object ForgetPassword : AuthRoute("forget_password")
}

sealed class MainRoute(val route: String) {
    object Home : MainRoute("home")
    object Profile : MainRoute("status")
    object Setting : MainRoute("setting")
    object Map : MainRoute("map")
}


sealed class HomeRoute(val route: String) {
    object ChatList : HomeRoute("chats_list")

    //    object ChatDetail : HomeRoute("chat_detail/{userJson}"){
//        fun createRoute(userJson : String) : String = "chat_detail/${userJson}"
//    }
    object ChatDetail : HomeRoute("chat_detail/{userId}") {
        fun createRoute(userId: String): String = "chat_detail/${userId}"
    }

    object SearchScreen : HomeRoute("search")
    object ZoomImage : HomeRoute("zoomImage/{imageId}") {
        fun createRoute(imageId: Int): String = "zoomImage/$imageId"
    }
}

sealed class MapInfo(val route: String) {
    object MapScreen : MapInfo("map_screen")
}

sealed class MeetingRoute(val route: String) {
    data object LobbyScreen : MeetingRoute("meeting_lobby/{usersJson}") {
        fun createRoute(users: List<String>): String {
            val usersJson = Uri.encode(Gson().toJson(users))  // Convert list to JSON & encode
            return "meeting_lobby/$usersJson"  // Corrected the mismatch
        }
    }

    data object CallScreen : MeetingRoute("meeting_call")
}


sealed class ChatInfo(val route: String) {
    object ProfileScreen : ChatInfo("profile")
    object ChatMedia : ChatInfo("media")
    object ProfileImage : ChatInfo("photo")
    object StarredMessage : ChatInfo("starred")
    object MediaVisibility : ChatInfo("visibility")
    object BlockUser : ChatInfo("block")
}

sealed class Call(val route: String) {
    object VoiceCall : Call("voice")
    object VideoCall : Call("video")
}

sealed class NavigationCommand {
    object ToMainApp : NavigationCommand()
    object ToAuth : NavigationCommand()
}


var bottomSheet: Boolean = false
