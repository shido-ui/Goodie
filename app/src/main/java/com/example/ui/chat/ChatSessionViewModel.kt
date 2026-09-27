package com.example.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.*
import com.example.data.local.AppDatabase
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.local.entities.SessionEntity
import com.example.data.model.*
import com.example.data.repository.ModelConfigRepository
import com.example.data.repository.SessionRepository
import com.example.data.repository.WorldStateRepository
import com.example.engine.WorldPulseEngine
import com.example.engine.WorldStateEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.*

data class ChatSessionUiState(
    val sessionId: String = "",
    val session: SessionEntity? = null,
    val worldState: WorldState = WorldState(),
    val messages: List<ChatMessageEntity> = emptyList(),
    val activeModelName: String = "",
    val activeProviderId: String = "",
    val isModelConfigured: Boolean = false,
    val isGenerating: Boolean = false,
    val lastError: String? = null,
    val suggestedActions: List<String> = listOf("Examine surroundings", "Speak to innkeeper", "Check inventory", "Wait for an hour")
)

class ChatSessionViewModel(
    application: Application,
    private val initialSessionId: String
) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val sessionRepo = SessionRepository(db)
    private val worldRepo = WorldStateRepository(db)
    private val modelConfigRepo = ModelConfigRepository.getInstance(application)
    private val providerManager = ProviderManager(modelConfigRepo.secureKeyStorage)

    private val narrativeDirector = NarrativeDirector()
    private val proposalExtractor = StateProposalExtractor()
    private val worldEngine = WorldStateEngine()
    private val pulseEngine = WorldPulseEngine()

    private val _uiState = MutableStateFlow(ChatSessionUiState(sessionId = initialSessionId))
    val uiState: StateFlow<ChatSessionUiState> = _uiState.asStateFlow()

    init {
        loadSessionData()
        observeConfiguration()
    }

    private fun observeConfiguration() {
        viewModelScope.launch {
            modelConfigRepo.activeConfig.collect { config ->
                val hasKey = modelConfigRepo.isReadyForChat()
                val model = ModelCatalog.findModel(config.providerId, config.modelId)
                _uiState.update { current ->
                    current.copy(
                        activeModelName = model?.displayName ?: config.modelId,
                        activeProviderId = config.providerId,
                        isModelConfigured = hasKey
                    )
                }
            }
        }
    }

    fun loadSessionData() {
        viewModelScope.launch {
            val session = sessionRepo.getSession(initialSessionId)
            val world = worldRepo.getWorldState(initialSessionId) ?: WorldState()
            val config = modelConfigRepo.getActiveConfig()
            val hasKey = modelConfigRepo.isReadyForChat()
            val model = ModelCatalog.findModel(config.providerId, config.modelId)

            _uiState.update { current ->
                current.copy(
                    session = session,
                    worldState = world,
                    activeModelName = model?.displayName ?: config.modelId,
                    activeProviderId = config.providerId,
                    isModelConfigured = hasKey
                )
            }

            // Observe chat messages
            sessionRepo.getChatMessagesFlow(initialSessionId).collect { msgs ->
                _uiState.update { it.copy(messages = msgs) }
                if (msgs.isEmpty()) {
                    // Send initial opening scene greeting
                    sendOpeningPrologue(session, world)
                }
            }
        }
    }

    private fun sendOpeningPrologue(session: SessionEntity?, world: WorldState) {
        viewModelScope.launch {
            val prologueText = when (session?.mode) {
                GameMode.CHARACTER_CHAT.name -> "You arrive before ${session.characterName ?: "your contact"}. They look up as you approach the designated meeting spot."
                GameMode.OPEN_WORLD_AND_CHARACTER.name -> "You and ${session.characterName ?: "your companion"} step into ${world.player.location}. The air is thick with anticipation."
                else -> "You find yourself in ${world.player.location} in the realm of ${world.worldName}. What do you wish to do?"
            }

            val msg = ChatMessageEntity(
                id = "msg_init_${System.currentTimeMillis()}",
                sessionId = initialSessionId,
                sender = "NARRATOR",
                content = prologueText,
                timestamp = System.currentTimeMillis(),
                worldDay = world.time.day,
                worldTimeFormatted = world.time.toFormattedString()
            )
            sessionRepo.addChatMessage(msg)
        }
    }

    fun sendPlayerAction(playerText: String) {
        if (playerText.isBlank()) return
        val currentState = _uiState.value
        val world = currentState.worldState
        val config = modelConfigRepo.getActiveConfig()

        if (!currentState.isModelConfigured) {
            _uiState.update {
                it.copy(lastError = "Cannot perform action: No AI model configured. Please add an API key in AI Settings.")
            }
            return
        }

        val playerMsg = ChatMessageEntity(
            id = "msg_player_${System.currentTimeMillis()}",
            sessionId = initialSessionId,
            sender = "PLAYER",
            speakerName = world.player.name,
            content = playerText.trim(),
            timestamp = System.currentTimeMillis(),
            worldDay = world.time.day,
            worldTimeFormatted = world.time.toFormattedString()
        )

        _uiState.update { it.copy(isGenerating = true, lastError = null) }

        viewModelScope.launch {
            // Save player message immediately
            sessionRepo.addChatMessage(playerMsg)

            val mode = try {
                GameMode.valueOf(currentState.session?.mode ?: GameMode.OPEN_WORLD.name)
            } catch (e: Exception) {
                GameMode.OPEN_WORLD
            }

            // Convert recent messages for AI prompt
            val recentAiMsgs = currentState.messages.takeLast(6).map {
                ChatMessage(
                    role = if (it.sender == "PLAYER") "user" else "assistant",
                    content = it.content
                )
            }

            val request = narrativeDirector.buildAiRequest(
                playerInput = playerText,
                world = world,
                mode = mode,
                modelId = config.modelId,
                recentChatHistory = recentAiMsgs,
                companionNpcId = currentState.session?.characterId
            )

            val aiResponse = providerManager.executeChat(request, config)

            if (!aiResponse.isSuccess || aiResponse.rawText == null) {
                val errorMsg = aiResponse.errorMessage ?: "Unknown error from AI provider"
                _uiState.update { it.copy(isGenerating = false, lastError = errorMsg) }

                // Insert error indicator message in chat
                val failureMsg = ChatMessageEntity(
                    id = "msg_err_${System.currentTimeMillis()}",
                    sessionId = initialSessionId,
                    sender = "NARRATOR",
                    content = "⚠️ [AI Provider Error]: $errorMsg\n\n(Authoritative world state preserved. You can retry your action or configure API keys in Settings.)",
                    timestamp = System.currentTimeMillis(),
                    worldDay = world.time.day,
                    worldTimeFormatted = world.time.toFormattedString()
                )
                sessionRepo.addChatMessage(failureMsg)
                return@launch
            }

            // Parse response
            val structured = proposalExtractor.extractStructuredResponse(aiResponse.rawText)

            // Authoritative state simulation & proposal validation
            var workingWorld = world
            val appliedSummaries = mutableListOf<String>()

            structured.proposals.forEach { proposal ->
                val (updatedWorld, summary) = worldEngine.applyValidatedProposal(proposal, workingWorld)
                workingWorld = updatedWorld
                appliedSummaries.add(summary)
            }

            // Deterministic World Pulse: Advance time (15 mins per turn if not explicitly advanced)
            val pulse = pulseEngine.executePulse(workingWorld, minutesToAdvance = 15, companionNpcId = currentState.session?.characterId)
            val finalWorld = pulse.updatedWorldState

            val appliedJson = if (appliedSummaries.isNotEmpty()) {
                val arr = JSONArray()
                appliedSummaries.forEach { arr.put(it) }
                arr.toString()
            } else null

            val assistantMsg = ChatMessageEntity(
                id = "msg_ai_${System.currentTimeMillis()}",
                sessionId = initialSessionId,
                sender = if (structured.dialogue != null) "NPC" else "NARRATOR",
                speakerName = structured.speakerNpcId ?: currentState.session?.characterName,
                content = structured.narrative,
                dialogue = structured.dialogue,
                appliedProposalsJson = appliedJson,
                timestamp = System.currentTimeMillis(),
                worldDay = finalWorld.time.day,
                worldTimeFormatted = finalWorld.time.toFormattedString()
            )

            // Atomic save to Room
            worldRepo.saveTurnAtomic(
                sessionId = initialSessionId,
                worldState = finalWorld,
                newMessages = listOf(assistantMsg),
                newLogs = pulse.generatedLogs
            )

            _uiState.update {
                it.copy(
                    worldState = finalWorld,
                    isGenerating = false,
                    lastError = null,
                    suggestedActions = if (structured.suggestedActions.isNotEmpty()) structured.suggestedActions else it.suggestedActions
                )
            }
        }
    }

    fun triggerManualPulse(minutes: Int = 30) {
        viewModelScope.launch {
            val currentWorld = _uiState.value.worldState
            val pulse = pulseEngine.executePulse(currentWorld, minutesToAdvance = minutes, companionNpcId = _uiState.value.session?.characterId)
            val updatedWorld = pulse.updatedWorldState

            val pulseMsg = ChatMessageEntity(
                id = "msg_pulse_${System.currentTimeMillis()}",
                sessionId = initialSessionId,
                sender = "NARRATOR",
                content = "⏳ [Time Passed]: $minutes minutes elapsed. The world simulation advanced (${updatedWorld.time.toFormattedString()}).",
                timestamp = System.currentTimeMillis(),
                worldDay = updatedWorld.time.day,
                worldTimeFormatted = updatedWorld.time.toFormattedString()
            )

            worldRepo.saveTurnAtomic(
                sessionId = initialSessionId,
                worldState = updatedWorld,
                newMessages = listOf(pulseMsg),
                newLogs = pulse.generatedLogs
            )

            _uiState.update { it.copy(worldState = updatedWorld) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(lastError = null) }
    }
}
