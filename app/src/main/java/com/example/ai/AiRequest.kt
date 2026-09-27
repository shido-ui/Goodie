package com.example.ai

import org.json.JSONArray
import org.json.JSONObject

data class ChatMessage(
    val role: String, // "system", "user", "assistant"
    val content: String
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("role", role)
        put("content", content)
    }

    companion object {
        fun fromJson(json: JSONObject): ChatMessage {
            return ChatMessage(
                role = json.optString("role", "user"),
                content = json.optString("content", "")
            )
        }
    }
}

data class AiRequest(
    val messages: List<ChatMessage>,
    val modelId: String,
    val temperature: Float = 0.8f,
    val maxTokens: Int = 2048,
    val responseFormatJson: Boolean = true
) {
    fun toOpenAiJson(): JSONObject = JSONObject().apply {
        put("model", modelId)
        val arr = JSONArray()
        messages.forEach { arr.put(it.toJson()) }
        put("messages", arr)
        put("temperature", temperature.toDouble())
        put("max_tokens", maxTokens)
        if (responseFormatJson) {
            put("response_format", JSONObject().put("type", "json_object"))
        }
    }
}

data class AiResponse(
    val isSuccess: Boolean,
    val rawText: String? = null,
    val errorMessage: String? = null,
    val httpStatusCode: Int = 200,
    val latencyMs: Long = 0L,
    val promptTokens: Int = 0,
    val completionTokens: Int = 0
) {
    companion object {
        fun success(text: String, latencyMs: Long, pTokens: Int = 0, cTokens: Int = 0): AiResponse {
            return AiResponse(
                isSuccess = true,
                rawText = text,
                latencyMs = latencyMs,
                promptTokens = pTokens,
                completionTokens = cTokens
            )
        }

        fun error(message: String, statusCode: Int = 500, latencyMs: Long = 0L): AiResponse {
            return AiResponse(
                isSuccess = false,
                errorMessage = message,
                httpStatusCode = statusCode,
                latencyMs = latencyMs
            )
        }
    }
}
