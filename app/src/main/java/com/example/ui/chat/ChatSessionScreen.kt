package com.example.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.ChatMessageEntity
import com.example.ui.components.ModelWarningBanner
import com.example.ui.components.RpgHudHeader
import com.example.ui.components.RpgTopAppBar
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import org.json.JSONArray

@Composable
fun ChatSessionScreen(
    sessionId: String,
    onNavigateBack: () -> Unit,
    onNavigateToCharacterSheet: (String) -> Unit,
    onNavigateToDebug: (String) -> Unit,
    onNavigateToCodex: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ChatSessionViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            RpgTopAppBar(
                title = uiState.session?.title ?: "RPG Adventure",
                subtitle = uiState.worldState.worldName,
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = { onNavigateToCharacterSheet(sessionId) },
                        modifier = Modifier.testTag("char_sheet_nav_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Character Sheet",
                            tint = RpgGoldLight
                        )
                    }
                    IconButton(
                        onClick = { onNavigateToCodex(sessionId) },
                        modifier = Modifier.testTag("codex_nav_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "World Codex",
                            tint = RpgManaCyan
                        )
                    }
                    IconButton(
                        onClick = { onNavigateToDebug(sessionId) },
                        modifier = Modifier.testTag("debug_nav_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = "World Debug",
                            tint = RpgArcaneSecondary
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_nav_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "AI Settings",
                            tint = RpgTextSecondary
                        )
                    }
                }
            )
        },
        containerColor = RpgDarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Live RPG HUD
            RpgHudHeader(
                player = uiState.worldState.player,
                time = uiState.worldState.time,
                modelName = uiState.activeModelName,
                providerId = uiState.activeProviderId,
                isModelConfigured = uiState.isModelConfigured,
                onModelClick = onNavigateToSettings
            )

            // Warning Banner if unconfigured
            if (!uiState.isModelConfigured) {
                ModelWarningBanner(onConfigureClick = onNavigateToSettings)
            }

            // Error snackbar if generation failed
            if (uiState.lastError != null) {
                Surface(
                    color = RpgHealthRed.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RpgHealthRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = RpgHealthRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.lastError ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(color = RpgHealthRed),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = RpgHealthRed)
                        }
                    }
                }
            }

            // Message Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.messages, key = { it.id }) { msg ->
                    ChatMessageCard(message = msg)
                }

                if (uiState.isGenerating) {
                    item {
                        GeneratingIndicator(modelName = uiState.activeModelName)
                    }
                }
            }

            // Suggested Action Chips
            if (uiState.suggestedActions.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.suggestedActions) { action ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = RpgDarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, RpgBorderColor),
                            modifier = Modifier.clickable {
                                inputText = action
                            }
                        ) {
                            Text(
                                text = action,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = RpgGoldLight,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Input Bar & Action Triggers
            Surface(
                color = RpgDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, RpgBorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Manual Pulse / Time Advance button (+30 min)
                    IconButton(
                        onClick = { viewModel.triggerManualPulse(30) },
                        modifier = Modifier.testTag("quick_pulse_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassBottom,
                            contentDescription = "Wait 30m",
                            tint = RpgArcaneSecondary
                        )
                    }

                    // Dice Roll shortcut
                    IconButton(
                        onClick = {
                            val roll = (1..20).random()
                            inputText = "I roll a d20: $roll. I attempt to "
                        },
                        modifier = Modifier.testTag("dice_roll_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = "Roll d20",
                            tint = RpgGoldPrimary
                        )
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        placeholder = {
                            Text(
                                "Describe your action, speech, or exploration...",
                                fontSize = 13.sp,
                                color = RpgTextMuted
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RpgGoldPrimary,
                            unfocusedBorderColor = RpgBorderColor,
                            focusedTextColor = RpgTextPrimary,
                            unfocusedTextColor = RpgTextPrimary
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !uiState.isGenerating) {
                                val text = inputText
                                inputText = ""
                                viewModel.sendPlayerAction(text)
                            }
                        },
                        enabled = inputText.isNotBlank() && !uiState.isGenerating,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank()) RpgGoldPrimary else RpgDarkSurfaceHighlight)
                            .testTag("send_action_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank()) RpgDarkBackground else RpgTextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageCard(message: ChatMessageEntity) {
    val isPlayer = message.sender == "PLAYER"
    val isNpc = message.sender == "NPC"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isPlayer) Alignment.End else Alignment.Start
    ) {
        // Speaker tag / Timestamp
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (isPlayer) "You (${message.speakerName ?: "Hero"})" else (message.speakerName ?: "Game Master"),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isPlayer) RpgManaCyan else if (isNpc) RpgArcaneSecondary else RpgGoldPrimary
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = message.worldTimeFormatted,
                style = MaterialTheme.typography.labelSmall.copy(color = RpgTextMuted)
            )
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isPlayer) 14.dp else 2.dp,
                bottomEnd = if (isPlayer) 2.dp else 14.dp
            ),
            color = if (isPlayer) RpgDarkSurfaceHighlight else RpgDarkSurface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isPlayer) RpgManaCyan.copy(alpha = 0.3f) else RpgBorderColor
            ),
            modifier = Modifier.fillMaxWidth(if (isPlayer) 0.85f else 1f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Dialogue section if present
                if (!message.dialogue.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = RpgArcaneDark.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RpgArcaneSecondary.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "\"${message.dialogue}\"",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = RpgArcaneLight,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            ),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Main narrative text
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = RpgTextPrimary,
                        lineHeight = 20.sp
                    )
                )

                // Applied state proposals summary badges
                if (!message.appliedProposalsJson.isNullOrBlank()) {
                    val proposals = try {
                        val arr = JSONArray(message.appliedProposalsJson)
                        val list = mutableListOf<String>()
                        for (i in 0 until arr.length()) list.add(arr.getString(i))
                        list
                    } catch (e: Exception) {
                        emptyList()
                    }

                    if (proposals.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            proposals.forEach { prop ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = RpgDarkBackground,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, RpgGoldPrimary.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = RpgStaminaGreen,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = prop,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = RpgGoldLight,
                                                fontSize = 11.sp
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
    }
}

@Composable
fun GeneratingIndicator(modelName: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = RpgDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, RpgBorderColor),
        modifier = Modifier.fillMaxWidth(0.7f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = RpgGoldPrimary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Simulating living world ($modelName)...",
                style = MaterialTheme.typography.bodySmall.copy(color = RpgTextSecondary)
            )
        }
    }
}
