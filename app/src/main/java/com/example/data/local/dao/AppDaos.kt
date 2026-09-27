package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY updatedAt DESC")
    fun getAllSessionsFlow(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: String)
}

@Dao
interface WorldStateDao {
    @Query("SELECT * FROM world_states WHERE sessionId = :sessionId")
    suspend fun getWorldState(sessionId: String): WorldStateEntity?

    @Query("SELECT * FROM world_states WHERE sessionId = :sessionId")
    fun getWorldStateFlow(sessionId: String): Flow<WorldStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWorldState(state: WorldStateEntity)

    @Query("DELETE FROM world_states WHERE sessionId = :sessionId")
    suspend fun deleteWorldState(sessionId: String)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSessionFlow(sessionId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(sessionId: String, limit: Int): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)
}

@Dao
interface SimulationLogDao {
    @Query("SELECT * FROM simulation_logs WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT 100")
    fun getLogsForSessionFlow(sessionId: String): Flow<List<SimulationLogDbEntity>>

    @Query("SELECT * FROM simulation_logs WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentLogs(sessionId: String, limit: Int): List<SimulationLogDbEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SimulationLogDbEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<SimulationLogDbEntity>)

    @Query("DELETE FROM simulation_logs WHERE sessionId = :sessionId")
    suspend fun deleteLogsForSession(sessionId: String)
}

@Dao
interface LoreDao {
    @Query("SELECT * FROM lore_entries WHERE sessionId = :sessionId ORDER BY category ASC")
    fun getLoreForSessionFlow(sessionId: String): Flow<List<LoreDbEntity>>

    @Query("SELECT * FROM lore_entries WHERE sessionId = :sessionId AND isUnlocked = 1")
    suspend fun getUnlockedLore(sessionId: String): List<LoreDbEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLore(lore: LoreDbEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoreList(loreList: List<LoreDbEntity>)

    @Query("DELETE FROM lore_entries WHERE sessionId = :sessionId")
    suspend fun deleteLoreForSession(sessionId: String)
}
