package com.example.ui.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.backup.BackupRestoreScreen
import com.example.ui.charactersheet.CharacterSheetScreen
import com.example.ui.chat.ChatSessionScreen
import com.example.ui.chat.ChatSessionViewModel
import com.example.ui.codex.CodexScreen
import com.example.ui.debug.WorldDebugScreen
import com.example.ui.debug.WorldDebugViewModel
import com.example.ui.home.HomeScreen
import com.example.ui.modes.ModeSelectionScreen
import com.example.ui.settings.AiSettingsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val app = context.applicationContext as Application

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToModeSelection = { navController.navigate(Screen.ModeSelection.route) },
                onNavigateToChatSession = { sessionId ->
                    navController.navigate(Screen.ChatSession.createRoute(sessionId))
                },
                onNavigateToSettings = { navController.navigate(Screen.AiSettings.route) },
                onNavigateToBackup = { navController.navigate(Screen.BackupRestore.route) }
            )
        }

        composable(Screen.ModeSelection.route) {
            ModeSelectionScreen(
                onNavigateBack = { navController.popBackStack() },
                onSessionCreated = { sessionId ->
                    navController.navigate(Screen.ChatSession.createRoute(sessionId)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(
            route = Screen.ChatSession.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val chatViewModel: ChatSessionViewModel = viewModel(
                key = "chat_$sessionId",
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return ChatSessionViewModel(app, sessionId) as T
                    }
                }
            )

            ChatSessionScreen(
                sessionId = sessionId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCharacterSheet = { id ->
                    navController.navigate(Screen.CharacterSheet.createRoute(id))
                },
                onNavigateToDebug = { id ->
                    navController.navigate(Screen.WorldDebug.createRoute(id))
                },
                onNavigateToCodex = { id ->
                    navController.navigate(Screen.Codex.createRoute(id))
                },
                onNavigateToSettings = { navController.navigate(Screen.AiSettings.route) },
                viewModel = chatViewModel
            )
        }

        composable(
            route = Screen.CharacterSheet.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            CharacterSheetScreen(
                sessionId = sessionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.WorldDebug.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val debugViewModel: WorldDebugViewModel = viewModel(
                key = "debug_$sessionId",
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return WorldDebugViewModel(app, sessionId) as T
                    }
                }
            )

            WorldDebugScreen(
                sessionId = sessionId,
                onNavigateBack = { navController.popBackStack() },
                viewModel = debugViewModel
            )
        }

        composable(
            route = Screen.Codex.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            CodexScreen(
                sessionId = sessionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AiSettings.route) {
            AiSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.BackupRestore.route) {
            BackupRestoreScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
