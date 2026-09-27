package com.example.ui.debug

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.ModelCatalog
import com.example.data.local.AppDatabase
import com.example.data.local.entities.SimulationLogDbEntity
import com.example.data.model.WorldState
import com.example.data.repository.ModelConfigRepository
import com.example.data.repository.WorldStateRepository
import com.example.engine.WorldConsistencyValidator
import com.example.engine.WorldPulseEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URI

data class WorldDebugUiState(
    val sessionId: String = "",
    val worldState: WorldState = WorldState(),
    val logs: List<SimulationLogDbEntity> = emptyList(),
    val providerId: String = "",
    val modelId: String = "",
    val endpointHost: String = "",
    val hasApiKey: Boolean = false,
    val auditResults: List<String> = emptyList(),
    val lastPulseSummary: String? = null
)

class WorldDebugViewModel(
    application: Application,
    private val sessionId: String
) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val worldRepo = WorldStateRepository(db)
    private val modelConfigRepo = ModelConfigRepository.getInstance(application)
    private val validator = WorldConsistencyValidator()
    private val pulseEngine = WorldPulseEngine()

    private val _uiState = MutableStateFlow(WorldDebugUiState(sessionId = sessionId))
    val uiState: StateFlow<WorldDebugUiState> = _uiState.asStateFlow()

    init {
        loadDebugData()
    }

    fun loadDebugData() {
        viewModelScope.launch {
            val config = modelConfigRepo.getActiveConfig()
            val hasKey = modelConfigRepo.secureKeyStorage.hasApiKey(config.providerId)
            val host = try {
                URI(config.endpointUrl).host ?: config.endpointUrl
            } catch (e: Exception) {
                "Unknown Host"
            }

            val world = worldRepo.getWorldState(sessionId) ?: WorldState()
            val logs = db.simulationLogDao().getRecentLogs(sessionId, 50)
            val audit = validator.validateWorldStateIntegrity(world)

            _uiState.value = _uiState.value.copy(
                worldState = world,
                logs = logs,
                providerId = config.providerId,
                modelId = config.modelId,
                endpointHost = host,
                hasApiKey = hasKey,
                auditResults = audit
            )
        }
    }

    fun triggerDebugPulse(minutes: Int = 60) {
        viewModelScope.launch {
            val currentWorld = _uiState.value.worldState
            val pulse = pulseEngine.executePulse(currentWorld, minutesToAdvance = minutes)
            worldRepo.saveTurnAtomic(
                sessionId = sessionId,
                worldState = pulse.updatedWorldState,
                newMessages = emptyList(),
                newLogs = pulse.generatedLogs
            )
            val updatedLogs = db.simulationLogDao().getRecentLogs(sessionId, 50)
            val audit = validator.validateWorldStateIntegrity(pulse.updatedWorldState)

            _uiState.value = _uiState.value.copy(
                worldState = pulse.updatedWorldState,
                logs = updatedLogs,
                auditResults = audit,
                lastPulseSummary = "Advanced $minutes min. Decisions: ${pulse.npcDecisions.size}, Fired consequences: ${pulse.firedConsequences.size}"
            )
        }
    }

    fun generateDiagnosticReport(): String {
        val state = _uiState.value
        val world = state.worldState
        return """
=== RPG AI HUB WORLD DIAGNOSTIC ===
Session ID: ${state.sessionId}
Schema Version: ${world.schemaVersion}
Provider: ${state.providerId} (Key Configured: ${state.hasApiKey})
Model ID: ${state.modelId}
Endpoint Host: ${state.endpointHost} (API Key: [REDACTED])

[WORLD TIME & STATE]
Time: ${world.time.toFormattedString()} (Total Minutes: ${world.time.toTotalMinutes()})
Player: ${world.player.name} | HP: ${world.player.hp}/${world.player.maxHp} | Gold: ${world.player.money}
Location: ${world.player.location}

[STATISTICS]
NPCs: ${world.npcs.size}
Quests: ${world.quests.size}
Factions: ${world.factions.size}
Pending Consequences: ${world.pendingConsequences.size}
Rumors: ${world.rumors.size}
Secrets: ${world.secrets.size}

[INTEGRITY AUDIT]
Violations: ${if (state.auditResults.isEmpty()) "None (0 violations)" else state.auditResults.joinToString("\n- ")}
""".trimIndent()
    }
}
