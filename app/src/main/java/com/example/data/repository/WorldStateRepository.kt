package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.local.entities.SimulationLogDbEntity
import com.example.data.local.entities.WorldStateEntity
import com.example.data.model.SimulationLogEntry
import com.example.data.model.WorldState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray

class WorldStateRepository(
    private val database: AppDatabase
) {
    private val worldStateDao = database.worldStateDao()
    private val sessionDao = database.sessionDao()
    private val chatMessageDao = database.chatMessageDao()
    private val simulationLogDao = database.simulationLogDao()

    suspend fun getWorldState(sessionId: String): WorldState? = withContext(Dispatchers.IO) {
        val entity = worldStateDao.getWorldState(sessionId) ?: return@withContext null
        try {
            WorldState.fromJsonString(entity.stateJson)
        } catch (e: Exception) {
            null
        }
    }

    fun getWorldStateFlow(sessionId: String): Flow<WorldState?> {
        return worldStateDao.getWorldStateFlow(sessionId).map { entity ->
            entity?.let {
                try {
                    WorldState.fromJsonString(it.stateJson)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    suspend fun saveTurnAtomic(
        sessionId: String,
        worldState: WorldState,
        newMessages: List<ChatMessageEntity>,
        newLogs: List<SimulationLogEntry>
    ) = withContext(Dispatchers.IO) {
        database.withTransaction {
            // 1. Update WorldState JSON
            worldStateDao.insertOrUpdateWorldState(
                WorldStateEntity(
                    sessionId = sessionId,
                    worldId = worldState.worldId,
                    schemaVersion = worldState.schemaVersion,
                    stateJson = worldState.toJsonString(),
                    updatedAt = System.currentTimeMillis()
                )
            )

            // 2. Insert chat messages
            if (newMessages.isNotEmpty()) {
                chatMessageDao.insertMessages(newMessages)
            }

            // 3. Insert simulation logs
            if (newLogs.isNotEmpty()) {
                val dbLogs = newLogs.map { log ->
                    val arr = JSONArray()
                    log.affectedEntities.forEach { arr.put(it) }
                    SimulationLogDbEntity(
                        id = log.id,
                        sessionId = sessionId,
                        eventType = log.eventType,
                        description = log.description,
                        affectedEntitiesJson = arr.toString(),
                        worldMinutes = log.timestampWorldMinutes,
                        timestamp = System.currentTimeMillis()
                    )
                }
                simulationLogDao.insertLogs(dbLogs)
            }

            // 4. Update session timestamp
            val session = sessionDao.getSessionById(sessionId)
            if (session != null) {
                sessionDao.updateSession(session.copy(updatedAt = System.currentTimeMillis()))
            }
        }
    }

    suspend fun updateWorldStateOnly(sessionId: String, worldState: WorldState) = withContext(Dispatchers.IO) {
        worldStateDao.insertOrUpdateWorldState(
            WorldStateEntity(
                sessionId = sessionId,
                worldId = worldState.worldId,
                schemaVersion = worldState.schemaVersion,
                stateJson = worldState.toJsonString(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}
