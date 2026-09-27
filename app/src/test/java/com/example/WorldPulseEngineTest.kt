package com.example

import com.example.data.model.*
import com.example.engine.PresetWorlds
import com.example.engine.WorldPulseEngine
import org.junit.Assert.*
import org.junit.Test

class WorldPulseEngineTest {

    private val pulseEngine = WorldPulseEngine()

    @Test
    fun `time advances correctly on pulse`() {
        val world = PresetWorlds.createEldoria().copy(time = WorldTime(day = 1, hour = 8, minute = 0))
        val result = pulseEngine.executePulse(world, minutesToAdvance = 45)

        assertEquals(1, result.updatedWorldState.time.day)
        assertEquals(8, result.updatedWorldState.time.hour)
        assertEquals(45, result.updatedWorldState.time.minute)
    }

    @Test
    fun `day rollover works properly after 24 hours`() {
        val world = PresetWorlds.createEldoria().copy(time = WorldTime(day = 1, hour = 23, minute = 30))
        val result = pulseEngine.executePulse(world, minutesToAdvance = 60)

        assertEquals(2, result.updatedWorldState.time.day)
        assertEquals(0, result.updatedWorldState.time.hour)
        assertEquals(30, result.updatedWorldState.time.minute)
    }

    @Test
    fun `pending consequence triggers when deadline is reached`() {
        val world = PresetWorlds.createEldoria().copy(
            time = WorldTime(day = 1, hour = 8, minute = 0),
            pendingConsequences = listOf(
                PendingConsequence(
                    id = "con_bandit_attack",
                    title = "Bandit Ambush",
                    description = "Bandits attack the merchant caravan.",
                    deadlineWorldMinutes = 500, // 08:20
                    triggerCondition = "TIME_ELAPSED",
                    isProcessed = false
                )
            )
        )

        // Current minutes at 08:00 is 8 * 60 = 480.
        // Advance 30 minutes -> 510 minutes.
        val result = pulseEngine.executePulse(world, minutesToAdvance = 30)

        assertEquals(1, result.firedConsequences.size)
        assertEquals("con_bandit_attack", result.firedConsequences[0].id)
        assertTrue(result.updatedWorldState.pendingConsequences[0].isProcessed)
    }
}
