package com.example.engine

import com.example.data.model.*

class LevelOfDetailManager {

    fun updateLodLevels(world: WorldState, companionNpcId: String? = null): List<NpcState> {
        val playerLoc = world.player.location
        val playerLocInfo = world.locations.find {
            it.id.equals(playerLoc, ignoreCase = true) || it.name.equals(playerLoc, ignoreCase = true)
        }
        val connectedLocIds = playerLocInfo?.connectedLocationIds?.toSet() ?: emptySet()

        return world.npcs.map { npc ->
            val isCompanion = npc.id == companionNpcId
            val isSameLocation = npc.currentLocation.equals(playerLoc, ignoreCase = true) ||
                    (playerLocInfo != null && npc.currentLocation.equals(playerLocInfo.id, ignoreCase = true))

            val isAdjacent = connectedLocIds.contains(npc.currentLocation) ||
                    world.locations.any { it.name.equals(npc.currentLocation, ignoreCase = true) && connectedLocIds.contains(it.id) }

            val newLod = when {
                isCompanion || isSameLocation -> SimulationLod.HIGH
                isAdjacent -> SimulationLod.MEDIUM
                else -> SimulationLod.LOW
            }

            npc.copy(simulationLod = newLod)
        }
    }
}
