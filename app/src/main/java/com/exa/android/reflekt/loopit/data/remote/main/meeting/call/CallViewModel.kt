
package com.exa.android.reflekt.loopit.data.remote.main.meeting.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.video.android.core.StreamVideo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
  private val streamVideo: StreamVideo
) : ViewModel() {

  // step 1 - get the StreamVideo instance and create a call
//  private val streamVideo = StreamVideo.instance()
  val call = streamVideo.call(Constants.callType, Constants.callId.toString())

  private val _uiState: MutableStateFlow<CallUiState> = MutableStateFlow(CallUiState.Loading)
  val uiState: MutableStateFlow<CallUiState> = _uiState

  // we have update for meeting scheduling

  fun join() {
    // step 2 - join the call
    viewModelScope.launch {
      val result = call.join(create = true, notify = true, ring = false)
      result.onSuccess {
        _uiState.value = CallUiState.Success
      }.onError { error ->
        // Unable to join. Device is offline or other usually connection issue.
        _uiState.value = CallUiState.Error(error.message)
      }
    }
  }

  fun leave() {
    call.leave()
  }
}

sealed interface CallUiState {

  data object Loading : CallUiState

  data object Success : CallUiState

  data class Error(val message: String) : CallUiState
}
