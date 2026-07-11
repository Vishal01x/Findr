package com.exa.android.reflekt.loopit.presentation.main.profile.components.header


import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.feature.main.profile.data.viewmodel.EditProfileViewModel
import com.exa.android.reflekt.loopit.data.feature.main.media.viewmodel.MediaSharingViewModel
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.viewmodel.UserViewModel
import com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.media.mediaSelectionSheet.MediaPickerHandler
import com.exa.android.reflekt.loopit.presentation.main.component.ImageFromUri
import com.exa.android.reflekt.loopit.presentation.main.component.showLoader
import com.exa.android.reflekt.loopit.core.network.Response
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.ProfileData
import com.exa.android.reflekt.loopit.presentation.main.component.textfield.AppTextField
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidMeshBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileHeader(
    profileData : ProfileData,
    navController: NavController,
    viewModel: EditProfileViewModel = hiltViewModel(),
    mediaSharingViewModel: MediaSharingViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val newProfileData = remember{ mutableStateOf(profileData.profileHeader) }
    val response = viewModel.responseState
    var nameError by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(profileData) {
        viewModel.loadUserProfileData(profileData)
    }

    // Back on success
    LaunchedEffect(response) {
        if (response is Response.Success) {
            navController.popBackStack()
        }
    }

    // Show error snackbar
    LaunchedEffect(response) {
        if (response is Response.Error) {
            snackbarHostState.showSnackbar(
                message = response.message,
                actionLabel = "Retry"
            )?.let { result ->
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.saveProfile(newProfileData.value, mediaSharingViewModel)
                }
            }
        }
    }

    var bannerImage by remember { mutableStateOf(false) }

    MediaPickerHandler(
        showAll = false,
        onLaunch = { uri ->
            if (bannerImage){
                viewModel.updateBanner(uri)
                newProfileData.value.bannerImageUrl = ""
            }
            else {
                viewModel.updateProfileImage(uri)
                newProfileData.value.profileImageUrl = ""
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = LiquidColors.TextPrimary
                    )) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Back", tint = LiquidColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LiquidColors.GlassSurface
                )
            )
        },
        containerColor = LiquidColors.BgBase,
        contentColor = LiquidColors.TextPrimary,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(scrollState)
            ) {
                // Banner
                ImageSelectionGroup(
                    title = "Banner",
                    imageUri = viewModel.bannerUri,
                    true,
                    onSelectImage = {
                        mediaSharingViewModel.showMediaPickerSheet = true
                        bannerImage = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )

                // Profile Image
                Box(
                    modifier = Modifier
                        .offset(y = (-40).dp)
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                ) {
                    ImageSelectionGroup(
                        title = "Profile Photo",
                        imageUri = viewModel.profileImageUri,
                        false,
                        onSelectImage = {
                            bannerImage = false
                            mediaSharingViewModel.showMediaPickerSheet = true
                        },
                        modifier = Modifier.size(120.dp)
                    )
                }

                // Input Groups
                ProfileInputGroup(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    name = viewModel.name,
                    headline = viewModel.headline,
                    role = viewModel.role,
                    nameError = nameError,
                    onNameChange = {
                        nameError = false
                        viewModel.updateName(it) },
                    onHeadlineChange = viewModel::updateHeadline,
                    onRoleChange = viewModel::updateRole
                )

                SocialLinksGroup(
                    modifier = Modifier.padding(16.dp),
                    youtube = viewModel.youtube,
                    linkedin = viewModel.linkedin,
                    email = viewModel.email,
                    portfolio = viewModel.portfolio,
                    onYoutubeChange = viewModel::updateYoutube,
                    onLinkedinChange = viewModel::updateLinkedin,
                    onEmailChange = viewModel::updateEmail,
                    onPortfolioChange = viewModel::updatePortfolio
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Cancel", color = LiquidColors.TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (viewModel.name.isNullOrEmpty()) {
                                nameError = true
                            } else {
                                nameError = false
                                viewModel.saveProfile(newProfileData.value,mediaSharingViewModel)
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = LiquidColors.Primary)
                    ) {
                        Text("Save", color = androidx.compose.ui.graphics.Color.White)
                    }
                }
            }

            // Loading Overlay
            if (response is Response.Loading) {
                showLoader()
            }
        }
    }
}

@Composable
private fun ImageSelectionGroup(
    title: String,
    imageUri: Uri?,
    isbanner : Boolean = true,
    onSelectImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Profile Image or Placeholder
        if (imageUri != null) {
            ImageFromUri(imageUri, Modifier.clickable { onSelectImage() })
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onSelectImage() }
                    .background(LiquidColors.GlassSurface)
                ,
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = title, tint = LiquidColors.Primary)
            }
        }

        if(isbanner) {
            // Floating Camera Icon
            IconButton(
                onClick = onSelectImage,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(36.dp)
                    .align(Alignment.BottomEnd)
                    .offset(y = 18.dp) // Half out of image
                    .background(
                        color = LiquidColors.GlassSurface,
                        shape = CircleShape
                    )
                    .border(2.dp, LiquidColors.GlassBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Add logo",
                    tint = LiquidColors.Primary
                )
            }
        }
    }
}


@Composable
private fun ProfileInputGroup(
    modifier: Modifier = Modifier,
    name: String,
    headline: String,
    role: String,
    nameError : Boolean,
    onNameChange: (String) -> Unit,
    onHeadlineChange: (String) -> Unit,
    onRoleChange: (String) -> Unit
) {
    val context = LocalContext.current

    Column(modifier = modifier) {
        AppTextField(
            value = name,
            onValueChange = onNameChange,
            label = "Full Name",
            leadingIcon = Icons.Default.Person,
            isError = nameError,
            errorMessage = if (nameError) "Name can't be empty" else null,
        )
        Spacer(modifier = Modifier.height(8.dp))

        val maxChars = 250

        Column {
            AppTextField(
                value = headline,
                onValueChange = {
                    if (it.length <= maxChars) onHeadlineChange(it)
                },
                label = "Headline",
                leadingIcon = Icons.Default.Info,
            )
            Text(
                text = "${headline.length} / $maxChars",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 8.dp, top = 2.dp),
                color = if (headline.length == maxChars) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        AppTextField(
            value = role,
            onValueChange = onRoleChange,
            label = "Role / Position",
            leadingIcon = Icons.Default.Work,
        )
    }
}

@Composable
private fun SocialLinksGroup(
    modifier: Modifier = Modifier,
    youtube: String,
    linkedin: String,
    email: String,
    portfolio: String,
    onYoutubeChange: (String) -> Unit,
    onLinkedinChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPortfolioChange: (String) -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = "Social Links",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
            color = LiquidColors.Primary
        )

        IconTextField(
            icon2 = R.drawable.github,
            value = youtube,
            onValueChange = onYoutubeChange,
            label = "GitHub URL"
        )
        IconTextField(
            icon2 = R.drawable.linedin,
            value = linkedin,
            onValueChange = onLinkedinChange,
            label = "LinkedIn URL"
        )
        IconTextField(
            icon1 = Icons.Default.Email,
            value = email,
            onValueChange = onEmailChange,
            label = "Email Address"
        )
        IconTextField(
            icon1 = Icons.Default.Link,
            value = portfolio,
            onValueChange = onPortfolioChange,
            label = "Portfolio URL"
        )
//        IconTextField(
//            icon = Icons.Default.Link,
//            value = portfolio,
//            onValueChange = onPortfolioChange,
//            label = "Extra URL"
//        )
    }
}

@Composable
private fun IconTextField(
    icon1: ImageVector? = null,
    icon2 : Int? = null,
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {

    AppTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        leadingIcon = icon1,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = if (label.contains("Email")) KeyboardType.Email
            else KeyboardType.Uri
        )
    )
}

