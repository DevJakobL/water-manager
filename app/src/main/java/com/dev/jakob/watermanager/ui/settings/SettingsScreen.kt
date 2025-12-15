package com.dev.jakob.watermanager.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.jakob.watermanager.R
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.ui.settings.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Ein Composable-Bildschirm zum Verwalten der Liste der Wasserbehälter.
 *
 * Dieser Bildschirm zeigt eine Liste von Behältern an und ermöglicht es dem Benutzer,
 * sie zu bearbeiten, zu entfernen und neue hinzuzufügen. Er folgt dem Unidirectional
 * Data Flow (UDF)-Muster, indem er den Zustand vom [SettingsViewModel] beobachtet
 * und Benutzeraktionen an das ViewModel weiterleitet.
 *
 * @param onNavigateUp Lambda-Funktion, die aufgerufen wird, wenn nach oben navigiert werden soll.
 * @param viewModel Das ViewModel, das die Zustandslogik für diesen Bildschirm bereitstellt.
 */
@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val containers by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.configure_containers),
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(containers) { container ->
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
                viewModel.saveContainers()
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
            modifier = Modifier.weight(0.5f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onRemoveClicked) {
            Text(text = "X")
        }
    }
}
