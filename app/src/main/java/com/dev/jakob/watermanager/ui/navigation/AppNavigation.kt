package com.dev.jakob.watermanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dev.jakob.watermanager.ui.settings.SettingsScreen
import com.dev.jakob.watermanager.ui.statistics.StatisticsScreen
import com.dev.jakob.watermanager.ui.welcome.WelcomeScreen

/**
 * Definiert die Navigationsrouten für die App.
 */
object AppRoutes {
    const val WELCOME = "welcome"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"
}

/**
 * Die zentrale Navigations-Composable-Funktion der App.
 *
 * Diese Funktion richtet den [NavHost] mit allen Composable-Zielen ein.
 * @param navController Der Controller, der die Navigation steuert.
 * @param modifier Ein Modifier, der vom Scaffold-Parent übergeben wird, um Padding zu setzen.
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.WELCOME,
        modifier = modifier
    ) {
        composable(AppRoutes.WELCOME) {
            WelcomeScreen()
        }
        composable(AppRoutes.STATISTICS) {
            StatisticsScreen()
        }
        composable(AppRoutes.SETTINGS) {
            SettingsScreen(
                onNavigateUp = { navController.popBackStack() }
            )
        }
    }
}
