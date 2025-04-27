package com.exa.android.reflekt.loopit.data.remote.main.ViewModel

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.remote.main.Repository.UserRepository
import com.exa.android.reflekt.loopit.util.Response
import com.exa.android.reflekt.loopit.util.isNetworkAvailable
import com.exa.android.reflekt.loopit.util.model.Profile.CollegeInfo
import com.exa.android.reflekt.loopit.util.model.Profile.ExperienceInfo
import com.exa.android.reflekt.loopit.util.model.Profile.ProfileData
import com.exa.android.reflekt.loopit.util.model.Profile.ProfileHeaderData
import com.exa.android.reflekt.loopit.util.model.Profile.SocialLinks
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

// ViewModel
@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    var name by mutableStateOf("")
    var headline by mutableStateOf("")
    var role by mutableStateOf("")
    var youtube by mutableStateOf("")
    var linkedin by mutableStateOf("")
    var email by mutableStateOf("")
    var portfolio by mutableStateOf("")

    var about by mutableStateOf("")

    var bannerUri by mutableStateOf<Uri?>(null)
    var profileImageUri by mutableStateOf<Uri?>(null)
    var capturedImageUri by mutableStateOf<Uri?>(null)

    private val _skillInput = mutableStateOf("")
    val skillInput: State<String> = _skillInput

    private val _isEditing = mutableStateOf(false)
    val isEditing: State<Boolean> = _isEditing

    private val _updatedSkills = mutableStateOf(mutableListOf<String>())
    val updatedSkills: State<List<String>> = _updatedSkills

    private val _stagedSkills = mutableStateOf(mutableListOf<String>())
    val stagedSkills: State<List<String>> = _stagedSkills


    var responseState by mutableStateOf<Response<Unit>?>(null)
        private set

    var curUser: String? = null

    init {
        curUser = userRepository.currentUser
    }

    fun updateName(value: String) {
        name = value
    }

    fun updateHeadline(value: String) {
        headline = value
    }

    fun updateRole(value: String) {
        role = value
    }

    fun updateYoutube(value: String) {
        youtube = value
    }

    fun updateLinkedin(value: String) {
        linkedin = value
    }

    fun updateEmail(value: String) {
        email = value
    }

    fun updatePortfolio(value: String) {
        portfolio = value
    }

    fun updateBanner(uri: Uri) {
        bannerUri = uri
    }

    fun updateProfileImage(uri: Uri) {
        profileImageUri = uri
    }

    fun updateAbout(desc: String) {
        about = desc
    }

    fun initialiseSkill(skill: MutableList<String>) {
        if (_updatedSkills.value.isEmpty()) {
            _updatedSkills.value = skill
        }
    }

    fun onSkillInputChanged(newValue: String) {
        _skillInput.value = newValue
    }

    fun setEditing(editing: Boolean) {
        _isEditing.value = editing
    }

    fun addSkillToStaging(skill: String = _skillInput.value) {
        val cleanSkill = skill.trim()
        if (cleanSkill.isNotBlank() &&
            !_updatedSkills.value.contains(cleanSkill) &&
            !_stagedSkills.value.contains(cleanSkill)
        ) {
            _stagedSkills.value.add(cleanSkill)
        }
        _skillInput.value = ""
    }

    fun removeStagedSkill(skill: String) {
        _stagedSkills.value.remove(skill)
    }

    fun removeUpdatedSkill(skill: String) {
        _updatedSkills.value.remove(skill)
    }

    fun cancelEditing(oldSkills: MutableList<String>) {
        _isEditing.value = false
        _skillInput.value = ""
        _stagedSkills.value.clear()
        _updatedSkills.value = oldSkills
        // Reset to current skills from backend if needed
    }

    fun onSuccess() {
        val finalSkills = (_updatedSkills.value + _stagedSkills.value).distinct()
        _isEditing.value = false
        _updatedSkills.value.clear()
        _updatedSkills.value.addAll(finalSkills)
        _stagedSkills.value.clear()
    }


    fun saveSkills() {
        val finalSkills = (_updatedSkills.value + _stagedSkills.value).distinct()
        updateUserSkills(finalSkills)
        when (responseState) {
            is Response.Error -> {}
            Response.Loading -> {}
            is Response.Success -> {
                _isEditing.value = false
                _updatedSkills.value.clear()
                _updatedSkills.value.addAll(finalSkills)
                _stagedSkills.value.clear()
            }

            null -> {}
        }
    }

    fun updateUserSkills(newSkills: List<String>) {
        viewModelScope.launch {
            try {
                userRepository.updateUserSkill(newSkills).collect {
                    responseState = it
                }
            } catch (e: Exception) {
                // Log.e("ViewModel", "Error updating skills", e)
            }
        }
    }


    fun captureImage(context: Context): Uri? {
        return try {
            val file = File.createTempFile(
                "profile_",
                ".jpg",
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            ).also { capturedImageUri = it }
        } catch (e: Exception) {
            null
        }
    }

    fun loadUserProfileData(profileData: ProfileData) {
        viewModelScope.launch {
            try {
                profileData?.let {
                    name = it.profileHeader?.name.orEmpty()
                    headline = it.profileHeader?.headline.orEmpty()
                    role = it.profileHeader?.role.orEmpty()
                    profileImageUri =
                        it.profileHeader?.profileImageUrl?.let { url -> Uri.parse(url) }
                    bannerUri = it.profileHeader?.bannerImageUrl?.let { url -> Uri.parse(url) }

                    linkedin = it.profileHeader?.socialLinks?.linkedin.orEmpty()
                    youtube = it.profileHeader?.socialLinks?.youtube.orEmpty()
                    email = it.profileHeader?.socialLinks?.email.orEmpty()
                    portfolio = it.profileHeader?.socialLinks?.portfolio.orEmpty()

                    about = it.about?.description.orEmpty()

                    _updatedSkills.value = it.skill.split(",").map { it.trim() }.toMutableList()
                }
            } catch (e: Exception) {
//                responseState = Response.Error("Failed to load profile data: ${e.localizedMessage}")
                // Log.d("ProfileScreen", "Failed to load profile data : ${e.localizedMessage}")
            }
        }
    }


    fun saveProfile(
        profileHeaderData: ProfileHeaderData,
        mediaSharingViewModel: MediaSharingViewModel
    ) {
        viewModelScope.launch {
            responseState = Response.Loading
            try {
                if (!isNetworkAvailable(context)) throw Exception("No Internet Available")

                val (profileUrl, bannerUrl) = withContext(Dispatchers.IO) {
                    val profileDeferred = async {
                        if (profileHeaderData.profileImageUrl.isBlank() && profileImageUri != null) {
                            runCatching {
                                val file = mediaSharingViewModel.createTempFileFromUri(
                                    context,
                                    profileImageUri!!
                                )
                                mediaSharingViewModel.uploadFileToCloudinary(file)
                            }.getOrNull()
                        } else null
                    }

                    val bannerDeferred = async {
                        if (profileHeaderData.bannerImageUrl.isBlank() && bannerUri != null) {
                            runCatching {
                                val file = mediaSharingViewModel.createTempFileFromUri(
                                    context,
                                    bannerUri!!
                                )
                                mediaSharingViewModel.uploadFileToCloudinary(file)
                            }.getOrNull()
                        } else null
                    }

                    Pair(profileDeferred.await(), bannerDeferred.await())
                }

                val newProfileUrl =
                    profileHeaderData.profileImageUrl.ifBlank { profileUrl?.mediaUrl.orEmpty() }
                val newBannerUrl =
                    profileHeaderData.bannerImageUrl.ifBlank { bannerUrl?.mediaUrl.orEmpty() }

                val profileHeader = ProfileHeaderData(
                    name = name,
                    headline = headline,
                    role = role,
                    profileImageUrl = newProfileUrl,
                    bannerImageUrl = newBannerUrl,
                    socialLinks = SocialLinks(
                        linkedin = linkedin,
                        youtube = youtube,
                        email = email,
                        portfolio = portfolio
                    )
                )

                profileHeaderData.name = name
                profileHeaderData.headline = headline
                profileHeaderData.role = role
                profileHeaderData.profileImageUrl = newProfileUrl
                profileHeaderData.bannerImageUrl = newBannerUrl
                profileHeaderData.socialLinks.portfolio = portfolio
                profileHeaderData.socialLinks.linkedin = linkedin
                profileHeaderData.socialLinks.youtube = youtube
                profileHeaderData.socialLinks.email = email

                updateUserProfileHeader(profileHeaderData)
                responseState = Response.Success(Unit)
                updateUserNameAndImage(profileHeaderData.name, profileHeaderData.profileImageUrl)
            } catch (e: Exception) {
                responseState = Response.Error("Failed to update profile: ${e.localizedMessage}")
            }
        }
    }


    fun updateUserProfileHeader(profileHeader: ProfileHeaderData) {
        viewModelScope.launch {
            userRepository.updateOrCreateProfileHeader(profileHeader)
        }
    }

    fun updateUserNameAndImage(name: String, imageUrl: String) {
        viewModelScope.launch {
            userRepository.updateUserNameAndImage(name, imageUrl)
        }
    }


    fun updateUserAbout() {
        viewModelScope.launch {
            userRepository.updateUserAbout(about).collect { result ->
                responseState = result
            }
        }
    }

    fun updateUserEducation(collegeInfo: CollegeInfo) {
        viewModelScope.launch {
            if (!isNetworkAvailable(context)) {
                responseState =
                    Response.Error("Failed Education update. Check Internet Connection")
            } else {
                userRepository.updateUserEducation(collegeInfo).collect {
                    responseState = it
                }
            }
        }
    }

    fun updateUserExperience(experienceInfo: ExperienceInfo) {
        viewModelScope.launch {
            if (!isNetworkAvailable(context)) {
                responseState =
                    Response.Error("Failed Experience update. Check Internet Connection")
            } else {
                userRepository.updateUserExperience(experienceInfo).collect {
                    responseState = it
                }
            }
        }
    }

    private val _rating = MutableStateFlow<Response<Pair<Int,Float>>>(Response.Success(Pair(0,0f)))
    val rating: StateFlow<Response<Pair<Int,Float>>> = _rating

    private val _verifiers =
        MutableStateFlow<Response<List<ProfileHeaderData>>>(Response.Success(emptyList()))
    val verifiers: StateFlow<Response<List<ProfileHeaderData>>> = _verifiers

    // State to hold the rating as Int
    private val _curUserRating = mutableStateOf(0)
    val curUserRating: State<Int> get() = _curUserRating

    fun updateProfileView(userId: String) {
        viewModelScope.launch {
            userRepository.updateProfileView(userId)
        }
    }

    fun updateRating(rating: Int, userId: String) {
        viewModelScope.launch {
            val result = userRepository.rateUser(rating, userId)
            if (result.isSuccess) {
                getRatingByCurUser(userId)
            } else {
                // Handle error
            }
        }
    }

    fun verifyProfile(userId: String) {
        viewModelScope.launch {
            val result = userRepository.verifyUser(userId)
            if (result.isSuccess) {

            } else {
                // Handle error
            }
        }
    }

    private val _profileViews = MutableLiveData<Int>()
    val profileViews: LiveData<Int> get() = _profileViews

    // Function to fetch the number of profile views
    fun getProfileView(userId: String?) {
        // Calling the repository function
        userRepository.getProfileView(userId) { viewersCount ->
            _profileViews.postValue(viewersCount)
        }
    }


    // Function to load rating from curUser for a specific user
    fun getRatingByCurUser(userId: String) {
        viewModelScope.launch {
            val result = userRepository.getRatingByCurUser(userId)
            result.onSuccess {
                // Update the rating in the state
                _curUserRating.value = it.toInt()
            }
            result.onFailure {
                // Handle failure, you can set a default value or show an error
                _curUserRating.value = 0
            }
        }
    }

    fun getAverageRating(userId: String?) {
        viewModelScope.launch {
            val result = try {
                userRepository.getAverageRating(userId)
            } catch (e: Exception) {
                Response.Error(e.localizedMessage)
            }
            _rating.value = result
        }

    }

    fun getAllVerifiersDetail(targetUserId: String?) {
        _verifiers.value = Response.Loading  // Show loading state

        viewModelScope.launch {
            userRepository.getAllVerifiersDetail(targetUserId).collect{
                _verifiers.value = it
            }

        }
    }

}
