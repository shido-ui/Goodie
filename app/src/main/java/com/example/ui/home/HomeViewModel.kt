package com.example.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.ModelCatalog
import com.example.data.local.AppDatabase
import com.example.data.local.entities.SessionEntity
import com.example.data.repository.ModelConfigRepository
import com.example.data.repository.SessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val sessions: List<SessionEntity> = emptyList(),
    val activeModelName: String = "Gemini 2.5 Flash",
    val activeProviderId: String = "gemini",
    val isModelConfigured: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val sessionRepository = SessionRepository(db)
    private val modelConfigRepository = ModelConfigRepository.getInstance(application)

    val uiState: StateFlow<HomeUiState> = sessionRepository.getAllSessions()
        .map { sessions ->
            val config = modelConfigRepository.getActiveConfig()
            val hasKey = modelConfigRepository.isReadyForChat()
            val model = ModelCatalog.findModel(config.providerId, config.modelId)

            HomeUiState(
                sessions = sessions,
                activeModelName = model?.displayName ?: config.modelId,
                activeProviderId = config.providerId,
                isModelConfigured = hasKey
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        )

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            sessionRepository.deleteSession(sessionId)
        }
    }
}
