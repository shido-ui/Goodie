package com.example.ai

import com.example.data.model.AiStructuredResponse
import com.example.data.model.StateProposal
import org.json.JSONObject

class StateProposalExtractor {

    fun extractStructuredResponse(rawText: String): AiStructuredResponse {
        val cleanText = stripMarkdownFences(rawText.trim())

        return try {
            val json = JSONObject(cleanText)
            AiStructuredResponse.fromJson(json).copy(rawJson = rawText)
        } catch (e: Exception) {
            // Try finding first '{' and last '}'
            val firstBrace = rawText.indexOf('{')
            val lastBrace = rawText.lastIndexOf('}')
            if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
                try {
                    val substring = rawText.substring(firstBrace, lastBrace + 1)
                    val json = JSONObject(substring)
                    return AiStructuredResponse.fromJson(json).copy(rawJson = rawText)
                } catch (ex: Exception) {
                    // Fall through to plain narrative fallback
                }
            }

            // Fallback: Use entire rawText as narrative, no mutations
            AiStructuredResponse(
                narrative = rawText,
                dialogue = null,
                suggestedActions = listOf("Look around", "Check inventory", "Speak with someone nearby"),
                proposals = emptyList(),
                rawJson = rawText
            )
        }
    }

    private fun stripMarkdownFences(text: String): String {
        var result = text
        if (result.startsWith("```json")) {
            result = result.removePrefix("```json").trim()
        } else if (result.startsWith("```")) {
            result = result.removePrefix("```").trim()
        }

        if (result.endsWith("```")) {
            result = result.removeSuffix("```").trim()
        }
        return result
    }
}
