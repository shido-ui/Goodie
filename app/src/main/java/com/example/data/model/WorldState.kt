package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

data class WorldState(
    val worldId: String = "world_default",
    val worldName: String = "Eldoria: The Shattered Realm",
    val worldDescription: String = "A high-fantasy continent recovering from the Arcane Cataclysm, filled with rival factions, ancient ruins, and forgotten magic.",
    val schemaVersion: Int = 2,
    val player: PlayerState = PlayerState(),
    val time: WorldTime = WorldTime(),
    val locations: List<LocationInfo> = emptyList(),
    val npcs: List<NpcState> = emptyList(),
    val factions: List<Faction> = emptyList(),
    val quests: List<Quest> = emptyList(),
    val questArcs: List<QuestArc> = emptyList(),
    val pendingConsequences: List<PendingConsequence> = emptyList(),
    val worldEvents: List<WorldEvent> = emptyList(),
    val rumors: List<Rumor> = emptyList(),
    val secrets: List<Secret> = emptyList(),
    val evidence: List<Evidence> = emptyList(),
    val memories: List<MemoryEntry> = emptyList(),
    val simulationLogs: List<SimulationLogEntry> = emptyList()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("worldId", worldId)
        put("worldName", worldName)
        put("worldDescription", worldDescription)
        put("schemaVersion", schemaVersion)
        put("player", player.toJson())
        put("time", time.toJson())

        val locArr = JSONArray()
        locations.forEach { locArr.put(it.toJson()) }
        put("locations", locArr)

        val npcArr = JSONArray()
        npcs.forEach { npcArr.put(it.toJson()) }
        put("npcs", npcArr)

        val facArr = JSONArray()
        factions.forEach { facArr.put(it.toJson()) }
        put("factions", facArr)

        val qArr = JSONArray()
        quests.forEach { qArr.put(it.toJson()) }
        put("quests", qArr)

        val qaArr = JSONArray()
        questArcs.forEach { qaArr.put(it.toJson()) }
        put("questArcs", qaArr)

        val pcArr = JSONArray()
        pendingConsequences.forEach { pcArr.put(it.toJson()) }
        put("pendingConsequences", pcArr)

        val weArr = JSONArray()
        worldEvents.forEach { weArr.put(it.toJson()) }
        put("worldEvents", weArr)

        val rumArr = JSONArray()
        rumors.forEach { rumArr.put(it.toJson()) }
        put("rumors", rumArr)

        val secArr = JSONArray()
        secrets.forEach { secArr.put(it.toJson()) }
        put("secrets", secArr)

        val evArr = JSONArray()
        evidence.forEach { evArr.put(it.toJson()) }
        put("evidence", evArr)

        val memArr = JSONArray()
        memories.forEach { memArr.put(it.toJson()) }
        put("memories", memArr)

        val simArr = JSONArray()
        // Keep max last 50 logs in persistent JSON
        simulationLogs.takeLast(50).forEach { simArr.put(it.toJson()) }
        put("simulationLogs", simArr)
    }

    fun toJsonString(): String = toJson().toString(2)

    companion object {
        const val CURRENT_SCHEMA_VERSION = 2

        fun fromJson(json: JSONObject): WorldState {
            val schemaVer = json.optInt("schemaVersion", 1)

            // Migration from v1 to v2 if necessary
            val player = PlayerState.fromJson(json.optJSONObject("player") ?: JSONObject())
            val time = WorldTime.fromJson(json.optJSONObject("time"))

            val locations = mutableListOf<LocationInfo>()
            val lArr = json.optJSONArray("locations")
            if (lArr != null) for (i in 0 until lArr.length()) lArr.optJSONObject(i)?.let { locations.add(LocationInfo.fromJson(it)) }

            val npcs = mutableListOf<NpcState>()
            val nArr = json.optJSONArray("npcs")
            if (nArr != null) for (i in 0 until nArr.length()) nArr.optJSONObject(i)?.let { npcs.add(NpcState.fromJson(it)) }

            val factions = mutableListOf<Faction>()
            val fArr = json.optJSONArray("factions")
            if (fArr != null) for (i in 0 until fArr.length()) fArr.optJSONObject(i)?.let { factions.add(Faction.fromJson(it)) }

            val quests = mutableListOf<Quest>()
            val qArr = json.optJSONArray("quests")
            if (qArr != null) for (i in 0 until qArr.length()) qArr.optJSONObject(i)?.let { quests.add(Quest.fromJson(it)) }

            val questArcs = mutableListOf<QuestArc>()
            val qaArr = json.optJSONArray("questArcs")
            if (qaArr != null) for (i in 0 until qaArr.length()) qaArr.optJSONObject(i)?.let { questArcs.add(QuestArc.fromJson(it)) }

            val pendingConsequences = mutableListOf<PendingConsequence>()
            val pcArr = json.optJSONArray("pendingConsequences")
            if (pcArr != null) for (i in 0 until pcArr.length()) pcArr.optJSONObject(i)?.let { pendingConsequences.add(PendingConsequence.fromJson(it)) }

            val worldEvents = mutableListOf<WorldEvent>()
            val weArr = json.optJSONArray("worldEvents")
            if (weArr != null) for (i in 0 until weArr.length()) weArr.optJSONObject(i)?.let { worldEvents.add(WorldEvent.fromJson(it)) }

            val rumors = mutableListOf<Rumor>()
            val rArr = json.optJSONArray("rumors")
            if (rArr != null) for (i in 0 until rArr.length()) rArr.optJSONObject(i)?.let { rumors.add(Rumor.fromJson(it)) }

            val secrets = mutableListOf<Secret>()
            val sArr = json.optJSONArray("secrets")
            if (sArr != null) for (i in 0 until sArr.length()) sArr.optJSONObject(i)?.let { secrets.add(Secret.fromJson(it)) }

            val evidence = mutableListOf<Evidence>()
            val evArr = json.optJSONArray("evidence")
            if (evArr != null) for (i in 0 until evArr.length()) evArr.optJSONObject(i)?.let { evidence.add(Evidence.fromJson(it)) }

            val memories = mutableListOf<MemoryEntry>()
            val memArr = json.optJSONArray("memories")
            if (memArr != null) for (i in 0 until memArr.length()) memArr.optJSONObject(i)?.let { memories.add(MemoryEntry.fromJson(it)) }

            val simulationLogs = mutableListOf<SimulationLogEntry>()
            val simArr = json.optJSONArray("simulationLogs")
            if (simArr != null) for (i in 0 until simArr.length()) simArr.optJSONObject(i)?.let { simulationLogs.add(SimulationLogEntry.fromJson(it)) }

            return WorldState(
                worldId = json.optString("worldId", "world_default"),
                worldName = json.optString("worldName", "Eldoria: The Shattered Realm"),
                worldDescription = json.optString("worldDescription", ""),
                schemaVersion = CURRENT_SCHEMA_VERSION,
                player = player,
                time = time,
                locations = locations,
                npcs = npcs,
                factions = factions,
                quests = quests,
                questArcs = questArcs,
                pendingConsequences = pendingConsequences,
                worldEvents = worldEvents,
                rumors = rumors,
                secrets = secrets,
                evidence = evidence,
                memories = memories,
                simulationLogs = simulationLogs
            )
        }

        fun fromJsonString(jsonStr: String): WorldState {
            return fromJson(JSONObject(jsonStr))
        }
    }
}
