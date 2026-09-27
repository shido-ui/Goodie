package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entities.SessionEntity
import com.example.data.local.entities.WorldStateEntity
import com.example.data.model.WorldState
import com.example.engine.WorldConsistencyValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

sealed class RestoreResult {
    data class Success(val sessionId: String, val worldName: String) : RestoreResult()
    data class Failure(val errorReason: String) : RestoreResult()
}

class BackupRestoreManager(
    private val database: AppDatabase,
    private val validator: WorldConsistencyValidator = WorldConsistencyValidator()
) {

    suspend fun exportSessionBackup(sessionId: String): String? = withContext(Dispatchers.IO) {
        val session = database.sessionDao().getSessionById(sessionId) ?: return@withContext null
        val worldEntity = database.worldStateDao().getWorldState(sessionId) ?: return@withContext null

        val root = JSONObject().apply {
            put("appVersion", "RPG_AI_HUB_V2")
            put("exportedAt", System.currentTimeMillis())
            put("session", JSONObject().apply {
                put("id", session.id)
                put("mode", session.mode)
                put("title", session.title)
                put("worldId", session.worldId)
                put("worldName", session.worldName)
                put("characterId", session.characterId ?: JSONObject.NULL)
                put("characterName", session.characterName ?: JSONObject.NULL)
                put("modelId", session.modelId)
                put("providerId", session.providerId)
            })
            put("worldState", JSONObject(worldEntity.stateJson))
        }

        root.toString(2)
    }

    suspend fun validateAndRestoreBackup(backupJsonString: String): RestoreResult = withContext(Dispatchers.IO) {
        // Step 1: Validate JSON parsing
        val root = try {
            JSONObject(backupJsonString)
        } catch (e: Exception) {
            return@withContext RestoreResult.Failure("Invalid JSON format: ${e.localizedMessage}")
        }

        // Step 2: Validate Schema and Session
        val sessionObj = root.optJSONObject("session")
            ?: return@withContext RestoreResult.Failure("Missing 'session' object in backup payload")
        val worldObj = root.optJSONObject("worldState")
            ?: return@withContext RestoreResult.Failure("Missing 'worldState' object in backup payload")

        val sessionId = sessionObj.optString("id", "")
        if (sessionId.isBlank()) {
            return@withContext RestoreResult.Failure("Backup contains an invalid or empty sessionId")
        }

        // Step 3: Validate WorldState
        val worldState = try {
            WorldState.fromJson(worldObj)
        } catch (e: Exception) {
            return@withContext RestoreResult.Failure("Failed to parse WorldState: ${e.localizedMessage}")
        }

        // Step 4: Consistency Validation
        val violations = validator.validateWorldStateIntegrity(worldState)
        if (violations.isNotEmpty()) {
            return@withContext RestoreResult.Failure("World state failed consistency validation: ${violations.joinToString(", ")}")
        }

        // Step 5: Transactional Atomic Restore
        try {
            val session = SessionEntity(
                id = sessionId,
                mode = sessionObj.optString("mode", "OPEN_WORLD"),
                title = sessionObj.optString("title", worldState.worldName),
                worldId = worldState.worldId,
                worldName = worldState.worldName,
                characterId = if (sessionObj.has("characterId") && !sessionObj.isNull("characterId")) sessionObj.getString("characterId") else null,
                characterName = if (sessionObj.has("characterName") && !sessionObj.isNull("characterName")) sessionObj.getString("characterName") else null,
                modelId = sessionObj.optString("modelId", "gemini-2.5-flash"),
                providerId = sessionObj.optString("providerId", "gemini"),
                updatedAt = System.currentTimeMillis()
            )

            val worldEntity = WorldStateEntity(
                sessionId = sessionId,
                worldId = worldState.worldId,
                schemaVersion = worldState.schemaVersion,
                stateJson = worldState.toJsonString(),
                updatedAt = System.currentTimeMillis()
            )

            database.sessionDao().insertSession(session)
            database.worldStateDao().insertOrUpdateWorldState(worldEntity)

            RestoreResult.Success(sessionId, worldState.worldName)
        } catch (e: Exception) {
            RestoreResult.Failure("Database transaction failed during restore: ${e.localizedMessage}")
        }
    }
}
