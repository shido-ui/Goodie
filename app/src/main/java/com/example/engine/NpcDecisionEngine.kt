package com.example.engine

import com.example.data.model.*

class NpcDecisionEngine {

    fun decideNextAction(npc: NpcState, world: WorldState, currentHour: Int): NpcDecision {
        if (!npc.isAlive) {
            return NpcDecision(
                npcId = npc.id,
                actionType = NpcActionType.REST,
                rationale = "NPC is deceased",
                timestampMinutes = world.time.toTotalMinutes()
            )
        }

        // 1. High Suspicion reaction (>70)
        if (npc.relationships.suspicion >= 70) {
            val guardFaction = world.factions.find { it.influenceScore >= 70 }
            return NpcDecision(
                npcId = npc.id,
                actionType = NpcActionType.REPORT_INFORMATION,
                targetEntityId = guardFaction?.id,
                rationale = "High suspicion (${npc.relationships.suspicion}/100): Alerting authorities or gathering allies",
                timestampMinutes = world.time.toTotalMinutes()
            )
        }

        // 2. High Hostility (>75)
        if (npc.relationships.hostility >= 75) {
            return NpcDecision(
                npcId = npc.id,
                actionType = NpcActionType.FLEE,
                rationale = "Extreme hostility: Withdrawing to safe territory or preparing defenses",
                timestampMinutes = world.time.toTotalMinutes()
            )
        }

        // 3. Active Goal processing
        val activeGoal = npc.goals.firstOrNull { !it.isCompleted }
        if (activeGoal != null && activeGoal.priority <= 1) {
            return NpcDecision(
                npcId = npc.id,
                actionType = NpcActionType.INVESTIGATE,
                rationale = "Pursuing top priority goal: ${activeGoal.description}",
                timestampMinutes = world.time.toTotalMinutes()
            )
        }

        // 4. Schedule Routine
        val scheduledItem = npc.schedule.find { currentHour in it.startHour..it.endHour }
        if (scheduledItem != null) {
            return NpcDecision(
                npcId = npc.id,
                actionType = NpcActionType.FOLLOW_SCHEDULE,
                targetLocationId = scheduledItem.locationId,
                rationale = "Following daily schedule: ${scheduledItem.activityDescription}",
                timestampMinutes = world.time.toTotalMinutes()
            )
        }

        // 5. Default Idle / Rest
        return NpcDecision(
            npcId = npc.id,
            actionType = NpcActionType.WAIT,
            rationale = "Resting or minding personal business",
            timestampMinutes = world.time.toTotalMinutes()
        )
    }
}
