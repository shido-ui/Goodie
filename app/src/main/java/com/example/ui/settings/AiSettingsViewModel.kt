package com.example.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.*
import com.example.data.repository.ModelConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AiSettingsUiState(
    val providerId: String = ProviderConfig.PROVIDER_GEMINI,
    val endpointUrl: String = ProviderConfig.DEFAULT_GEMINI_ENDPOINT,
    val modelId: String = ProviderConfig.DEFAULT_GEMINI_MODEL_ID,
    val customModelInput: String = "",
    val apiKeyInput: String = "",
    val maskedApiKey: String = "None configured",
    val hasKeyConfigured: Boolean = false,
    val temperature: Float = 0.8f,
    val maxTokens: Int = 2048,
    val isTestingConnection: Boolean = false,
    val testResultSuccess: Boolean? = null,
    val testResultMessage: String? = null,
    val saveSuccessMessage: String? = null
)

class AiSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val modelConfigRepo = ModelConfigRepository.getInstance(application)
    private val providerManager = ProviderManager(modelConfigRepo.secureKeyStorage)

    private val _uiState = MutableStateFlow(AiSettingsUiState())
    val uiState: StateFlow<AiSettingsUiState> = _uiState.asStateFlow()

    init {
        loadCurrentConfig()
    }

    private fun loadCurrentConfig() {
        val config = modelConfigRepo.getActiveConfig()
        val hasKey = modelConfigRepo.secureKeyStorage.hasApiKey(config.providerId)
        val masked = modelConfigRepo.getMaskedApiKey(config.providerId)

        _uiState.value = _uiState.value.copy(
            providerId = config.providerId,
            endpointUrl = config.endpointUrl,
            modelId = config.modelId,
            customModelInput = config.modelId,
            maskedApiKey = masked,
            hasKeyConfigured = hasKey,
            temperature = config.temperature,
            maxTokens = config.maxOutputTokens
        )
    }

    fun selectProvider(providerId: String) {
        val defaultEndpoint = when (providerId.lowercase()) {
            ProviderConfig.PROVIDER_GEMINI -> ProviderConfig.DEFAULT_GEMINI_ENDPOINT
            ProviderConfig.PROVIDER_OPENAI -> ProviderConfig.DEFAULT_OPENAI_ENDPOINT
            else -> "http://10.0.2.2:11434/v1/chat/completions"
        }
        val defaultModel = when (providerId.lowercase()) {
            ProviderConfig.PROVIDER_GEMINI -> ProviderConfig.DEFAULT_GEMINI_MODEL_ID
            ProviderConfig.PROVIDER_OPENAI -> ProviderConfig.DEFAULT_OPENAI_MODEL_ID
            else -> "llama3"
        }
        val hasKey = modelConfigRepo.secureKeyStorage.hasApiKey(providerId)
        val masked = modelConfigRepo.getMaskedApiKey(providerId)

        _uiState.value = _uiState.value.copy(
            providerId = providerId,
            endpointUrl = defaultEndpoint,
            modelId = defaultModel,
            customModelInput = defaultModel,
            hasKeyConfigured = hasKey,
            maskedApiKey = masked,
            apiKeyInput = "",
            testResultSuccess = null,
            testResultMessage = null
        )
    }

    fun updateEndpoint(url: String) {
        _uiState.value = _uiState.value.copy(endpointUrl = url)
    }

    fun selectModel(modelId: String) {
        _uiState.value = _uiState.value.copy(
            modelId = modelId,
            customModelInput = modelId
        )
    }

    fun updateCustomModel(modelId: String) {
        _uiState.value = _uiState.value.copy(
            modelId = modelId,
            customModelInput = modelId
        )
    }

    fun updateApiKeyInput(key: String) {
        _uiState.value = _uiState.value.copy(apiKeyInput = key)
    }

    fun updateTemperature(temp: Float) {
        _uiState.value = _uiState.value.copy(temperature = temp)
    }

    fun updateMaxTokens(tokens: Int) {
        _uiState.value = _uiState.value.copy(maxTokens = tokens)
    }

    fun saveConfiguration() {
        val state = _uiState.value
        val newConfig = ProviderConfig(
            providerId = state.providerId,
            endpointUrl = state.endpointUrl.trim(),
            modelId = state.modelId.trim().ifBlank { ProviderConfig.DEFAULT_GEMINI_MODEL_ID },
            temperature = state.temperature,
            maxOutputTokens = state.maxTokens
        )

        modelConfigRepo.saveConfig(newConfig)

        if (state.apiKeyInput.isNotBlank()) {
            modelConfigRepo.saveApiKey(state.providerId, state.apiKeyInput.trim())
        }

        val hasKey = modelConfigRepo.secureKeyStorage.hasApiKey(state.providerId)
        val masked = modelConfigRepo.getMaskedApiKey(state.providerId)

        _uiState.value = state.copy(
            apiKeyInput = "",
            hasKeyConfigured = hasKey,
            maskedApiKey = masked,
            saveSuccessMessage = "Configuration saved successfully! Active model: ${newConfig.modelId}"
        )
    }

    fun clearApiKey() {
        val state = _uiState.value
        modelConfigRepo.removeApiKey(state.providerId)
        _uiState.value = state.copy(
            apiKeyInput = "",
            hasKeyConfigured = false,
            maskedApiKey = "None configured",
            saveSuccessMessage = "API key cleared for ${state.providerId}."
        )
    }

    fun testConnection() {
        val state = _uiState.value
        _uiState.value = state.copy(
            isTestingConnection = true,
            testResultSuccess = null,
            testResultMessage = null
        )

        viewModelScope.launch {
            val config = ProviderConfig(
                providerId = state.providerId,
                endpointUrl = state.endpointUrl.trim(),
                modelId = state.modelId.trim(),
                temperature = 0.2f,
                maxOutputTokens = 50
            )

            // Temporarily use the input key if typed, otherwise existing saved key
            val keyToUse = if (state.apiKeyInput.isNotBlank()) state.apiKeyInput.trim()
            else modelConfigRepo.secureKeyStorage.getApiKey(state.providerId)

            val provider = providerManager.getProvider(state.providerId)
            val response = provider.testConnection(config, keyToUse)

            _uiState.value = _uiState.value.copy(
                isTestingConnection = false,
                testResultSuccess = response.isSuccess,
                testResultMessage = if (response.isSuccess) {
                    "Connection Successful! Latency: ${response.latencyMs}ms"
                } else {
                    response.errorMessage ?: "Failed to connect to ${config.endpointUrl}"
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            testResultSuccess = null,
            testResultMessage = null,
            saveSuccessMessage = null
        )
    }
}
