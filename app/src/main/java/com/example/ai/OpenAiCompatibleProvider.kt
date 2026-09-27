package com.example.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class OpenAiCompatibleProvider(
    override val providerId: String,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
) : AiProvider {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun executeChat(
        request: AiRequest,
        config: ProviderConfig,
        apiKey: String?
    ): AiResponse = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        if (apiKey.isNullOrBlank() && !config.endpointUrl.contains("localhost") && !config.endpointUrl.contains("10.0.2.2")) {
            return@withContext AiResponse.error(
                "API Key is missing for provider '${config.providerId}'. Please enter a valid API key in AI Settings.",
                statusCode = 401
            )
        }

        val requestJson = request.toOpenAiJson().toString()
        val requestBody = requestJson.toRequestBody(jsonMediaType)

        val requestBuilder = Request.Builder()
            .url(config.endpointUrl)
            .post(requestBody)

        if (!apiKey.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $apiKey")
        }
        requestBuilder.header("Content-Type", "application/json")

        config.customHeaders.forEach { (key, value) ->
            requestBuilder.header(key, value)
        }

        try {
            val response = client.newCall(requestBuilder.build()).execute()
            val latency = System.currentTimeMillis() - startTime
            val responseBody = response.body?.string() ?: ""
            val statusCode = response.code

            if (!response.isSuccessful) {
                val errorDetails = parseErrorBody(responseBody, statusCode)
                return@withContext AiResponse.error(
                    errorDetails,
                    statusCode = statusCode,
                    latencyMs = latency
                )
            }

            // Parse response
            val root = JSONObject(responseBody)
            val choices = root.optJSONArray("choices")
            if (choices == null || choices.length() == 0) {
                return@withContext AiResponse.error(
                    "Provider returned 200 OK but no choices were present in the response.",
                    statusCode = 200,
                    latencyMs = latency
                )
            }

            val firstChoice = choices.getJSONObject(0)
            val messageObj = firstChoice.optJSONObject("message")
            val content = messageObj?.optString("content") ?: ""

            val usageObj = root.optJSONObject("usage")
            val promptTokens = usageObj?.optInt("prompt_tokens", 0) ?: 0
            val completionTokens = usageObj?.optInt("completion_tokens", 0) ?: 0

            return@withContext AiResponse.success(
                text = content,
                latencyMs = latency,
                pTokens = promptTokens,
                cTokens = completionTokens
            )
        } catch (e: UnknownHostException) {
            AiResponse.error("Network unavailable: Unable to resolve host. Check your internet connection.", statusCode = 0)
        } catch (e: SocketTimeoutException) {
            AiResponse.error("Request timed out after waiting for response from the AI provider.", statusCode = 408)
        } catch (e: IOException) {
            AiResponse.error("Connection error: ${e.message ?: "Failed to connect to AI server"}", statusCode = 0)
        } catch (e: Exception) {
            AiResponse.error("Unexpected error during AI generation: ${e.localizedMessage ?: e.javaClass.simpleName}", statusCode = 500)
        }
    }

    override suspend fun testConnection(config: ProviderConfig, apiKey: String?): AiResponse {
        val testRequest = AiRequest(
            messages = listOf(
                ChatMessage("system", "You are an AI assistant. Return JSON: {\"status\": \"ok\", \"message\": \"Connection successful\"}"),
                ChatMessage("user", "Ping")
            ),
            modelId = config.modelId,
            temperature = 0.2f,
            maxTokens = 50,
            responseFormatJson = false
        )
        return executeChat(testRequest, config, apiKey)
    }

    private fun parseErrorBody(errorBody: String, statusCode: Int): String {
        if (errorBody.isBlank()) {
            return when (statusCode) {
                401 -> "HTTP 401 Unauthorized: Invalid or missing API key."
                403 -> "HTTP 403 Forbidden: API key does not have permission for this model."
                404 -> "HTTP 404 Not Found: Model or endpoint URL not found."
                429 -> "HTTP 429 Rate Limit / Quota Exceeded: Your API plan has exceeded requests or quota."
                500, 502, 503 -> "HTTP $statusCode: Provider server encountered an internal error."
                else -> "HTTP $statusCode: Request failed."
            }
        }

        try {
            val json = JSONObject(errorBody)
            val errorObj = json.optJSONObject("error")
            if (errorObj != null) {
                val message = errorObj.optString("message", "")
                val type = errorObj.optString("type", "")
                val code = errorObj.optString("code", "")
                val prefix = if (type.isNotBlank()) "[$type] " else ""
                val suffix = if (code.isNotBlank()) " (Code: $code)" else ""
                return "HTTP $statusCode: $prefix$message$suffix"
            }
        } catch (e: Exception) {
            // Not JSON
        }

        val truncated = if (errorBody.length > 200) errorBody.take(200) + "..." else errorBody
        return "HTTP $statusCode: $truncated"
    }
}
