package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entities.SessionEntity
import com.example.data.model.GameMode
import com.example.ui.components.ModelWarningBanner
import com.example.ui.components.RpgTopAppBar
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    onNavigateToModeSelection: () -> Unit,
    onNavigateToChatSession: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToBackup: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            RpgTopAppBar(
                title = "RPG AI Hub",
                subtitle = "Living World & Roleplay Platform",
                actions = {
                    IconButton(
                        onClick = onNavigateToBackup,
                        modifier = Modifier.testTag("backup_restore_nav_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Backup,
                            contentDescription = "Backup & Restore",
                            tint = RpgTextSecondary
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("ai_settings_nav_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "AI Settings",
                            tint = RpgGoldPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToModeSelection,
                containerColor = RpgGoldPrimary,
                contentColor = RpgDarkBackground,
                icon = { Icon(Icons.Default.Add, contentDescription = "New Campaign") },
                text = { Text("New Adventure", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("new_adventure_fab")
            )
        },
        containerColor = RpgDarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
        ) {
            // 1. Model Warning if unconfigured
            if (!uiState.isModelConfigured) {
                item {
                    ModelWarningBanner(
                        onConfigureClick = onNavigateToSettings
                    )
                }
            }

            // 2. Hero Banner Card
            item {
                HeroBannerCard(
                    activeModel = uiState.activeModelName,
                    provider = uiState.activeProviderId,
                    onConfigureClick = onNavigateToSettings
                )
            }

            // 3. Quick Game Modes Grid
            item {
                Text(
                    text = "CAMPAIGN MODES",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = RpgGoldPrimary,
                        letterSpacing = 1.2.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModeCard(
                        title = "Open World",
                        icon = Icons.Default.Explore,
                        color = RpgGoldPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToModeSelection() }
                    )
                    ModeCard(
                        title = "Character Chat",
                        icon = Icons.Default.Person,
                        color = RpgArcaneSecondary,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToModeSelection() }
                    )
                    ModeCard(
                        title = "Companion",
                        icon = Icons.Default.Group,
                        color = RpgManaCyan,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToModeSelection() }
                    )
                }
            }

            // 4. Saved RPG Sessions Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SAVED ADVENTURES (${uiState.sessions.size})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = RpgTextSecondary,
                            letterSpacing = 1.2.sp
                        )
                    )
                }
            }

            // 5. Saved Sessions List or Empty State
            if (uiState.sessions.isEmpty()) {
                item {
                    EmptySessionsCard(onCreateClick = onNavigateToModeSelection)
                }
            } else {
                items(uiState.sessions, key = { it.id }) { session ->
                    SessionCard(
                        session = session,
                        onClick = { onNavigateToChatSession(session.id) },
                        onDelete = { viewModel.deleteSession(session.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HeroBannerCard(
    activeModel: String,
    provider: String,
    onConfigureClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = RpgDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, RpgBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            RpgArcaneDark.copy(alpha = 0.35f),
                            Color.Transparent
                        ),
                        radius = 400f
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = RpgGoldPrimary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RpgGoldPrimary.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "LIVING WORLD ENGINE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = RpgGoldLight
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onConfigureClick() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "AI Model",
                            tint = RpgManaCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$activeModel ($provider)",
                            style = MaterialTheme.typography.labelSmall.copy(color = RpgManaCyan)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Forge Your Destiny",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = RpgTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "A deterministic living world simulation powered by AI narration. Dynamic NPC decisions, evolving factions, and persistent consequences.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = RpgTextSecondary,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}

@Composable
fun ModeCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = RpgDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, RpgBorderColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = RpgTextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SessionCard(
    session: SessionEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(session.updatedAt))

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = RpgDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, RpgBorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("session_card_${session.id}")
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val modeColor = when (session.mode) {
                        GameMode.OPEN_WORLD.name -> RpgGoldPrimary
                        GameMode.CHARACTER_CHAT.name -> RpgArcaneSecondary
                        else -> RpgManaCyan
                    }
                    val modeLabel = when (session.mode) {
                        GameMode.OPEN_WORLD.name -> "Open World"
                        GameMode.CHARACTER_CHAT.name -> "Character"
                        else -> "Companion"
                    }
                    Surface(
                        color = modeColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = modeLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = modeColor
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall.copy(color = RpgTextMuted)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = RpgTextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (session.characterName != null) "${session.worldName} • Companion: ${session.characterName}" else session.worldName,
                    style = MaterialTheme.typography.bodySmall.copy(color = RpgTextSecondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Session",
                    tint = RpgTextMuted
                )
            }
        }
    }
}

@Composable
fun EmptySessionsCard(onCreateClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = RpgDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, RpgBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AutoStories,
                contentDescription = null,
                tint = RpgGoldPrimary.copy(alpha = 0.6f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No Active Adventures",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = RpgTextPrimary
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Begin your journey across Eldoria, Neo-Veridia, or create your own custom RPG world.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = RpgTextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onCreateClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RpgGoldPrimary,
                    contentColor = RpgDarkBackground
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Start New Adventure", fontWeight = FontWeight.Bold)
            }
        }
    }
}
