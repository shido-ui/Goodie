package com.example.ui.codex

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
import com.example.data.repository.WorldStateRepository
import com.example.ui.components.RpgTopAppBar
import com.example.ui.modes.SectionHeader
import com.example.ui.theme.*

@Composable
fun CodexScreen(
    sessionId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val worldRepo = remember { WorldStateRepository(db) }
    val worldState by worldRepo.getWorldStateFlow(sessionId).collectAsState(initial = null)

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Rumors", "Secrets", "Quests", "Locations")

    Scaffold(
        topBar = {
            RpgTopAppBar(
                title = "World Codex & Intel",
                subtitle = worldState?.worldName ?: "Eldoria",
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
            val world = worldState!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = RpgDarkSurface,
                    contentColor = RpgGoldPrimary
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            // Rumors tab
                            if (world.rumors.isEmpty()) {
                                item {
                                    Text("No rumors circulating yet.", style = MaterialTheme.typography.bodySmall.copy(color = RpgTextMuted))
                                }
                            } else {
                                items(world.rumors) { rumor ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = RpgDarkSurface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, RpgBorderColor),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Tavern Gossip",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = RpgGoldPrimary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                                Text(
                                                    text = "Spread: ${rumor.spreadCount}x",
                                                    style = MaterialTheme.typography.labelSmall.copy(color = RpgTextMuted)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "\"${rumor.text}\"",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = RpgTextPrimary,
                                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            // Secrets tab
                            if (world.secrets.isEmpty()) {
                                item {
                                    Text("No secrets discovered.", style = MaterialTheme.typography.bodySmall.copy(color = RpgTextMuted))
                                }
                            } else {
                                items(world.secrets) { secret ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = RpgDarkSurface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, RpgArcaneSecondary.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = secret.title,
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = RpgArcaneLight
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = secret.content,
                                                style = MaterialTheme.typography.bodySmall.copy(color = RpgTextPrimary)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            // Quests tab
                            if (world.quests.isEmpty()) {
                                item {
                                    Text("No active quests in journal.", style = MaterialTheme.typography.bodySmall.copy(color = RpgTextMuted))
                                }
                            } else {
                                items(world.quests) { quest ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = RpgDarkSurface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, RpgBorderColor),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = quest.title,
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = RpgGoldPrimary
                                                    )
                                                )
                                                Text(
                                                    text = quest.status.name,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = RpgManaCyan
                                                    )
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = quest.description,
                                                style = MaterialTheme.typography.bodySmall.copy(color = RpgTextSecondary)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            quest.objectives.forEach { obj ->
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = if (obj.isCompleted) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                                        contentDescription = null,
                                                        tint = if (obj.isCompleted) RpgStaminaGreen else RpgTextMuted,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = obj.description,
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = if (obj.isCompleted) RpgTextMuted else RpgTextPrimary
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        3 -> {
                            // Locations tab
                            items(world.locations) { loc ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = RpgDarkSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, RpgBorderColor),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "${loc.name} (${loc.region})",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = RpgTextPrimary
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = loc.description,
                                            style = MaterialTheme.typography.bodySmall.copy(color = RpgTextSecondary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
