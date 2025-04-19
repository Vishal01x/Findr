package com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.linkPreview.viewModel

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.remote.main.Repository.LinkMetadataRepository
import com.exa.android.reflekt.loopit.data.local.domain.LinkMetadata
import com.exa.android.reflekt.loopit.util.LinkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MetaDataViewModel @Inject constructor(
    private val metadataRepository: LinkMetadataRepository
) : ViewModel() {
    private val _linkStates = mutableStateMapOf<String, LinkState>()
    val linkStates: SnapshotStateMap<String, LinkState> = _linkStates

    fun getLinkState(message: String): StateFlow<LinkState> {
        val urls = LinkUtils.findLinksInText(message)
        val key = urls.firstOrNull()?.url ?: return MutableStateFlow(LinkState.Loading)

        return flow {
            if (!_linkStates.containsKey(key)) {
                _linkStates[key] = LinkState.Loading
                _linkStates[key] = try {
                    val result = metadataRepository.getMetadata(key)
                    LinkState.Success(result.getOrNull()) // Using Kotlin's built-in Result
                } catch (e: Exception) {
                    LinkState.Error(e.message ?: "Unknown error")
                }
            }
            emit(_linkStates[key] ?: LinkState.Loading)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = _linkStates[key] ?: LinkState.Loading
        )
    }

    fun refreshMetadata(message: String) {
        val urls = LinkUtils.findLinksInText(message)
        urls.firstOrNull()?.url?.let { url ->
            viewModelScope.launch {
                _linkStates[url] = LinkState.Loading
                _linkStates[url] = try {
                    val result = metadataRepository.getMetadata(url)
                    LinkState.Success(result.getOrNull()) // Using Kotlin's built-in Result
                } catch (e: Exception) {
                    LinkState.Error(e.message ?: "Error")
                }
            }
        }
    }
}

/** State representation for link preview */
sealed class LinkState {
    data object Loading : LinkState()
    data class Success(val metadata: LinkMetadata?) : LinkState()
    data class Error(val message: String) : LinkState()
}
