package com.devscion.auditforge.ui.sessions.detail.uploads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devscion.auditforge.FilePicker
import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.domain.usecase.uploads.DeleteUploadUseCase
import com.devscion.auditforge.domain.usecase.uploads.GetUploadsUseCase
import com.devscion.auditforge.domain.usecase.uploads.UploadFileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class UploadsViewModel(
    @InjectedParam private val sessionId: String,
    private val getUploadsUseCase: GetUploadsUseCase,
    private val uploadFileUseCase: UploadFileUseCase,
    private val deleteUploadUseCase: DeleteUploadUseCase,
    private val filePicker: FilePicker,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UploadsUiState())
    val uiState: StateFlow<UploadsUiState> = _uiState.asStateFlow()

    init {
        onIntent(UploadsIntent.Load)
    }

    fun onIntent(intent: UploadsIntent) {
        when (intent) {
            UploadsIntent.Load -> loadUploads()
            UploadsIntent.ShowTypeSelector -> _uiState.update { it.copy(showTypeSelector = true) }
            UploadsIntent.HideTypeSelector -> _uiState.update { it.copy(showTypeSelector = false, selectedUploadType = null) }
            is UploadsIntent.SelectTypeAndPickFile -> pickAndUpload(intent)
            is UploadsIntent.RequestDelete -> _uiState.update { it.copy(uploadToDelete = intent.upload) }
            UploadsIntent.ConfirmDelete -> deleteUpload()
            UploadsIntent.CancelDelete -> _uiState.update { it.copy(uploadToDelete = null) }
            UploadsIntent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun loadUploads() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getUploadsUseCase(sessionId)) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, uploads = result.data.data) }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                ApiResult.Unauthorized, ApiResult.NetworkError -> _uiState.update {
                    it.copy(isLoading = false, error = "Failed to load uploads. Check your connection.")
                }
            }
        }
    }

    private fun pickAndUpload(intent: UploadsIntent.SelectTypeAndPickFile) {
        viewModelScope.launch {
            _uiState.update { it.copy(showTypeSelector = false, selectedUploadType = intent.uploadType) }
            val picked = filePicker.pickFile(intent.uploadType.allowedExtensions)
            if (picked == null) {
                _uiState.update { it.copy(selectedUploadType = null) }
                return@launch
            }
            _uiState.update { it.copy(isUploading = true) }
            when (val result = uploadFileUseCase(sessionId, picked, intent.uploadType)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isUploading = false, selectedUploadType = null) }
                    loadUploads()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isUploading = false, selectedUploadType = null, error = result.message)
                }
                ApiResult.Unauthorized, ApiResult.NetworkError -> _uiState.update {
                    it.copy(isUploading = false, selectedUploadType = null, error = "Upload failed. Check your connection.")
                }
            }
        }
    }

    private fun deleteUpload() {
        val upload = _uiState.value.uploadToDelete ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, uploadToDelete = null) }
            when (val result = deleteUploadUseCase(sessionId, upload.id)) {
                is ApiResult.Success -> {
                    _uiState.update { state ->
                        state.copy(isDeleting = false, uploads = state.uploads.filter { it.id != upload.id })
                    }
                }
                is ApiResult.Error -> _uiState.update { it.copy(isDeleting = false, error = result.message) }
                ApiResult.Unauthorized, ApiResult.NetworkError -> _uiState.update {
                    it.copy(isDeleting = false, error = "Failed to delete upload. Check your connection.")
                }
            }
        }
    }
}
