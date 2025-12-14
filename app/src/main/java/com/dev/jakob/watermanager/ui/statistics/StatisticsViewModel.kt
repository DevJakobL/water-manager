package com.dev.jakob.watermanager.ui.statistics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dev.jakob.watermanager.data.repository.WaterRepository
import java.time.Instant
import java.time.ZoneId
import java.util.Date

/**
 * Data class to hold the water intake for a specific date.
 * @param date The date of the water intake.
 * @param amount The total amount of water consumed on that date.
 */
data class CalendarData(val date: Date, val amount: Float)

/**
 * [ViewModel] for the [StatisticsFragment].
 * It provides the data for the calendar by fetching it from the [WaterRepository].
 * @param waterRepository The repository for accessing water data.
 */
class StatisticsViewModel(private val waterRepository: WaterRepository) : ViewModel() {

    private val _calendarData = MutableLiveData<List<CalendarData>>()
    val calendarData: LiveData<List<CalendarData>> = _calendarData

    init {
        loadCalendarData()
    }

    /**
     * Loads the water intake data from the repository and processes it for the calendar.
     * It groups the water intake by day and sums the amounts.
     */
    private fun loadCalendarData() {
        val waterIntakeList = waterRepository.loadWaterIntake()

        val entries = waterIntakeList
            .groupBy { water ->
                // Group entries by their date in the system's default timezone
                Instant.ofEpochMilli(water.timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            .map { (date, dailyWaterEntries) ->
                // For each day, sum the total amount of water
                val totalAmount = dailyWaterEntries.sumOf { it.amount }.toFloat()
                // Convert the LocalDate back to a Date object for the UI
                val dateAsJavaUtilDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
                CalendarData(dateAsJavaUtilDate, totalAmount)
            }

        _calendarData.value = entries
    }
}
