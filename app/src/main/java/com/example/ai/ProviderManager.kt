package com.example.ai

import com.example.data.local.SecureKeyStorage
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ProviderManager(
    private val keyStorage: SecureKeyStorage
) {
    private val providers = mutableMapOf<String, AiProvider>()
    private val cache = mutableMapOf<String, String>()
    private val cacheMutex = Mutex()

    init {
        registerProvider(OpenAiCompatibleProvider(ProviderConfig.PROVIDER_GEMINI))
        registerProvider(OpenAiCompatibleProvider(ProviderConfig.PROVIDER_OPENAI))
        registerProvider(OpenAiCompatibleProvider(ProviderConfig.PROVIDER_CUSTOM))
    }

    fun registerProvider(provider: AiProvider) {
        providers[provider.providerId.lowercase()] = provider
    }

    fun getProvider(providerId: String): AiProvider {
        return providers[providerId.lowercase()] ?: OpenAiCompatibleProvider(providerId)
    }

    suspend fun executeChat(request: AiRequest, config: ProviderConfig): AiResponse {
        val provider = getProvider(config.providerId)
        val apiKey = keyStorage.getApiKey(config.providerId)
        return provider.executeChat(request, config, apiKey)
    }

    suspend fun testConnection(config: ProviderConfig): AiResponse {
        val provider = getProvider(config.providerId)
        val apiKey = keyStorage.getApiKey(config.providerId)
        return provider.testConnection(config, apiKey)
    }

    suspend fun getCachedOrNull(cacheKey: String): String? = cacheMutex.withLock {
        cache[cacheKey]
    }

    suspend fun putCache(cacheKey: String, responseText: String) = cacheMutex.withLock {
        if (cache.size > 100) {
            val oldest = cache.keys.firstOrNull()
            if (oldest != null) cache.remove(oldest)
        }
        cache[cacheKey] = responseText
    }
}
