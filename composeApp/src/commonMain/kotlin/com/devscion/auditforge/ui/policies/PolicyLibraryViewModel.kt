package com.devscion.auditforge.ui.policies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.domain.usecase.policies.GetPolicyPackDetailUseCase
import com.devscion.auditforge.domain.usecase.policies.GetPolicyPacksUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class PolicyLibraryViewModel(
    private val getPolicyPacksUseCase: GetPolicyPacksUseCase,
    private val getPolicyPackDetailUseCase: GetPolicyPackDetailUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PolicyLibraryUiState())
    val uiState: StateFlow<PolicyLibraryUiState> = _uiState.asStateFlow()

    init {
        onIntent(PolicyLibraryIntent.Load)
    }

    fun onIntent(intent: PolicyLibraryIntent) {
        when (intent) {
            PolicyLibraryIntent.Load -> loadPacks()
            is PolicyLibraryIntent.SetFrameworkFilter -> _uiState.update {
                it.copy(frameworkFilter = intent.framework)
            }
            is PolicyLibraryIntent.UpdateSearch -> _uiState.update {
                it.copy(searchQuery = intent.query)
            }
            is PolicyLibraryIntent.SelectPack -> loadPackDetail(intent.packId)
            PolicyLibraryIntent.CloseDetail -> _uiState.update {
                it.copy(selectedPackId = null, packDetail = null)
            }
            PolicyLibraryIntent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun loadPacks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getPolicyPacksUseCase()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, packs = result.data.data)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                ApiResult.Unauthorized, ApiResult.NetworkError -> _uiState.update {
                    it.copy(isLoading = false, error = "Failed to load policy packs. Check your connection.")
                }
            }
        }
    }

    private fun loadPackDetail(packId: String) {
        if (_uiState.value.selectedPackId == packId) {
            _uiState.update { it.copy(selectedPackId = null, packDetail = null) }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(selectedPackId = packId, isLoadingDetail = true, packDetail = null)
            }
            when (val result = getPolicyPackDetailUseCase(packId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoadingDetail = false, packDetail = result.data)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoadingDetail = false, selectedPackId = null, error = result.message)
                }
                ApiResult.Unauthorized, ApiResult.NetworkError -> _uiState.update {
                    it.copy(isLoadingDetail = false, selectedPackId = null, error = "Failed to load pack details.")
                }
            }
        }
    }
}
