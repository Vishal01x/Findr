package com.exa.android.reflekt.loopit.data.feature.main.chat.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.repository.UserRepository
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.Status
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.User
import com.exa.android.reflekt.loopit.core.network.Response
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.ExtraActivity
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.ProfileData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _userStatus = MutableLiveData<Status>(Status()) // Default status
    val userStatus: LiveData<Status> = _userStatus

//    private val _userDetail = MutableStateFlow<Response<User?>>(Response.Loading) // Default loading state
//    val userDetail: StateFlow<Response<User?>> = _userDetail

    private val _userDetails = MutableStateFlow<Map<String, Response<User?>>>(emptyMap())
    val userDetail: StateFlow<Map<String, Response<User?>>> = _userDetails


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

    fun subscribeTopics(){
        viewModelScope.launch {
            userRepository.subscribeTopics()
        }
    }


//    fun getUserDetail(userId: String) {
//        viewModelScope.launch {
//            userRepository.getUserDetail(userId)
//                .catch { exception ->
//                    _userDetail.value = Response.Error(exception.localizedMessage ?: "Error fetching user")
//                }
//                .collect { response ->
//                    _userDetail.value = response
//                }
//        }
//    }


    fun getUserDetail(userId: String) {
        viewModelScope.launch {
            userRepository.getUserDetail(userId)
                .catch { exception ->
                    _userDetails.update { current ->
                        current + (userId to Response.Error(exception.localizedMessage ?: "Error fetching user"))
                    }
                }
                .collect { response ->
                    _userDetails.update { current ->
                        current + (userId to response)
                    }
                }
        }
    }


    fun getProfileData(userId: String?) {
        val current = _userProfileData.value
        if (current is Response.Success && current.data.profileHeader.uid == userId) {
            return
        }
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