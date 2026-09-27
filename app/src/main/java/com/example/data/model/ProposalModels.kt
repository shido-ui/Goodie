package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

enum class StateProposalType {
    DAMAGE_PLAYER,
    HEAL_PLAYER,
    ADD_MONEY,
    SPEND_MONEY,
    GAIN_ITEM,
    LOSE_ITEM,
    CHANGE_LOCATION,
    ADVANCE_TIME,
    UPDATE_RELATIONSHIP,
    UPDATE_NPC_LOCATION,
    UPDATE_NPC_ACTIVITY,
    PROGRESS_QUEST,
    FAIL_QUEST,
    DISCOVER_SECRET,
    SPREAD_RUMOR,
    TRIGGER_CONSEQUENCE
}

data class StateProposal(
    val type: StateProposalType,
    val target: String? = null,          // entity id, quest id, item name, location id
    val value: String? = null,           // numeric amount, new value, status
    val extraData: Map<String, String> = emptyMap()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("type", type.name)
        put("target", target ?: JSONObject.NULL)
        put("value", value ?: JSONObject.NULL)
        val ex = JSONObject()
        extraData.forEach { (k, v) -> ex.put(k, v) }
        put("extraData", ex)
    }

    companion object {
        fun fromJson(json: JSONObject): StateProposal? {
            val typeStr = json.optString("type", "")
            val type = try {
                StateProposalType.valueOf(typeStr)
            } catch (e: Exception) {
                return null
            }
            val target = if (json.has("target") && !json.isNull("target")) json.getString("target") else null
            val value = if (json.has("value") && !json.isNull("value")) json.getString("value") else null
            val ex = mutableMapOf<String, String>()
            val exObj = json.optJSONObject("extraData")
            if (exObj != null) {
                val keys = exObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    ex[k] = exObj.optString(k, "")
                }
            }
            return StateProposal(type = type, target = target, value = value, extraData = ex)
        }
    }
}

data class StateValidationResult(
    val isValid: Boolean,
    val proposal: StateProposal,
    val reason: String,
    val appliedMutationSummary: String? = null
)

data class AiStructuredResponse(
    val narrative: String,
    val dialogue: String? = null,
    val speakerNpcId: String? = null,
    val suggestedActions: List<String> = emptyList(),
    val proposals: List<StateProposal> = emptyList(),
    val rawJson: String? = null
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("narrative", narrative)
        put("dialogue", dialogue ?: JSONObject.NULL)
        put("speakerNpcId", speakerNpcId ?: JSONObject.NULL)
        val actArr = JSONArray()
        suggestedActions.forEach { actArr.put(it) }
        put("suggestedActions", actArr)
        val propArr = JSONArray()
        proposals.forEach { propArr.put(it.toJson()) }
        put("proposals", propArr)
    }

    companion object {
        fun fromJson(json: JSONObject): AiStructuredResponse {
            val acts = mutableListOf<String>()
            val aArr = json.optJSONArray("suggestedActions")
            if (aArr != null) for (i in 0 until aArr.length()) acts.add(aArr.optString(i))

            val props = mutableListOf<StateProposal>()
            val pArr = json.optJSONArray("proposals")
            if (pArr != null) {
                for (i in 0 until pArr.length()) {
                    pArr.optJSONObject(i)?.let { obj ->
                        StateProposal.fromJson(obj)?.let { props.add(it) }
                    }
                }
            }

            return AiStructuredResponse(
                narrative = json.optString("narrative", ""),
                dialogue = if (json.has("dialogue") && !json.isNull("dialogue")) json.getString("dialogue") else null,
                speakerNpcId = if (json.has("speakerNpcId") && !json.isNull("speakerNpcId")) json.getString("speakerNpcId") else null,
                suggestedActions = acts,
                proposals = props
            )
        }
    }
}
