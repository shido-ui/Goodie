package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

enum class QuestStatus {
    INACTIVE,
    ACTIVE,
    COMPLETED,
    FAILED,
    ABANDONED
}

data class QuestObjective(
    val id: String,
    val description: String,
    val isCompleted: Boolean = false,
    val currentProgress: Int = 0,
    val targetProgress: Int = 1
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("description", description)
        put("isCompleted", isCompleted)
        put("currentProgress", currentProgress)
        put("targetProgress", targetProgress)
    }

    companion object {
        fun fromJson(json: JSONObject): QuestObjective {
            return QuestObjective(
                id = json.optString("id", "obj_${System.currentTimeMillis()}"),
                description = json.optString("description", ""),
                isCompleted = json.optBoolean("isCompleted", false),
                currentProgress = json.optInt("currentProgress", 0),
                targetProgress = json.optInt("targetProgress", 1)
            )
        }
    }
}

data class QuestReward(
    val money: Int = 0,
    val itemRewards: List<InventoryItem> = emptyList(),
    val factionReputationChanges: Map<String, Int> = emptyMap()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("money", money)
        val arr = JSONArray()
        itemRewards.forEach { arr.put(it.toJson()) }
        put("itemRewards", arr)
        val repObj = JSONObject()
        factionReputationChanges.forEach { (k, v) -> repObj.put(k, v) }
        put("factionReputationChanges", repObj)
    }

    companion object {
        fun fromJson(json: JSONObject?): QuestReward {
            if (json == null) return QuestReward()
            val items = mutableListOf<InventoryItem>()
            val arr = json.optJSONArray("itemRewards")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    arr.optJSONObject(i)?.let { items.add(InventoryItem.fromJson(it)) }
                }
            }
            val rep = mutableMapOf<String, Int>()
            val rObj = json.optJSONObject("factionReputationChanges")
            if (rObj != null) {
                val keys = rObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    rep[k] = rObj.optInt(k, 0)
                }
            }
            return QuestReward(
                money = json.optInt("money", 0),
                itemRewards = items,
                factionReputationChanges = rep
            )
        }
    }
}

data class Quest(
    val id: String,
    val title: String,
    val description: String,
    val status: QuestStatus = QuestStatus.INACTIVE,
    val giverNpcId: String? = null,
    val relatedLocationId: String? = null,
    val objectives: List<QuestObjective> = emptyList(),
    val reward: QuestReward = QuestReward(),
    val consequencesOnFailure: String? = null
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("description", description)
        put("status", status.name)
        put("giverNpcId", giverNpcId ?: JSONObject.NULL)
        put("relatedLocationId", relatedLocationId ?: JSONObject.NULL)
        val objArr = JSONArray()
        objectives.forEach { objArr.put(it.toJson()) }
        put("objectives", objArr)
        put("reward", reward.toJson())
        put("consequencesOnFailure", consequencesOnFailure ?: JSONObject.NULL)
    }

    companion object {
        fun fromJson(json: JSONObject): Quest {
            val objList = mutableListOf<QuestObjective>()
            val arr = json.optJSONArray("objectives")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    arr.optJSONObject(i)?.let { objList.add(QuestObjective.fromJson(it)) }
                }
            }
            return Quest(
                id = json.optString("id"),
                title = json.optString("title"),
                description = json.optString("description", ""),
                status = try {
                    QuestStatus.valueOf(json.optString("status", QuestStatus.INACTIVE.name))
                } catch (e: Exception) {
                    QuestStatus.INACTIVE
                },
                giverNpcId = if (json.has("giverNpcId") && !json.isNull("giverNpcId")) json.getString("giverNpcId") else null,
                relatedLocationId = if (json.has("relatedLocationId") && !json.isNull("relatedLocationId")) json.getString("relatedLocationId") else null,
                objectives = objList,
                reward = QuestReward.fromJson(json.optJSONObject("reward")),
                consequencesOnFailure = if (json.has("consequencesOnFailure") && !json.isNull("consequencesOnFailure")) json.getString("consequencesOnFailure") else null
            )
        }
    }
}

data class QuestArc(
    val id: String,
    val title: String,
    val description: String,
    val questIds: List<String> = emptyList(),
    val isCompleted: Boolean = false
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("description", description)
        val arr = JSONArray()
        questIds.forEach { arr.put(it) }
        put("questIds", arr)
        put("isCompleted", isCompleted)
    }

    companion object {
        fun fromJson(json: JSONObject): QuestArc {
            val list = mutableListOf<String>()
            val arr = json.optJSONArray("questIds")
            if (arr != null) {
                for (i in 0 until arr.length()) list.add(arr.optString(i))
            }
            return QuestArc(
                id = json.optString("id"),
                title = json.optString("title"),
                description = json.optString("description", ""),
                questIds = list,
                isCompleted = json.optBoolean("isCompleted", false)
            )
        }
    }
}

data class Faction(
    val id: String,
    val name: String,
    val description: String,
    val influenceScore: Int = 50, // 0 to 100
    val leaderNpcId: String? = null,
    val headquartersLocationId: String? = null,
    val alliedFactionIds: List<String> = emptyList(),
    val enemyFactionIds: List<String> = emptyList()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("description", description)
        put("influenceScore", influenceScore)
        put("leaderNpcId", leaderNpcId ?: JSONObject.NULL)
        put("headquartersLocationId", headquartersLocationId ?: JSONObject.NULL)
        val allies = JSONArray()
        alliedFactionIds.forEach { allies.put(it) }
        put("alliedFactionIds", allies)
        val enemies = JSONArray()
        enemyFactionIds.forEach { enemies.put(it) }
        put("enemyFactionIds", enemies)
    }

    companion object {
        fun fromJson(json: JSONObject): Faction {
            val allies = mutableListOf<String>()
            val aArr = json.optJSONArray("alliedFactionIds")
            if (aArr != null) for (i in 0 until aArr.length()) allies.add(aArr.optString(i))

            val enemies = mutableListOf<String>()
            val eArr = json.optJSONArray("enemyFactionIds")
            if (eArr != null) for (i in 0 until eArr.length()) enemies.add(eArr.optString(i))

            return Faction(
                id = json.optString("id"),
                name = json.optString("name"),
                description = json.optString("description", ""),
                influenceScore = json.optInt("influenceScore", 50),
                leaderNpcId = if (json.has("leaderNpcId") && !json.isNull("leaderNpcId")) json.getString("leaderNpcId") else null,
                headquartersLocationId = if (json.has("headquartersLocationId") && !json.isNull("headquartersLocationId")) json.getString("headquartersLocationId") else null,
                alliedFactionIds = allies,
                enemyFactionIds = enemies
            )
        }
    }
}

data class PendingConsequence(
    val id: String,
    val title: String,
    val description: String,
    val deadlineWorldMinutes: Int,
    val triggerCondition: String, // e.g. "ALWAYS", "LOW_FACTION_REP", "TIME_ELAPSED"
    val affectedNpcIds: List<String> = emptyList(),
    val isProcessed: Boolean = false,
    val outcomeNarrative: String? = null
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("description", description)
        put("deadlineWorldMinutes", deadlineWorldMinutes)
        put("triggerCondition", triggerCondition)
        val arr = JSONArray()
        affectedNpcIds.forEach { arr.put(it) }
        put("affectedNpcIds", arr)
        put("isProcessed", isProcessed)
        put("outcomeNarrative", outcomeNarrative ?: JSONObject.NULL)
    }

    companion object {
        fun fromJson(json: JSONObject): PendingConsequence {
            val list = mutableListOf<String>()
            val arr = json.optJSONArray("affectedNpcIds")
            if (arr != null) for (i in 0 until arr.length()) list.add(arr.optString(i))
            return PendingConsequence(
                id = json.optString("id"),
                title = json.optString("title"),
                description = json.optString("description", ""),
                deadlineWorldMinutes = json.optInt("deadlineWorldMinutes", 0),
                triggerCondition = json.optString("triggerCondition", "TIME_ELAPSED"),
                affectedNpcIds = list,
                isProcessed = json.optBoolean("isProcessed", false),
                outcomeNarrative = if (json.has("outcomeNarrative") && !json.isNull("outcomeNarrative")) json.getString("outcomeNarrative") else null
            )
        }
    }
}

data class WorldEvent(
    val id: String,
    val title: String,
    val description: String,
    val locationId: String,
    val timestampMinutes: Int,
    val involvedEntityNames: List<String> = emptyList()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("description", description)
        put("locationId", locationId)
        put("timestampMinutes", timestampMinutes)
        val arr = JSONArray()
        involvedEntityNames.forEach { arr.put(it) }
        put("involvedEntityNames", arr)
    }

    companion object {
        fun fromJson(json: JSONObject): WorldEvent {
            val list = mutableListOf<String>()
            val arr = json.optJSONArray("involvedEntityNames")
            if (arr != null) for (i in 0 until arr.length()) list.add(arr.optString(i))
            return WorldEvent(
                id = json.optString("id", "event_${System.currentTimeMillis()}"),
                title = json.optString("title"),
                description = json.optString("description", ""),
                locationId = json.optString("locationId", "Oakvale Tavern"),
                timestampMinutes = json.optInt("timestampMinutes", 0),
                involvedEntityNames = list
            )
        }
    }
}

data class Rumor(
    val id: String,
    val text: String,
    val isTrue: Boolean = true,
    val confidence: Int = 70, // 0 to 100
    val spreadCount: Int = 1,
    val knownByNpcIds: List<String> = emptyList(),
    val timestampMinutes: Int = 0
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("text", text)
        put("isTrue", isTrue)
        put("confidence", confidence)
        put("spreadCount", spreadCount)
        val arr = JSONArray()
        knownByNpcIds.forEach { arr.put(it) }
        put("knownByNpcIds", arr)
        put("timestampMinutes", timestampMinutes)
    }

    companion object {
        fun fromJson(json: JSONObject): Rumor {
            val list = mutableListOf<String>()
            val arr = json.optJSONArray("knownByNpcIds")
            if (arr != null) for (i in 0 until arr.length()) list.add(arr.optString(i))
            return Rumor(
                id = json.optString("id", "rumor_${System.currentTimeMillis()}"),
                text = json.optString("text"),
                isTrue = json.optBoolean("isTrue", true),
                confidence = json.optInt("confidence", 70),
                spreadCount = json.optInt("spreadCount", 1),
                knownByNpcIds = list,
                timestampMinutes = json.optInt("timestampMinutes", 0)
            )
        }
    }
}

data class Secret(
    val id: String,
    val title: String,
    val content: String,
    val importance: Int = 5,
    val knownByNpcIds: List<String> = emptyList(),
    val isDiscoveredByPlayer: Boolean = false
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("content", content)
        put("importance", importance)
        val arr = JSONArray()
        knownByNpcIds.forEach { arr.put(it) }
        put("knownByNpcIds", arr)
        put("isDiscoveredByPlayer", isDiscoveredByPlayer)
    }

    companion object {
        fun fromJson(json: JSONObject): Secret {
            val list = mutableListOf<String>()
            val arr = json.optJSONArray("knownByNpcIds")
            if (arr != null) for (i in 0 until arr.length()) list.add(arr.optString(i))
            return Secret(
                id = json.optString("id", "sec_${System.currentTimeMillis()}"),
                title = json.optString("title"),
                content = json.optString("content"),
                importance = json.optInt("importance", 5),
                knownByNpcIds = list,
                isDiscoveredByPlayer = json.optBoolean("isDiscoveredByPlayer", false)
            )
        }
    }
}

data class Evidence(
    val id: String,
    val title: String,
    val description: String,
    val connectedLocationId: String? = null,
    val connectedNpcId: String? = null,
    val connectedSecretId: String? = null
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("description", description)
        put("connectedLocationId", connectedLocationId ?: JSONObject.NULL)
        put("connectedNpcId", connectedNpcId ?: JSONObject.NULL)
        put("connectedSecretId", connectedSecretId ?: JSONObject.NULL)
    }

    companion object {
        fun fromJson(json: JSONObject): Evidence {
            return Evidence(
                id = json.optString("id", "ev_${System.currentTimeMillis()}"),
                title = json.optString("title"),
                description = json.optString("description", ""),
                connectedLocationId = if (json.has("connectedLocationId") && !json.isNull("connectedLocationId")) json.getString("connectedLocationId") else null,
                connectedNpcId = if (json.has("connectedNpcId") && !json.isNull("connectedNpcId")) json.getString("connectedNpcId") else null,
                connectedSecretId = if (json.has("connectedSecretId") && !json.isNull("connectedSecretId")) json.getString("connectedSecretId") else null
            )
        }
    }
}
