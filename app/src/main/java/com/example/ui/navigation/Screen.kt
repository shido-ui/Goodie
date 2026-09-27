package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object ModeSelection : Screen("mode_selection")
    object AiSettings : Screen("ai_settings")
    object BackupRestore : Screen("backup_restore")

    object ChatSession : Screen("chat_session/{sessionId}") {
        fun createRoute(sessionId: String) = "chat_session/$sessionId"
    }

    object CharacterSheet : Screen("character_sheet/{sessionId}") {
        fun createRoute(sessionId: String) = "character_sheet/$sessionId"
    }

    object WorldDebug : Screen("world_debug/{sessionId}") {
        fun createRoute(sessionId: String) = "world_debug/$sessionId"
    }

    object Codex : Screen("codex/{sessionId}") {
        fun createRoute(sessionId: String) = "codex/$sessionId"
    }
}
