package com.devscion.auditforge.ui.policies

import com.devscion.auditforge.domain.model.PolicyFramework
import com.devscion.auditforge.domain.model.PolicyPackDetail
import com.devscion.auditforge.domain.model.PolicyPackSummary

data class PolicyLibraryUiState(
    val packs: List<PolicyPackSummary> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val frameworkFilter: PolicyFramework? = null,
    val searchQuery: String = "",
    val selectedPackId: String? = null,
    val packDetail: PolicyPackDetail? = null,
    val isLoadingDetail: Boolean = false,
) {
    val filteredPacks: List<PolicyPackSummary>
        get() {
            val byFramework = if (frameworkFilter == null) packs
            else packs.filter { it.framework == frameworkFilter }
            return if (searchQuery.isBlank()) byFramework
            else byFramework.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
            }
        }
}

sealed class PolicyLibraryIntent {
    data object Load : PolicyLibraryIntent()
    data class SetFrameworkFilter(val framework: PolicyFramework?) : PolicyLibraryIntent()
    data class UpdateSearch(val query: String) : PolicyLibraryIntent()
    data class SelectPack(val packId: String) : PolicyLibraryIntent()
    data object CloseDetail : PolicyLibraryIntent()
    data object DismissError : PolicyLibraryIntent()
}
