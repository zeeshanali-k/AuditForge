package com.devscion.auditforge.ui.sessions.detail.policies

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
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class PoliciesViewModel(
    @InjectedParam private val sessionId: String,
    private val getPolicyPacksUseCase: GetPolicyPacksUseCase,
    private val getPolicyPackDetailUseCase: GetPolicyPackDetailUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PoliciesUiState())
    val uiState: StateFlow<PoliciesUiState> = _uiState.asStateFlow()

    init {
        onIntent(PoliciesIntent.Load)
    }

    fun onIntent(intent: PoliciesIntent) {
        when (intent) {
            PoliciesIntent.Load -> loadPacks()
            is PoliciesIntent.SetFrameworkFilter -> _uiState.update { it.copy(frameworkFilter = intent.framework) }
            is PoliciesIntent.TogglePackSelection -> _uiState.update { state ->
                val ids = if (intent.packId in state.selectedPackIds) {
                    state.selectedPackIds - intent.packId
                } else {
                    state.selectedPackIds + intent.packId
                }
                state.copy(selectedPackIds = ids, hasChanges = true)
            }
            is PoliciesIntent.ExpandPack -> loadPackDetail(intent.packId)
            PoliciesIntent.CollapsePack -> _uiState.update { it.copy(expandedPackId = null, packDetail = null) }
            PoliciesIntent.SaveSelection -> _uiState.update { it.copy(hasChanges = false) }
            PoliciesIntent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun loadPacks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getPolicyPacksUseCase()) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, packs = result.data.data) }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                ApiResult.Unauthorized, ApiResult.NetworkError -> _uiState.update {
                    it.copy(isLoading = false, error = "Failed to load policy packs. Check your connection.")
                }
            }
        }
    }

    private fun loadPackDetail(packId: String) {
        if (_uiState.value.expandedPackId == packId) {
            _uiState.update { it.copy(expandedPackId = null, packDetail = null) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(expandedPackId = packId, isLoadingDetail = true, packDetail = null) }
            when (val result = getPolicyPackDetailUseCase(packId)) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoadingDetail = false, packDetail = result.data) }
                is ApiResult.Error -> _uiState.update { it.copy(isLoadingDetail = false, expandedPackId = null, error = result.message) }
                ApiResult.Unauthorized, ApiResult.NetworkError -> _uiState.update {
                    it.copy(isLoadingDetail = false, expandedPackId = null, error = "Failed to load pack details.")
                }
            }
        }
    }
}
