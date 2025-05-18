package com.exa.android.reflekt.loopit.presentation.navigation.component

import android.net.Uri
import androidx.navigation.NavController
import com.exa.android.reflekt.loopit.util.model.Profile.CollegeInfo
import com.exa.android.reflekt.loopit.util.model.Profile.ExperienceInfo
import com.exa.android.reflekt.loopit.util.model.Profile.ExtraActivity
import com.exa.android.reflekt.loopit.util.model.Profile.ProfileData
import com.google.gson.Gson
import java.net.URLEncoder

sealed class AuthRoute(val route : String){
    object Login : AuthRoute("login")
    object Register : AuthRoute("register")
    object Verification : AuthRoute("verification")
    object ForgetPassword : AuthRoute("forget_password")
    object TermsCondition : AuthRoute("terms")
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
    object Photo : MainRoute("photo")
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

    object EditProfileHeader: ProjectRoute("edit_profile_header"){
        fun createRoute(navController: NavController,item: ProfileData) {

            navController.currentBackStackEntry?.savedStateHandle?.set("header", item)
            navController.navigate("edit_profile_header")
        }
    }

    object EditExtraCurricularScreen: ProjectRoute("edit_extra_card"){
        fun createRoute(navController: NavController,item: ExtraActivity) {
            navController.currentBackStackEntry?.savedStateHandle?.set("extra", item)
            navController.navigate("edit_extra_card")
        }
    }

    object FullExtraCard : ProjectRoute("full_extra_card/{userId}"){
        fun createRoute(navController: NavController,userId: String?, item: ExtraActivity) {
            navController.currentBackStackEntry?.savedStateHandle?.set("extra_card_data", item)
            navController.navigate("full_extra_card/${userId}")
        }
    }

    object EditEducation: ProjectRoute("edit_education"){
        fun createRoute(navController: NavController,item: CollegeInfo) {
            navController.currentBackStackEntry?.savedStateHandle?.set("college", item)
            navController.navigate("edit_education")
        }
    }

    object EditExperience: ProjectRoute("edit_experience"){
        fun createRoute(navController: NavController, item: ExperienceInfo) {
//            val json = Gson().toJson(item)
//            val encodedChatJson = URLEncoder.encode(json, "UTF-8")
////            val json = Uri.encode(Gson().toJson(item))
//            return "edit_experience/$encodedChatJson"
            navController.currentBackStackEntry?.savedStateHandle?.set("experience", item)
            navController.navigate("edit_experience")
        }
    }
    object VerifierScreen : ProfileRoute("verify?userId={userId}"){
        fun createRoute(userId: String?):String{
            return if (userId != null) "verify?userId=$userId" else "verify"
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


sealed class PhotoRoute(val route: String){
    object ViewPhotoUsingUrl : PhotoRoute("photo/view_photo?imageUrl={imageUrl}") {
        fun createRoute(imageUrl: String?): String {
            return "photo/view_photo?imageUrl=${Uri.encode(imageUrl)}"
        }
    }

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
