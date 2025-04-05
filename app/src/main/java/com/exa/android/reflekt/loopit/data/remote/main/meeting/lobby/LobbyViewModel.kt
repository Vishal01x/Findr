
package com.exa.android.reflekt.loopit.data.remote.main.meeting.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.video.android.core.Call
import io.getstream.video.android.core.StreamVideo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LobbyViewModel @Inject constructor(
  private val streamVideo: StreamVideo
) : ViewModel() {

  private val _uiState: MutableStateFlow<LobbyUiState> = MutableStateFlow(LobbyUiState.Loading)
  val uiState: StateFlow<LobbyUiState> = _uiState

  init {
    viewModelScope.launch {
      // request a refreshed user token
//      val userNumber = Random.nextInt(10000)
//      val name = "stream$userNumber"
//      val apiKey = MEET_API_KEY
//      val userId = "stream"
//      val token = StreamVideo.devToken(userId)
//      // initialize the Stream Video SDK
//      val streamVideo = StreamVideoBuilder(
//        context = MyApp.app,
//        apiKey = apiKey,
//        token = token,
//        user = User(
//          id = "stream",
//          name = name,
//          image = "http://placekitten.com/200/300",
//          role = "admin",
//          custom = mapOf("email" to userId),
//        ),
//      ).build()

      // create a call
      val call = streamVideo.call(Constants.callType, Constants.callId.toString())
      _uiState.value = LobbyUiState.TokenRefreshed(call)
    }
  }

  override fun onCleared() {
    super.onCleared()

    // uninstall Stream Video SDK
    StreamVideo.removeClient()
  }
}

sealed interface LobbyUiState {

  data object Loading : LobbyUiState

  data class TokenRefreshed(val call: Call) : LobbyUiState

  data object JoinCompleted : LobbyUiState

  data class Error(val message: String?) : LobbyUiState
}
