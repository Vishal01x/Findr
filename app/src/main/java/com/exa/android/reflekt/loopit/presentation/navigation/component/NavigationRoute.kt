package com.exa.android.reflekt.loopit.presentation.navigation.component

import android.net.Uri
import com.exa.android.reflekt.loopit.util.model.Profile.CollegeInfo
import com.exa.android.reflekt.loopit.util.model.Profile.ExperienceInfo
import com.exa.android.reflekt.loopit.util.model.Profile.ExtraActivity
import com.exa.android.reflekt.loopit.util.model.Profile.ProfileData
import com.google.gson.Gson

sealed class AuthRoute(val route : String){
    object Login : AuthRoute("login")
    object Register : AuthRoute("register")
    object Verification : AuthRoute("verification")
    object ForgetPassword : AuthRoute("forget_password")
    companion object{
        const val ROOT="auth"
    }
}



sealed class MainRoute(val route : String){
    object Home : MainRoute("home")
    object Profile : MainRoute("profile")
    object Setting : MainRoute("setting")
    object Map : MainRoute("map_graph")
    object Project : MainRoute("project_graph")
    companion object{
        const val ROOT="main_app"
    }
}

sealed class ProfileRoute(val route: String) {
    object UserProfile : ProjectRoute("user_profile?userId={userId}") {
        fun createRoute(userId: String? = null): String {
            return if (userId != null) "user_profile?userId=$userId" else "user_profile"
        }
    }

    object EditProfileHeader: ProjectRoute("edit_profile_header/{itemJson}"){
        fun createRoute(item: ProfileData): String {
            val json = Uri.encode(Gson().toJson(item))
            return "edit_profile_header/$json"
        }
    }

    object EditExtraCurricularScreen: ProjectRoute("edit_extra_card/{itemJson}"){
        fun createRoute(item: ExtraActivity): String {
            val json = Uri.encode(Gson().toJson(item))
            return "edit_extra_card/$json"
        }
    }

    object FullExtraCard : ProjectRoute("full_extra_card/{userId}/{itemJson}"){
        fun createRoute(userId: String?, item: ExtraActivity): String {
            val json = Uri.encode(Gson().toJson(item))
            return "full_extra_card/$userId/$json"
        }
    }

    object EditEducation: ProjectRoute("edit_education/{itemJson}"){
        fun createRoute(item: CollegeInfo): String {
            val json = Uri.encode(Gson().toJson(item))
            return "edit_education/$json"
        }
    }

    object EditExperience: ProjectRoute("edit_experience/{itemJson}"){
        fun createRoute(item: ExperienceInfo): String {
            val json = Uri.encode(Gson().toJson(item))
            return "edit_experience/$json"
        }
    }

    object ProfileSetting : ProfileRoute("profile_setting")
    object ProfileHelp : ProfileRoute("help")
    object ProfileTerms : ProfileRoute("terms")

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

sealed class ProjectRoute(val route: String) {
    object ProjectList : ProjectRoute("project_list")
    object ProjectDetail : ProjectRoute("project_detail/{projectId}") {
        fun createRoute(projectId: String): String = "project_detail/${projectId}"
    }
    object CreateProject : ProjectRoute("create_project")
    object EditProject : ProjectRoute("edit_project/{projectId}") {
        fun createRoute(projectId: String): String = "edit_project/${projectId}"
    }
    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
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
    object ProfileScreen : ChatInfo("profilee")
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
