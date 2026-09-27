package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.ai.AiModel
import com.example.ai.ModelCatalog
import com.example.ai.ProviderConfig
import com.example.data.local.SecureKeyStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class ModelConfigRepository(
    private val context: Context,
    val secureKeyStorage: SecureKeyStorage
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _activeConfig = MutableStateFlow(loadConfigFromPrefs())
    val activeConfig: StateFlow<ProviderConfig> = _activeConfig.asStateFlow()

    private val _hasKeyConfigured = MutableStateFlow(checkHasKey())
    val hasKeyConfigured: StateFlow<Boolean> = _hasKeyConfigured.asStateFlow()

    companion object {
        private const val PREFS_NAME = "rpg_ai_model_config"
        private const val KEY_CONFIG_JSON = "active_provider_config_json"

        @Volatile
        private var INSTANCE: ModelConfigRepository? = null

        fun getInstance(context: Context): ModelConfigRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ModelConfigRepository(
                    context.applicationContext,
                    SecureKeyStorage(context.applicationContext)
                ).also { INSTANCE = it }
            }
        }
    }

    private fun loadConfigFromPrefs(): ProviderConfig {
        val jsonStr = prefs.getString(KEY_CONFIG_JSON, null)
        if (!jsonStr.isNullOrBlank()) {
            try {
                return ProviderConfig.fromJson(JSONObject(jsonStr))
            } catch (e: Exception) {
                // Fallback to default Gemini config
            }
        }
        return ProviderConfig.defaultGemini()
    }

    private fun checkHasKey(): Boolean {
        val current = _activeConfig.value
        return secureKeyStorage.hasApiKey(current.providerId)
    }

    fun getActiveConfig(): ProviderConfig = _activeConfig.value

    fun getActiveModelDisplayName(): String {
        val config = getActiveConfig()
        val model = ModelCatalog.findModel(config.providerId, config.modelId)
        return model?.displayName ?: config.modelId
    }

    fun saveConfig(config: ProviderConfig) {
        prefs.edit().putString(KEY_CONFIG_JSON, config.toJson().toString()).apply()
        _activeConfig.value = config
        _hasKeyConfigured.value = secureKeyStorage.hasApiKey(config.providerId)
    }

    fun saveApiKey(providerId: String, apiKey: String) {
        secureKeyStorage.saveApiKey(providerId, apiKey)
        _hasKeyConfigured.value = secureKeyStorage.hasApiKey(_activeConfig.value.providerId)
    }

    fun removeApiKey(providerId: String) {
        secureKeyStorage.removeApiKey(providerId)
        _hasKeyConfigured.value = secureKeyStorage.hasApiKey(_activeConfig.value.providerId)
    }

    fun isReadyForChat(): Boolean {
        val config = getActiveConfig()
        if (config.endpointUrl.contains("localhost") || config.endpointUrl.contains("10.0.2.2")) {
            return true
        }
        return secureKeyStorage.hasApiKey(config.providerId)
    }

    fun getMaskedApiKey(providerId: String): String {
        return secureKeyStorage.getMaskedApiKey(providerId)
    }

    fun clearConfig() {
        prefs.edit().clear().apply()
        _activeConfig.value = ProviderConfig.defaultGemini()
        _hasKeyConfigured.value = false
    }
}
