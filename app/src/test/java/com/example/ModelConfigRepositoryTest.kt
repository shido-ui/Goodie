package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.ProviderConfig
import com.example.data.repository.ModelConfigRepository
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ModelConfigRepositoryTest {

    @Test
    fun `save model configuration persists and is immediately accessible`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ModelConfigRepository.getInstance(context)

        val customConfig = ProviderConfig(
            providerId = "gemini",
            endpointUrl = "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions",
            modelId = "gemini-2.5-pro",
            temperature = 0.7f,
            maxOutputTokens = 1500
        )

        // 1. Save config and key
        repo.saveConfig(customConfig)
        repo.saveApiKey("gemini", "dummy_test_key_12345")

        // 2. Active config must match exactly
        val active = repo.getActiveConfig()
        assertEquals("gemini", active.providerId)
        assertEquals("gemini-2.5-pro", active.modelId)
        assertEquals(0.7f, active.temperature, 0.01f)
        assertEquals(1500, active.maxOutputTokens)

        // 3. Ready for chat check must be true
        assertTrue(repo.isReadyForChat())
    }
}
