package com.exa.android.reflekt.loopit.presentation.navigation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.*
import androidx.navigation.compose.composable
import com.google.gson.Gson
import java.net.URLDecoder
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigation
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
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.MainRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProfileRoute
import com.exa.android.reflekt.loopit.util.model.Profile.CollegeInfo
import com.exa.android.reflekt.loopit.util.model.Profile.ExperienceInfo
import com.exa.android.reflekt.loopit.util.model.Profile.ExtraActivity
import com.exa.android.reflekt.loopit.util.model.Profile.ProfileData
import com.exa.android.reflekt.loopit.util.model.Profile.ProfileHeaderData


fun NavGraphBuilder.mainAppNavGraph(context: Context,navController: NavHostController) {

    navigation(startDestination = "home", route = "main_app") {
        homeNavGraph(navController)


        composable(MainRoute.Setting.route) {
            StatusScreen(navController)
        }

        profileNavGraph(context,navController)
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
                    navController.navigate(ProfileRoute.EditProfileHeader.createRoute(profileData)){
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onAddExtraCard = {id,extra->
                    if(extra == null){
                        navController.navigate(ProfileRoute.EditExtraCurricularScreen.createRoute(
                            ExtraActivity()
                        )){
                            launchSingleTop = true
                            restoreState = true
                        }
                    }else {
                        navController.navigate(ProfileRoute.FullExtraCard.createRoute(id,extra)){
                            launchSingleTop = true
                            restoreState = true
                        }
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
                onEditEducation = {
                    navController.navigate(ProfileRoute.EditEducation.createRoute(it ?: CollegeInfo())){
                        launchSingleTop = true
                        restoreState = true
                    }
                },

                onEditExperience = {
                    navController.navigate(ProfileRoute.EditExperience.createRoute(it ?: ExperienceInfo())){
                        launchSingleTop = true
                        restoreState = true
                    }
                }

            )
        }

        composable(
            route = ProfileRoute.EditProfileHeader.route,
            arguments = listOf(navArgument("itemJson") { type = NavType.StringType })
        ) {
            val itemJson = it.arguments?.getString("itemJson")
            val item = remember(itemJson) {
                itemJson?.let {
                    val decoded = URLDecoder.decode(it, "UTF-8")
                    Gson().fromJson(decoded, ProfileData::class.java)
                }
            }

            EditProfileHeader(item ?:ProfileData(),navController)
        }

        composable(
            route = ProfileRoute.EditExtraCurricularScreen.route,
            arguments = listOf(navArgument("itemJson") { type = NavType.StringType })
        ) {

            val itemJson = it.arguments?.getString("itemJson")
            val item = remember(itemJson) {
                itemJson?.let {
                    val decoded = URLDecoder.decode(it, "UTF-8")
                    Gson().fromJson(decoded, ExtraActivity::class.java)
                }
            }
            EditExtracurricularScreen(item ?: ExtraActivity(),navController)
        }

        composable(
            route = ProfileRoute.EditEducation.route,
            arguments = listOf(navArgument("itemJson") { type = NavType.StringType })
        ) {

            val itemJson = it.arguments?.getString("itemJson")
            val item = remember(itemJson) {
                itemJson?.let {
                    val decoded = URLDecoder.decode(it, "UTF-8")
                    Gson().fromJson(decoded, CollegeInfo::class.java)
                }
            }
            EditEducationScreen(navController,item ?: CollegeInfo())
        }

        composable(
            route = ProfileRoute.EditExperience.route,
            arguments = listOf(
                navArgument("itemJson") { type = NavType.StringType })
        ) {

            val itemJson = it.arguments?.getString("itemJson")
            val item = remember(itemJson) {
                itemJson?.let {
                    val decoded = URLDecoder.decode(it, "UTF-8")
                    Gson().fromJson(decoded, ExperienceInfo::class.java)
                }
            }
            EditExperienceScreen(navController,item ?: ExperienceInfo())
        }

        composable(
            route = ProfileRoute.FullExtraCard.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType
                                      nullable = true
                                      defaultValue = null},
                navArgument("itemJson") { type = NavType.StringType })
        ) {

            val userId = it.arguments?.getString("userId")
            val itemJson = it.arguments?.getString("itemJson")
            val item = remember(itemJson) {
                itemJson?.let {
                    val decoded = URLDecoder.decode(it, "UTF-8")
                    Gson().fromJson(decoded, ExtraActivity::class.java)
                }
            }

            if (item != null) {
                FullExtraCardScreen(navController,
                    userId.isNullOrEmpty(), item, onEditClick =  {
                    navController.navigate(ProfileRoute.EditExtraCurricularScreen.createRoute(it)){
                        launchSingleTop = true
                        restoreState = true
                    }
                })
            }
        }

        composable(route = ProfileRoute.ProfileSetting.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigate = {route-> navController.navigate(route) }
            )
        }

        composable(ProfileRoute.ProfileHelp.route) {
            HelpScreen(
                onBack = { navController.popBackStack() },
                onContactSupport = {
                    // Handle contact support action
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "message/rfc822"
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("support@yourapp.com"))
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
                    }
                },
                onFaqItemClick = { question ->
                    // Handle FAQ item click if needed
                }
            )
        }
        composable(ProfileRoute.ProfileTerms.route) {
            TermsPrivacyScreen(
                onBack = { navController.popBackStack() },
                // Only include onAcceptTerms if this is a mandatory acceptance screen
                // Otherwise omit the parameter to hide the accept button
                onAcceptTerms = { accepted ->
                    if (accepted) {
                        // Save acceptance and proceed
                        navController.popBackStack()
                    }
                }
            )
        }

    }
}


//object MainRoutes {
//    const val MainApp = "main_app"
//    const val Profile = "profile"
//    const val Settings = "settings"
//}