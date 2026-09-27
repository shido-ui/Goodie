package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.BackupRestoreManager
import com.example.data.repository.RestoreResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupRestoreValidatorTest {

    @Test
    fun `malformed json is rejected safely`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        val manager = BackupRestoreManager(db)

        val badJson = "{ invalid_json_syntax: 123 "
        val result = manager.validateAndRestoreBackup(badJson)

        assertTrue(result is RestoreResult.Failure)
        val failure = result as RestoreResult.Failure
        assertTrue(failure.errorReason.contains("Invalid JSON"))
    }

    @Test
    fun `payload missing session object is rejected`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        val manager = BackupRestoreManager(db)

        val missingSessionJson = """
        {
          "appVersion": "RPG_AI_HUB_V2",
          "worldState": {}
        }
        """.trimIndent()
        val result = manager.validateAndRestoreBackup(missingSessionJson)

        assertTrue(result is RestoreResult.Failure)
        val failure = result as RestoreResult.Failure
        assertTrue(failure.errorReason.contains("Missing 'session'"))
    }
}
