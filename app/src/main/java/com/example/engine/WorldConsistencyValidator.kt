package com.example.engine

import com.example.data.model.*

class WorldConsistencyValidator {

    fun validateProposal(proposal: StateProposal, world: WorldState): StateValidationResult {
        return when (proposal.type) {
            StateProposalType.DAMAGE_PLAYER -> {
                val amount = proposal.value?.toIntOrNull() ?: 0
                if (amount < 0) {
                    StateValidationResult(false, proposal, "Damage amount cannot be negative")
                } else {
                    val newHp = (world.player.hp - amount).coerceAtLeast(0)
                    StateValidationResult(
                        true,
                        proposal,
                        "Valid damage proposal",
                        "Player took $amount damage (HP: ${world.player.hp} -> $newHp)"
                    )
                }
            }

            StateProposalType.HEAL_PLAYER -> {
                val amount = proposal.value?.toIntOrNull() ?: 0
                if (amount < 0) {
                    StateValidationResult(false, proposal, "Heal amount cannot be negative")
                } else {
                    val newHp = (world.player.hp + amount).coerceAtMost(world.player.maxHp)
                    StateValidationResult(
                        true,
                        proposal,
                        "Valid heal proposal",
                        "Player healed $amount HP (HP: ${world.player.hp} -> $newHp)"
                    )
                }
            }

            StateProposalType.ADD_MONEY -> {
                val amount = proposal.value?.toIntOrNull() ?: 0
                if (amount < 0) {
                    StateValidationResult(false, proposal, "Money addition cannot be negative")
                } else {
                    val newMoney = world.player.money + amount
                    StateValidationResult(
                        true,
                        proposal,
                        "Valid gold gain",
                        "Gained $amount Gold (Total: $newMoney)"
                    )
                }
            }

            StateProposalType.SPEND_MONEY -> {
                val amount = proposal.value?.toIntOrNull() ?: 0
                if (amount < 0) {
                    StateValidationResult(false, proposal, "Spending amount cannot be negative")
                } else if (world.player.money < amount) {
                    StateValidationResult(
                        false,
                        proposal,
                        "Insufficient gold: Player has ${world.player.money}, tried to spend $amount"
                    )
                } else {
                    val newMoney = world.player.money - amount
                    StateValidationResult(
                        true,
                        proposal,
                        "Valid gold spend",
                        "Spent $amount Gold (Remaining: $newMoney)"
                    )
                }
            }

            StateProposalType.GAIN_ITEM -> {
                val itemName = proposal.target ?: proposal.value ?: "Item"
                val qty = proposal.extraData["quantity"]?.toIntOrNull() ?: 1
                if (qty <= 0) {
                    StateValidationResult(false, proposal, "Item quantity must be > 0")
                } else {
                    StateValidationResult(
                        true,
                        proposal,
                        "Valid item gain",
                        "Acquired: $itemName (x$qty)"
                    )
                }
            }

            StateProposalType.LOSE_ITEM -> {
                val itemName = proposal.target ?: proposal.value ?: ""
                val existing = world.player.inventory.find {
                    it.id.equals(itemName, ignoreCase = true) || it.name.equals(itemName, ignoreCase = true)
                }
                if (existing == null) {
                    StateValidationResult(false, proposal, "Player does not possess item '$itemName'")
                } else {
                    StateValidationResult(
                        true,
                        proposal,
                        "Valid item removal",
                        "Removed from inventory: ${existing.name}"
                    )
                }
            }

            StateProposalType.CHANGE_LOCATION -> {
                val locTarget = proposal.target ?: proposal.value ?: ""
                val validLoc = world.locations.find {
                    it.id.equals(locTarget, ignoreCase = true) || it.name.equals(locTarget, ignoreCase = true)
                }
                if (validLoc == null) {
                    StateValidationResult(
                        false,
                        proposal,
                        "Location '$locTarget' does not exist in authoritative world map"
                    )
                } else {
                    StateValidationResult(
                        true,
                        proposal,
                        "Valid location change",
                        "Moved to ${validLoc.name}"
                    )
                }
            }

            StateProposalType.ADVANCE_TIME -> {
                val minutes = proposal.value?.toIntOrNull() ?: 30
                if (minutes < 0 || minutes > 1440) {
                    StateValidationResult(false, proposal, "Time advancement must be between 0 and 1440 minutes")
                } else {
                    val newTime = world.time.advanceMinutes(minutes)
                    StateValidationResult(
                        true,
                        proposal,
                        "Valid time advance",
                        "Time advanced $minutes minutes (${newTime.toFormattedString()})"
                    )
                }
            }

            StateProposalType.UPDATE_RELATIONSHIP -> {
                val npcId = proposal.target ?: ""
                val npc = world.npcs.find { it.id.equals(npcId, ignoreCase = true) || it.name.equals(npcId, ignoreCase = true) }
                if (npc == null) {
                    StateValidationResult(false, proposal, "Target NPC '$npcId' not found in world")
                } else {
                    StateValidationResult(
                        true,
                        proposal,
                        "Valid relationship change",
                        "Relationship updated with ${npc.name}"
                    )
                }
            }

            StateProposalType.PROGRESS_QUEST -> {
                val questId = proposal.target ?: ""
                val quest = world.quests.find { it.id.equals(questId, ignoreCase = true) || it.title.equals(questId, ignoreCase = true) }
                if (quest == null) {
                    StateValidationResult(false, proposal, "Quest '$questId' not found")
                } else if (quest.status == QuestStatus.COMPLETED) {
                    StateValidationResult(false, proposal, "Quest is already completed")
                } else {
                    StateValidationResult(
                        true,
                        proposal,
                        "Valid quest progression",
                        "Quest updated: ${quest.title}"
                    )
                }
            }

            StateProposalType.FAIL_QUEST -> {
                val questId = proposal.target ?: ""
                val quest = world.quests.find { it.id.equals(questId, ignoreCase = true) || it.title.equals(questId, ignoreCase = true) }
                if (quest == null) {
                    StateValidationResult(false, proposal, "Quest '$questId' not found")
                } else {
                    StateValidationResult(true, proposal, "Quest failed", "Quest failed: ${quest.title}")
                }
            }

            StateProposalType.DISCOVER_SECRET -> {
                val secretId = proposal.target ?: ""
                val secret = world.secrets.find { it.id.equals(secretId, ignoreCase = true) || it.title.equals(secretId, ignoreCase = true) }
                if (secret == null) {
                    StateValidationResult(false, proposal, "Secret '$secretId' not found in world registry")
                } else {
                    StateValidationResult(true, proposal, "Secret discovered", "Secret discovered: ${secret.title}")
                }
            }

            StateProposalType.SPREAD_RUMOR -> {
                val text = proposal.value ?: proposal.target ?: ""
                if (text.isBlank()) {
                    StateValidationResult(false, proposal, "Rumor text cannot be blank")
                } else {
                    StateValidationResult(true, proposal, "Rumor spreading", "Rumor spread through region")
                }
            }

            StateProposalType.TRIGGER_CONSEQUENCE -> {
                val conId = proposal.target ?: ""
                val con = world.pendingConsequences.find { it.id.equals(conId, ignoreCase = true) }
                if (con == null) {
                    StateValidationResult(false, proposal, "Pending consequence '$conId' not found")
                } else {
                    StateValidationResult(true, proposal, "Consequence triggered", "Consequence triggered: ${con.title}")
                }
            }

            else -> StateValidationResult(true, proposal, "Generic proposal accepted", null)
        }
    }

    fun validateWorldStateIntegrity(world: WorldState): List<String> {
        val violations = mutableListOf<String>()

        // 1. HP bounds
        if (world.player.hp < 0 || world.player.hp > world.player.maxHp) {
            violations.add("Player HP (${world.player.hp}) exceeds bounds [0, ${world.player.maxHp}]")
        }

        // 2. Money bounds
        if (world.player.money < 0) {
            violations.add("Player money cannot be negative: ${world.player.money}")
        }

        // 3. NPC Uniqueness
        val npcIds = world.npcs.map { it.id }
        if (npcIds.size != npcIds.distinct().size) {
            violations.add("Duplicate NPC IDs detected in world state")
        }

        // 4. Time validity
        if (world.time.hour !in 0..23 || world.time.minute !in 0..59 || world.time.day < 1) {
            violations.add("World time is invalid: ${world.time.toFormattedString()}")
        }

        // 5. Locations validity
        val locIds = world.locations.map { it.id }.toSet()
        world.npcs.forEach { npc ->
            val matches = locIds.contains(npc.currentLocation) || world.locations.any { it.name.equals(npc.currentLocation, ignoreCase = true) }
            if (!matches) {
                violations.add("NPC ${npc.name} is in unknown location '${npc.currentLocation}'")
            }
        }

        return violations
    }
}
