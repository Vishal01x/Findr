package com.exa.android.reflekt.loopit.presentation.navigation.component

import android.net.Uri
import androidx.navigation.NavController
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.CollegeInfo
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.ExperienceInfo
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.ExtraActivity
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.ProfileData
import com.google.gson.Gson

sealed class AuthRoute(val route : String){
    object Onboarding : AuthRoute("onboarding")
    object Login : AuthRoute("login")
    object Register : AuthRoute("register")
    object Verification : AuthRoute("verification")
    object ForgetPassword : AuthRoute("forget_password")
    object TermsCondition : AuthRoute("terms")
    object GoogleProfileSetup : AuthRoute("google_profile_setup")
    companion object{
        const val ROOT="auth"
    }
}



sealed class MainRoute(val route : String){
    object Home : MainRoute("home")
    object Profile : MainRoute("profile")
    object Setting : MainRoute("setting")
    object Map : MainRoute("map_graph")
    object Project : MainRoute("post_graph")
    object Photo : MainRoute("photo")
    companion object{
        const val ROOT="main_app"
    }
}

sealed class ProfileRoute(val route: String) {
    object UserProfile : PostRoute("user_profile?userId={userId}") {
        fun createRoute(userId: String? = null): String {
            return if (userId != null) "user_profile?userId=$userId" else "user_profile"
        }
    }

    object EditProfileHeader: PostRoute("edit_profile_header"){
        fun createRoute(navController: NavController,item: ProfileData) {

            navController.currentBackStackEntry?.savedStateHandle?.set("header", item)
            navController.navigate("edit_profile_header")
        }
    }

    object EditExtraCurricularScreen: PostRoute("edit_extra_card"){
        fun createRoute(navController: NavController,item: ExtraActivity) {
            navController.currentBackStackEntry?.savedStateHandle?.set("extra", item)
            navController.navigate("edit_extra_card")
        }
    }

    object FullExtraCard : PostRoute("full_extra_card/{userId}"){
        fun createRoute(navController: NavController,userId: String?, item: ExtraActivity) {
            navController.currentBackStackEntry?.savedStateHandle?.set("extra_card_data", item)
            navController.navigate("full_extra_card/${userId}")
        }
    }

    object EditEducation: PostRoute("edit_education"){
        fun createRoute(navController: NavController,item: CollegeInfo) {
            navController.currentBackStackEntry?.savedStateHandle?.set("college", item)
            navController.navigate("edit_education")
        }
    }

    object EditExperience: PostRoute("edit_experience"){
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
    object ProfileViewer : ProfileRoute("profile_viewer")
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
    object MapScreen2 : MapInfo("map_screen2_0")
    object MapSearchScreen2 : MapInfo("searchScreen2_0")
    object UserListScreen : MapInfo("userListScreen")
}


sealed class PhotoRoute(val route: String){
    object ViewPhotoUsingUrl : PhotoRoute("photo/view_photo?imageUrl={imageUrl}") {
        fun createRoute(imageUrl: String?): String {
            return "photo/view_photo?imageUrl=${Uri.encode(imageUrl)}"
        }
    }

}


sealed class PostRoute(val route: String) {
    object PostList : PostRoute("project_list")
    object PostDetail : PostRoute("project_detail/{projectId}") {
        fun createRoute(projectId: String): String = "project_detail/${projectId}"
    }
    object CreatePost : PostRoute("create_post")
    object EditPost : PostRoute("edit_post/{postId}") {
        fun createRoute(postId: String): String = "edit_post/${postId}"
    }
    object MyPosts : PostRoute("my_posts")
    object EnrollmentManagement : PostRoute("enrollment_management/{postId}") {
        fun createRoute(postId: String): String = "enrollment_management/${postId}"
    }
    object ManageEnrollments : PostRoute("manage_enrollments/{postId}") {
        fun createRoute(postId: String): String = "manage_enrollments/${postId}"
    }
    object ManageRegistrations : PostRoute("manage_registrations/{postId}") {
        fun createRoute(postId: String): String = "manage_registrations/${postId}"
    }
    object AuthorManagement : PostRoute("author_management/{postId}") {
        fun createRoute(postId: String): String = "author_management/${postId}"
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
