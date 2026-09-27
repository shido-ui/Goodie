package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai.ModelCatalog
import com.example.ai.ProviderConfig
import com.example.ui.components.RpgTopAppBar
import com.example.ui.modes.SectionHeader
import com.example.ui.theme.*

@Composable
fun AiSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AiSettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            RpgTopAppBar(
                title = "AI Model Settings",
                subtitle = "Provider, BYOK Keys & Model Parameters",
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
                        onClick = { viewModel.saveConfiguration() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RpgGoldPrimary,
                            contentColor = RpgDarkBackground
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_ai_settings_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Save & Apply Configuration",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
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
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
            // Success or error feedback alerts
            if (uiState.saveSuccessMessage != null) {
                item {
                    AlertFeedbackCard(
                        isSuccess = true,
                        message = uiState.saveSuccessMessage ?: "",
                        onDismiss = { viewModel.clearMessages() }
                    )
                }
            }

            if (uiState.testResultMessage != null) {
                item {
                    AlertFeedbackCard(
                        isSuccess = uiState.testResultSuccess == true,
                        message = uiState.testResultMessage ?: "",
                        onDismiss = { viewModel.clearMessages() }
                    )
                }
            }

            // 1. AI Provider Selector
            item {
                SectionHeader(title = "1. AI PROVIDER")
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProviderChoiceChip(
                        title = "Google Gemini",
                        isSelected = uiState.providerId == ProviderConfig.PROVIDER_GEMINI,
                        onClick = { viewModel.selectProvider(ProviderConfig.PROVIDER_GEMINI) },
                        modifier = Modifier.weight(1f)
                    )
                    ProviderChoiceChip(
                        title = "OpenAI",
                        isSelected = uiState.providerId == ProviderConfig.PROVIDER_OPENAI,
                        onClick = { viewModel.selectProvider(ProviderConfig.PROVIDER_OPENAI) },
                        modifier = Modifier.weight(1f)
                    )
                    ProviderChoiceChip(
                        title = "Custom / Ollama",
                        isSelected = uiState.providerId == ProviderConfig.PROVIDER_CUSTOM,
                        onClick = { viewModel.selectProvider(ProviderConfig.PROVIDER_CUSTOM) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. HTTPS Endpoint
            item {
                SectionHeader(title = "2. HTTPS ENDPOINT URL")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.endpointUrl,
                    onValueChange = { viewModel.updateEndpoint(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("endpoint_url_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RpgGoldPrimary,
                        unfocusedBorderColor = RpgBorderColor,
                        focusedTextColor = RpgTextPrimary,
                        unfocusedTextColor = RpgTextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    label = { Text("API Endpoint", color = RpgTextSecondary) }
                )
            }

            // 3. API Key Management (Secure Android Keystore)
            item {
                SectionHeader(title = "3. API KEY (BYOK SECURE VAULT)")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Encrypted in Android Keystore. Never stored in plaintext, never committed to Git.",
                    style = MaterialTheme.typography.bodySmall.copy(color = RpgTextMuted)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Current key status
                Surface(
                    color = RpgDarkSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RpgBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Current Key: ${uiState.maskedApiKey}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (uiState.hasKeyConfigured) RpgStaminaGreen else RpgHealthRed
                                )
                            )
                        }
                        if (uiState.hasKeyConfigured) {
                            TextButton(onClick = { viewModel.clearApiKey() }) {
                                Text("Clear Key", color = RpgHealthRed, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.apiKeyInput,
                    onValueChange = { viewModel.updateApiKeyInput(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input"),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide key" else "Show key",
                                tint = RpgTextSecondary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RpgGoldPrimary,
                        unfocusedBorderColor = RpgBorderColor,
                        focusedTextColor = RpgTextPrimary,
                        unfocusedTextColor = RpgTextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    label = { Text("Enter New API Key", color = RpgTextSecondary) }
                )
            }

            // 4. Model Catalog Selection
            item {
                SectionHeader(title = "4. SELECT MODEL")
                Spacer(modifier = Modifier.height(8.dp))

                val availableModels = when (uiState.providerId.lowercase()) {
                    ProviderConfig.PROVIDER_GEMINI -> ModelCatalog.GEMINI_MODELS
                    ProviderConfig.PROVIDER_OPENAI -> ModelCatalog.OPENAI_MODELS
                    else -> ModelCatalog.CUSTOM_MODELS
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableModels.forEach { model ->
                        val isSelected = uiState.modelId == model.modelId
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) RpgDarkSurfaceVariant else RpgDarkSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) RpgGoldPrimary else RpgBorderColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectModel(model.modelId) }
                                .testTag("model_option_${model.modelId}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.selectModel(model.modelId) },
                                    colors = RadioButtonDefaults.colors(selectedColor = RpgGoldPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = model.displayName,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = RpgTextPrimary
                                        )
                                    )
                                    Text(
                                        text = model.description,
                                        style = MaterialTheme.typography.bodySmall.copy(color = RpgTextMuted)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom Model Override field
                OutlinedTextField(
                    value = uiState.customModelInput,
                    onValueChange = { viewModel.updateCustomModel(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_model_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RpgGoldPrimary,
                        unfocusedBorderColor = RpgBorderColor,
                        focusedTextColor = RpgTextPrimary,
                        unfocusedTextColor = RpgTextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    label = { Text("Model ID Override", color = RpgTextSecondary) }
                )
            }

            // 5. Test Connection Button
            item {
                OutlinedButton(
                    onClick = { viewModel.testConnection() },
                    enabled = !uiState.isTestingConnection,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("test_connection_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RpgManaCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RpgManaCyan)
                ) {
                    if (uiState.isTestingConnection) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = RpgManaCyan,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Test AI Connection Ping", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 6. Generation Parameters (Temperature, Tokens)
            item {
                SectionHeader(title = "5. GENERATION PARAMETERS")
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Temperature: ${String.format("%.2f", uiState.temperature)} (Creativity vs Determinism)",
                    style = MaterialTheme.typography.bodySmall.copy(color = RpgTextSecondary)
                )
                Slider(
                    value = uiState.temperature,
                    onValueChange = { viewModel.updateTemperature(it) },
                    valueRange = 0.0f..1.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = RpgGoldPrimary,
                        activeTrackColor = RpgGoldPrimary,
                        inactiveTrackColor = RpgDarkSurfaceHighlight
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Max Tokens: ${uiState.maxTokens}",
                    style = MaterialTheme.typography.bodySmall.copy(color = RpgTextSecondary)
                )
                Slider(
                    value = uiState.maxTokens.toFloat(),
                    onValueChange = { viewModel.updateMaxTokens(it.toInt()) },
                    valueRange = 512f..4096f,
                    steps = 7,
                    colors = SliderDefaults.colors(
                        thumbColor = RpgArcaneSecondary,
                        activeTrackColor = RpgArcaneSecondary,
                        inactiveTrackColor = RpgDarkSurfaceHighlight
                    )
                )
            }
        }
    }
}

@Composable
fun ProviderChoiceChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) RpgDarkSurfaceVariant else RpgDarkSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isSelected) RpgGoldPrimary else RpgBorderColor
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) RpgGoldLight else RpgTextSecondary
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun AlertFeedbackCard(
    isSuccess: Boolean,
    message: String,
    onDismiss: () -> Unit
) {
    val color = if (isSuccess) RpgStaminaGreen else RpgHealthRed
    Surface(
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = color
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall.copy(color = color),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = color)
            }
        }
    }
}
