package com.example.ui.modes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.SessionEntity
import com.example.data.model.GameMode
import com.example.data.model.NpcState
import com.example.data.model.WorldState
import com.example.data.repository.ModelConfigRepository
import com.example.data.repository.SessionRepository
import com.example.engine.PresetWorlds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

enum class PresetWorldType(val title: String, val subtitle: String) {
    ELDORIA("Eldoria: The Shattered Realm", "High Fantasy • Ancient Ley Lines • Crown Knights"),
    NEO_VERIDIA("Neo-Veridia 2088", "Cyberpunk • Megacorps • Rogue Netrunners"),
    RAVENLOFT("Ravenloft: Shadow of the Moon", "Gothic Horror • Vampires • Occult Mysteries")
}

data class ModeSelectionUiState(
    val selectedMode: GameMode = GameMode.OPEN_WORLD,
    val selectedWorldPreset: PresetWorldType = PresetWorldType.ELDORIA,
    val customCampaignTitle: String = "The Chronicles of Eldoria",
    val selectedCompanionNpcId: String? = null,
    val availableNpcs: List<NpcState> = emptyList(),
    val isCreating: Boolean = false,
    val createdSessionId: String? = null
)

class ModeSelectionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val sessionRepository = SessionRepository(db)
    private val modelConfigRepo = ModelConfigRepository.getInstance(application)

    private val _uiState = MutableStateFlow(ModeSelectionUiState())
    val uiState: StateFlow<ModeSelectionUiState> = _uiState.asStateFlow()

    init {
        updateWorldPreset(PresetWorldType.ELDORIA)
    }

    fun selectMode(mode: GameMode) {
        _uiState.value = _uiState.value.copy(selectedMode = mode)
    }

    fun updateWorldPreset(preset: PresetWorldType) {
        val world = getWorldForPreset(preset)
        _uiState.value = _uiState.value.copy(
            selectedWorldPreset = preset,
            customCampaignTitle = world.worldName,
            availableNpcs = world.npcs,
            selectedCompanionNpcId = world.npcs.firstOrNull()?.id
        )
    }

    fun updateCampaignTitle(title: String) {
        _uiState.value = _uiState.value.copy(customCampaignTitle = title)
    }

    fun selectCompanion(npcId: String) {
        _uiState.value = _uiState.value.copy(selectedCompanionNpcId = npcId)
    }

    fun createAndStartSession(onCreated: (String) -> Unit) {
        val state = _uiState.value
        _uiState.value = state.copy(isCreating = true)

        viewModelScope.launch {
            val worldState = getWorldForPreset(state.selectedWorldPreset)
            val config = modelConfigRepo.getActiveConfig()
            val companion = worldState.npcs.find { it.id == state.selectedCompanionNpcId }

            val sessionId = "session_${UUID.randomUUID()}"
            val sessionEntity = SessionEntity(
                id = sessionId,
                mode = state.selectedMode.name,
                title = state.customCampaignTitle.ifBlank { worldState.worldName },
                worldId = worldState.worldId,
                worldName = worldState.worldName,
                characterId = companion?.id,
                characterName = companion?.name,
                modelId = config.modelId,
                providerId = config.providerId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            sessionRepository.createSession(sessionEntity, worldState)
            _uiState.value = state.copy(isCreating = false, createdSessionId = sessionId)
            onCreated(sessionId)
        }
    }

    private fun getWorldForPreset(preset: PresetWorldType): WorldState {
        return when (preset) {
            PresetWorldType.ELDORIA -> PresetWorlds.createEldoria()
            PresetWorldType.NEO_VERIDIA -> PresetWorlds.createCyberpunk()
            PresetWorldType.RAVENLOFT -> PresetWorlds.createGothic()
        }
    }
}
