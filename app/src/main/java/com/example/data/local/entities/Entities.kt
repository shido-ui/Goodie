package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    indices = [Index(value = ["updatedAt"])]
)
data class SessionEntity(
    @PrimaryKey val id: String,
    val mode: String, // OPEN_WORLD, CHARACTER_CHAT, OPEN_WORLD_AND_CHARACTER
    val title: String,
    val worldId: String,
    val worldName: String,
    val characterId: String? = null,
    val characterName: String? = null,
    val modelId: String,
    val providerId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "world_states")
data class WorldStateEntity(
    @PrimaryKey val sessionId: String,
    val worldId: String,
    val schemaVersion: Int,
    val stateJson: String, // Full JSON serialization of WorldState
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chat_messages",
    indices = [Index(value = ["sessionId", "timestamp"])]
)
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val sender: String, // "PLAYER", "NARRATOR", "NPC"
    val speakerName: String? = null,
    val content: String,
    val dialogue: String? = null,
    val appliedProposalsJson: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val worldDay: Int = 1,
    val worldTimeFormatted: String = "08:00"
)

@Entity(
    tableName = "simulation_logs",
    indices = [Index(value = ["sessionId", "timestamp"])]
)
data class SimulationLogDbEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val eventType: String,
    val description: String,
    val affectedEntitiesJson: String,
    val worldMinutes: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "lore_entries")
data class LoreDbEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val category: String,
    val title: String,
    val content: String,
    val tagsJson: String,
    val isUnlocked: Boolean = true
)
