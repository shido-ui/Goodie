package com.example.ai

import org.json.JSONObject

data class ProviderConfig(
    val providerId: String = DEFAULT_PROVIDER_ID,
    val endpointUrl: String = DEFAULT_GEMINI_ENDPOINT,
    val modelId: String = DEFAULT_GEMINI_MODEL_ID,
    val temperature: Float = 0.8f,
    val maxOutputTokens: Int = 2048,
    val customHeaders: Map<String, String> = emptyMap(),
    val isEnabled: Boolean = true
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("providerId", providerId)
        put("endpointUrl", endpointUrl)
        put("modelId", modelId)
        put("temperature", temperature.toDouble())
        put("maxOutputTokens", maxOutputTokens)
        val h = JSONObject()
        customHeaders.forEach { (k, v) -> h.put(k, v) }
        put("customHeaders", h)
        put("isEnabled", isEnabled)
    }

    companion object {
        const val PROVIDER_GEMINI = "gemini"
        const val PROVIDER_OPENAI = "openai"
        const val PROVIDER_CUSTOM = "custom"

        const val DEFAULT_PROVIDER_ID = PROVIDER_GEMINI
        const val DEFAULT_GEMINI_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions"
        const val DEFAULT_GEMINI_MODEL_ID = "gemini-2.5-flash"

        const val DEFAULT_OPENAI_ENDPOINT = "https://api.openai.com/v1/chat/completions"
        const val DEFAULT_OPENAI_MODEL_ID = "gpt-4o-mini"

        fun defaultGemini(): ProviderConfig = ProviderConfig(
            providerId = PROVIDER_GEMINI,
            endpointUrl = DEFAULT_GEMINI_ENDPOINT,
            modelId = DEFAULT_GEMINI_MODEL_ID,
            temperature = 0.8f,
            maxOutputTokens = 2048
        )

        fun defaultOpenAi(): ProviderConfig = ProviderConfig(
            providerId = PROVIDER_OPENAI,
            endpointUrl = DEFAULT_OPENAI_ENDPOINT,
            modelId = DEFAULT_OPENAI_MODEL_ID,
            temperature = 0.8f,
            maxOutputTokens = 2048
        )

        fun fromJson(json: JSONObject?): ProviderConfig {
            if (json == null) return defaultGemini()
            val headers = mutableMapOf<String, String>()
            val hObj = json.optJSONObject("customHeaders")
            if (hObj != null) {
                val keys = hObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    headers[k] = hObj.optString(k, "")
                }
            }

            return ProviderConfig(
                providerId = json.optString("providerId", DEFAULT_PROVIDER_ID),
                endpointUrl = json.optString("endpointUrl", DEFAULT_GEMINI_ENDPOINT),
                modelId = json.optString("modelId", DEFAULT_GEMINI_MODEL_ID),
                temperature = json.optDouble("temperature", 0.8).toFloat(),
                maxOutputTokens = json.optInt("maxOutputTokens", 2048),
                customHeaders = headers,
                isEnabled = json.optBoolean("isEnabled", true)
            )
        }
    }
}
