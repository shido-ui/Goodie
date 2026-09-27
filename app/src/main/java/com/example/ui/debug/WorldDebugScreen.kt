package com.example.ui.debug

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.RpgTopAppBar
import com.example.ui.modes.SectionHeader
import com.example.ui.theme.*

@Composable
fun WorldDebugScreen(
    sessionId: String,
    onNavigateBack: () -> Unit,
    viewModel: WorldDebugViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            RpgTopAppBar(
                title = "World State Debugger",
                subtitle = "Engine Diagnostics & Telemetry",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = {
                            val report = viewModel.generateDiagnosticReport()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("RPG AI Hub Diagnostic", report)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Diagnostic report copied (Secrets REDACTED)", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("copy_diagnostic_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Report",
                            tint = RpgManaCyan
                        )
                    }
                }
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
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
            // 1. Telemetry & AI Model Panel
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RpgDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, RpgBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "ACTIVE AI CONFIGURATION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = RpgGoldPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        DebugRow("Provider", uiState.providerId)
                        DebugRow("Model ID", uiState.modelId)
                        DebugRow("Endpoint Host", uiState.endpointHost)
                        DebugRow("API Key Stored", if (uiState.hasApiKey) "YES (Encrypted in Keystore)" else "NO")
                        DebugRow("API Secrets", "[AUTOMATICALLY REDACTED]")
                    }
                }
            }

            // 2. Authoritative World Status
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RpgDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, RpgBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "AUTHORITATIVE WORLD STATE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = RpgManaCyan
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val world = uiState.worldState
                        DebugRow("Schema Version", "v${world.schemaVersion}")
                        DebugRow("World Time", world.time.toFormattedString())
                        DebugRow("Elapsed Minutes", "${world.time.toTotalMinutes()} min")
                        DebugRow("Player HP", "${world.player.hp}/${world.player.maxHp}")
                        DebugRow("Player Gold", "${world.player.money}g")
                        DebugRow("Player Location", world.player.location)
                        DebugRow("Total NPCs", "${world.npcs.size}")
                        DebugRow("Active Quests", "${world.quests.size}")
                        DebugRow("Factions", "${world.factions.size}")
                        DebugRow("Pending Consequences", "${world.pendingConsequences.size}")
                    }
                }
            }

            // 3. World Engine Audit & Actions
            item {
                SectionHeader(title = "SIMULATION CONTROLS")
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.triggerDebugPulse(60) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RpgArcaneSecondary,
                            contentColor = RpgDarkBackground
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("debug_pulse_button")
                    ) {
                        Text("+1 Hour Pulse", fontWeight = FontWeight.Bold)
                    }
                }

                if (uiState.lastPulseSummary != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = uiState.lastPulseSummary ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(color = RpgStaminaGreen)
                    )
                }
            }

            // 4. Consistency Audit Status
            item {
                SectionHeader(title = "WORLD CONSISTENCY AUDIT")
                Spacer(modifier = Modifier.height(4.dp))
                if (uiState.auditResults.isEmpty()) {
                    Surface(
                        color = RpgStaminaGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RpgStaminaGreen.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RpgStaminaGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "All authoritative consistency checks PASSED (0 violations).",
                                style = MaterialTheme.typography.bodySmall.copy(color = RpgStaminaGreen)
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        uiState.auditResults.forEach { violation ->
                            Surface(
                                color = RpgHealthRed.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, RpgHealthRed),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = RpgHealthRed)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = violation,
                                        style = MaterialTheme.typography.bodySmall.copy(color = RpgHealthRed)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Recent Simulation Log Feed
            item {
                SectionHeader(title = "SIMULATION LOGS (${uiState.logs.size})")
            }

            if (uiState.logs.isEmpty()) {
                item {
                    Text(
                        text = "No simulation logs recorded yet.",
                        style = MaterialTheme.typography.bodySmall.copy(color = RpgTextMuted)
                    )
                }
            } else {
                items(uiState.logs, key = { it.id }) { log ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = RpgDarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, RpgBorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = log.eventType,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = RpgGoldLight
                                    )
                                )
                                Text(
                                    text = "${log.worldMinutes}m",
                                    style = MaterialTheme.typography.labelSmall.copy(color = RpgTextMuted)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = log.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = RpgTextSecondary,
                                    fontFamily = FontFamily.Monospace,
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

@Composable
fun DebugRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = RpgTextMuted)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = RpgTextPrimary,
                fontFamily = FontFamily.Monospace
            )
        )
    }
}
