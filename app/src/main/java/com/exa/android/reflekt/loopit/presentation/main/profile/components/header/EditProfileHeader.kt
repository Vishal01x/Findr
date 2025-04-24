package com.exa.android.reflekt.loopit.presentation.main.profile.components.header


import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.EditProfileViewModel
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.MediaSharingViewModel
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.UserViewModel
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.mediaSelectionSheet.MediaPickerHandler
import com.exa.android.reflekt.loopit.presentation.main.Home.component.ImageFromUri
import com.exa.android.reflekt.loopit.presentation.main.Home.component.ImageUsingCoil
import com.exa.android.reflekt.loopit.presentation.main.Home.component.showLoader
import com.exa.android.reflekt.loopit.util.Response
import com.exa.android.reflekt.loopit.util.isNetworkAvailable
import com.exa.android.reflekt.loopit.util.model.Profile.ProfileData
import com.exa.android.reflekt.loopit.util.model.Profile.ProfileHeaderData
import com.exa.android.reflekt.loopit.util.model.Profile.SocialLinks
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

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
                        color = MaterialTheme.colorScheme.onSurface
                    )) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
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
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (viewModel.name.isNullOrEmpty()) {
                            nameError = true
                        } else {
                            nameError = false
                            viewModel.saveProfile(newProfileData.value,mediaSharingViewModel)
                        }
                    }) {
                        Text("Save")
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
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                ,
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = title)
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
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    )
                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Add logo",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
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
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Full Name") },
            isError = nameError,
            supportingText = {
                if (nameError) Text("Name can't be empty", color = MaterialTheme.colorScheme.error)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
//        OutlinedTextField(
//            value = headline,
//            onValueChange = onHeadlineChange,
//            label = { Text("Headline") },
//            modifier = Modifier.fillMaxWidth(),
//            singleLine = true
//        )

        val maxChars = 100

        Column {
            OutlinedTextField(
                value = headline,
                onValueChange = {
                    if (it.length <= maxChars) {
                        onHeadlineChange(it)
                    } else {
                        Toast.makeText(context, "Headline cannot exceed $maxChars characters", Toast.LENGTH_SHORT).show()
                    }
                },
                label = { Text("Headline") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(
                text = "${headline.length} / $maxChars",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 8.dp, top = 2.dp),
                color = if (headline.length == maxChars) Color.Red else Color.Gray
            )
        }



        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = role,
            onValueChange = onRoleChange,
            label = { Text("Role/Position") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
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
            modifier = Modifier.padding(bottom = 8.dp)
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

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        leadingIcon = {
            if(icon1 != null)
            Icon(imageVector = icon1, contentDescription = label)
            else
                icon2?.let { painterResource(it) }
                    ?.let { Icon(painter = it, contentDescription = label) }
        },
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = if (label.contains("Email")) KeyboardType.Email
            else KeyboardType.Uri
        )
    )
}

