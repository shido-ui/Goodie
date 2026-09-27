package com.example.ai

interface AiProvider {
    val providerId: String
    suspend fun executeChat(request: AiRequest, config: ProviderConfig, apiKey: String?): AiResponse
    suspend fun testConnection(config: ProviderConfig, apiKey: String?): AiResponse
}
