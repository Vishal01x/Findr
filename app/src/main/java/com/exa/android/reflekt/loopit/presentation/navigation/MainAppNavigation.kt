package com.exa.android.reflekt.loopit.presentation.navigation

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.collection.intLongMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.composable
import com.google.gson.Gson
import java.net.URLDecoder
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigation
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.LocationViewModel
import com.exa.android.reflekt.loopit.presentation.main.Home.component.PhotoViewerScreen
import com.exa.android.reflekt.loopit.presentation.main.StatusScreen
import com.exa.android.reflekt.loopit.presentation.main.profile.ProfileScreen
import com.exa.android.reflekt.loopit.presentation.main.profile.components.education.EditEducationScreen
import com.exa.android.reflekt.loopit.presentation.main.profile.components.education.EditExperienceScreen
import com.exa.android.reflekt.loopit.presentation.main.profile.components.extra_card.EditExtracurricularScreen
import com.exa.android.reflekt.loopit.presentation.main.profile.components.extra_card.ExtracurricularCard
import com.exa.android.reflekt.loopit.presentation.main.profile.components.extra_card.FullExtraCardScreen
import com.exa.android.reflekt.loopit.presentation.main.profile.components.header.EditProfileHeader
import com.exa.android.reflekt.loopit.presentation.main.profile.components.setting.HelpScreen
import com.exa.android.reflekt.loopit.presentation.main.profile.components.setting.SettingsScreen
import com.exa.android.reflekt.loopit.presentation.main.profile.components.setting.TermsPrivacyScreen
import com.exa.android.reflekt.loopit.presentation.main.profile.components.setting.TermsPrivacyWebView
import com.exa.android.reflekt.loopit.presentation.main.profile.feedback.VerifierScreen
import com.exa.android.reflekt.loopit.presentation.navigation.component.AuthRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.MainRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.PhotoRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProfileRoute
import com.exa.android.reflekt.loopit.util.model.Profile.CollegeInfo
import com.exa.android.reflekt.loopit.util.model.Profile.ExperienceInfo
import com.exa.android.reflekt.loopit.util.model.Profile.ExtraActivity
import com.exa.android.reflekt.loopit.util.model.Profile.ProfileData
import com.exa.android.reflekt.loopit.util.model.Profile.ProfileHeaderData
import com.exa.android.reflekt.loopit.util.showToast


fun NavGraphBuilder.mainAppNavGraph(context: Context,navController: NavHostController,locationViewModel: LocationViewModel) {

    navigation(startDestination = "map_graph", route = "main_app") {
        homeNavGraph(navController)


        composable(MainRoute.Setting.route) {
            StatusScreen(navController)
        }
        mapNavGraph(navController, locationViewModel)
        profileNavGraph(context,navController)
        photoViewScreen(navController)
    }


}


fun NavGraphBuilder.profileNavGraph(context : Context, navController: NavHostController) {

    navigation(startDestination = ProfileRoute.UserProfile.route, route = "profile") {
        composable(
            route = ProfileRoute.UserProfile.route,
            arguments = listOf(navArgument("userId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            val userId = it.arguments?.getString("userId")

            ProfileScreen(
                userId,
                navController,
                onEditHeaderClick = {profileData ->
                    ProfileRoute.EditProfileHeader.createRoute(navController,profileData)
                },
                onAddExtraCard = {id,extra->
                    if(extra == null){
                        ProfileRoute.EditExtraCurricularScreen.createRoute(navController,ExtraActivity())
                    }else {
                        ProfileRoute.FullExtraCard.createRoute(navController,id,extra)
                    }
                },
                openChat = { userId ->
                    if (userId != null) {
                        navController.navigate(HomeRoute.ChatDetail.createRoute(userId)) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                openImage = {imageUrl ->
                    navController.navigate(PhotoRoute.ViewPhotoUsingUrl.createRoute(imageUrl = imageUrl))
                },
                onEditEducation = {
                    ProfileRoute.EditEducation.createRoute(navController,it ?: CollegeInfo())
                },

                onEditExperience = {
                    ProfileRoute.EditExperience.createRoute(navController,it ?: ExperienceInfo())
                },

                onViewVerifier = {userId->
                    navController.navigate(ProfileRoute.VerifierScreen.createRoute(userId))
                }
            )
        }

        composable(
            route = ProfileRoute.EditProfileHeader.route,
            //arguments = listOf(navArgument("itemJson") { type = NavType.StringType })
        ) {
            val item = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<ProfileData>("header")

            EditProfileHeader(item ?:ProfileData(),navController)
        }

        composable(
            route = ProfileRoute.EditExtraCurricularScreen.route,
           // arguments = listOf(navArgument("itemJson") { type = NavType.StringType })
        ) {

            val item = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<ExtraActivity>("extra")
            EditExtracurricularScreen(item ?: ExtraActivity(),navController)
        }

        composable(
            route = ProfileRoute.EditEducation.route,
            //arguments = listOf(navArgument("itemJson") { type = NavType.StringType })
        ) {

            val item = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<CollegeInfo?>("college")

            EditEducationScreen(navController,item ?: CollegeInfo())
        }

        composable(
            route = ProfileRoute.EditExperience.route,
//            arguments = listOf(
//                navArgument("itemJson") { type = NavType.StringType })
        ) {

//
            val item = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<ExperienceInfo>("experience")


            EditExperienceScreen(navController,item ?: ExperienceInfo())
        }

        composable(
            "full_extra_card/{userId}",
            arguments = listOf(navArgument("userId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            val item = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<ExtraActivity>("extra_card_data") ?: ExtraActivity()

            if (item != null) {
                FullExtraCardScreen(navController,
                    userId.isNullOrEmpty(), item, onEditClick =  {
                    ProfileRoute.EditExtraCurricularScreen.createRoute(navController,it)
                })
            }
        }

        composable(route = ProfileRoute.ProfileSetting.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigate = {route-> navController.navigate(route) },
                onLogOutClick = {navController.navigate(AuthRoute.Login.route){
                    popUpTo(0) { inclusive = true } // Pops everything
                    launchSingleTop = true // Avoid multiple instances
                }
                    showToast(context,"Logout Successfully")
                },
                onDeleteAccount = {
                    navController.navigate(AuthRoute.Login.route){
                        popUpTo(0) { inclusive = true } // Pops everything
                        launchSingleTop = true // Avoid multiple instances
                    }
                    showToast(context,"Account Delete")
                }


            )
        }

        composable(
            route = ProfileRoute.VerifierScreen.route,
            arguments = listOf(navArgument("userId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ){backStackEntry->
            val userId = backStackEntry.arguments?.getString("userId")
            VerifierScreen(
                userId,
                onProfileClick = {userId->
                    navController.navigate(ProfileRoute.UserProfile.createRoute(userId))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }


        composable(ProfileRoute.ProfileHelp.route) {
            HelpScreen(
                onBack = { navController.popBackStack() },
                onContactSupport = {
                    // Handle contact support action
                    /*val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "message/rfc822"
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("ankitraj4323@gmail.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "App Support Request")
                    }
                    try {
                        context.startActivity(Intent.createChooser(intent, "Send email..."))
                    } catch (e: ActivityNotFoundException) {
                        // Handle case where no email client is installed
                        Toast.makeText(
                            context,
                            "Please install an email app to contact support",
                            Toast.LENGTH_LONG
                        ).show()
                    }*/
                    showContactSupportOptions(context)
                },
                onFaqItemClick = { question ->
                    // Handle FAQ item click if needed
                }
            )
        }
        composable(ProfileRoute.ProfileTerms.route) {
            /*TermsPrivacyScreen(
                onBack = { navController.popBackStack() },
                // Only include onAcceptTerms if this is a mandatory acceptance screen
                // Otherwise omit the parameter to hide the accept button
                onAcceptTerms = { accepted ->
                    if (accepted) {
                        // Save acceptance and proceed
                        navController.popBackStack()
                    }
                }
            )*/
            TermsPrivacyWebView(
                onBack = { navController.popBackStack() }
            )
        }

    }
}


fun showContactSupportOptions(context: Context) {
    val options = listOf("Email", "WhatsApp")

    AlertDialog.Builder(context)
        .setTitle("Contact Support")
        .setItems(options.toTypedArray()) { _, which ->
            when (options[which]) {
                "Email" -> {
                    val email = "findr.contact.help@gmail.com"
                    val subject = Uri.encode("App Support Request")
                    val body = Uri.encode(
                        "Hi Findr Support,\n\nI'm experiencing an issue with the app. Please help me with the following:\n\n[Describe your issue here]\n\nThank you!"
                    )
                    val mailto = "mailto:$email?subject=$subject&body=$body"

                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(mailto)
                    }

                    try {
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(
                            context,
                            "Please install an email app to contact support",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }


                "WhatsApp" -> {
                    val phoneNumber = "7645992680" // Replace with your full phone number (country code + number, no "+")
                    val message = "Hi there! I need help with the app."
                    val url = "https://wa.me/$phoneNumber?text=${Uri.encode(message)}"
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(url)
                        setPackage("com.whatsapp") // This restricts the intent to WhatsApp only
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(
                            context,
                            "Please install WhatsApp to contact support",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
        .show()
}


fun NavGraphBuilder.photoViewScreen(navController: NavHostController) {
    navigation(startDestination = PhotoRoute.ViewPhotoUsingUrl.route, route = "photo") {
        composable(
            route = PhotoRoute.ViewPhotoUsingUrl.route,
            arguments = listOf(navArgument("imageUrl") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            val encodedUrl = it.arguments?.getString("imageUrl")
            val decodedUrl = encodedUrl?.let { Uri.decode(it) }

            PhotoViewerScreen(imageUrl = decodedUrl ?:"") {
                navController.popBackStack()
            }
        }
    }
}


//object MainRoutes {
//    const val MainApp = "main_app"
//    const val Profile = "profile"
//    const val Settings = "settings"
//}