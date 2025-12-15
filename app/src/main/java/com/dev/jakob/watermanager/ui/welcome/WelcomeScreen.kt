package com.dev.jakob.watermanager.ui.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.jakob.watermanager.R
import com.dev.jakob.watermanager.ui.theme.WaterManagerTheme
import com.dev.jakob.watermanager.ui.welcome.viewmodel.WelcomeViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Dies ist der Hauptbildschirm der Water Manager App, der den gesamten Wasserverbrauch anzeigt
 * und Schaltflächen zum Hinzufügen von Wasser basierend auf vordefinierten Behältern bereitstellt.
 *
 * @param welcomeViewModel Das ViewModel, das die Zustandslogik für diesen Bildschirm bereitstellt.
 */
@Composable
fun WelcomeScreen(
    welcomeViewModel: WelcomeViewModel = koinViewModel()
) {
    val uiState by welcomeViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        welcomeViewModel.refreshData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.total_water_intake),
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = stringResource(id = R.string.water_amount_ml, uiState.totalWaterToday),
            fontSize = 34.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            uiState.containers.forEach { container ->
                Button(
                    onClick = { welcomeViewModel.addWater(container) },
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(text = stringResource(id = R.string.container_button_text, container.name, container.size))
                }
            }
        }
    }
}

/**
 * Eine Vorschau-Composable für den WelcomeScreen.
 * Zeigt eine Beispielansicht des WelcomeScreen mit Platzhalterdaten an.
 */
@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    WaterManagerTheme {
        WelcomeScreen()
    }
}
