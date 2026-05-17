package com.devscion.auditforge.ui.sessions.detail.uploads

import com.devscion.auditforge.domain.model.UploadSummary
import com.devscion.auditforge.domain.model.UploadType

data class UploadsUiState(
    val uploads: List<UploadSummary> = emptyList(),
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val error: String? = null,
    val selectedUploadType: UploadType? = null,
    val showTypeSelector: Boolean = false,
    val uploadToDelete: UploadSummary? = null,
    val isDeleting: Boolean = false,
)

sealed class UploadsIntent {
    data object Load : UploadsIntent()
    data object ShowTypeSelector : UploadsIntent()
    data object HideTypeSelector : UploadsIntent()
    data class SelectTypeAndPickFile(val uploadType: UploadType) : UploadsIntent()
    data class RequestDelete(val upload: UploadSummary) : UploadsIntent()
    data object ConfirmDelete : UploadsIntent()
    data object CancelDelete : UploadsIntent()
    data object DismissError : UploadsIntent()
}
