package com.example.ui.modes

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.GameMode
import com.example.ui.components.RpgTopAppBar
import com.example.ui.theme.*

@Composable
fun ModeSelectionScreen(
    onNavigateBack: () -> Unit,
    onSessionCreated: (String) -> Unit,
    viewModel: ModeSelectionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            RpgTopAppBar(
                title = "New Adventure",
                subtitle = "Choose Mode, World & Companion",
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            Surface(
                color = RpgDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, RpgBorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = { viewModel.createAndStartSession(onSessionCreated) },
                        enabled = !uiState.isCreating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RpgGoldPrimary,
                            contentColor = RpgDarkBackground
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("start_adventure_button")
                    ) {
                        if (uiState.isCreating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = RpgDarkBackground,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Begin Campaign",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = RpgDarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
            // 1. Game Mode Selector
            item {
                SectionHeader(title = "1. CHOOSE GAMEPLAY MODE")
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GameMode.values().forEach { mode ->
                        val isSelected = uiState.selectedMode == mode
                        SelectableCard(
                            title = mode.displayName,
                            description = mode.description,
                            isSelected = isSelected,
                            accentColor = when (mode) {
                                GameMode.OPEN_WORLD -> RpgGoldPrimary
                                GameMode.CHARACTER_CHAT -> RpgArcaneSecondary
                                GameMode.OPEN_WORLD_AND_CHARACTER -> RpgManaCyan
                            },
                            onClick = { viewModel.selectMode(mode) }
                        )
                    }
                }
            }

            // 2. World Selection
            item {
                SectionHeader(title = "2. CHOOSE REALM / WORLD")
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PresetWorldType.values().forEach { preset ->
                        val isSelected = uiState.selectedWorldPreset == preset
                        SelectableCard(
                            title = preset.title,
                            description = preset.subtitle,
                            isSelected = isSelected,
                            accentColor = RpgGoldLight,
                            onClick = { viewModel.updateWorldPreset(preset) }
                        )
                    }
                }
            }

            // 3. Companion Selection (if Character Chat or Open World + Character)
            if (uiState.selectedMode != GameMode.OPEN_WORLD && uiState.availableNpcs.isNotEmpty()) {
                item {
                    SectionHeader(title = "3. CHOOSE CHARACTER / COMPANION")
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.availableNpcs.forEach { npc ->
                            val isSelected = uiState.selectedCompanionNpcId == npc.id
                            SelectableCard(
                                title = "${npc.name} • ${npc.title}",
                                description = "Personality: ${npc.personality}\nLocation: ${npc.currentLocation}",
                                isSelected = isSelected,
                                accentColor = RpgArcaneSecondary,
                                onClick = { viewModel.selectCompanion(npc.id) }
                            )
                        }
                    }
                }
            }

            // 4. Custom Campaign Title
            item {
                SectionHeader(title = "4. CAMPAIGN TITLE")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.customCampaignTitle,
                    onValueChange = { viewModel.updateCampaignTitle(it) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RpgGoldPrimary,
                        unfocusedBorderColor = RpgBorderColor,
                        focusedTextColor = RpgTextPrimary,
                        unfocusedTextColor = RpgTextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    label = { Text("Campaign Name", color = RpgTextSecondary) }
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            color = RpgGoldPrimary,
            letterSpacing = 1.2.sp
        )
    )
}

@Composable
fun SelectableCard(
    title: String,
    description: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) RpgDarkSurfaceVariant else RpgDarkSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isSelected) accentColor else RpgBorderColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = accentColor,
                    unselectedColor = RpgTextMuted
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) RpgTextPrimary else RpgTextSecondary
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = RpgTextMuted,
                        lineHeight = 16.sp
                    )
                )
            }
        }
    }
}
