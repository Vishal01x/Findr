package com.exa.android.reflekt.loopit.presentation.navigation.nav_graph

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.navigation.*
import androidx.navigation.compose.composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigation
import com.exa.android.reflekt.loopit.data.feature.main.map.data.viewmodel.LocationViewModel
import com.exa.android.reflekt.loopit.presentation.main.component.PhotoViewerScreen
import com.exa.android.reflekt.loopit.presentation.main.profile.ProfileScreen
import com.exa.android.reflekt.loopit.presentation.main.profile.components.education.EditEducationScreen
import com.exa.android.reflekt.loopit.presentation.main.profile.components.education.EditExperienceScreen
import com.exa.android.reflekt.loopit.presentation.main.profile.components.extra_card.EditExtracurricularScreen
import com.exa.android.reflekt.loopit.presentation.main.profile.components.extra_card.FullExtraCardScreen
import com.exa.android.reflekt.loopit.presentation.main.profile.components.header.EditProfileHeader
import com.exa.android.reflekt.loopit.presentation.main.profile.components.setting.HelpScreen
import com.exa.android.reflekt.loopit.presentation.main.profile.components.setting.SettingsScreen
import com.exa.android.reflekt.loopit.presentation.main.profile.components.setting.TermsPrivacyWebView
import com.exa.android.reflekt.loopit.presentation.main.profile.feedback.VerifierScreen
import com.exa.android.reflekt.loopit.presentation.navigation.component.AuthRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.PhotoRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProfileRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.PlacementRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.MainRoute
import com.exa.android.reflekt.loopit.presentation.main.placement.RecruitmentSeasonTimelineScreen
import com.exa.android.reflekt.loopit.presentation.main.placement.RecruitingCompaniesScreen
import com.exa.android.reflekt.loopit.presentation.main.placement.YearlyPlacementStatisticsScreen
import com.exa.android.reflekt.loopit.presentation.main.placement.CompanyDetailScreen
import com.exa.android.reflekt.loopit.presentation.main.placement.DetailedCompanyInsightsScreen
import com.exa.android.reflekt.loopit.presentation.main.placement.EligibilityFormScreen
import com.exa.android.reflekt.loopit.presentation.main.placement.AddCompanyFormScreen
import com.exa.android.reflekt.loopit.presentation.main.placement.PlacedStudentsScreen
import com.exa.android.reflekt.loopit.presentation.main.placement.PlacementPredictionScreen
import com.exa.android.reflekt.loopit.presentation.main.placement.vm.RecruitingCompaniesViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.Color
import com.exa.android.reflekt.loopit.presentation.main.notification.screen.NotificationScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.CollegeInfo
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.ExperienceInfo
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.ExtraActivity
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.ProfileData
import com.exa.android.reflekt.loopit.core.utils.showToast
import com.exa.android.reflekt.loopit.presentation.navigation.component.PostRoute


fun NavGraphBuilder.mainAppNavGraph(context: Context,navController: NavHostController,locationViewModel: LocationViewModel) {

    navigation(startDestination = "post_graph", route = "main_app") {
        homeNavGraph(navController)
        mapNavGraph(navController, locationViewModel)
        postNavGraph(navController)
        placementNavGraph(navController)
        profileNavGraph(context,navController)
        photoViewScreen(navController)
        
        composable(MainRoute.Notification.route) {
            NotificationScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(ProfileRoute.ProfileSetting.route) },
                onNavigateToRoute = { route -> 
                    try {
                        if (route.contains("://")) {
                            val request = androidx.navigation.NavDeepLinkRequest.Builder
                                .fromUri(android.net.Uri.parse(route))
                                .build()
                            navController.navigate(request)
                        } else {
                            navController.navigate(route)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                onNavigateToChat = { userId -> navController.navigate(HomeRoute.ChatDetail.createRoute(userId)) },
                onNavigateToProfile = { userId -> navController.navigate(ProfileRoute.UserProfile.createRoute(userId)) },
                onNavigateToPost = { postId -> navController.navigate(com.exa.android.reflekt.loopit.presentation.navigation.component.PostRoute.PostDetail.createRoute(postId)) }
            )
        }
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
            }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "findr://profile/{userId}" },
                navDeepLink { uriPattern = "https://reflekt.com/profile/{userId}" }
            )
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
                        ProfileRoute.EditExtraCurricularScreen.createRoute(navController,
                            ExtraActivity()
                        )
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

            EditProfileHeader(item ?: ProfileData(),navController)
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
                true,
                userId,
                onProfileClick = {userId->
                    navController.navigate(ProfileRoute.UserProfile.createRoute(userId))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = ProfileRoute.ProfileViewer.route,
            arguments = listOf(navArgument("userId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }),
            deepLinks = listOf(navDeepLink { uriPattern = "findr://profile_views/{userId}" })
        ){backStackEntry->
            val userId = backStackEntry.arguments?.getString("userId")
            VerifierScreen(
                false,
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



fun NavGraphBuilder.placementNavGraph(navController: NavHostController) {
    navigation(startDestination = PlacementRoute.Statistics.route, route = MainRoute.Placement.route) {
        composable(PlacementRoute.Timeline.route) {
            val viewModel: com.exa.android.reflekt.loopit.presentation.main.placement.vm.PlacementHomeViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            RecruitmentSeasonTimelineScreen(
                onBack = { navController.popBackStack() },
                onCompanyClick = { companyId -> navController.navigate(PlacementRoute.CompanyDetail.createRoute(companyId)) },
                companies = state.companies
            )
        }
        composable(PlacementRoute.RecruitingCompanies.route) {
            val viewModel: RecruitingCompaniesViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()

            RecruitingCompaniesScreen(
                companies = state.companies,
                isAdmin = state.isAdmin,
                onBack = { navController.popBackStack() },
                onCompanyClick = { company -> navController.navigate(PlacementRoute.CompanyDetail.createRoute(company.id)) },
                onAddCompanyClick = { navController.navigate(PlacementRoute.AddCompanyForm.route) }
            )
        }
        composable(PlacementRoute.AddCompanyForm.route) {
            AddCompanyFormScreen(
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }
        composable(PlacementRoute.Statistics.route) {
            val viewModel: com.exa.android.reflekt.loopit.presentation.main.placement.vm.PlacementHomeViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacementHomeSideEffect.NavigateToCompanyDetail ->
                            navController.navigate(PlacementRoute.CompanyDetail.createRoute(effect.companyId))
                        is com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacementHomeSideEffect.NavigateToAddCompany ->
                            navController.navigate(PlacementRoute.AddCompanyForm.route)
                        is com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacementHomeSideEffect.NavigateToAllCompanies ->
                            navController.navigate(PlacementRoute.RecruitingCompanies.route)
                        is com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacementHomeSideEffect.NavigateToTimeline ->
                            navController.navigate(PlacementRoute.Timeline.route)
                        is com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacementHomeSideEffect.NavigateToAlumni ->
                            navController.navigate(PlacementRoute.PlacedStudents.createRoute())
                        is com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacementHomeSideEffect.ShowToast ->
                            Unit // Toast is handled via state.seedResult snackbar in the screen
                    }
                }
            }

            YearlyPlacementStatisticsScreen(
                onBack = { navController.navigate(PostRoute.PostList.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
                },
                onViewTimelineClick = { viewModel.onEvent(com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacementHomeEvent.ViewTimelineClicked) },
                onViewCompaniesClick = { viewModel.onEvent(com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacementHomeEvent.ViewAllCompaniesClicked) },
                onViewAlumniClick = { viewModel.onEvent(com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacementHomeEvent.ViewAllAlumniClicked) },
                onViewAlumniWithFiltersClick = { batch, minPkg, maxPkg ->
                    navController.navigate(PlacementRoute.PlacedStudents.createRoute(batch = batch, packageMin = minPkg, packageMax = maxPkg))
                },
                onAlumniBatchSelected = { batch -> viewModel.onEvent(com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacementHomeEvent.AlumniBatchSelected(batch)) },
                alumniSelectedBatch = state.alumniSelectedBatch,
                onEventClick = { postId -> navController.navigate(com.exa.android.reflekt.loopit.presentation.navigation.component.PostRoute.PostDetail.createRoute(postId)) },
                isAdmin = state.isAdmin,
                onAddCompanyClick = { viewModel.onEvent(com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacementHomeEvent.AddCompanyClicked) },
                onSeedDataClick = { viewModel.onEvent(com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacementHomeEvent.SeedData) },
                isSeeding = state.isSeeding,
                seedResult = state.seedResult,
                onDismissSeedResult = { viewModel.onEvent(com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacementHomeEvent.DismissSeedResult) },
                companies = state.companies,
                onCompanyClick = { id -> viewModel.onEvent(com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacementHomeEvent.CompanyClicked(id)) },
                onCompareBatchesClick = { navController.navigate(PlacementRoute.Analytics.route) },
                onAiPredictionClick = { navController.navigate(PlacementRoute.Prediction.route) }
            )
        }
        composable(
            route = PlacementRoute.CompanyDetail.route,
            arguments = listOf(navArgument("companyId") { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://reflekt.com/company/{companyId}" },
                navDeepLink { uriPattern = "findr://company/{companyId}" }
            )
        ) { backStackEntry ->
            val companyId = backStackEntry.arguments?.getString("companyId") ?: ""
            CompanyDetailScreen(
                companyId = companyId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEligibilityForm = { navController.navigate(PlacementRoute.EligibilityForm.route) },
                onNavigateToPlacedStudents = { batch, company ->
                    navController.navigate(PlacementRoute.PlacedStudents.createRoute(batch = batch, company = company))
                }
            )
        }
        composable(PlacementRoute.Analytics.route) {
            com.exa.android.reflekt.loopit.presentation.main.placement.PlacementAnalyticsScreen(navController = navController)
        }
        composable(
            route = PlacementRoute.CompanyInsights.route,
            arguments = listOf(navArgument("companyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val companyId = backStackEntry.arguments?.getString("companyId") ?: ""
            DetailedCompanyInsightsScreen(
                companyId = companyId,
                onBack = { navController.popBackStack() },
                onCheckEligibility = { navController.navigate(PlacementRoute.EligibilityForm.route) }
            )
        }
        composable(PlacementRoute.EligibilityForm.route) {
            EligibilityFormScreen(
                onBack = { navController.popBackStack() },
                onSubmit = { navController.popBackStack() } // Navigate back on submit for now
            )
        }
        composable(PlacementRoute.Prediction.route) {
            PlacementPredictionScreen(
                onBack = { navController.popBackStack() },
                onNavigateToCompanyDetail = { companyId ->
                    navController.navigate(PlacementRoute.CompanyDetail.createRoute(companyId))
                }
            )
        }
        composable(
            route = PlacementRoute.PlacedStudents.route,
            arguments = listOf(
                navArgument("company") { type = NavType.StringType; defaultValue = "" },
                navArgument("batch") { type = NavType.StringType; defaultValue = "" },
                navArgument("packageMin") { type = NavType.StringType; defaultValue = "" },
                navArgument("packageMax") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val company = backStackEntry.arguments?.getString("company")
                ?.let { android.net.Uri.decode(it) }
                ?.takeIf { it.isNotBlank() }
            val batchStr = backStackEntry.arguments?.getString("batch")
            val batch = batchStr?.toIntOrNull()
            val pkgMinStr = backStackEntry.arguments?.getString("packageMin")
            val pkgMin = pkgMinStr?.toFloatOrNull()
            val pkgMaxStr = backStackEntry.arguments?.getString("packageMax")
            val pkgMax = pkgMaxStr?.toFloatOrNull()
            PlacedStudentsScreen(
                onBack = { navController.popBackStack() },
                preFilterCompany = company,
                preFilterBatch = batch,
                preFilterPackageMin = pkgMin,
                preFilterPackageMax = pkgMax
            )
        }
    }
}
