package com.example.engine

import com.example.data.model.*

object PresetWorlds {

    fun createEldoria(): WorldState {
        val locations = listOf(
            LocationInfo(
                id = "loc_oakvale",
                name = "Oakvale Tavern",
                region = "Heartlands",
                description = "A warm, timber-framed tavern with a roaring hearth, smelling of roasted boar and spiced mead.",
                dangerLevel = 1,
                connectedLocationIds = listOf("loc_silverwood", "loc_eldoria_market")
            ),
            LocationInfo(
                id = "loc_eldoria_market",
                name = "Eldoria Grand Market",
                region = "Crown City",
                description = "A bustling plaza lined with silk canopies, alchemical stalls, and armorers hawking their wares.",
                dangerLevel = 1,
                connectedLocationIds = listOf("loc_oakvale", "loc_silverwood", "loc_ruins")
            ),
            LocationInfo(
                id = "loc_silverwood",
                name = "Silverwood Forest",
                region = "Outer Reaches",
                description = "An ancient mist-shrouded forest of glowing luminescent flora, sacred to the Moonweavers.",
                dangerLevel = 3,
                connectedLocationIds = listOf("loc_oakvale", "loc_eldoria_market", "loc_ruins")
            ),
            LocationInfo(
                id = "loc_ruins",
                name = "Cataclysm Ruins",
                region = "The Scar",
                description = "Cracked obsidian pillars and fractured ley-line portals pulsing with unstable raw magic.",
                dangerLevel = 4,
                connectedLocationIds = listOf("loc_silverwood", "loc_eldoria_market")
            )
        )

        val factions = listOf(
            Faction(
                id = "fac_crown_guard",
                name = "The Silver Crown Guard",
                description = "The lawful defenders and royal order of Eldoria, maintaining peace and trade routes.",
                influenceScore = 80,
                headquartersLocationId = "loc_eldoria_market"
            ),
            Faction(
                id = "fac_moonweavers",
                name = "Moonweaver Enclave",
                description = "A mysterious circle of elven druids and mages guarding the ancient ley lines from corruption.",
                influenceScore = 65,
                headquartersLocationId = "loc_silverwood"
            ),
            Faction(
                id = "fac_shadow_syndicate",
                name = "The Obsidian Syndicate",
                description = "An underground network of smugglers, information brokers, and relic hunters.",
                influenceScore = 50,
                headquartersLocationId = "loc_oakvale"
            )
        )

        val npcs = listOf(
            NpcState(
                id = "npc_elena",
                name = "Elena Brightwood",
                title = "Innkeeper & Rumor Broker",
                personality = "Shrewd, hospitable, observant, discreet",
                factionId = "fac_shadow_syndicate",
                currentLocation = "Oakvale Tavern",
                currentActivity = "Cleaning tankards and greeting patrons",
                money = 120,
                relationships = NpcRelationships(trust = 55, suspicion = 20, friendship = 10),
                schedule = listOf(
                    NpcScheduleItem(6, 12, "loc_oakvale", "Opening tavern & prep"),
                    NpcScheduleItem(12, 23, "loc_oakvale", "Serving patrons & listening to rumors")
                ),
                goals = listOf(
                    NpcGoal("g_elena_1", "Maintain the tavern safe from bandits", priority = 1),
                    NpcGoal("g_elena_2", "Acquire information on the missing royal caravan", priority = 2)
                ),
                memories = listOf(
                    NpcMemory("m_elena_init", "The road bandits have grown bolder near Silverwood.", importance = 6)
                ),
                knownRumors = listOf("A mysterious purple glow was seen rising from the Cataclysm Ruins last night.")
            ),
            NpcState(
                id = "npc_valerius",
                name = "Commander Valerius",
                title = "Captain of the Crown Guard",
                personality = "Stern, honorable, vigilant, dutiful",
                factionId = "fac_crown_guard",
                currentLocation = "Eldoria Grand Market",
                currentActivity = "Inspecting sentry posts",
                money = 250,
                relationships = NpcRelationships(trust = 45, suspicion = 30, respect = 60),
                schedule = listOf(
                    NpcScheduleItem(6, 14, "loc_eldoria_market", "Guarding the market gate"),
                    NpcScheduleItem(14, 20, "loc_oakvale", "Meeting informants & resting")
                ),
                goals = listOf(
                    NpcGoal("g_val_1", "Recover the stolen Sunshard relic", priority = 1, relatedFactionId = "fac_crown_guard")
                ),
                knownRumors = listOf("Someone inside the merchant council is leaking guard patrol schedules.")
            ),
            NpcState(
                id = "npc_lyra",
                name = "Lyra Nightshade",
                title = "Enclave Seeker",
                personality = "Enigmatic, fiercely intelligent, cautious",
                factionId = "fac_moonweavers",
                currentLocation = "Silverwood Forest",
                currentActivity = "Harvesting celestial moss",
                money = 80,
                relationships = NpcRelationships(trust = 40, suspicion = 40, friendship = 0),
                schedule = listOf(
                    NpcScheduleItem(0, 10, "loc_silverwood", "Meditating at the Moon Well"),
                    NpcScheduleItem(10, 18, "loc_oakvale", "Exchanging herbs with Elena")
                ),
                goals = listOf(
                    NpcGoal("g_lyra_1", "Seal the dimensional rift in the Cataclysm Ruins", priority = 1)
                )
            )
        )

        val quests = listOf(
            Quest(
                id = "q_sunshard",
                title = "The Stolen Sunshard",
                description = "Commander Valerius needs an independent adventurer to investigate the theft of the Sunshard amulet from the Crown armory.",
                status = QuestStatus.ACTIVE,
                giverNpcId = "npc_valerius",
                relatedLocationId = "loc_eldoria_market",
                objectives = listOf(
                    QuestObjective("obj_1", "Speak with Elena at the Oakvale Tavern about suspicious buyers", isCompleted = false),
                    QuestObjective("obj_2", "Search the Silverwood perimeter for the thief's hideout", isCompleted = false),
                    QuestObjective("obj_3", "Recover the Sunshard and return it to Valerius", isCompleted = false)
                ),
                reward = QuestReward(
                    money = 150,
                    itemRewards = listOf(
                        InventoryItem("item_sun_ring", "Sunforged Ring", "Grants resistance to dark magic and slight luminescence in dungeons.", 1, 100, "ARMOR")
                    ),
                    factionReputationChanges = mapOf("fac_crown_guard" to 25)
                )
            )
        )

        val rumors = listOf(
            Rumor(
                id = "rumor_1",
                text = "The Sunshard wasn't taken by bandits—it was stolen by someone wearing Crown Guard insignia.",
                isTrue = true,
                confidence = 85,
                knownByNpcIds = listOf("npc_elena")
            )
        )

        val secrets = listOf(
            Secret(
                id = "sec_1",
                title = "Crown Guard Traitor",
                content = "Lieutenant Kael has secretly allied with the Obsidian Syndicate to sell enchanted artifacts.",
                importance = 8,
                knownByNpcIds = listOf("npc_elena")
            )
        )

        val starterItems = listOf(
            InventoryItem("item_iron_sword", "Steel Broadsword", "A well-balanced double-edged sword bearing the mark of Eldoria smiths.", 1, 30, "WEAPON", isEquipped = true),
            InventoryItem("item_leather_armor", "Reinforced Leather Armor", "Durable boiled leather with steel studs.", 1, 40, "ARMOR", isEquipped = true),
            InventoryItem("item_health_pot", "Healing Draught", "Restores 40 HP instantly when consumed.", 3, 15, "CONSUMABLE")
        )

        return WorldState(
            worldId = "world_eldoria",
            worldName = "Eldoria: The Shattered Realm",
            worldDescription = "A rich high-fantasy world of arcane mysteries, kingdoms, and legendary beasts.",
            player = PlayerState(
                name = "Kaelen",
                title = "Freelance Blade",
                hp = 100,
                maxHp = 100,
                money = 75,
                location = "Oakvale Tavern",
                attributes = PlayerAttributes(14, 12, 13, 11, 10, 12),
                inventory = starterItems,
                factionReputations = mapOf("fac_crown_guard" to 10, "fac_moonweavers" to 0, "fac_shadow_syndicate" to 5)
            ),
            time = WorldTime(day = 1, hour = 8, minute = 30),
            locations = locations,
            npcs = npcs,
            factions = factions,
            quests = quests,
            rumors = rumors,
            secrets = secrets
        )
    }

    fun createCyberpunk(): WorldState {
        val locations = listOf(
            LocationInfo("loc_neon_noodle", "Noodle Bar 'Neon 99'", "Lower Docks", "A rain-slicked cyber ramen stall bathed in holographic neon advertisements.", dangerLevel = 1, connectedLocationIds = listOf("loc_corpo_plaza", "loc_black_market")),
            LocationInfo("loc_corpo_plaza", "Arasaka Spire Plaza", "Upper District", "Gleaming monorails, armed corporate security drones, and chrome skyscrapers.", dangerLevel = 2, connectedLocationIds = listOf("loc_neon_noodle", "loc_black_market")),
            LocationInfo("loc_black_market", "Undercity Black Market", "Sub-Level 4", "Illegal cyberware ripperdocs, shadow netrunners, and contraband tech.", dangerLevel = 4, connectedLocationIds = listOf("loc_neon_noodle", "loc_corpo_plaza"))
        )

        val factions = listOf(
            Faction("fac_megacorp", "Vertex Dynamics", "Omnipresent megacorporation controlling the city's power grid and surveillance.", influenceScore = 95),
            Faction("fac_glitch_runners", "The Glitch Syndicate", "Underground hacker collective fighting for free data and decentralized cyberware.", influenceScore = 60)
        )

        val npcs = listOf(
            NpcState(
                id = "npc_nyx",
                name = "Nyx Vane",
                title = "Elite Netrunner",
                personality = "Cynical, fast-talking, brilliant, loyal to friends",
                factionId = "fac_glitch_runners",
                currentLocation = "Noodle Bar 'Neon 99'",
                currentActivity = "Sipping synth-brew while monitoring encrypted ICE feeds",
                money = 400,
                relationships = NpcRelationships(trust = 60, suspicion = 25, friendship = 20)
            )
        )

        val items = listOf(
            InventoryItem("item_smart_pistol", "Tsunami Smart Pistol", "Auto-targeting sidearm with micro-homing bullets.", 1, 200, "WEAPON", isEquipped = true),
            InventoryItem("item_cyber_deck", "Militech Cyberdeck mk.II", "Neural deck loaded with breach protocols and ping hacks.", 1, 500, "MISC", isEquipped = true),
            InventoryItem("item_stim_pack", "Neuro-Stim Hypospray", "Rapidly cleanses neural feedback and restores 50 HP.", 2, 50, "CONSUMABLE")
        )

        return WorldState(
            worldId = "world_cyberpunk",
            worldName = "Neo-Veridia 2088",
            worldDescription = "A dystopian cyberpunk metropolis ruled by megacorps and cybernetic mercenaries.",
            player = PlayerState(
                name = "Viper",
                title = "Street Mercenary",
                hp = 100,
                maxHp = 100,
                money = 250,
                location = "Noodle Bar 'Neon 99'",
                attributes = PlayerAttributes(11, 15, 12, 14, 10, 11),
                inventory = items,
                factionReputations = mapOf("fac_megacorp" to -20, "fac_glitch_runners" to 30)
            ),
            time = WorldTime(day = 1, hour = 22, minute = 15),
            locations = locations,
            npcs = npcs,
            factions = factions
        )
    }

    fun createGothic(): WorldState {
        val locations = listOf(
            LocationInfo("loc_manor", "Blackwood Manor", "Misty Moors", "A crumbling Victorian manor with candlelit halls and whispering portraits.", dangerLevel = 2, connectedLocationIds = listOf("loc_graveyard", "loc_village")),
            LocationInfo("loc_graveyard", "St. Jude's Crypts", "Hallowed Ground", "Fog-choked mausoleums and broken iron gates overlooking the valley.", dangerLevel = 4, connectedLocationIds = listOf("loc_manor", "loc_village")),
            LocationInfo("loc_village", "Oakhaven Hamlet", "Valley", "Desolate cobblestone village where townsfolk bar their doors at dusk.", dangerLevel = 1, connectedLocationIds = listOf("loc_manor", "loc_graveyard"))
        )

        val factions = listOf(
            Faction("fac_inquisition", "The Silver Inquisitors", "Holy zealots hunting supernatural creatures with silver and fire.", influenceScore = 75),
            Faction("fac_bloodcourt", "The Sanguine Coven", "Ancient vampires controlling the nobility from the shadows.", influenceScore = 80)
        )

        val npcs = listOf(
            NpcState(
                id = "npc_victoria",
                name = "Lady Victoria Blackwood",
                title = "Mistress of the Manor",
                personality = "Aristocratic, melancholic, harboring dark secrets",
                factionId = "fac_bloodcourt",
                currentLocation = "Blackwood Manor",
                currentActivity = "Gazing out the arched window at the stormy moors",
                money = 600,
                relationships = NpcRelationships(trust = 40, suspicion = 50, respect = 70)
            )
        )

        val items = listOf(
            InventoryItem("item_silver_cane", "Silver-Headed Sword Cane", "Concealed dueling blade forged from pure refined silver.", 1, 80, "WEAPON", isEquipped = true),
            InventoryItem("item_holy_water", "Vial of Blessed Water", "Burns undead and evil entities on contact.", 2, 25, "CONSUMABLE"),
            InventoryItem("item_pocket_watch", "Gilded Pocket Watch", "An heirloom ticking in reverse under supernatural presence.", 1, 50, "MISC")
        )

        return WorldState(
            worldId = "world_gothic",
            worldName = "Ravenloft: Shadow of the Moon",
            worldDescription = "A dark gothic horror realm of vampires, occult curses, and relentless fog.",
            player = PlayerState(
                name = "Gabriel",
                title = "Occult Investigator",
                hp = 100,
                maxHp = 100,
                money = 40,
                location = "Blackwood Manor",
                attributes = PlayerAttributes(10, 12, 12, 15, 14, 11),
                inventory = items,
                factionReputations = mapOf("fac_inquisition" to 15, "fac_bloodcourt" to -10)
            ),
            time = WorldTime(day = 1, hour = 23, minute = 45),
            locations = locations,
            npcs = npcs,
            factions = factions
        )
    }
}
