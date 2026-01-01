package com.dev.jakob.watermanager.ui.settings

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dev.jakob.watermanager.R
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.ui.settings.viewmodel.SettingsViewModel
import com.dev.jakob.watermanager.ui.theme.WaterManagerTheme
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

/**
 * Datenklasse, die eine einzelne Einstellungsseite repräsentiert.
 * Macht die Einstellungsnavigation leicht erweiterbar.
 *
 * @param route Die eindeutige Route für die Navigation.
 * @param labelResId Die String-Ressourcen-ID für den Titel der Seite.
 * @param icon Das Icon, das in der Navigationsleiste angezeigt wird.
 * @param content Der Composable-Inhalt der Seite.
 */
data class SettingPage(
    val route: String,
    @StringRes val labelResId: Int,
    val icon: ImageVector,
    val content: @Composable () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()

    // Zustand für den Container-Bearbeitungsdialog
    var showContainerDialog by remember { mutableStateOf(false) }
    var editingContainer by remember { mutableStateOf<Container?>(null) }

    // Speichern der Einstellungen, wenn der Screen verlassen wird
    DisposableEffect(Unit) {
        onDispose {
            viewModel.saveSettings()
        }
    }

    // Liste der Einstellungsseiten. Leicht erweiterbar durch Hinzufügen neuer SettingPage-Objekte.
    val settingPages = listOf(
        SettingPage(
            route = "calculation",
            labelResId = R.string.calculation,
            icon = Icons.Filled.Calculate,
            content = {
                CalculationScreen(
                    uiState = uiState,
                    onWeightChange = { weightString ->
                        viewModel.onBodyWeightChanged(weightString.toIntOrNull() ?: 0)
                    },
                    onFactorChange = { factor ->
                        viewModel.onCalculationFactorChanged(factor.roundToInt())
                    }
                )
            }
        ),
        SettingPage(
            route = "vessels",
            labelResId = R.string.vessels,
            icon = Icons.Filled.LocalDrink,
            content = {
                VesselsScreen(
                    uiState = uiState,
                    onAddContainer = {
                        editingContainer = Container(name = "", size = 0)
                        showContainerDialog = true
                    },
                    onEditContainer = { container ->
                        editingContainer = container
                        showContainerDialog = true
                    },
                    onDeleteContainer = viewModel::removeContainer
                )
            }
        ),
        SettingPage(
            route = "notifications",
            labelResId = R.string.notifications,
            icon = Icons.Filled.Notifications,
            content = {
                NotificationsScreen(
                    uiState = uiState,
                    onNotificationsEnabledChange = viewModel::onNotificationsEnabledChanged,
                    onTimeRangeChange = { start, end ->
                        viewModel.onNotificationStartHourChanged(start)
                        viewModel.onNotificationEndHourChanged(end)
                    },
                    onTestNotification = viewModel::sendTestNotification
                )
            }
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val selectedTabIndex = settingPages.indexOfFirst { it.route == currentRoute }.takeIf { it != -1 } ?: 0

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.menu_settings)) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                        }
                    }
                )
                TabRow(selectedTabIndex = selectedTabIndex) {
                    settingPages.forEachIndexed { index, page ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {
                                navController.navigate(page.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            },
                            text = { Text(stringResource(page.labelResId)) },
                            icon = { Icon(page.icon, contentDescription = null) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = settingPages.first().route,
            modifier = Modifier.padding(paddingValues)
        ) {
            settingPages.forEach { page ->
                composable(page.route) {
                    page.content()
                }
            }
        }
    }

    if (showContainerDialog) {
        ContainerEditDialog(
            container = editingContainer ?: Container(name = "", size = 0),
            onDismiss = { showContainerDialog = false },
            onConfirm = { updatedContainer ->
                viewModel.onContainerUpdated(updatedContainer)
                showContainerDialog = false
            }
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light Mode")
@Composable
fun SettingsScreenPreview() {
    WaterManagerTheme {
        SettingsScreen(onNavigateUp = {})
    }
}
