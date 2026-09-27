package com.example.ui.backup

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.RpgTopAppBar
import com.example.ui.modes.SectionHeader
import com.example.ui.settings.AlertFeedbackCard
import com.example.ui.theme.*

@Composable
fun BackupRestoreScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupRestoreViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            RpgTopAppBar(
                title = "Backup & Restore",
                subtitle = "Export / Import Campaign World Saves",
                onBackClick = onNavigateBack
            )
        },
        containerColor = RpgDarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
            // Feedback Card
            if (uiState.resultSuccessMessage != null) {
                item {
                    AlertFeedbackCard(
                        isSuccess = true,
                        message = uiState.resultSuccessMessage ?: "",
                        onDismiss = { viewModel.clearFeedback() }
                    )
                }
            }
            if (uiState.resultErrorMessage != null) {
                item {
                    AlertFeedbackCard(
                        isSuccess = false,
                        message = uiState.resultErrorMessage ?: "",
                        onDismiss = { viewModel.clearFeedback() }
                    )
                }
            }

            // 1. Export Section
            item {
                SectionHeader(title = "1. EXPORT CAMPAIGN SAVE (JSON)")
                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.sessions.isEmpty()) {
                    Text(
                        text = "No saved adventures found to export.",
                        style = MaterialTheme.typography.bodySmall.copy(color = RpgTextMuted)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.sessions.forEach { s ->
                            val isSelected = uiState.selectedExportSessionId == s.id
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) RpgDarkSurfaceVariant else RpgDarkSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    if (isSelected) RpgGoldPrimary else RpgBorderColor
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectExportSession(s.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.selectExportSession(s.id) },
                                        colors = RadioButtonDefaults.colors(selectedColor = RpgGoldPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = s.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = RpgTextPrimary
                                            )
                                        )
                                        Text(
                                            text = "${s.worldName} (${s.mode})",
                                            style = MaterialTheme.typography.bodySmall.copy(color = RpgTextMuted)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = { viewModel.exportSelectedSession() },
                            enabled = !uiState.isProcessing && uiState.selectedExportSessionId != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RpgGoldPrimary,
                                contentColor = RpgDarkBackground
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("export_backup_button")
                        ) {
                            Text("Export Selected Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Exported JSON payload viewer
            if (uiState.exportedJson != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = RpgDarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, RpgBorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "EXPORTED PAYLOAD",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = RpgGoldLight
                                    )
                                )
                                TextButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("RPG Save", uiState.exportedJson)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Save JSON copied to clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text("Copy JSON", color = RpgManaCyan)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.exportedJson ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = RpgTextSecondary
                                ),
                                maxLines = 8
                            )
                        }
                    }
                }
            }

            // 2. Restore Section
            item {
                SectionHeader(title = "2. RESTORE SAVE (VALIDATED IMPORT)")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Requires valid JSON, matching schema version, and passing consistency validation.",
                    style = MaterialTheme.typography.bodySmall.copy(color = RpgTextMuted)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.importInputText,
                    onValueChange = { viewModel.updateImportText(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("import_json_input"),
                    placeholder = {
                        Text(
                            "Paste exported JSON backup payload here...",
                            color = RpgTextMuted,
                            fontSize = 12.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RpgGoldPrimary,
                        unfocusedBorderColor = RpgBorderColor,
                        focusedTextColor = RpgTextPrimary,
                        unfocusedTextColor = RpgTextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.validateAndRestore() },
                    enabled = !uiState.isProcessing && uiState.importInputText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RpgArcaneSecondary,
                        contentColor = RpgDarkBackground
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("restore_backup_button")
                ) {
                    Text("Validate & Restore Campaign", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
