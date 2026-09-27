package com.example.engine

import com.example.data.model.*

class WorldStateEngine(
    val validator: WorldConsistencyValidator = WorldConsistencyValidator(),
    val pulseEngine: WorldPulseEngine = WorldPulseEngine()
) {

    fun applyValidatedProposal(proposal: StateProposal, world: WorldState): Pair<WorldState, String> {
        val validation = validator.validateProposal(proposal, world)
        if (!validation.isValid) {
            return Pair(world, "REJECTED: ${validation.reason}")
        }

        var currentWorld = world
        val summary = validation.appliedMutationSummary ?: "Proposal applied"

        when (proposal.type) {
            StateProposalType.DAMAGE_PLAYER -> {
                val amount = proposal.value?.toIntOrNull() ?: 0
                val newHp = (world.player.hp - amount).coerceAtLeast(0)
                currentWorld = world.copy(
                    player = world.player.copy(hp = newHp)
                )
            }

            StateProposalType.HEAL_PLAYER -> {
                val amount = proposal.value?.toIntOrNull() ?: 0
                val newHp = (world.player.hp + amount).coerceAtMost(world.player.maxHp)
                currentWorld = world.copy(
                    player = world.player.copy(hp = newHp)
                )
            }

            StateProposalType.ADD_MONEY -> {
                val amount = proposal.value?.toIntOrNull() ?: 0
                currentWorld = world.copy(
                    player = world.player.copy(money = world.player.money + amount)
                )
            }

            StateProposalType.SPEND_MONEY -> {
                val amount = proposal.value?.toIntOrNull() ?: 0
                currentWorld = world.copy(
                    player = world.player.copy(money = (world.player.money - amount).coerceAtLeast(0))
                )
            }

            StateProposalType.GAIN_ITEM -> {
                val itemName = proposal.target ?: proposal.value ?: "Item"
                val qty = proposal.extraData["quantity"]?.toIntOrNull() ?: 1
                val itemType = proposal.extraData["itemType"] ?: "MISC"
                val value = proposal.extraData["value"]?.toIntOrNull() ?: 10

                val existingIndex = world.player.inventory.indexOfFirst { it.name.equals(itemName, ignoreCase = true) }
                val updatedInv = world.player.inventory.toMutableList()
                if (existingIndex >= 0) {
                    val existing = updatedInv[existingIndex]
                    updatedInv[existingIndex] = existing.copy(quantity = existing.quantity + qty)
                } else {
                    updatedInv.add(
                        InventoryItem(
                            id = "item_${System.currentTimeMillis()}_${(100..999).random()}",
                            name = itemName,
                            description = proposal.extraData["description"] ?: "Acquired during journey.",
                            quantity = qty,
                            value = value,
                            itemType = itemType
                        )
                    )
                }
                currentWorld = world.copy(player = world.player.copy(inventory = updatedInv))
            }

            StateProposalType.LOSE_ITEM -> {
                val itemName = proposal.target ?: proposal.value ?: ""
                val qty = proposal.extraData["quantity"]?.toIntOrNull() ?: 1
                val updatedInv = world.player.inventory.mapNotNull { item ->
                    if (item.id.equals(itemName, ignoreCase = true) || item.name.equals(itemName, ignoreCase = true)) {
                        val remaining = item.quantity - qty
                        if (remaining > 0) item.copy(quantity = remaining) else null
                    } else {
                        item
                    }
                }
                currentWorld = world.copy(player = world.player.copy(inventory = updatedInv))
            }

            StateProposalType.CHANGE_LOCATION -> {
                val locTarget = proposal.target ?: proposal.value ?: ""
                val validLoc = world.locations.find {
                    it.id.equals(locTarget, ignoreCase = true) || it.name.equals(locTarget, ignoreCase = true)
                }
                if (validLoc != null) {
                    currentWorld = world.copy(
                        player = world.player.copy(location = validLoc.name)
                    )
                }
            }

            StateProposalType.ADVANCE_TIME -> {
                val minutes = proposal.value?.toIntOrNull() ?: 30
                val pulse = pulseEngine.executePulse(currentWorld, minutes)
                currentWorld = pulse.updatedWorldState
            }

            StateProposalType.UPDATE_RELATIONSHIP -> {
                val npcId = proposal.target ?: ""
                val trustDelta = proposal.extraData["trustDelta"]?.toIntOrNull() ?: 0
                val suspicionDelta = proposal.extraData["suspicionDelta"]?.toIntOrNull() ?: 0
                val friendshipDelta = proposal.extraData["friendshipDelta"]?.toIntOrNull() ?: 0

                val updatedNpcs = world.npcs.map { npc ->
                    if (npc.id.equals(npcId, ignoreCase = true) || npc.name.equals(npcId, ignoreCase = true)) {
                        val rel = npc.relationships
                        val newRel = rel.copy(
                            trust = (rel.trust + trustDelta).coerceIn(0, 100),
                            suspicion = (rel.suspicion + suspicionDelta).coerceIn(0, 100),
                            friendship = (rel.friendship + friendshipDelta).coerceIn(-100, 100)
                        )
                        npc.copy(relationships = newRel)
                    } else {
                        npc
                    }
                }
                currentWorld = world.copy(npcs = updatedNpcs)
            }

            StateProposalType.PROGRESS_QUEST -> {
                val questId = proposal.target ?: ""
                val objectiveIndex = proposal.extraData["objectiveIndex"]?.toIntOrNull() ?: 0
                val updatedQuests = world.quests.map { q ->
                    if (q.id.equals(questId, ignoreCase = true) || q.title.equals(questId, ignoreCase = true)) {
                        val updatedObjectives = q.objectives.mapIndexed { idx, obj ->
                            if (idx == objectiveIndex) obj.copy(isCompleted = true, currentProgress = obj.targetProgress) else obj
                        }
                        val allDone = updatedObjectives.isNotEmpty() && updatedObjectives.all { it.isCompleted }
                        q.copy(
                            objectives = updatedObjectives,
                            status = if (allDone) QuestStatus.COMPLETED else QuestStatus.ACTIVE
                        )
                    } else {
                        q
                    }
                }
                currentWorld = world.copy(quests = updatedQuests)
            }

            StateProposalType.FAIL_QUEST -> {
                val questId = proposal.target ?: ""
                val updatedQuests = world.quests.map { q ->
                    if (q.id.equals(questId, ignoreCase = true) || q.title.equals(questId, ignoreCase = true)) {
                        q.copy(status = QuestStatus.FAILED)
                    } else {
                        q
                    }
                }
                currentWorld = world.copy(quests = updatedQuests)
            }

            StateProposalType.DISCOVER_SECRET -> {
                val secretId = proposal.target ?: ""
                val updatedSecrets = world.secrets.map { sec ->
                    if (sec.id.equals(secretId, ignoreCase = true) || sec.title.equals(secretId, ignoreCase = true)) {
                        sec.copy(isDiscoveredByPlayer = true)
                    } else {
                        sec
                    }
                }
                currentWorld = world.copy(secrets = updatedSecrets)
            }

            StateProposalType.SPREAD_RUMOR -> {
                val text = proposal.value ?: proposal.target ?: ""
                val newRumor = Rumor(
                    id = "rumor_${System.currentTimeMillis()}",
                    text = text,
                    timestampMinutes = world.time.toTotalMinutes()
                )
                currentWorld = world.copy(rumors = world.rumors + newRumor)
            }

            StateProposalType.TRIGGER_CONSEQUENCE -> {
                val conId = proposal.target ?: ""
                val updatedConsequences = world.pendingConsequences.map { con ->
                    if (con.id.equals(conId, ignoreCase = true)) {
                        con.copy(isProcessed = true)
                    } else {
                        con
                    }
                }
                currentWorld = world.copy(pendingConsequences = updatedConsequences)
            }

            else -> {}
        }

        return Pair(currentWorld, summary)
    }
}
