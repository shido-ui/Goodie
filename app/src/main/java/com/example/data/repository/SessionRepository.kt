package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.local.entities.SessionEntity
import com.example.data.local.entities.SimulationLogDbEntity
import com.example.data.local.entities.WorldStateEntity
import com.example.data.model.SimulationLogEntry
import com.example.data.model.WorldState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray

class SessionRepository(
    private val database: AppDatabase
) {
    private val sessionDao = database.sessionDao()
    private val chatMessageDao = database.chatMessageDao()
    private val simulationLogDao = database.simulationLogDao()

    fun getAllSessions(): Flow<List<SessionEntity>> = sessionDao.getAllSessionsFlow()

    suspend fun getSession(sessionId: String): SessionEntity? = withContext(Dispatchers.IO) {
        sessionDao.getSessionById(sessionId)
    }

    suspend fun createSession(session: SessionEntity, initialWorldState: WorldState) = withContext(Dispatchers.IO) {
        database.withTransaction {
            sessionDao.insertSession(session)
            database.worldStateDao().insertOrUpdateWorldState(
                WorldStateEntity(
                    sessionId = session.id,
                    worldId = initialWorldState.worldId,
                    schemaVersion = initialWorldState.schemaVersion,
                    stateJson = initialWorldState.toJsonString(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun updateSession(session: SessionEntity) = withContext(Dispatchers.IO) {
        sessionDao.updateSession(session)
    }

    suspend fun deleteSession(sessionId: String) = withContext(Dispatchers.IO) {
        database.withTransaction {
            sessionDao.deleteSessionById(sessionId)
            database.worldStateDao().deleteWorldState(sessionId)
            chatMessageDao.deleteMessagesForSession(sessionId)
            simulationLogDao.deleteLogsForSession(sessionId)
            database.loreDao().deleteLoreForSession(sessionId)
        }
    }

    fun getChatMessagesFlow(sessionId: String): Flow<List<ChatMessageEntity>> =
        chatMessageDao.getMessagesForSessionFlow(sessionId)

    fun getSimulationLogsFlow(sessionId: String): Flow<List<SimulationLogDbEntity>> =
        simulationLogDao.getLogsForSessionFlow(sessionId)

    suspend fun addChatMessage(message: ChatMessageEntity) = withContext(Dispatchers.IO) {
        chatMessageDao.insertMessage(message)
    }
}
