package com.example.ui.charactersheet

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.WorldStateRepository
import com.example.ui.components.RpgTopAppBar
import com.example.ui.modes.SectionHeader
import com.example.ui.theme.*

@Composable
fun CharacterSheetScreen(
    sessionId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val worldRepo = remember { WorldStateRepository(db) }
    val worldState by worldRepo.getWorldStateFlow(sessionId).collectAsState(initial = null)

    Scaffold(
        topBar = {
            RpgTopAppBar(
                title = "Character & Inventory",
                subtitle = worldState?.player?.name ?: "Hero Sheet",
                onBackClick = onNavigateBack
            )
        },
        containerColor = RpgDarkBackground
    ) { innerPadding ->
        if (worldState == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = RpgGoldPrimary)
            }
        } else {
            val player = worldState!!.player
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
            ) {
                // 1. Hero Overview Header
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = RpgDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, RpgBorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = player.name,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = RpgGoldPrimary
                                        )
                                    )
                                    Text(
                                        text = player.title,
                                        style = MaterialTheme.typography.bodySmall.copy(color = RpgTextSecondary)
                                    )
                                }
                                Surface(
                                    color = RpgDarkSurfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, RpgGoldPrimary.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "${player.money} Gold",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = RpgGoldLight
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // HP Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Hit Points (HP)",
                                    style = MaterialTheme.typography.labelSmall.copy(color = RpgTextSecondary)
                                )
                                Text(
                                    text = "${player.hp} / ${player.maxHp}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = RpgHealthRed
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (player.hp.toFloat() / player.maxHp.toFloat()).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = RpgHealthRed,
                                trackColor = RpgDarkSurfaceHighlight
                            )
                        }
                    }
                }

                // 2. Core Attributes Matrix
                item {
                    SectionHeader(title = "CORE ATTRIBUTES")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AttributeTile("STR", player.attributes.strength, Modifier.weight(1f))
                        AttributeTile("DEX", player.attributes.dexterity, Modifier.weight(1f))
                        AttributeTile("CON", player.attributes.constitution, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AttributeTile("INT", player.attributes.intelligence, Modifier.weight(1f))
                        AttributeTile("WIS", player.attributes.wisdom, Modifier.weight(1f))
                        AttributeTile("CHA", player.attributes.charisma, Modifier.weight(1f))
                    }
                }

                // 3. Inventory & Gear
                item {
                    SectionHeader(title = "INVENTORY (${player.inventory.size})")
                }

                if (player.inventory.isEmpty()) {
                    item {
                        Text(
                            text = "Your inventory is currently empty.",
                            style = MaterialTheme.typography.bodySmall.copy(color = RpgTextMuted)
                        )
                    }
                } else {
                    items(player.inventory, key = { it.id }) { item ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = RpgDarkSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, RpgBorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = RpgTextPrimary
                                            )
                                        )
                                        if (item.isEquipped) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = RpgGoldPrimary.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "EQUIPPED",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = RpgGoldLight,
                                                        fontSize = 9.sp
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = item.description,
                                        style = MaterialTheme.typography.bodySmall.copy(color = RpgTextSecondary)
                                    )
                                }
                                Text(
                                    text = "x${item.quantity}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = RpgManaCyan
                                    )
                                )
                            }
                        }
                    }
                }

                // 4. Faction Reputations
                item {
                    SectionHeader(title = "FACTION STANDINGS")
                }

                if (worldState!!.factions.isEmpty()) {
                    item {
                        Text("No factions known.", style = MaterialTheme.typography.bodySmall.copy(color = RpgTextMuted))
                    }
                } else {
                    items(worldState!!.factions) { faction ->
                        val standing = player.factionReputations[faction.id] ?: 0
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = RpgDarkSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, RpgBorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = faction.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = RpgTextPrimary
                                        )
                                    )
                                    Text(
                                        text = faction.description,
                                        style = MaterialTheme.typography.bodySmall.copy(color = RpgTextSecondary),
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    text = if (standing >= 0) "+$standing" else "$standing",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (standing >= 0) RpgStaminaGreen else RpgHealthRed
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttributeTile(
    name: String,
    value: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = RpgDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, RpgBorderColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = RpgTextSecondary
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$value",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = RpgGoldLight
                )
            )
        }
    }
}
