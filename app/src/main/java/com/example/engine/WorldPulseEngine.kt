package com.example.engine

import com.example.data.model.*

data class PulseResult(
    val updatedWorldState: WorldState,
    val minutesAdvanced: Int,
    val npcDecisions: List<NpcDecision>,
    val firedConsequences: List<PendingConsequence>,
    val generatedLogs: List<SimulationLogEntry>
)

class WorldPulseEngine(
    private val npcDecisionEngine: NpcDecisionEngine = NpcDecisionEngine(),
    private val lodManager: LevelOfDetailManager = LevelOfDetailManager()
) {

    fun executePulse(
        world: WorldState,
        minutesToAdvance: Int = 30,
        companionNpcId: String? = null
    ): PulseResult {
        val newTime = world.time.advanceMinutes(minutesToAdvance)
        val currentMinutes = newTime.toTotalMinutes()
        val logs = mutableListOf<SimulationLogEntry>()
        val decisions = mutableListOf<NpcDecision>()

        // 1. Update LOD
        val npcsWithLod = lodManager.updateLodLevels(world.copy(time = newTime), companionNpcId)

        // 2. Simulate NPCs based on LOD
        val updatedNpcs = npcsWithLod.map { npc ->
            if (!npc.isAlive) return@map npc

            when (npc.simulationLod) {
                SimulationLod.HIGH, SimulationLod.MEDIUM -> {
                    val decision = npcDecisionEngine.decideNextAction(npc, world, newTime.hour)
                    decisions.add(decision)

                    var newLocation = npc.currentLocation
                    var newActivity = npc.currentActivity

                    if (decision.actionType == NpcActionType.FOLLOW_SCHEDULE && decision.targetLocationId != null) {
                        val locObj = world.locations.find { it.id == decision.targetLocationId }
                        newLocation = locObj?.name ?: decision.targetLocationId
                        newActivity = decision.rationale
                    } else if (decision.actionType == NpcActionType.INVESTIGATE) {
                        newActivity = "Investigating: ${decision.rationale}"
                    } else if (decision.actionType == NpcActionType.REPORT_INFORMATION) {
                        newActivity = "Reporting suspicious activity"
                    }

                    if (newLocation != npc.currentLocation) {
                        logs.add(
                            SimulationLogEntry(
                                id = "log_move_${npc.id}_$currentMinutes",
                                timestampWorldMinutes = currentMinutes,
                                eventType = "NPC_MOVE",
                                description = "${npc.name} moved from ${npc.currentLocation} to $newLocation (${decision.rationale})",
                                affectedEntities = listOf(npc.id)
                            )
                        )
                    }

                    npc.copy(currentLocation = newLocation, currentActivity = newActivity)
                }
                SimulationLod.LOW -> {
                    // Low LOD: only simple activity updates if hour changed
                    npc
                }
            }
        }

        // 3. Process Pending Consequences
        val firedConsequences = mutableListOf<PendingConsequence>()
        val updatedConsequences = world.pendingConsequences.map { con ->
            if (!con.isProcessed && currentMinutes >= con.deadlineWorldMinutes) {
                firedConsequences.add(con)
                logs.add(
                    SimulationLogEntry(
                        id = "log_con_${con.id}_$currentMinutes",
                        timestampWorldMinutes = currentMinutes,
                        eventType = "CONSEQUENCE_FIRED",
                        description = "World consequence triggered: ${con.title}. ${con.description}",
                        affectedEntities = con.affectedNpcIds
                    )
                )
                con.copy(isProcessed = true)
            } else {
                con
            }
        }

        // 4. Spread rumors (deterministic spread count increase)
        val updatedRumors = world.rumors.map { rumor ->
            if (rumor.spreadCount < 10) {
                rumor.copy(spreadCount = rumor.spreadCount + 1)
            } else {
                rumor
            }
        }

        val updatedWorld = world.copy(
            time = newTime,
            npcs = updatedNpcs,
            pendingConsequences = updatedConsequences,
            rumors = updatedRumors,
            simulationLogs = world.simulationLogs + logs
        )

        return PulseResult(
            updatedWorldState = updatedWorld,
            minutesAdvanced = minutesToAdvance,
            npcDecisions = decisions,
            firedConsequences = firedConsequences,
            generatedLogs = logs
        )
    }
}
