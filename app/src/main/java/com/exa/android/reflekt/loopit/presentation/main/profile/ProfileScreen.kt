package com.exa.android.reflekt.loopit.presentation.main.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
    onEditEducation: (CollegeInfo?) -> Unit,
    onEditExperience: (ExperienceInfo) -> Unit,
    userViewModel: UserViewModel = hiltViewModel(),
    editProfileViewModel: EditProfileViewModel = hiltViewModel()
) {
    val profileData by userViewModel.userProfileData.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.getProfileData(userId)
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
        containerColor = Color.White,
        contentColor = Color.White
    ) { padding ->

        when (val response = profileData) {
            is Response.Error -> {
                ProfileContent(
                    profileData = ProfileData(),
                    userId = userId,
                    onEditHeaderClick = onEditHeaderClick,
                    onAddExtraCard = onAddExtraCard,
                    openChat = openChat,
                    onEditEducation = onEditEducation,
                    onEditExperience = onEditExperience,
                    padding = padding
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
                    onEditHeaderClick = onEditHeaderClick,
                    onAddExtraCard = onAddExtraCard,
                    openChat = openChat,
                    onEditEducation = onEditEducation,
                    onEditExperience = onEditExperience,
                    padding = padding
                )
            }
        }
    }
}

@Composable
fun ProfileContent(
    profileData: ProfileData,
    userId: String?,
    onEditHeaderClick: (ProfileData) -> Unit,
    onAddExtraCard: (String?, ExtraActivity?) -> Unit,
    openChat: (String?) -> Unit,
    onEditEducation: (CollegeInfo?) -> Unit,
    onEditExperience: (ExperienceInfo) -> Unit,
    padding: PaddingValues
) {
    val scrollState = rememberLazyListState()

    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
    ) {
        item {
            ImageHeader(
                userId.isNullOrEmpty(),
                profileData.profileHeader,
                onEditClick = {
                    Log.d("ProfileScreen", "onEditclickatIcon")
                    onEditHeaderClick(profileData)
                }
            )
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 8.dp)) {

                ProfileHeader(
                    userId,
                    profileData.profileHeader,
                    openChat = { openChat(userId) }
                )

                Spacer(Modifier.height(12.dp))

                AboutCard(
                    userId.isNullOrEmpty(),
                    title = "About",
                    content = profileData.about.description
                )

                Spacer(Modifier.height(12.dp))

                val skill = profileData.skill
                SkillsCard(userId.isNullOrEmpty(),skill.split(",").map { it.trim() })

                Spacer(Modifier.height(12.dp))

                ExtracurricularCard(userId, onAddClick = { onAddExtraCard(userId, it) })

                Spacer(Modifier.height(12.dp))

                ExperienceCard(
                    userId.isNullOrEmpty(),
                    experienceInfo = profileData.experienceInfo,
                    onEditExperience = { onEditExperience(profileData.experienceInfo) }
                )

                Spacer(Modifier.height(12.dp))

                EducationCard(
                    userId.isNullOrEmpty(),
                    collegeInfo = profileData.collegeInfo,
                    onEditEducation = { onEditEducation(profileData.collegeInfo) }
                )

                Spacer(Modifier.height(4.dp))
            }
        }
    }
}