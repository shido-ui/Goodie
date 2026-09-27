package com.example.ai

data class AiModel(
    val modelId: String,
    val providerId: String,
    val displayName: String,
    val description: String,
    val capabilities: List<String> = listOf("chat", "structured_json", "reasoning"),
    val contextLimitTokens: Int = 128000,
    val isEnabled: Boolean = true
)

object ModelCatalog {
    val GEMINI_MODELS = listOf(
        AiModel(
            modelId = "gemini-2.5-flash",
            providerId = "gemini",
            displayName = "Gemini 2.5 Flash",
            description = "Recommended: Ultra fast, highly intelligent, native structured outputs.",
            contextLimitTokens = 1000000
        ),
        AiModel(
            modelId = "gemini-2.5-pro",
            providerId = "gemini",
            displayName = "Gemini 2.5 Pro",
            description = "Maximum reasoning depth for complex world narrative and factions.",
            contextLimitTokens = 2000000
        ),
        AiModel(
            modelId = "gemini-2.0-flash",
            providerId = "gemini",
            displayName = "Gemini 2.0 Flash",
            description = "High speed generation for low-latency roleplay turns.",
            contextLimitTokens = 1000000
        ),
        AiModel(
            modelId = "gemini-1.5-flash",
            providerId = "gemini",
            displayName = "Gemini 1.5 Flash",
            description = "Reliable lightweight multimodal & narrative model.",
            contextLimitTokens = 1000000
        )
    )

    val OPENAI_MODELS = listOf(
        AiModel(
            modelId = "gpt-4o",
            providerId = "openai",
            displayName = "GPT-4o",
            description = "Flagship high-intelligence OpenAI roleplay model.",
            contextLimitTokens = 128000
        ),
        AiModel(
            modelId = "gpt-4o-mini",
            providerId = "openai",
            displayName = "GPT-4o Mini",
            description = "Fast, cost-effective OpenAI model for continuous narrative.",
            contextLimitTokens = 128000
        )
    )

    val CUSTOM_MODELS = listOf(
        AiModel(
            modelId = "custom-model",
            providerId = "custom",
            displayName = "Custom Endpoint Model",
            description = "User-defined model via local Ollama, LM Studio, or custom proxy.",
            contextLimitTokens = 32000
        )
    )

    fun getAllPredefined(): List<AiModel> = GEMINI_MODELS + OPENAI_MODELS + CUSTOM_MODELS

    fun findModel(providerId: String, modelId: String): AiModel? {
        return getAllPredefined().find { it.providerId.equals(providerId, ignoreCase = true) && it.modelId == modelId }
            ?: AiModel(
                modelId = modelId,
                providerId = providerId,
                displayName = modelId,
                description = "User-configured model"
            )
    }
}
