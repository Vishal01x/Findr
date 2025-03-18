//package com.exa.android.reflekt.ui.viewmodel
//
//import androidx.compose.runtime.mutableStateMapOf
//import androidx.compose.runtime.snapshots.SnapshotStateMap
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.exa.android.reflekt.data.repository.GetLinkPreviewUseCase
//import com.exa.android.reflekt.domain.LinkMetadata
//import com.exa.android.reflekt.util.LinkUtils
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class ChatViewModel @Inject constructor(
//    private val getLinkPreview: GetLinkPreviewUseCase
//) : ViewModel() {
//    private val _linkPreviews = mutableStateMapOf<String, LinkMetadata>()
//    val linkPreviews: SnapshotStateMap<String, LinkMetadata> = _linkPreviews
//
//    fun processMessageLinks(message: String) {
//        viewModelScope.launch {
//            LinkUtils.findLinksInText(message).forEach { link ->
//                if (!_linkPreviews.containsKey(link.url)) {
//                    when (val result = getLinkPreview(link.url)) {
//                        is Result.Success -> _linkPreviews[link.url] = result.data
//                        is Result.Failure -> {/* Handle error */}
//                    }
//                }
//            }
//        }
//    }
//}