package com.dev.jakob.watermanager.ui.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.dev.jakob.watermanager.R
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.data.model.Water
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import org.koin.androidx.compose.koinViewModel
import java.util.*

/**
 * Zeigt einen Kalender mit dem Wasserverbrauch an.
 *
 * Diese Composable-Funktion zeigt eine Kalenderansicht, die den täglichen Wasserverbrauch des Benutzers
 * visuell darstellt. Sie beobachtet die Daten vom [StatisticsViewModel], um den Kalender mit
 * Dekoratoren zu aktualisieren, die einen schnellen Überblick über die historischen Verbrauchsdaten geben.
 * Unter dem Kalender wird eine Liste der an einem ausgewählten Tag getrunkenen Gefäße angezeigt.
 *
 * @param viewModel Das ViewModel, das die Zustandslogik für diesen Bildschirm bereitstellt.
 */
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDayWaterIntake by viewModel.selectedDayWaterIntake.collectAsState()
    val containers by viewModel.containers.collectAsState()
    var selectedDate by remember { mutableStateOf(Date()) }

    LaunchedEffect(Unit) {
        viewModel.refreshData()
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        selectedDate = today
        viewModel.onDaySelected(today)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxHeight(0.7f), // Assign 70% of the height to the calendar
            factory = { context ->
                MaterialCalendarView(context).apply {
                    setHeaderTextAppearance(R.style.CalendarHeaderText)
                    setWeekDayTextAppearance(R.style.CalendarWeekDayText)
                    setDateTextAppearance(R.style.CalendarDateText)
                    setSelectedDate(CalendarDay.today())
                    setTileHeight(200) // Increase tile height

                    setOnDateChangedListener { _, calendarDay, selected ->
                        if (selected) {
                            val selectedJavaUtilDate = Calendar.getInstance().apply {
                                set(Calendar.YEAR, calendarDay.year)
                                set(Calendar.MONTH, calendarDay.month - 1)
                                set(Calendar.DAY_OF_MONTH, calendarDay.day)
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.time
                            selectedDate = selectedJavaUtilDate
                            viewModel.onDaySelected(selectedJavaUtilDate)
                        }
                    }
                }
            },
            update = { calendarView ->
                calendarView.removeDecorators()
                uiState.forEach { entry ->
                    val calendarDay = getCalendarDay(entry.date)
                    calendarView.addDecorator(WaterIntakeDecorator(calendarDay, entry.amount))
                }
                calendarView.invalidate()
            }
        )

        // Display the list of water intake for the selected day
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(selectedDayWaterIntake) { waterEntry ->
                WaterIntakeItem(waterEntry) {
                    viewModel.deleteWaterIntake(waterEntry)
                }
                HorizontalDivider(color = Color.Gray, thickness = 0.5.dp)
            }
            item {
                AddWaterIntakeRow(containers = containers, onAdd = { container ->
                    viewModel.addWaterIntake(container, selectedDate)
                })
            }
        }
    }
}

/**
 * Zeigt eine Zeile zum Hinzufügen eines neuen Wasseraufnahme-Eintrags an.
 *
 * @param containers Die Liste der verfügbaren [Container].
 * @param onAdd Lambda-Funktion, die aufgerufen wird, wenn ein [Container] hinzugefügt wird.
 */
@Composable
fun AddWaterIntakeRow(containers: List<Container>, onAdd: (Container) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedContainer by remember { mutableStateOf<Container?>(null) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            TextButton(onClick = { expanded = true }) {
                val text = selectedContainer?.let {
                    val amountInLiters = it.size / 1000.0
                    String.format(Locale.getDefault(), "%s (%.2f L)", it.name, amountInLiters)
                } ?: "Gefäß auswählen"
                Text(text)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                containers.forEach { container ->
                    DropdownMenuItem(
                        text = {
                            val amountInLiters = container.size / 1000.0
                            val formattedAmount = String.format(Locale.getDefault(), "%.2f L", amountInLiters)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(container.name)
                                Text(formattedAmount)
                            }
                        },
                        onClick = {
                            selectedContainer = container
                            expanded = false
                        }
                    )
                }
            }
        }
        IconButton(onClick = { selectedContainer?.let { onAdd(it) } }, enabled = selectedContainer != null) {
            Icon(Icons.Filled.Add, contentDescription = "Hinzufügen")
        }
    }
}


/**
 * Zeigt ein einzelnes Wasseraufnahme-Element an.
 *
 * Diese Composable-Funktion stellt die Details eines einzelnen Wasseraufnahme-Eintrags dar,
 * einschließlich des Gefäßnamens und der Menge in Litern, sowie einen Löschen-Button.
 *
 * @param water Das [Water]-Objekt, das angezeigt werden soll.
 * @param onDelete Lambda-Funktion, die aufgerufen wird, wenn der Löschen-Button geklickt wird.
 */
@Composable
fun WaterIntakeItem(water: Water, onDelete: (Water) -> Unit) {
    val amountInLiters = water.amount / 1000.0
    val formattedAmount = String.format(Locale.getDefault(), "%.2f L", amountInLiters)

    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically // Align items vertically in the center
    ) {
        Row(modifier = Modifier.weight(1f)) { // Give text content some weight
            Text(text = water.containerName)
            Text(text = formattedAmount, modifier = Modifier.padding(start = 8.dp)) // Add some padding between name and amount
        }
        IconButton(onClick = { showDialog = true }) {
            Icon(Icons.Filled.Delete, contentDescription = "Löschen")
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Eintrag löschen") },
            text = { Text("Möchten Sie diesen Eintrag wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(water)
                    showDialog = false
                }) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

/**
 * Konvertiert ein [Date]-Objekt in ein [CalendarDay]-Objekt.
 *
 * @param date Das zu konvertierende Datum.
 * @return Das [CalendarDay]-Objekt.
 */
private fun getCalendarDay(date: Date): CalendarDay {
    val calendar = Calendar.getInstance().apply { time = date }
    return CalendarDay.from(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1, // MaterialCalendarView months are 1-based (1-12).
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}
