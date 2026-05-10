package com.wod.app.ui

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wod.app.WodApp
import com.wod.app.domain.model.TimerType
import com.wod.app.ui.completion.CompletionScreen
import com.wod.app.ui.components.WodDrawerContent
import com.wod.app.ui.config.ConfigScreen
import com.wod.app.ui.diary.DiaryDetailScreen
import com.wod.app.ui.diary.DiaryScreen
import com.wod.app.ui.home.HomeScreen
import com.wod.app.ui.settings.SettingsScreen
import com.wod.app.ui.timer.TimerRunningScreen
import com.wod.app.ui.wods.WodDetailScreen
import com.wod.app.ui.wods.WodsLibraryScreen
import com.wod.app.domain.model.TimerConfig
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun WodNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    fun closeDrawer() = scope.launch { drawerState.close() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            WodDrawerContent(
                currentRoute = currentRoute,
                onTimer = { navController.navigate(Routes.HOME) { launchSingleTop = true }; closeDrawer() },
                onMyWods = { navController.navigate(Routes.WODS); closeDrawer() },
                onDiary = { navController.navigate(Routes.DIARY); closeDrawer() },
                onSettings = { navController.navigate(Routes.SETTINGS); closeDrawer() },
            )
        },
    ) {
        NavHost(navController = navController, startDestination = Routes.HOME) {

            composable(Routes.HOME) {
                HomeScreen(
                    onTimerTypeClick = { type -> navController.navigate(Routes.config(type.name)) },
                    onDiaryClick = { navController.navigate(Routes.DIARY) },
                    onMyWodsClick = { navController.navigate(Routes.WODS) },
                    onMenuClick = { scope.launch { drawerState.open() } },
                )
            }

            composable(
                Routes.CONFIG,
                arguments = listOf(navArgument("type") { type = NavType.StringType }),
            ) { entry ->
                val typeName = entry.arguments?.getString("type") ?: TimerType.TABATA.name
                val type = runCatching { TimerType.valueOf(typeName) }.getOrDefault(TimerType.TABATA)
                val context = LocalContext.current
                ConfigScreen(
                    type = type,
                    onBack = { navController.popBackStack() },
                    onStart = { navController.navigate(Routes.timer(type.name)) },
                    onStartWithConfig = { config ->
                        (context.applicationContext as WodApp).pendingConfig = config
                        navController.navigate(Routes.timer(type.name))
                    },
                )
            }

            composable(
                Routes.TIMER,
                arguments = listOf(navArgument("type") { type = NavType.StringType }),
            ) { entry ->
                val typeName = entry.arguments?.getString("type") ?: TimerType.TABATA.name
                val type = runCatching { TimerType.valueOf(typeName) }.getOrDefault(TimerType.TABATA)
                TimerRunningScreen(
                    type = type,
                    onBack = { navController.popBackStack() },
                    onComplete = {
                        navController.navigate(Routes.COMPLETION) {
                            popUpTo(Routes.HOME)
                        }
                    },
                )
            }

            composable(Routes.COMPLETION) {
                CompletionScreen(onDone = { navController.popBackStack(Routes.HOME, inclusive = false) })
            }

            composable(Routes.DIARY) {
                DiaryScreen(
                    onBack = { navController.popBackStack() },
                    onLogClick = { navController.navigate(Routes.DIARY_DETAIL) },
                )
            }

            composable(Routes.DIARY_DETAIL) {
                DiaryDetailScreen(
                    onBack = { navController.popBackStack() },
                    onDeleted = { navController.popBackStack() },
                )
            }

            composable(Routes.WODS) {
                WodsLibraryScreen(
                    onBack = { navController.popBackStack() },
                    onWodClick = { id -> navController.navigate(Routes.wodDetail(id)) },
                    onEditWod = { id -> navController.navigate(Routes.wodEdit(id)) },
                    onStartTimer = { typeName -> navController.navigate(Routes.timer(typeName)) },
                )
            }

            composable(
                Routes.WOD_EDIT,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) {
                val app = LocalContext.current.applicationContext as WodApp
                val wod = app.selectedWod ?: run { navController.popBackStack(); return@composable }
                val config = runCatching { Json.decodeFromString<TimerConfig>(wod.configJson) }.getOrNull()
                ConfigScreen(
                    type = wod.type,
                    onBack = { navController.popBackStack() },
                    onStart = {},
                    initialConfig = config,
                    onSaveEdited = { newConfig ->
                        val newJson = Json.encodeToString<TimerConfig>(newConfig)
                        app.appScope.launch { app.savedWodRepository.save(wod.copy(configJson = newJson)) }
                        navController.popBackStack()
                    },
                )
            }

            composable(
                Routes.WOD_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val id = entry.arguments?.getString("id") ?: return@composable
                WodDetailScreen(
                    id = id,
                    onBack = { navController.popBackStack() },
                    onStartTimer = { typeName -> navController.navigate(Routes.timer(typeName)) },
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
