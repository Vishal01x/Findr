package com.exa.android.reflekt.loopit.presentation.main.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.EditProfileViewModel
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.UserViewModel
import com.exa.android.reflekt.loopit.presentation.main.Home.component.showLoader
import com.exa.android.reflekt.loopit.presentation.main.profile.components.*
import com.exa.android.reflekt.loopit.presentation.main.profile.components.education.EducationCard
import com.exa.android.reflekt.loopit.presentation.main.profile.components.education.ExperienceCard
import com.exa.android.reflekt.loopit.presentation.main.profile.components.extra_card.ExtracurricularCard
import com.exa.android.reflekt.loopit.presentation.main.profile.components.header.ImageHeader
import com.exa.android.reflekt.loopit.presentation.main.profile.components.header.ProfileHeader
import com.exa.android.reflekt.loopit.presentation.main.profile.feedback.ProfileFeedback
import com.exa.android.reflekt.loopit.presentation.main.profile.feedback.ProfileViews
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProfileRoute
import com.exa.android.reflekt.loopit.util.Response
import com.exa.android.reflekt.loopit.util.model.Profile.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String?,
    navController: NavController,
    onEditHeaderClick: (ProfileData) -> Unit,
    onAddExtraCard: (String?, ExtraActivity?) -> Unit,
    openChat: (String?) -> Unit,
    openImage: (String?) -> Unit,
    onEditEducation: (CollegeInfo?) -> Unit,
    onEditExperience: (ExperienceInfo) -> Unit,
    onViewVerifier : (String?) -> Unit,
    userViewModel: UserViewModel = hiltViewModel(),
    editProfileViewModel: EditProfileViewModel = hiltViewModel()
) {
    val profileData by userViewModel.userProfileData.collectAsState()
    val curUser = editProfileViewModel.curUser

    LaunchedEffect(Unit) {
        userViewModel.getProfileData(userId)
        editProfileViewModel.getAverageRating(userId)
        editProfileViewModel.getAllVerifiersDetail(userId)
        if(userId != null && userId != curUser) {
            editProfileViewModel.getRatingByCurUser(userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Profile Screen",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onTertiary
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.shadow(elevation = 10.dp),
                actions = {
                    IconButton(onClick = { navController.navigate(ProfileRoute.ProfileSetting.route) }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.secondary
    ) { padding ->

        when (val response = profileData) {
            is Response.Error -> {
                ProfileContent(
                    profileData = ProfileData(),
                    userId = userId,
                    curUser = curUser,
                    onEditHeaderClick = onEditHeaderClick,
                    onAddExtraCard = onAddExtraCard,
                    openChat = openChat,
                    openImage = openImage,
                    onEditEducation = onEditEducation,
                    onEditExperience = onEditExperience,
                    onViewVerifier = onViewVerifier,
                    padding = padding,
                    editProfileViewModel
                )
            }

            Response.Loading -> {
                showLoader("Loading Profile...")
            }

            is Response.Success -> {
                editProfileViewModel.loadUserProfileData(response.data)
                ProfileContent(
                    profileData = response.data,
                    userId = userId,
                    curUser = curUser,
                    onEditHeaderClick = onEditHeaderClick,
                    onAddExtraCard = onAddExtraCard,
                    openChat = openChat,
                    openImage = openImage,
                    onEditEducation = onEditEducation,
                    onEditExperience = onEditExperience,
                    onViewVerifier = onViewVerifier,
                    padding = padding,
                    editProfileViewModel
                )
            }
        }
    }
}

@Composable
fun ProfileContent(
    profileData: ProfileData,
    userId: String?,
    curUser : String?,
    onEditHeaderClick: (ProfileData) -> Unit,
    onAddExtraCard: (String?, ExtraActivity?) -> Unit,
    openChat: (String?) -> Unit,
    openImage: (String?) -> Unit,
    onEditEducation: (CollegeInfo?) -> Unit,
    onEditExperience: (ExperienceInfo) -> Unit,
    onViewVerifier : (String?) -> Unit,
    padding: PaddingValues,
    editProfileViewModel: EditProfileViewModel
) {
    val scrollState = rememberLazyListState()

    LaunchedEffect(userId) {
        if (userId != curUser && userId != null) {
            editProfileViewModel.updateProfileView(userId, profileData.profileHeader.profileImageUrl)
        }
    }

    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
    ) {
        item {
            ImageHeader(
                userId == null || userId == curUser,
                profileData.profileHeader,
                onEditClick = {
                    onEditHeaderClick(profileData)
                },
                openImage = openImage
            )
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 8.dp)) {

                ProfileHeader(
                    userId,
                    curUser,
                    profileData.profileHeader,
                    openChat = { openChat(userId) },
                    editProfileViewModel
                )

                Spacer(Modifier.height(12.dp))

                AboutCard(
                    userId == null || userId == curUser,
                    title = "About",
                    content = profileData.about.description
                )

                Spacer(Modifier.height(12.dp))

                val skill = profileData.skill
                SkillsCard(userId == null || userId == curUser, skill.split(",").map { it.trim() })

                Spacer(Modifier.height(12.dp))

                ExtracurricularCard(userId, curUser, onAddClick = { onAddExtraCard(userId, it) })

                Spacer(Modifier.height(12.dp))

                ExperienceCard(
                    userId == null || userId == curUser,
                    experienceInfo = profileData.experienceInfo,
                    onEditExperience = { onEditExperience(profileData.experienceInfo) }
                )

                Spacer(Modifier.height(12.dp))

                EducationCard(
                    userId == null || userId == curUser,
                    collegeInfo = profileData.collegeInfo,
                    onEditEducation = { onEditEducation(profileData.collegeInfo) }
                )

                Spacer(Modifier.height(12.dp))

                ProfileFeedback(userId, editProfileViewModel) {
                    onViewVerifier(userId)
                }

                if (userId == null || userId == curUser) {
                    ProfileViews(userId,editProfileViewModel)
                }

                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

