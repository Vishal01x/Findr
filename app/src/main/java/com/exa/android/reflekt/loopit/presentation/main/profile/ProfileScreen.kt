package com.exa.android.reflekt.loopit.presentation.main.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.EditProfileViewModel
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.UserViewModel
import com.exa.android.reflekt.loopit.presentation.main.Home.component.showLoader
import com.exa.android.reflekt.loopit.presentation.main.profile.components.AboutCard
import com.exa.android.reflekt.loopit.presentation.main.profile.components.education.EducationCard
import com.exa.android.reflekt.loopit.presentation.main.profile.components.education.ExperienceCard
import com.exa.android.reflekt.loopit.presentation.main.profile.components.extra_card.ExtracurricularCard
import com.exa.android.reflekt.loopit.presentation.main.profile.components.header.ProfileHeader
import com.exa.android.reflekt.loopit.presentation.main.profile.components.SkillsCard
import com.exa.android.reflekt.loopit.presentation.main.profile.components.header.ImageHeader
import com.exa.android.reflekt.loopit.presentation.main.profile.components.header.Profile
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProfileRoute
import com.exa.android.reflekt.loopit.util.Response
import com.exa.android.reflekt.loopit.util.model.Profile.CollegeInfo
import com.exa.android.reflekt.loopit.util.model.Profile.ExperienceInfo
import com.exa.android.reflekt.loopit.util.model.Profile.ExtraActivity
import com.exa.android.reflekt.loopit.util.model.Profile.ProfileData
import com.google.firebase.auth.userProfileChangeRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String?, navController: NavController,onEditHeaderClick: (ProfileData) -> Unit,
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

    var showDialog by remember { mutableStateOf(false) }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Screen") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Close")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(ProfileRoute.ProfileSetting.route) }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                },
                modifier = Modifier.background(MaterialTheme.colorScheme.background)
            )
        },
        containerColor = Color.White,
        contentColor = Color.White
    ) { padding ->

        when (val response = profileData) {
            is Response.Error -> {

            }

            Response.Loading -> {
                showLoader("Loading Profile...")
            }

            is Response.Success -> {
                editProfileViewModel.loadUserProfileData(response.data)
                val scrollState = rememberLazyListState()
                val profileData = remember(response) { response.data }

                LazyColumn(
                    state = scrollState,
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    // Header Image
                    item {
                        ImageHeader(
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
                            SkillsCard(skill.split(",").map { it.trim() })

                            Spacer(Modifier.height(12.dp))

                            ExtracurricularCard(userId, onAddClick = { onAddExtraCard(userId, it) })

                            Spacer(Modifier.height(12.dp))

                            ExperienceCard(
                                userId.isNullOrEmpty(),
                                experienceInfo = profileData.experienceInfo,
                                onEditExperience = {
                                    onEditExperience(profileData.experienceInfo)
                                }
                            )

                            Spacer(Modifier.height(12.dp))

                            EducationCard(
                                userId.isNullOrEmpty(),
                                collegeInfo = profileData.collegeInfo,
                                onEditEducation = {
                                    onEditEducation(profileData.collegeInfo)
                                }
                            )

                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }


            }
        }
    }
}


//@Preview(showBackground = true)
//@Composable
//fun PreviewProfileScreen() {
//    ProfileScreen(){}
//}