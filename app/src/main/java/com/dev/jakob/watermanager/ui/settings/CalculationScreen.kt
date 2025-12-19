package com.dev.jakob.watermanager.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dev.jakob.watermanager.R
import com.dev.jakob.watermanager.ui.settings.viewmodel.SettingsUiState

/**
 * Bildschirm zur Berechnung des täglichen Wasserziels.
 *
 * @param uiState Der aktuelle Zustand der UI.
 * @param onWeightChange Callback für Änderungen am Körpergewicht (liefert den rohen String-Wert).
 * @param onFactorChange Callback für Änderungen am Berechnungsfaktor.
 */
@Composable
fun CalculationScreen(
    uiState: SettingsUiState,
    onWeightChange: (String) -> Unit,
    onFactorChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = if (uiState.bodyWeight > 0) uiState.bodyWeight.toString() else "",
            onValueChange = onWeightChange,
            label = { Text(stringResource(R.string.body_weight_in_kg)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${stringResource(R.string.calculation_factor)}: ${uiState.calculationFactor} ml/kg",
            style = MaterialTheme.typography.bodyLarge
        )
        Slider(
            value = uiState.calculationFactor.toFloat(),
            onValueChange = onFactorChange,
            valueRange = 30f..45f,
            steps = 14
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${stringResource(R.string.daily_goal)}: ${uiState.dailyGoal} ml (${"%.1f".format(uiState.dailyGoal / 1000f)} L)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.daily_goal_info_text),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
