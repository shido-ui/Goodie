package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

enum class MemoryType {
    CONVERSATION,
    CHARACTER,
    NPC,
    WORLD,
    PLAYER,
    RELATIONSHIP,
    EVENT,
    FACT
}

data class MemoryEntry(
    val id: String,
    val type: MemoryType,
    val content: String,
    val importance: Int = 5, // 1 to 10
    val timestampWorldMinutes: Int = 0,
    val source: String = "SYSTEM",
    val confidence: Float = 1.0f,
    val relatedEntityId: String? = null
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("type", type.name)
        put("content", content)
        put("importance", importance)
        put("timestampWorldMinutes", timestampWorldMinutes)
        put("source", source)
        put("confidence", confidence.toDouble())
        put("relatedEntityId", relatedEntityId ?: JSONObject.NULL)
    }

    companion object {
        fun fromJson(json: JSONObject): MemoryEntry {
            return MemoryEntry(
                id = json.optString("id", "mem_${System.currentTimeMillis()}"),
                type = try {
                    MemoryType.valueOf(json.optString("type", MemoryType.FACT.name))
                } catch (e: Exception) {
                    MemoryType.FACT
                },
                content = json.optString("content"),
                importance = json.optInt("importance", 5),
                timestampWorldMinutes = json.optInt("timestampWorldMinutes", 0),
                source = json.optString("source", "SYSTEM"),
                confidence = json.optDouble("confidence", 1.0).toFloat(),
                relatedEntityId = if (json.has("relatedEntityId") && !json.isNull("relatedEntityId")) json.getString("relatedEntityId") else null
            )
        }
    }
}

enum class LoreCategory {
    WORLD_RULES,
    HISTORY,
    LOCATIONS,
    FACTIONS,
    CHARACTERS,
    CULTURES,
    MAGIC,
    TECHNOLOGIES,
    RELIGIONS,
    POLITICAL,
    IMPORTANT_EVENTS,
    USER_CREATED
}

data class LoreEntry(
    val id: String,
    val category: LoreCategory,
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val isUnlocked: Boolean = true
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("category", category.name)
        put("title", title)
        put("content", content)
        val arr = JSONArray()
        tags.forEach { arr.put(it) }
        put("tags", arr)
        put("isUnlocked", isUnlocked)
    }

    companion object {
        fun fromJson(json: JSONObject): LoreEntry {
            val list = mutableListOf<String>()
            val arr = json.optJSONArray("tags")
            if (arr != null) for (i in 0 until arr.length()) list.add(arr.optString(i))
            return LoreEntry(
                id = json.optString("id", "lore_${System.currentTimeMillis()}"),
                category = try {
                    LoreCategory.valueOf(json.optString("category", LoreCategory.WORLD_RULES.name))
                } catch (e: Exception) {
                    LoreCategory.WORLD_RULES
                },
                title = json.optString("title"),
                content = json.optString("content"),
                tags = list,
                isUnlocked = json.optBoolean("isUnlocked", true)
            )
        }
    }
}

data class SimulationLogEntry(
    val id: String,
    val timestampWorldMinutes: Int,
    val eventType: String, // NPC_MOVE, SCHEDULE_EXEC, RUMOR_SPREAD, CONSEQUENCE_FIRED, FACTION_SHIFT, QUEST_UPDATE, COMBAT, ECONOMY
    val description: String,
    val affectedEntities: List<String> = emptyList()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("timestampWorldMinutes", timestampWorldMinutes)
        put("eventType", eventType)
        put("description", description)
        val arr = JSONArray()
        affectedEntities.forEach { arr.put(it) }
        put("affectedEntities", arr)
    }

    companion object {
        fun fromJson(json: JSONObject): SimulationLogEntry {
            val list = mutableListOf<String>()
            val arr = json.optJSONArray("affectedEntities")
            if (arr != null) for (i in 0 until arr.length()) list.add(arr.optString(i))
            return SimulationLogEntry(
                id = json.optString("id", "sim_${System.currentTimeMillis()}"),
                timestampWorldMinutes = json.optInt("timestampWorldMinutes", 0),
                eventType = json.optString("eventType", "GENERAL"),
                description = json.optString("description", ""),
                affectedEntities = list
            )
        }
    }
}
