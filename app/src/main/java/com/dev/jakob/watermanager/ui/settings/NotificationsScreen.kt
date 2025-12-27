package com.dev.jakob.watermanager.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.dev.jakob.watermanager.ui.settings.viewmodel.SettingsUiState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    uiState: SettingsUiState,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    onTimeRangeChange: (Int, Int) -> Unit
) {
    val context = LocalContext.current
    
    // Launcher für die Berechtigungsanfrage
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onNotificationsEnabledChange(true)
        } else {
            // Optional: Hier könnte man eine Erklärung anzeigen, warum die Berechtigung wichtig ist.
            // Fürs Erste lassen wir den Switch einfach aus.
            onNotificationsEnabledChange(false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Benachrichtigungen aktivieren", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = uiState.notificationsEnabled,
                onCheckedChange = { shouldEnable ->
                    if (shouldEnable) {
                        // Prüfen, ob wir auf Android 13+ sind und die Berechtigung brauchen
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                onNotificationsEnabledChange(true)
                            } else {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            // Unter Android 13 ist keine Laufzeitberechtigung nötig
                            onNotificationsEnabledChange(true)
                        }
                    } else {
                        onNotificationsEnabledChange(false)
                    }
                }
            )
        }

        if (uiState.notificationsEnabled) {
            HorizontalDivider()
            
            Text(text = "Zeitraum", style = MaterialTheme.typography.titleMedium)
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val rangeStart = uiState.notificationStartHour.toFloat()
                val rangeEnd = uiState.notificationEndHour.toFloat()
                
                // Ensure valid range for display
                val safeStart = rangeStart.coerceIn(0f, 23f)
                val safeEnd = rangeEnd.coerceIn(safeStart, 23f)

                Text(text = "${safeStart.toInt()}:00 Uhr - ${safeEnd.toInt()}:00 Uhr")
                
                RangeSlider(
                    value = safeStart..safeEnd,
                    onValueChange = { range ->
                        val newStart = range.start.roundToInt()
                        val newEnd = range.endInclusive.roundToInt()
                        // Only update if values actually changed to avoid jitter
                        if (newStart != uiState.notificationStartHour || newEnd != uiState.notificationEndHour) {
                            onTimeRangeChange(newStart, newEnd)
                        }
                    },
                    valueRange = 0f..23f,
                    steps = 22
                )
            }
        }
    }
}
