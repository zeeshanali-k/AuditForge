package com.devscion.auditforge.ui.sessions.detail.findings

import com.devscion.auditforge.domain.model.Finding
import com.devscion.auditforge.domain.model.FindingStatus
import com.devscion.auditforge.domain.model.FindingsSummary
import com.devscion.auditforge.domain.model.RuleSeverity

data class FindingsUiState(
    val isLoading: Boolean = false,
    val allFindings: List<Finding> = emptyList(),
    val summary: FindingsSummary? = null,
    val filterSeverity: Set<RuleSeverity> = emptySet(),
    val filterStatus: FindingStatus? = null,
    val filterCategory: String? = null,
    val searchQuery: String = "",
    val selectedFinding: Finding? = null,
    val isUpdatingStatus: Boolean = false,
    val pendingStatus: FindingStatus? = null,
    val pendingComment: String = "",
    val error: String? = null,
) {
    val hasActiveFilters: Boolean
        get() = filterSeverity.isNotEmpty() || filterStatus != null || filterCategory != null || searchQuery.isNotEmpty()

    val displayedFindings: List<Finding>
        get() {
            var result = allFindings
            if (filterSeverity.isNotEmpty()) result = result.filter { it.severity in filterSeverity }
            if (filterStatus != null) result = result.filter { it.status == filterStatus }
            if (filterCategory != null) result = result.filter { it.category.equals(filterCategory, ignoreCase = true) }
            if (searchQuery.isNotEmpty()) result = result.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true) ||
                    it.ruleId.contains(searchQuery, ignoreCase = true)
            }
            return result
        }

    val availableCategories: List<String>
        get() = allFindings.map { it.category }.distinct().sorted()
}

sealed class FindingsIntent {
    data object Load : FindingsIntent()
    data class ToggleSeverityFilter(val severity: RuleSeverity) : FindingsIntent()
    data class SetStatusFilter(val status: FindingStatus?) : FindingsIntent()
    data class SetCategoryFilter(val category: String?) : FindingsIntent()
    data class SetSearchQuery(val query: String) : FindingsIntent()
    data object ClearFilters : FindingsIntent()
    data class SelectFinding(val finding: Finding) : FindingsIntent()
    data object DismissFindingDetail : FindingsIntent()
    data class SetPendingStatus(val status: FindingStatus) : FindingsIntent()
    data class SetPendingComment(val comment: String) : FindingsIntent()
    data object ConfirmStatusUpdate : FindingsIntent()
    data object DismissError : FindingsIntent()
}
