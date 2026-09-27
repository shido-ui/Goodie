package com.example.ui.backup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.SessionEntity
import com.example.data.repository.BackupRestoreManager
import com.example.data.repository.RestoreResult
import com.example.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BackupRestoreUiState(
    val sessions: List<SessionEntity> = emptyList(),
    val selectedExportSessionId: String? = null,
    val exportedJson: String? = null,
    val importInputText: String = "",
    val isProcessing: Boolean = false,
    val resultSuccessMessage: String? = null,
    val resultErrorMessage: String? = null
)

class BackupRestoreViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val sessionRepo = SessionRepository(db)
    private val backupManager = BackupRestoreManager(db)

    private val _uiState = MutableStateFlow(BackupRestoreUiState())
    val uiState: StateFlow<BackupRestoreUiState> = _uiState.asStateFlow()

    init {
        loadSessions()
    }

    private fun loadSessions() {
        viewModelScope.launch {
            sessionRepo.getAllSessions().collect { list ->
                _uiState.value = _uiState.value.copy(
                    sessions = list,
                    selectedExportSessionId = if (_uiState.value.selectedExportSessionId == null) list.firstOrNull()?.id else _uiState.value.selectedExportSessionId
                )
            }
        }
    }

    fun selectExportSession(sessionId: String) {
        _uiState.value = _uiState.value.copy(selectedExportSessionId = sessionId, exportedJson = null)
    }

    fun updateImportText(text: String) {
        _uiState.value = _uiState.value.copy(importInputText = text)
    }

    fun exportSelectedSession() {
        val sessionId = _uiState.value.selectedExportSessionId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            val json = backupManager.exportSessionBackup(sessionId)
            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                exportedJson = json,
                resultSuccessMessage = if (json != null) "Backup exported successfully!" else null,
                resultErrorMessage = if (json == null) "Failed to export backup for session." else null
            )
        }
    }

    fun validateAndRestore() {
        val text = _uiState.value.importInputText
        if (text.isBlank()) {
            _uiState.value = _uiState.value.copy(resultErrorMessage = "Please paste JSON backup data to restore.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, resultErrorMessage = null, resultSuccessMessage = null)
            val result = backupManager.validateAndRestoreBackup(text.trim())
            when (result) {
                is RestoreResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        importInputText = "",
                        resultSuccessMessage = "Successfully restored campaign: '${result.worldName}' (ID: ${result.sessionId})"
                    )
                }
                is RestoreResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        resultErrorMessage = "Restore rejected: ${result.errorReason}"
                    )
                }
            }
        }
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(resultSuccessMessage = null, resultErrorMessage = null)
    }
}
