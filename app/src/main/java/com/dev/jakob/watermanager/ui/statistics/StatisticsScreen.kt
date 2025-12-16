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

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedDate by remember { mutableStateOf(Date()) }

    LaunchedEffect(Unit) {
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
            modifier = Modifier.fillMaxHeight(0.7f),
            factory = { context ->
                MaterialCalendarView(context).apply {
                    setHeaderTextAppearance(R.style.CalendarHeaderText)
                    setWeekDayTextAppearance(R.style.CalendarWeekDayText)
                    setDateTextAppearance(R.style.CalendarDateText)
                    setSelectedDate(CalendarDay.today())
                    setTileHeight(200)

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
                uiState.calendarData.forEach { entry ->
                    val calendarDay = getCalendarDay(entry.date)
                    calendarView.addDecorator(WaterIntakeDecorator(calendarDay, entry.amount))
                }
                calendarView.invalidate()
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(uiState.selectedDayWaterIntake) { waterEntry ->
                WaterIntakeItem(waterEntry) {
                    viewModel.deleteWaterIntake(waterEntry)
                }
                HorizontalDivider(color = Color.Gray, thickness = 0.5.dp)
            }
            item {
                AddWaterIntakeRow(containers = uiState.containers, onAdd = { container ->
                    viewModel.addWaterIntake(container, selectedDate)
                })
            }
        }
    }
}

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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f)) {
            Text(text = water.name)
            Text(text = formattedAmount, modifier = Modifier.padding(start = 8.dp))
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

private fun getCalendarDay(date: Date): CalendarDay {
    val calendar = Calendar.getInstance().apply { time = date }
    return CalendarDay.from(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}
