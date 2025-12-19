package com.dev.jakob.watermanager.ui.statistics

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.jakob.watermanager.R
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.data.model.Water
import com.dev.jakob.watermanager.ui.theme.WaterManagerTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*

/**
 * Stellt die aggregierten Statistiken für eine Woche dar.
 */
data class WeeklySummary(
    val averageIntake: Int,
    val goalMetDays: Int,
    val extraHydratedDays: Int
)

/**
 * Stellt die Daten für einen einzelnen Tag im Kalender dar.
 */
data class DayData(
    val date: LocalDate,
    val intake: Int,
    val dailyGoal: Int,
    val entries: List<Water> = emptyList() // Liste der einzelnen Einträge
) {
    val progress: Float
        get() = if (dailyGoal > 0) intake.toFloat() / dailyGoal.toFloat() else 0f
}

/**
 * Der Hauptbildschirm für die Anzeige von Trinkstatistiken (zustandsbehaftet).
 * @param onNavigateUp Callback, um in der Navigation eine Ebene nach oben zu gehen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateUp: () -> Unit,
    viewModel: StatisticsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var selectedDay by remember { mutableStateOf<DayData?>(null) }

    // Konvertiere uiState.calendarData zu DayData für den Kalender
    val monthData = uiState.calendarData.map { entry ->
        val date = entry.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        DayData(
            date = date,
            intake = entry.amount,
            dailyGoal = uiState.dailyGoal,
            entries = if (selectedDay?.date == date) uiState.selectedDayWaterIntake else emptyList()
        )
    }

    StatisticsScreenContent(
        uiState = uiState,
        monthData = monthData,
        sheetState = sheetState,
        selectedDay = selectedDay,
        onDayClick = { day ->
            selectedDay = day
            viewModel.onDaySelected(Date.from(day.date.atStartOfDay(ZoneId.systemDefault()).toInstant()))
            scope.launch { sheetState.show() }
        },
        onDismissBottomSheet = {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) selectedDay = null
            }
        },
        onAddWater = { container ->
            selectedDay?.let {
                viewModel.addWaterIntake(container, Date.from(it.date.atStartOfDay(ZoneId.systemDefault()).toInstant()))
            }
        },
        onDeleteWater = { water ->
            viewModel.deleteWaterIntake(water)
        },
        onNavigateUp = onNavigateUp
    )
}

/**
 * Die UI für den Statistikbildschirm (zustandslos).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatisticsScreenContent(
    uiState: StatisticsUiState,
    monthData: List<DayData>,
    sheetState: SheetState,
    selectedDay: DayData?,
    onDayClick: (DayData) -> Unit,
    onDismissBottomSheet: () -> Unit,
    onAddWater: (Container) -> Unit,
    onDeleteWater: (Water) -> Unit,
    onNavigateUp: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.menu_statistics)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WeeklySummaryCard(summary = uiState.weeklySummary)
            Spacer(modifier = Modifier.height(24.dp))
            MonthlyCalendar(
                monthData = monthData,
                onDayClick = onDayClick
            )
        }

        if (sheetState.isVisible && selectedDay != null) {
            val updatedSelectedDay = selectedDay.copy(
                entries = uiState.selectedDayWaterIntake,
                intake = uiState.selectedDayWaterIntake.sumOf { it.amount }
            )
            DayDetailsBottomSheet(
                dayData = updatedSelectedDay,
                containers = uiState.containers,
                onDismiss = onDismissBottomSheet,
                onAddWater = onAddWater,
                onDeleteWater = onDeleteWater,
                sheetState = sheetState
            )
        }
    }
}


/**
 * Zeigt eine Zusammenfassung der wöchentlichen Trinkstatistiken.
 */
@Composable
private fun WeeklySummaryCard(summary: WeeklySummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryItem(stringResource(R.string.average_intake), "${summary.averageIntake} ml")
            SummaryItem(stringResource(R.string.goal_met_days), summary.goalMetDays.toString())
            SummaryItem(stringResource(R.string.extra_hydrated_days), summary.extraHydratedDays.toString())
        }
    }
}

/**
 * Ein einzelnes Element in der Wochenübersicht.
 */
@Composable
private fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * Zeigt einen Monatskalender mit farbigen Indikatoren für den täglichen Fortschritt.
 */
@Composable
private fun MonthlyCalendar(
    monthData: List<DayData>,
    onDayClick: (DayData) -> Unit,
    modifier: Modifier = Modifier
) {
    val yearMonth = if (monthData.isNotEmpty()) YearMonth.from(monthData.first().date) else YearMonth.now()
    val firstDayOfMonth = yearMonth.atDay(1).dayOfWeek.value % 7
    val daysInMonth = yearMonth.lengthOfMonth()

    Column(modifier = modifier) {
        Text(
            text = "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${yearMonth.year}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            (1..7).forEach { day ->
                val dayName = java.time.DayOfWeek.of(day).getDisplayName(TextStyle.SHORT, Locale.getDefault())
                Text(text = dayName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        val dayMap = monthData.associateBy { it.date.dayOfMonth }
        val totalCells = (firstDayOfMonth + daysInMonth + 6) / 7 * 7
        for (i in 0 until totalCells / 7) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (j in 0..6) {
                    val dayIndex = i * 7 + j
                    val dayOfMonth = dayIndex - firstDayOfMonth + 1
                    if (dayOfMonth in 1..daysInMonth) {
                        val dayData = dayMap[dayOfMonth]
                        DayCell(day = dayOfMonth, data = dayData, onClick = {
                            val date = yearMonth.atDay(dayOfMonth)
                            val data = dayMap[dayOfMonth] ?: DayData(date, 0, 0)
                            onDayClick(data)
                        })
                    } else {
                        Box(modifier = Modifier.size(40.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * Eine einzelne Zelle für einen Tag im Kalender.
 */
@Composable
private fun DayCell(day: Int, data: DayData?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = day.toString(), fontSize = 14.sp)
        if (data != null) {
            DayProgressIndicator(progress = data.progress, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

/**
 * Der farbige Indikator für den Fortschritt eines Tages.
 */
@Composable
private fun DayProgressIndicator(progress: Float, modifier: Modifier = Modifier) {
    val (color, showPlus) = when {
        progress > 1.2f -> MaterialTheme.colorScheme.tertiary to true
        progress >= 0.8f -> MaterialTheme.colorScheme.primary to false
        else -> MaterialTheme.colorScheme.surfaceVariant to false
    }

    Box(
        modifier = modifier
            .padding(bottom = 4.dp)
            .size(width = 20.dp, height = 6.dp)
            .background(color, CircleShape)
    ) {
        if (showPlus) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

/**
 * Ein Bottom-Sheet, das die Details und Bearbeitungsoptionen für einen Tag anzeigt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayDetailsBottomSheet(
    dayData: DayData,
    containers: List<Container>,
    onDismiss: () -> Unit,
    onAddWater: (Container) -> Unit,
    onDeleteWater: (Water) -> Unit,
    sheetState: SheetState
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = bottomPadding)
        ) {
            // Header
            item {
                Text(
                    text = dayData.date.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d. MMMM", Locale.getDefault())),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Zusammenfassung
            item { DetailItem(label = stringResource(R.string.intake_amount), value = "${dayData.intake} ml") }
            item { DetailItem(label = stringResource(R.string.daily_goal), value = "${dayData.dailyGoal} ml") }
            item {
                val difference = dayData.intake - dayData.dailyGoal
                DetailItem(
                    label = stringResource(R.string.difference),
                    value = "${if (difference >= 0) "+" else ""}$difference ml",
                    valueColor = if (difference >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) }

            // Liste der Einträge
            item {
                Text(
                    text = stringResource(R.string.entries),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(dayData.entries) { entry ->
                WaterIntakeListItem(entry = entry, onDelete = { onDeleteWater(entry) })
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) }

            // Aktionen
            item { AddWaterControl(containers = containers, onAdd = onAddWater) }

            // Warnhinweis
            if (dayData.progress > 1.5) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.overhydration_warning_message),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Ein Listenelement für einen einzelnen Wasser-Eintrag mit Löschfunktion.
 */
@Composable
private fun WaterIntakeListItem(entry: Water, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "${entry.name} (${entry.amount} ml)")
        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.delete_entry),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Steuerelement zum Hinzufügen eines neuen Wasser-Eintrags.
 */
@Composable
private fun AddWaterControl(containers: List<Container>, onAdd: (Container) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedContainer by remember { mutableStateOf<Container?>(null) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(modifier = Modifier.weight(1f)) {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(selectedContainer?.name ?: stringResource(R.string.select_container))
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                containers.forEach { container ->
                    DropdownMenuItem(
                        text = { Text("${container.name} (${container.size} ml)") },
                        onClick = {
                            selectedContainer = container
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = { selectedContainer?.let { onAdd(it) } },
            enabled = selectedContainer != null
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_drink_button))
        }
    }
}


/**
 * Ein einzelnes Detail-Element im Bottom-Sheet.
 */
@Composable
private fun DetailItem(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StatisticsScreenPreview() {
    WaterManagerTheme {
        val mockUiState = StatisticsUiState(
            weeklySummary = WeeklySummary(2150, 4, 2),
            containers = listOf(
                Container(id = 1, name = "Glas", size = 250),
                Container(id = 2, name = "Flasche", size = 500)
            ),
            dailyGoal = 2500
        )
        val mockMonthData = (1..31).map { day ->
            val date = LocalDate.of(2023, 10, day)
            val intake = when {
                day % 7 == 0 -> 3200
                day % 3 == 0 -> 2500
                else -> 1200
            }
            DayData(date = date, intake = intake, dailyGoal = 2500)
        }

        StatisticsScreenContent(
            uiState = mockUiState,
            monthData = mockMonthData,
            sheetState = rememberModalBottomSheetState(),
            selectedDay = null,
            onDayClick = {},
            onDismissBottomSheet = {},
            onAddWater = {},
            onDeleteWater = {},
            onNavigateUp = {}
        )
    }
}
