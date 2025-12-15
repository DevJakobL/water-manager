package com.dev.jakob.watermanager.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dev.jakob.watermanager.ui.navigation.AppNavigation
import com.dev.jakob.watermanager.ui.navigation.AppRoutes
import kotlinx.coroutines.launch

/**
 * Repräsentiert ein einzelnes Ziel im Navigation Drawer.
 *
 * @param route Die Route für die Navigation.
 * @param title Der im Menü angezeigte Text.
 * @param icon Das im Menü angezeigte Icon.
 */
data class NavDrawerItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

/**
 * Die Haupt-Composable, die die gesamte App-UI mit Scaffold und Navigation Drawer strukturiert.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navItems = listOf(
        NavDrawerItem(AppRoutes.WELCOME, "Home", Icons.Default.Home),
        NavDrawerItem(AppRoutes.STATISTICS, "Statistics", Icons.Default.DateRange),
        NavDrawerItem(AppRoutes.SETTINGS, "Settings", Icons.Default.Settings)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                navItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.title) },
                        selected = item.route == currentRoute,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val title = navItems.find { it.route == currentRoute }?.title ?: "Water Manager"
                        Text(title)
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.apply {
                                    if (isClosed) open() else close()
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            AppNavigation(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
