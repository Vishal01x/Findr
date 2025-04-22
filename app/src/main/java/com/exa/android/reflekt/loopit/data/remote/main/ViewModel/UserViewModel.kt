package com.exa.android.reflekt.loopit.data.remote.main.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.remote.main.Repository.UserRepository
import com.exa.android.reflekt.loopit.util.model.Status
import com.exa.android.reflekt.loopit.util.model.User
import com.exa.android.reflekt.loopit.util.Response
import com.exa.android.reflekt.loopit.util.model.Profile.ExtraActivity
import com.exa.android.reflekt.loopit.util.model.Profile.ProfileData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {

    private val _userStatus = MutableLiveData<Status>(Status()) // Default status
    val userStatus: LiveData<Status> = _userStatus

    private val _userDetail = MutableStateFlow<Response<User?>>(Response.Loading) // Default loading state
    val userDetail: StateFlow<Response<User?>> = _userDetail

    private val _userProfileData = MutableStateFlow<Response<ProfileData>>(Response.Loading)
    val userProfileData : StateFlow<Response<ProfileData>> = _userProfileData

    private val _userExtraActivity = MutableStateFlow<Response<List<ExtraActivity>>>(Response.Loading)
    val userExtraActivity : StateFlow<Response<List<ExtraActivity>>> = _userExtraActivity

    var curUser: String? = null

    init {
        curUser = userRepository.currentUser
    }

    fun updateOnlineStatus(userId: String, isOnline: Boolean) {
        viewModelScope.launch {
            userRepository.updateUserStatus(userId, isOnline)
        }
    }

    fun setTypingStatus(userId: String, toUserId: String?) {
        viewModelScope.launch {
            userRepository.setTypingStatus(userId, toUserId)
        }
    }

    fun updateUnreadMessages(curUser: String, otherUser: String) {
        viewModelScope.launch {
            userRepository.updateUnreadMessages(curUser, otherUser)
        }
    }

    fun observeUserConnectivity() {
        viewModelScope.launch {
            userRepository.observeUserConnectivity()
        }
    }

    fun observeUserStatus(userId: String) {
        userRepository.getUserStatus(userId).observeForever { status ->
            _userStatus.value = status ?: Status() // Ensure non-null status
        }
    }

    fun getUserDetail(userId: String) {
        viewModelScope.launch {
            userRepository.getUserDetail(userId)
                .catch { exception ->
                    _userDetail.value = Response.Error(exception.localizedMessage ?: "Error fetching user")
                }
                .collect { response ->
                    _userDetail.value = response
                }
        }
    }

    fun getProfileData(userId: String?) {
        viewModelScope.launch {
            if (curUser != null) {
                userRepository.getProfileData(userId)
                    .collect { response ->
                        _userProfileData.value = response
                    }
            }
        }
    }

    fun getExtraActivity(userId: String?) {
        viewModelScope.launch {
            if (curUser != null) {
                userRepository.getUserActivity(userId)
                    .collect { response ->
                        _userExtraActivity.value = response
                    }
            }
        }
    }

}