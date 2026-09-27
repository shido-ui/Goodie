package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

enum class GameMode(val displayName: String, val description: String) {
    OPEN_WORLD("Open World RPG", "Explore freely, interact with the world, shape history and complete epic quests."),
    CHARACTER_CHAT("Character Chat", "Focus on deep personal conversation and relationship building with a specific NPC."),
    OPEN_WORLD_AND_CHARACTER("Open World + Companion", "Journey through the living world alongside a chosen companion who reacts dynamically.")
}

data class PlayerAttributes(
    val strength: Int = 10,
    val dexterity: Int = 10,
    val constitution: Int = 10,
    val intelligence: Int = 10,
    val wisdom: Int = 10,
    val charisma: Int = 10
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("strength", strength)
        put("dexterity", dexterity)
        put("constitution", constitution)
        put("intelligence", intelligence)
        put("wisdom", wisdom)
        put("charisma", charisma)
    }

    companion object {
        fun fromJson(json: JSONObject?): PlayerAttributes {
            if (json == null) return PlayerAttributes()
            return PlayerAttributes(
                strength = json.optInt("strength", 10),
                dexterity = json.optInt("dexterity", 10),
                constitution = json.optInt("constitution", 10),
                intelligence = json.optInt("intelligence", 10),
                wisdom = json.optInt("wisdom", 10),
                charisma = json.optInt("charisma", 10)
            )
        }
    }
}

data class InventoryItem(
    val id: String,
    val name: String,
    val description: String,
    val quantity: Int = 1,
    val value: Int = 0,
    val itemType: String = "MISC", // WEAPON, ARMOR, CONSUMABLE, QUEST, MISC
    val isEquipped: Boolean = false
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("description", description)
        put("quantity", quantity)
        put("value", value)
        put("itemType", itemType)
        put("isEquipped", isEquipped)
    }

    companion object {
        fun fromJson(json: JSONObject): InventoryItem {
            return InventoryItem(
                id = json.optString("id", "item_${System.currentTimeMillis()}"),
                name = json.optString("name", "Unknown Item"),
                description = json.optString("description", ""),
                quantity = json.optInt("quantity", 1),
                value = json.optInt("value", 0),
                itemType = json.optString("itemType", "MISC"),
                isEquipped = json.optBoolean("isEquipped", false)
            )
        }
    }
}

data class StatusEffect(
    val id: String,
    val name: String,
    val description: String,
    val durationTurns: Int,
    val isDebuff: Boolean = false
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("description", description)
        put("durationTurns", durationTurns)
        put("isDebuff", isDebuff)
    }

    companion object {
        fun fromJson(json: JSONObject): StatusEffect {
            return StatusEffect(
                id = json.optString("id", "effect_${System.currentTimeMillis()}"),
                name = json.optString("name", "Unknown Effect"),
                description = json.optString("description", ""),
                durationTurns = json.optInt("durationTurns", 1),
                isDebuff = json.optBoolean("isDebuff", false)
            )
        }
    }
}

data class PlayerState(
    val name: String = "Adventurer",
    val title: String = "Wanderer",
    val hp: Int = 100,
    val maxHp: Int = 100,
    val money: Int = 50,
    val location: String = "Oakvale Tavern",
    val attributes: PlayerAttributes = PlayerAttributes(),
    val inventory: List<InventoryItem> = emptyList(),
    val statusEffects: List<StatusEffect> = emptyList(),
    val factionReputations: Map<String, Int> = emptyMap() // factionId -> reputation (-100 to 100)
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("name", name)
        put("title", title)
        put("hp", hp)
        put("maxHp", maxHp)
        put("money", money)
        put("location", location)
        put("attributes", attributes.toJson())
        
        val invArr = JSONArray()
        inventory.forEach { invArr.put(it.toJson()) }
        put("inventory", invArr)

        val effectsArr = JSONArray()
        statusEffects.forEach { effectsArr.put(it.toJson()) }
        put("statusEffects", effectsArr)

        val repObj = JSONObject()
        factionReputations.forEach { (k, v) -> repObj.put(k, v) }
        put("factionReputations", repObj)
    }

    companion object {
        fun fromJson(json: JSONObject): PlayerState {
            val attributes = PlayerAttributes.fromJson(json.optJSONObject("attributes"))
            
            val invList = mutableListOf<InventoryItem>()
            val invArr = json.optJSONArray("inventory")
            if (invArr != null) {
                for (i in 0 until invArr.length()) {
                    invArr.optJSONObject(i)?.let { invList.add(InventoryItem.fromJson(it)) }
                }
            }

            val effectsList = mutableListOf<StatusEffect>()
            val effectsArr = json.optJSONArray("statusEffects")
            if (effectsArr != null) {
                for (i in 0 until effectsArr.length()) {
                    effectsArr.optJSONObject(i)?.let { effectsList.add(StatusEffect.fromJson(it)) }
                }
            }

            val repMap = mutableMapOf<String, Int>()
            val repObj = json.optJSONObject("factionReputations")
            if (repObj != null) {
                val keys = repObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    repMap[k] = repObj.optInt(k, 0)
                }
            }

            return PlayerState(
                name = json.optString("name", "Adventurer"),
                title = json.optString("title", "Wanderer"),
                hp = json.optInt("hp", 100),
                maxHp = json.optInt("maxHp", 100),
                money = json.optInt("money", 50),
                location = json.optString("location", "Oakvale Tavern"),
                attributes = attributes,
                inventory = invList,
                statusEffects = effectsList,
                factionReputations = repMap
            )
        }
    }
}

data class WorldTime(
    val day: Int = 1,
    val hour: Int = 8,
    val minute: Int = 0
) {
    fun toFormattedString(): String {
        val hh = hour.toString().padStart(2, '0')
        val mm = minute.toString().padStart(2, '0')
        return "Day $day, $hh:$mm"
    }

    fun toTotalMinutes(): Int = (day - 1) * 24 * 60 + hour * 60 + minute

    fun advanceMinutes(minutesToAdd: Int): WorldTime {
        val total = toTotalMinutes() + minutesToAdd
        val newDay = (total / (24 * 60)) + 1
        val remainingMin = total % (24 * 60)
        val newHour = remainingMin / 60
        val newMinute = remainingMin % 60
        return WorldTime(day = newDay, hour = newHour, minute = newMinute)
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("day", day)
        put("hour", hour)
        put("minute", minute)
    }

    companion object {
        fun fromJson(json: JSONObject?): WorldTime {
            if (json == null) return WorldTime()
            return WorldTime(
                day = json.optInt("day", 1),
                hour = json.optInt("hour", 8),
                minute = json.optInt("minute", 0)
            )
        }
    }
}

data class LocationInfo(
    val id: String,
    val name: String,
    val region: String,
    val description: String,
    val dangerLevel: Int = 1, // 1 (Safe) to 5 (Deadly)
    val connectedLocationIds: List<String> = emptyList()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("region", region)
        put("description", description)
        put("dangerLevel", dangerLevel)
        val arr = JSONArray()
        connectedLocationIds.forEach { arr.put(it) }
        put("connectedLocationIds", arr)
    }

    companion object {
        fun fromJson(json: JSONObject): LocationInfo {
            val list = mutableListOf<String>()
            val arr = json.optJSONArray("connectedLocationIds")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    list.add(arr.optString(i))
                }
            }
            return LocationInfo(
                id = json.optString("id"),
                name = json.optString("name"),
                region = json.optString("region", "General"),
                description = json.optString("description", ""),
                dangerLevel = json.optInt("dangerLevel", 1),
                connectedLocationIds = list
            )
        }
    }
}
