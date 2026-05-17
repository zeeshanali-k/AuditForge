package com.devscion.auditforge.ui.sessions.detail.policies

import com.devscion.auditforge.domain.model.PolicyFramework
import com.devscion.auditforge.domain.model.PolicyPackDetail
import com.devscion.auditforge.domain.model.PolicyPackSummary

data class PoliciesUiState(
    val packs: List<PolicyPackSummary> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedPackIds: Set<String> = emptySet(),
    val hasChanges: Boolean = false,
    val frameworkFilter: PolicyFramework? = null,
    val expandedPackId: String? = null,
    val packDetail: PolicyPackDetail? = null,
    val isLoadingDetail: Boolean = false,
) {
    val filteredPacks: List<PolicyPackSummary>
        get() = if (frameworkFilter == null) packs
        else packs.filter { it.framework == frameworkFilter }
}

sealed class PoliciesIntent {
    data object Load : PoliciesIntent()
    data class SetFrameworkFilter(val framework: PolicyFramework?) : PoliciesIntent()
    data class TogglePackSelection(val packId: String) : PoliciesIntent()
    data class ExpandPack(val packId: String) : PoliciesIntent()
    data object CollapsePack : PoliciesIntent()
    data object SaveSelection : PoliciesIntent()
    data object DismissError : PoliciesIntent()
}
