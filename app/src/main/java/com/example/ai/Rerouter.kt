package com.example.ai

import com.example.data.model.*

data class ContextPackage(
    val gameMode: GameMode,
    val playerLocation: LocationInfo?,
    val nearbyNpcs: List<NpcState>,
    val companionNpc: NpcState?,
    val activeQuests: List<Quest>,
    val relevantMemories: List<MemoryEntry>,
    val relevantRumors: List<Rumor>,
    val recentEvents: List<WorldEvent>,
    val activeFactionStandings: Map<String, Int>
)

class Rerouter {

    fun buildContextPackage(
        playerInput: String,
        world: WorldState,
        mode: GameMode,
        companionNpcId: String? = null
    ): ContextPackage {
        val playerLocName = world.player.location
        val playerLocInfo = world.locations.find {
            it.id.equals(playerLocName, ignoreCase = true) || it.name.equals(playerLocName, ignoreCase = true)
        }

        // 1. Relevant NPCs (Nearby + Companion)
        val companion = if (companionNpcId != null) world.npcs.find { it.id == companionNpcId } else null
        val nearbyNpcs = world.npcs.filter { npc ->
            npc.id != companionNpcId && (
                npc.currentLocation.equals(playerLocName, ignoreCase = true) ||
                (playerLocInfo != null && npc.currentLocation.equals(playerLocInfo.id, ignoreCase = true))
            )
        }.take(3)

        // 2. Relevant Quests (Active quests matching location or nearby NPCs)
        val activeQuests = world.quests.filter { q ->
            q.status == QuestStatus.ACTIVE && (
                q.relatedLocationId == playerLocInfo?.id ||
                q.giverNpcId == companionNpcId ||
                nearbyNpcs.any { it.id == q.giverNpcId } ||
                true // Include top active quests
            )
        }.take(2)

        // 3. Top Memories (Bounded retrieval, sorted by importance)
        val relevantMemories = world.memories
            .sortedByDescending { it.importance }
            .take(5)

        // 4. Relevant Rumors (Known by nearby NPCs)
        val relevantRumors = world.rumors
            .filter { r -> nearbyNpcs.any { n -> r.knownByNpcIds.contains(n.id) } || r.isTrue }
            .take(3)

        // 5. Recent World Events
        val recentEvents = world.worldEvents.takeLast(2)

        return ContextPackage(
            gameMode = mode,
            playerLocation = playerLocInfo,
            nearbyNpcs = nearbyNpcs,
            companionNpc = companion,
            activeQuests = activeQuests,
            relevantMemories = relevantMemories,
            relevantRumors = relevantRumors,
            recentEvents = recentEvents,
            activeFactionStandings = world.player.factionReputations
        )
    }
}
