package com.example

import com.example.data.model.*
import com.example.engine.PresetWorlds
import com.example.engine.WorldConsistencyValidator
import org.junit.Assert.*
import org.junit.Test

class WorldConsistencyValidatorTest {

    private val validator = WorldConsistencyValidator()
    private val world = PresetWorlds.createEldoria()

    @Test
    fun `damage proposal validates correctly within bounds`() {
        val proposal = StateProposal(type = StateProposalType.DAMAGE_PLAYER, value = "25")
        val result = validator.validateProposal(proposal, world)
        assertTrue(result.isValid)
    }

    @Test
    fun `spend money rejects if player has insufficient funds`() {
        // Player has 75 gold in Eldoria starter
        val validProposal = StateProposal(type = StateProposalType.SPEND_MONEY, value = "50")
        val validResult = validator.validateProposal(validProposal, world)
        assertTrue(validResult.isValid)

        val invalidProposal = StateProposal(type = StateProposalType.SPEND_MONEY, value = "999")
        val invalidResult = validator.validateProposal(invalidProposal, world)
        assertFalse(invalidResult.isValid)
        assertTrue(invalidResult.reason.contains("Insufficient gold"))
    }

    @Test
    fun `change location rejects if target location is unknown`() {
        val validProposal = StateProposal(type = StateProposalType.CHANGE_LOCATION, target = "loc_silverwood")
        val validResult = validator.validateProposal(validProposal, world)
        assertTrue(validResult.isValid)

        val invalidProposal = StateProposal(type = StateProposalType.CHANGE_LOCATION, target = "Atlantis Undersea Palace")
        val invalidResult = validator.validateProposal(invalidProposal, world)
        assertFalse(invalidResult.isValid)
        assertTrue(invalidResult.reason.contains("does not exist"))
    }
}
