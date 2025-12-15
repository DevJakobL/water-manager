package com.dev.jakob.watermanager.ui.statistics

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.dev.jakob.watermanager.R
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
 *
 * @param viewModel Das ViewModel, das die Zustandslogik für diesen Bildschirm bereitstellt.
 */
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            MaterialCalendarView(context).apply {
                setHeaderTextAppearance(R.style.CalendarHeaderText)
                setWeekDayTextAppearance(R.style.CalendarWeekDayText)
                setDateTextAppearance(R.style.CalendarDateText)
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
