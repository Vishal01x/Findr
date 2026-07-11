package com.exa.android.reflekt.loopit.presentation.main.placement.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.main.placement.PlacementAdminRepository
import com.exa.android.reflekt.loopit.data.feature.main.placement.PlacementRepository
import com.exa.android.reflekt.loopit.presentation.main.placement.model.CompanyDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecruitingCompaniesState(
    val isLoading: Boolean = true,
    val companies: List<CompanyDetail> = emptyList(),
    val isAdmin: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RecruitingCompaniesViewModel @Inject constructor(
    private val placementRepository: PlacementRepository,
    private val adminRepository: PlacementAdminRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RecruitingCompaniesState())
    val state: StateFlow<RecruitingCompaniesState> = _state.asStateFlow()

    init {
        checkAdminStatus()
        fetchCompanies()
    }

    private fun checkAdminStatus() {
        viewModelScope.launch {
            val isAdmin = adminRepository.isCurrentUserAdmin()
            _state.update { it.copy(isAdmin = isAdmin) }
        }
    }

    private fun fetchCompanies() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            // Use getCompaniesForList() which fetches only summary fields,
            // avoiding expensive nested arrays (stages, resources, PDFs)
            // that are only needed on the Company Detail screen.
            placementRepository.getCompaniesForList().collect { companiesList ->
                _state.update { it.copy(companies = companiesList, isLoading = false) }
            }
        }
    }
}
