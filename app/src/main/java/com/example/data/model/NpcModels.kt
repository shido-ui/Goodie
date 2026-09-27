package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

enum class SimulationLod {
    HIGH,   // Full simulation: in player location or direct companion
    MEDIUM, // Town / regional simulation: updates schedules & major goals
    LOW     // Distant background: low-frequency ticks
}

enum class NpcActionType {
    FOLLOW_SCHEDULE,
    INVESTIGATE,
    ASK_ALLY,
    REPORT_INFORMATION,
    TRAVEL,
    TRADE,
    PROTECT,
    WAIT,
    FLEE,
    REST
}

data class NpcDecision(
    val npcId: String,
    val actionType: NpcActionType,
    val targetLocationId: String? = null,
    val targetEntityId: String? = null,
    val rationale: String,
    val timestampMinutes: Int
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("npcId", npcId)
        put("actionType", actionType.name)
        put("targetLocationId", targetLocationId ?: JSONObject.NULL)
        put("targetEntityId", targetEntityId ?: JSONObject.NULL)
        put("rationale", rationale)
        put("timestampMinutes", timestampMinutes)
    }

    companion object {
        fun fromJson(json: JSONObject): NpcDecision {
            return NpcDecision(
                npcId = json.optString("npcId"),
                actionType = try {
                    NpcActionType.valueOf(json.optString("actionType", NpcActionType.WAIT.name))
                } catch (e: Exception) {
                    NpcActionType.WAIT
                },
                targetLocationId = if (json.has("targetLocationId") && !json.isNull("targetLocationId")) json.getString("targetLocationId") else null,
                targetEntityId = if (json.has("targetEntityId") && !json.isNull("targetEntityId")) json.getString("targetEntityId") else null,
                rationale = json.optString("rationale", ""),
                timestampMinutes = json.optInt("timestampMinutes", 0)
            )
        }
    }
}

data class NpcRelationships(
    val trust: Int = 50,         // 0 to 100
    val suspicion: Int = 10,     // 0 to 100
    val friendship: Int = 0,     // -100 to 100
    val hostility: Int = 0,      // 0 to 100
    val respect: Int = 50,       // 0 to 100
    val fear: Int = 0,           // 0 to 100
    val loyalty: Int = 50        // 0 to 100
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("trust", trust)
        put("suspicion", suspicion)
        put("friendship", friendship)
        put("hostility", hostility)
        put("respect", respect)
        put("fear", fear)
        put("loyalty", loyalty)
    }

    companion object {
        fun fromJson(json: JSONObject?): NpcRelationships {
            if (json == null) return NpcRelationships()
            return NpcRelationships(
                trust = json.optInt("trust", 50).coerceIn(0, 100),
                suspicion = json.optInt("suspicion", 10).coerceIn(0, 100),
                friendship = json.optInt("friendship", 0).coerceIn(-100, 100),
                hostility = json.optInt("hostility", 0).coerceIn(0, 100),
                respect = json.optInt("respect", 50).coerceIn(0, 100),
                fear = json.optInt("fear", 0).coerceIn(0, 100),
                loyalty = json.optInt("loyalty", 50).coerceIn(0, 100)
            )
        }
    }
}

data class NpcScheduleItem(
    val startHour: Int,
    val endHour: Int,
    val locationId: String,
    val activityDescription: String
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("startHour", startHour)
        put("endHour", endHour)
        put("locationId", locationId)
        put("activityDescription", activityDescription)
    }

    companion object {
        fun fromJson(json: JSONObject): NpcScheduleItem {
            return NpcScheduleItem(
                startHour = json.optInt("startHour", 8),
                endHour = json.optInt("endHour", 17),
                locationId = json.optString("locationId", "Oakvale Tavern"),
                activityDescription = json.optString("activityDescription", "Working")
            )
        }
    }
}

data class NpcGoal(
    val id: String,
    val description: String,
    val priority: Int = 1, // 1 (Highest) to 5 (Lowest)
    val isCompleted: Boolean = false,
    val relatedFactionId: String? = null
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("description", description)
        put("priority", priority)
        put("isCompleted", isCompleted)
        put("relatedFactionId", relatedFactionId ?: JSONObject.NULL)
    }

    companion object {
        fun fromJson(json: JSONObject): NpcGoal {
            return NpcGoal(
                id = json.optString("id", "goal_${System.currentTimeMillis()}"),
                description = json.optString("description", ""),
                priority = json.optInt("priority", 1),
                isCompleted = json.optBoolean("isCompleted", false),
                relatedFactionId = if (json.has("relatedFactionId") && !json.isNull("relatedFactionId")) json.getString("relatedFactionId") else null
            )
        }
    }
}

data class NpcMemory(
    val id: String,
    val text: String,
    val importance: Int = 5, // 1 (Minor) to 10 (Critical)
    val timestampWorldMinutes: Int = 0,
    val source: String = "OBSERVATION" // PLAYER_ACTION, CONVERSATION, RUMOR, OBSERVATION
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("text", text)
        put("importance", importance)
        put("timestampWorldMinutes", timestampWorldMinutes)
        put("source", source)
    }

    companion object {
        fun fromJson(json: JSONObject): NpcMemory {
            return NpcMemory(
                id = json.optString("id", "mem_${System.currentTimeMillis()}"),
                text = json.optString("text", ""),
                importance = json.optInt("importance", 5),
                timestampWorldMinutes = json.optInt("timestampWorldMinutes", 0),
                source = json.optString("source", "OBSERVATION")
            )
        }
    }
}

data class NpcState(
    val id: String,
    val name: String,
    val title: String,
    val personality: String,
    val factionId: String? = null,
    val currentLocation: String,
    val currentActivity: String = "Idle",
    val money: Int = 20,
    val relationships: NpcRelationships = NpcRelationships(),
    val goals: List<NpcGoal> = emptyList(),
    val schedule: List<NpcScheduleItem> = emptyList(),
    val memories: List<NpcMemory> = emptyList(),
    val knownRumors: List<String> = emptyList(),
    val isAlive: Boolean = true,
    val simulationLod: SimulationLod = SimulationLod.HIGH
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("title", title)
        put("personality", personality)
        put("factionId", factionId ?: JSONObject.NULL)
        put("currentLocation", currentLocation)
        put("currentActivity", currentActivity)
        put("money", money)
        put("relationships", relationships.toJson())
        put("isAlive", isAlive)
        put("simulationLod", simulationLod.name)

        val goalsArr = JSONArray()
        goals.forEach { goalsArr.put(it.toJson()) }
        put("goals", goalsArr)

        val schedArr = JSONArray()
        schedule.forEach { schedArr.put(it.toJson()) }
        put("schedule", schedArr)

        val memArr = JSONArray()
        memories.forEach { memArr.put(it.toJson()) }
        put("memories", memArr)

        val rumArr = JSONArray()
        knownRumors.forEach { rumArr.put(it) }
        put("knownRumors", rumArr)
    }

    companion object {
        fun fromJson(json: JSONObject): NpcState {
            val goalsList = mutableListOf<NpcGoal>()
            val gArr = json.optJSONArray("goals")
            if (gArr != null) {
                for (i in 0 until gArr.length()) {
                    gArr.optJSONObject(i)?.let { goalsList.add(NpcGoal.fromJson(it)) }
                }
            }

            val schedList = mutableListOf<NpcScheduleItem>()
            val sArr = json.optJSONArray("schedule")
            if (sArr != null) {
                for (i in 0 until sArr.length()) {
                    sArr.optJSONObject(i)?.let { schedList.add(NpcScheduleItem.fromJson(it)) }
                }
            }

            val memList = mutableListOf<NpcMemory>()
            val mArr = json.optJSONArray("memories")
            if (mArr != null) {
                for (i in 0 until mArr.length()) {
                    mArr.optJSONObject(i)?.let { memList.add(NpcMemory.fromJson(it)) }
                }
            }

            val rumorsList = mutableListOf<String>()
            val rArr = json.optJSONArray("knownRumors")
            if (rArr != null) {
                for (i in 0 until rArr.length()) {
                    rumorsList.add(rArr.optString(i))
                }
            }

            return NpcState(
                id = json.optString("id"),
                name = json.optString("name"),
                title = json.optString("title", ""),
                personality = json.optString("personality", "Neutral"),
                factionId = if (json.has("factionId") && !json.isNull("factionId")) json.getString("factionId") else null,
                currentLocation = json.optString("currentLocation", "Oakvale Tavern"),
                currentActivity = json.optString("currentActivity", "Idle"),
                money = json.optInt("money", 20),
                relationships = NpcRelationships.fromJson(json.optJSONObject("relationships")),
                goals = goalsList,
                schedule = schedList,
                memories = memList,
                knownRumors = rumorsList,
                isAlive = json.optBoolean("isAlive", true),
                simulationLod = try {
                    SimulationLod.valueOf(json.optString("simulationLod", SimulationLod.HIGH.name))
                } catch (e: Exception) {
                    SimulationLod.HIGH
                }
            )
        }
    }
}
