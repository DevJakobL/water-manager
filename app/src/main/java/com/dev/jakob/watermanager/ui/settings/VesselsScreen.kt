package com.dev.jakob.watermanager.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dev.jakob.watermanager.R
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.ui.settings.viewmodel.SettingsUiState

/**
 * Bildschirm zur Verwaltung der Trinkgefäße.
 *
 * @param uiState Der aktuelle Zustand der UI.
 * @param onAddContainer Callback für das Hinzufügen eines neuen Gefäßes.
 * @param onEditContainer Callback für das Bearbeiten eines Gefäßes.
 * @param onDeleteContainer Callback für das Löschen eines Gefäßes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VesselsScreen(
    uiState: SettingsUiState,
    onAddContainer: () -> Unit,
    onEditContainer: (Container) -> Unit,
    onDeleteContainer: (Container) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddContainer) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.new_container))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 80.dp) // Platz für FAB
        ) {
            items(uiState.containers) { container ->
                ContainerCard(
                    container = container,
                    onEdit = { onEditContainer(container) },
                    onDelete = { onDeleteContainer(container) },
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }
}


/**
 * Eine Karte, die ein einzelnes Trinkgefäß darstellt.
 *
 * @param container Das anzuzeigende [Container]-Objekt.
 * @param onEdit Callback, der aufgerufen wird, wenn der Bearbeiten-Button geklickt wird.
 * @param onDelete Callback, der aufgerufen wird, wenn der Löschen-Button geklickt wird.
 * @param modifier Optionaler [Modifier] für diese Komponente.
 */
@Composable
private fun ContainerCard(
    container: Container,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Place, // Platzhalter-Icon
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = container.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${container.size} ml",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_container))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_container), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

/**
 * Ein Dialog zum Hinzufügen oder Bearbeiten eines Trinkgefäßes.
 *
 * @param container Der zu bearbeitende [Container].
 * @param onDismiss Callback, wenn der Dialog geschlossen wird.
 * @param onConfirm Callback, wenn die Änderungen bestätigt werden.
 */
@Composable
fun ContainerEditDialog(
    container: Container,
    onDismiss: () -> Unit,
    onConfirm: (Container) -> Unit
) {
    var name by remember { mutableStateOf(container.name) }
    var size by remember { mutableStateOf(container.size.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (container.id == 0L) stringResource(R.string.add_container) else stringResource(R.string.edit_container)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.container_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = size,
                    onValueChange = { newValue ->
                        size = newValue.filter { it.isDigit() }
                    },
                    label = { Text(stringResource(R.string.container_size_ml)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedSize = size.toIntOrNull() ?: 0
                    if (name.isNotBlank() && parsedSize > 0) {
                        onConfirm(container.copy(name = name, size = parsedSize))
                    }
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
