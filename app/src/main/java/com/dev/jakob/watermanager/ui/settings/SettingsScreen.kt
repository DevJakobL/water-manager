package com.dev.jakob.watermanager.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.jakob.watermanager.R
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.ui.settings.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Ein Composable-Bildschirm zum Verwalten der Liste der Wasserbehälter und der täglichen Zieleinstellungen.
 *
 * Dieser Bildschirm zeigt eine Liste von Behältern an und ermöglicht es dem Benutzer,
 * sie zu bearbeiten, zu entfernen und neue hinzuzufügen. Außerdem können Benutzer
 * ihr Körpergewicht und einen Berechnungsfaktor eingeben, um das tägliche Wasserziel
 * automatisch zu berechnen. Er folgt dem Unidirectional Data Flow (UDF)-Muster,
 * indem er den Zustand vom [SettingsViewModel] beobachtet und Benutzeraktionen
 * an das ViewModel weiterleitet.
 *
 * @param onNavigateUp Lambda-Funktion, die aufgerufen wird, wenn nach oben navigiert werden soll.
 * @param viewModel Das ViewModel, das die Zustandslogik für diesen Bildschirm bereitstellt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState() // Observe SettingsUiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Body Weight Input
        Text(
            text = stringResource(id = R.string.body_weight_label),
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        TextField(
            value = if (uiState.bodyWeight > 0) uiState.bodyWeight.toString() else "",
            onValueChange = { newValue ->
                val weight = newValue.toIntOrNull() ?: 0
                viewModel.onBodyWeightChanged(weight)
            },
            label = { Text(stringResource(id = R.string.body_weight_unit)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Calculation Factor Selection
        Text(
            text = stringResource(id = R.string.calculation_factor_label),
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val factors = listOf(30, 40)
            factors.forEach { factor ->
                FilterChip(
                    selected = uiState.calculationFactor == factor,
                    onClick = { viewModel.onCalculationFactorChanged(factor) },
                    label = { Text("$factor ml/kg") }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Display Daily Goal
        Text(
            text = stringResource(id = R.string.daily_goal_label),
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = stringResource(id = R.string.daily_goal_value, uiState.dailyGoal),
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))


        // Existing Container Configuration
        Text(
            text = stringResource(id = R.string.configure_containers),
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(uiState.containers) { container -> // Use uiState.containers
                ContainerItem(
                    container = container,
                    onContainerChanged = { updatedContainer ->
                        viewModel.onContainerUpdated(updatedContainer)
                    },
                    onRemoveClicked = {
                        viewModel.removeContainer(container)
                    }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { viewModel.addContainer() }) {
                Text(text = stringResource(id = R.string.add_container))
            }
            Button(onClick = {
                viewModel.saveSettings() // Call saveSettings()
                onNavigateUp()
            }) {
                Text(text = stringResource(id = R.string.save))
            }
        }
    }
}

/**
 * Ein Composable, das ein einzelnes Behälterelement in der Liste anzeigt.
 *
 * @param container Der anzuzeigende Behälter.
 * @param onContainerChanged Lambda, das aufgerufen wird, wenn der Name oder die Größe des Behälters geändert wird.
 * @param onRemoveClicked Lambda, das aufgerufen wird, wenn auf die Schaltfläche "Entfernen" geklickt wird.
 */
@Composable
fun ContainerItem(
    container: Container,
    onContainerChanged: (Container) -> Unit,
    onRemoveClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextField(
            value = container.name,
            onValueChange = { newName ->
                onContainerChanged(container.copy(name = newName))
            },
            label = { Text("Name") },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        TextField(
            value = container.size.toString(),
            onValueChange = { newSize ->
                val size = newSize.toIntOrNull() ?: 0
                onContainerChanged(container.copy(size = size))
            },
            label = { Text("Size (ml)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(0.5f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onRemoveClicked) {
            Text(text = "X")
        }
    }
}
