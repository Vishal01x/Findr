package com.exa.android.reflekt.loopit.data.feature.main.suggestion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SuggestionsViewModel @Inject constructor(
    private val repo: SuggestionRepository
) : ViewModel() {

    private val _suggestions = MutableStateFlow<List<SuggestedUser>>(emptyList())
    val suggestions: StateFlow<List<SuggestedUser>> = _suggestions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _suggestions.value = repo.getSuggestedUsers()
            _isLoading.value = false
        }
    }
}
