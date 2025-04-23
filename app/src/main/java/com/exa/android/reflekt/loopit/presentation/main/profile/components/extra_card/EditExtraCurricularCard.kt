package com.exa.android.reflekt.loopit.presentation.main.profile.components.extra_card


// Jetpack Compose
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

// Accompanist or Coil (choose depending on your image loader)
import coil.compose.AsyncImage
// If you're using Accompanist Coil
// import com.google.accompanist.coil.rememberCoilPainter

// Activity result API for image picking

// Kotlin coroutine scope
import kotlinx.coroutines.launch

// Hilt
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// Android
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.remote.main.Repository.BrandfetchViewModel
import com.exa.android.reflekt.loopit.data.remote.main.Repository.UserRepository
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.MediaSharingViewModel
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.mediaSelectionSheet.MediaPickerHandler
import com.exa.android.reflekt.loopit.presentation.main.Home.component.showLoader
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProfileRoute
import com.exa.android.reflekt.loopit.util.model.Profile.ExtraActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExtracurricularScreen(
    activity: ExtraActivity,
    navController: NavController,
    extracurricularViewModel: ExtracurricularViewModel = hiltViewModel(),
    brandfetchViewModel: BrandfetchViewModel = hiltViewModel(),
    mediaSharingViewModel: MediaSharingViewModel = hiltViewModel()
) {

    LaunchedEffect(activity) {
        extracurricularViewModel.loadActivityDetails(activity)
    }

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    var isValidUrl by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }

    // Extract domain when link changes
    val domain = remember(extracurricularViewModel.link) {
        Uri.parse(extracurricularViewModel.link).host?.removePrefix("www.") ?: ""
    }

    // Brandfetch logo loading state
    LaunchedEffect(domain,isValidUrl) {
        if (domain.isNotEmpty() && isValidUrl) {
            try {
                extracurricularViewModel.setLogoLoadingState(true)
                val result = brandfetchViewModel.fetchBrandInfoSingle(domain)
                result?.logos?.firstOrNull()?.formats?.firstOrNull()?.src?.let { logoUrl ->
                    extracurricularViewModel.updateLogo(logoUrl)
                }
                result?.domain?.let { extracurricularViewModel.updateDomain(it) }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(
                    message = "Auto-logo fetch failed: ${e.message}",
                    actionLabel = "Retry"
                )
            } finally {
                extracurricularViewModel.setLogoLoadingState(false)
            }
        }
    }

    // State Observers
    LaunchedEffect(extracurricularViewModel.state) {
        when (val state = extracurricularViewModel.state) {
            is ExtracurricularState.Success -> navController.popBackStack(ProfileRoute.UserProfile.route, false)
            is ExtracurricularState.Error -> {
                snackbarHostState.showSnackbar(
                    message = "Failed to Update Extra Activity. Check Internet Connection",
                    actionLabel = "Retry"
                )
            }

            else -> {}
        }
    }

    MediaPickerHandler(
        showAll = false,
        onLaunch = { uri ->
            extracurricularViewModel.updateUri(uri)
        }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Activity",
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
        bottomBar = {
            ActionButtons(
                loading = extracurricularViewModel.state.isLoading(),
                onCancel = { navController.popBackStack() },
                onSave = {
                    if (extracurricularViewModel.name.isNullOrBlank()) {
                        nameError = true
                    } else {
                        nameError = false
                        extracurricularViewModel.saveActivity(mediaSharingViewModel, activity.id)
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()

        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxSize()
            ) {
                ImageSection(
                    imageUri = extracurricularViewModel.imageUri,
                    logoUrl = extracurricularViewModel.logoUrl,
                    loading = extracurricularViewModel.isLogoLoading,
                    onImageClick = { mediaSharingViewModel.showMediaPickerSheet = true }
                )

                InputSection(
                    name = extracurricularViewModel.name,
                    link = extracurricularViewModel.link,
                    nameError = nameError,
                    description = extracurricularViewModel.description,
                    onNameChange = {
                        nameError = false
                        extracurricularViewModel.updateName(it)
                    },
                    onLinkChange = extracurricularViewModel::updateLink,
                    onDescriptionChange = extracurricularViewModel::updateDescription,
                    onFetchLogo = {
                        isValidUrl = true

                    }
                )
            }

            if (extracurricularViewModel.state.isLoading()) {
                showLoader()
            }
        }
    }

}

@Composable
private fun InputSection(
    name: String,
    link: String,
    description: String,
    nameError: Boolean,
    onNameChange: (String) -> Unit,
    onLinkChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onFetchLogo: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            isError = nameError,
            supportingText = {
                if (nameError) Text("Title can't be empty", color = MaterialTheme.colorScheme.error)
            },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        LinkInputRow(link = link, onLinkChange = onLinkChange, onFetchLogo = { onFetchLogo() })

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp, max = 150.dp),
            maxLines = 4,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
        )
    }
}

@Composable
private fun LinkInputRow(link: String, onLinkChange: (String) -> Unit, onFetchLogo: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = link,
            onValueChange = onLinkChange,
            label = { Text("Link") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = { onFetchLogo() },
            enabled = link.isNotBlank()
        ) {
            Text("Add")
        }
    }
}

@Composable
private fun ActionButtons(loading: Boolean, onCancel: () -> Unit, onSave: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.End
    ) {
//        if (loading) {
//            CircularProgressIndicator(modifier = Modifier.size(24.dp))
//        } else {
            OutlinedButton(onClick = onCancel) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onSave) {
                Text("Save")
            }
       // }
    }
}

// Updated ViewModel
@HiltViewModel
class ExtracurricularViewModel @Inject constructor(
    private val repository: UserRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    var name by mutableStateOf("")
    var link by mutableStateOf("")
    var description by mutableStateOf("")
    var imageUri by mutableStateOf<Uri?>(null)
    var logoUrl by mutableStateOf<String?>(null)
    var logoDomain by mutableStateOf<String?>(null)
    var isLogoLoading by mutableStateOf(false)
    var state by mutableStateOf<ExtracurricularState>(ExtracurricularState.Idle)

    fun loadActivityDetails(activity: ExtraActivity) {
        name = activity.name
        link = activity.link
        description = activity.description
        logoUrl = activity.media
        logoDomain = activity.domain
    }

    fun updateName(value: String) {
        name = value
    }

    fun updateUri(value: Uri) {
        imageUri = value
    }

    fun updateLink(value: String) {
        link = value
    }

    fun updateDescription(value: String) {
        description = value
    }

    fun updateLogo(url: String?) {
        logoUrl = url
    }

    fun updateDomain(domain: String) {
        logoDomain = domain
    }


    fun setLogoLoadingState(loading: Boolean) {
        isLogoLoading = loading
    }


    fun saveActivity(mediaSharingViewModel: MediaSharingViewModel, id : String?) {
        viewModelScope.launch {
            state = ExtracurricularState.Loading
            try {
                if (name.isEmpty()) ExtracurricularState.Error("Title can't be empty")
                // Handle image URI upload
                val imageUrl: String? = if (imageUri == null) {
                    null
                } else {
                    val file = mediaSharingViewModel.createTempFileFromUri(context, imageUri!!)
                    // Upload the file asynchronously
                    val uploadResult = withContext(Dispatchers.IO) {
                        mediaSharingViewModel.uploadFileToCloudinary(file)
                    }
                    uploadResult?.mediaUrl
                }

                // Update activity in the repository (database)
                repository.updateExtraActivity(
                    activity = ExtraActivity(
                        id = id ?: UUID.randomUUID().toString(),
                        name = name,
                        link = link,
                        description = description,
                        media = imageUrl ?: logoUrl ?: "",
                        domain = logoDomain ?: ".com"
                    )
                )
                state = ExtracurricularState.Success
            } catch (e: Exception) {
                // Handle exceptions, set error state
                state = ExtracurricularState.Error(e.message ?: "Failed to save activity")
            }
        }
    }

    fun deleteExtraActivity(extraActivity: ExtraActivity){
        viewModelScope.launch {
            state = ExtracurricularState.Loading
            try {
                repository.deleteExtraActivity(extraActivity.id)
                state = ExtracurricularState.Success
            }catch (e: Exception) {
                // Handle exceptions, set error state
                state = ExtracurricularState.Error(e.message ?: "Failed to save activity")
            }
        }
    }

}


@Composable
private fun ImageSection(
    imageUri: Uri?,
    logoUrl: String?,
    loading: Boolean,
    onImageClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
    ) {
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val url = if(imageUri == null && logoUrl.isNullOrEmpty())R.drawable.placeholder
             else imageUri ?: logoUrl

            AsyncImage(
                model = url,
                contentDescription = "Activity Logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }

        IconButton(
            onClick = onImageClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 16.dp, y = 16.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = "Add logo",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

sealed class ExtracurricularState {
    object Idle : ExtracurricularState()
    object Loading : ExtracurricularState()
    object Success : ExtracurricularState()
    class Error(val message: String) : ExtracurricularState()

    fun isLoading() = this is Loading
}