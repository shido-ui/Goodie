package com.example

import com.example.ai.StateProposalExtractor
import com.example.data.model.StateProposalType
import org.junit.Assert.*
import org.junit.Test

class StateProposalExtractorTest {

    private val extractor = StateProposalExtractor()

    @Test
    fun `extracts structured JSON with markdown code fences successfully`() {
        val jsonPayload = """
        ```json
        {
          "narrative": "The tavern fire crackles warmly.",
          "dialogue": "Welcome back traveler.",
          "speakerNpcId": "npc_elena",
          "suggestedActions": ["Order an ale", "Ask for rumors"],
          "proposals": [
            {
              "type": "ADD_MONEY",
              "value": "20"
            }
          ]
        }
        ```
        """.trimIndent()

        val result = extractor.extractStructuredResponse(jsonPayload)
        assertEquals("The tavern fire crackles warmly.", result.narrative)
        assertEquals("Welcome back traveler.", result.dialogue)
        assertEquals("npc_elena", result.speakerNpcId)
        assertEquals(2, result.suggestedActions.size)
        assertEquals(1, result.proposals.size)
        assertEquals(StateProposalType.ADD_MONEY, result.proposals[0].type)
        assertEquals("20", result.proposals[0].value)
    }

    @Test
    fun `fallback to plain narrative when response is plain text without crashing`() {
        val rawText = "You search the dusty shelves and discover an old brass key."
        val result = extractor.extractStructuredResponse(rawText)

        assertEquals("You search the dusty shelves and discover an old brass key.", result.narrative)
        assertNull(result.dialogue)
        assertTrue(result.proposals.isEmpty())
    }
}
