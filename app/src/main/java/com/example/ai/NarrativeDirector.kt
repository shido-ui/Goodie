package com.example.ai

import com.example.data.model.*

class NarrativeDirector(
    private val rerouter: Rerouter = Rerouter()
) {

    fun buildAiRequest(
        playerInput: String,
        world: WorldState,
        mode: GameMode,
        modelId: String,
        recentChatHistory: List<ChatMessage>,
        companionNpcId: String? = null
    ): AiRequest {
        val contextPackage = rerouter.buildContextPackage(playerInput, world, mode, companionNpcId)
        val systemPrompt = buildSystemPrompt(world, contextPackage)
        val userPrompt = buildTurnUserPrompt(playerInput, world, contextPackage)

        val messages = mutableListOf<ChatMessage>()
        messages.add(ChatMessage("system", systemPrompt))

        // Add recent conversation history (bounded to last 6 messages)
        val historyToInclude = recentChatHistory.takeLast(6)
        messages.addAll(historyToInclude)

        messages.add(ChatMessage("user", userPrompt))

        return AiRequest(
            messages = messages,
            modelId = modelId,
            temperature = 0.8f,
            maxTokens = 2048,
            responseFormatJson = true
        )
    }

    private fun buildSystemPrompt(world: WorldState, ctx: ContextPackage): String {
        return """
You are the Game Master and Storyteller for RPG AI Hub, an immersive persistent roleplaying engine.
World: ${world.worldName}
Description: ${world.worldDescription}

CORE ENGINE CONTRACT:
1. The AI PROPOSES state mutations; the Authoritative Game Engine DECIDES and VALIDATES them.
2. NEVER assume state changes took effect unless you propose them in the "proposals" JSON array.
3. You cannot arbitrarily alter gold, HP, teleport, or complete quests without proposing them.
4. If an action fails or is impossible, narrate the struggle or failure realistically.
5. Provide rich, atmospheric sensory details, character dialogue, and dramatic immersion.

CURRENT MODE: ${ctx.gameMode.displayName}

You MUST respond strictly in valid JSON matching this schema:
{
  "narrative": "Vivid atmospheric narration of what happens in the third person or second person perspective.",
  "dialogue": "Spoken words of the interacting character/NPC, or null if nobody spoke.",
  "speakerNpcId": "ID or Name of the NPC speaking, or null",
  "suggestedActions": ["Action 1", "Action 2", "Action 3"],
  "proposals": [
    {
      "type": "DAMAGE_PLAYER | HEAL_PLAYER | ADD_MONEY | SPEND_MONEY | GAIN_ITEM | LOSE_ITEM | CHANGE_LOCATION | ADVANCE_TIME | UPDATE_RELATIONSHIP | PROGRESS_QUEST | FAIL_QUEST | DISCOVER_SECRET | SPREAD_RUMOR",
      "target": "target identifier or name",
      "value": "numeric amount or value string",
      "extraData": { "optionalKey": "optionalValue" }
    }
  ]
}
""".trimIndent()
    }

    private fun buildTurnUserPrompt(
        playerInput: String,
        world: WorldState,
        ctx: ContextPackage
    ): String {
        val player = world.player
        val invSummary = player.inventory.joinToString(", ") { "${it.name} (x${it.quantity})" }.ifBlank { "Empty" }
        val npcsSummary = ctx.nearbyNpcs.joinToString("; ") { "${it.name} (${it.title}): ${it.currentActivity}, Trust: ${it.relationships.trust}/100" }.ifBlank { "None nearby" }
        val questsSummary = ctx.activeQuests.joinToString("; ") { q ->
            "${q.title} [Objectives: " + q.objectives.joinToString(", ") { "${it.description} (${if (it.isCompleted) "Done" else "Pending"})" } + "]"
        }.ifBlank { "None active" }

        val companionText = if (ctx.companionNpc != null) {
            "COMPANION: ${ctx.companionNpc.name} (${ctx.companionNpc.title}) - Trust: ${ctx.companionNpc.relationships.trust}/100, Suspicion: ${ctx.companionNpc.relationships.suspicion}/100, Personality: ${ctx.companionNpc.personality}"
        } else ""

        return """
[AUTHORITATIVE WORLD STATUS]
Time: ${world.time.toFormattedString()}
Player: ${player.name} (${player.title}) | HP: ${player.hp}/${player.maxHp} | Gold: ${player.money}
Location: ${player.location} (${ctx.playerLocation?.region ?: "Unknown Region"})
Inventory: $invSummary
$companionText
Nearby NPCs: $npcsSummary
Active Quests: $questsSummary

[PLAYER ACTION]
$playerInput
""".trimIndent()
    }
}
