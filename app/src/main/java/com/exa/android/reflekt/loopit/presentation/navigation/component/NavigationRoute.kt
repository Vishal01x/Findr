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
    object Placement : MainRoute("placement_graph")
    object Setting : MainRoute("setting")
    object Map : MainRoute("map_graph")
    object Project : MainRoute("post_graph")
    object Photo : MainRoute("photo")
    object Notification : MainRoute("notification")
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

sealed class PlacementRoute(val route: String) {
    object Timeline : PlacementRoute("placement_timeline")
    object RecruitingCompanies : PlacementRoute("placement_companies")
    object Statistics : PlacementRoute("placement_statistics")
    object CompanyDetail : PlacementRoute("placement_company_detail/{companyId}") {
        fun createRoute(companyId: String): String = "placement_company_detail/$companyId"
    }
    object CompanyInsights : PlacementRoute("placement_company_insights/{companyId}") {
        fun createRoute(companyId: String): String = "placement_company_insights/$companyId"
    }
    object EligibilityForm : PlacementRoute("placement_eligibility_form")
    object AddCompanyForm : PlacementRoute("placement_add_company_form")
    object PlacedStudents : PlacementRoute("placement_placed_students?company={company}&batch={batch}&packageMin={packageMin}&packageMax={packageMax}") {
        fun createRoute(company: String? = null, batch: Int? = null, packageMin: Float? = null, packageMax: Float? = null): String {
            // Company names can contain spaces/&/other characters that would otherwise
            // corrupt the query string, so they must be percent-encoded here.
            val encodedCompany = company?.let { android.net.Uri.encode(it) } ?: ""
            return "placement_placed_students?company=$encodedCompany&batch=${batch ?: ""}&packageMin=${packageMin ?: ""}&packageMax=${packageMax ?: ""}"
        }
    }
    object Analytics : PlacementRoute("placement_analytics")
    object Prediction : PlacementRoute("placement_prediction")
}

sealed class HomeRoute(val route: String) {
    object ChatList : HomeRoute("chats_list")

    //    object ChatDetail : HomeRoute("chat_detail/{userJson}"){
//        fun createRoute(userJson : String) : String = "chat_detail/${userJson}"
//    }
    object ChatDetail : HomeRoute("chat_detail/{userId}?eventTitle={eventTitle}") {
        fun createRoute(userId: String, eventTitle: String? = null): String =
            if (!eventTitle.isNullOrBlank()) "chat_detail/$userId?eventTitle=${android.net.Uri.encode(eventTitle)}"
            else "chat_detail/$userId"
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
    object PostDetail : PostRoute("project_detail/{projectId}?scrollTo={scrollTo}") {
        fun createRoute(projectId: String, scrollTo: String = ""): String =
            if (scrollTo.isBlank()) "project_detail/$projectId"
            else "project_detail/$projectId?scrollTo=$scrollTo"
    }
    // Legacy single-screen create (kept for backward compat)
    object CreatePost : PostRoute("create_post")
    // New multi-screen create flow
    object CreatePostSelection : PostRoute("create_post_selection")
    object CreateProject : PostRoute("create_project?postId={postId}") {
        fun createRoute(postId: String? = null): String =
            if (postId != null) "create_project?postId=$postId" else "create_project"
    }
    object CreateEvent : PostRoute("create_event?postId={postId}") {
        fun createRoute(postId: String? = null): String =
            if (postId != null) "create_event?postId=$postId" else "create_event"
    }
    object CreateSharePost : PostRoute("create_share_post?postId={postId}") {
        fun createRoute(postId: String? = null): String =
            if (postId != null) "create_share_post?postId=$postId" else "create_share_post"
    }
    object CreatePoll : PostRoute("create_poll?postId={postId}") {
        fun createRoute(postId: String? = null): String =
            if (postId != null) "create_poll?postId=$postId" else "create_poll"
    }
    object MarkdownEditor : PostRoute("markdown_editor")
    object EditPost : PostRoute("edit_post/{postId}") {
        fun createRoute(postId: String): String = "edit_post/${postId}"
    }
    object MyPosts : PostRoute("my_posts?userId={userId}") {
        fun createRoute(userId: String = ""): String =
            if (userId.isBlank()) "my_posts"
            else "my_posts?userId=$userId"
    }
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
    object UserSearch : PostRoute("user_search/{postId}?title={title}") {
        fun createRoute(postId: String, title: String): String =
            "user_search/$postId?title=${android.net.Uri.encode(title)}"
    }
    // mode = "project" (enrolled collaborators) or "event" (registered participants)
    object ShortlistedUsers : PostRoute("shortlisted_users/{postId}?mode={mode}&title={title}") {
        fun createRoute(postId: String, mode: String, title: String): String =
            "shortlisted_users/$postId?mode=$mode&title=${android.net.Uri.encode(title)}"
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
