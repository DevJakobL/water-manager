package com.dev.jakob.watermanager.ui.statistics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dev.jakob.watermanager.R
import com.dev.jakob.watermanager.databinding.FragmentStatisticsBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Calendar
import java.util.Date

/**
 * A [Fragment] that displays statistics about water consumption.
 * It shows a calendar with the water intake for each day.
 */
class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    // Inject StatisticsViewModel using Koin
    private val viewModel: StatisticsViewModel by viewModel()

    /**
     * Inflates the layout for this fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called when the fragment's view has been created.
     * It observes the calendar data from the [StatisticsViewModel].
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCalendarStyling()
        viewModel.calendarData.observe(viewLifecycleOwner) { data ->
            updateCalendarDecorators(data)
        }
    }

    /**
     * Sets the styling for the calendar view to match a dark theme.
     */
    private fun setupCalendarStyling() {
        binding.calendarView.apply {
            // Set colors for text
            setHeaderTextAppearance(R.style.CalendarHeaderText)
            setWeekDayTextAppearance(R.style.CalendarWeekDayText)
            setDateTextAppearance(R.style.CalendarDateText)

            // Set background to transparent to show the fragment's background
            setBackgroundColor(Color.TRANSPARENT)
        }
    }

    /**
     * Clears existing decorators and adds new ones to the calendar for each data entry.
     * @param data The data for the calendar.
     */
    private fun updateCalendarDecorators(data: List<CalendarData>) {
        binding.calendarView.apply {
            // Remove all previous decorators to prevent duplicates
            removeDecorators()
            // Create and add a new decorator for each individual data entry
            data.forEach { entry ->
                val calendarDay = getCalendarDay(entry.date)
                addDecorator(WaterIntakeDecorator(calendarDay, entry.amount))
            }
            // Invalidate the view to ensure it redraws
            invalidate()
        }
    }

    /**
     * Converts a [Date] object to a [CalendarDay].
     * @param date The date to convert.
     * @return The corresponding [CalendarDay].
     */
    private fun getCalendarDay(date: Date): CalendarDay {
        val calendar = Calendar.getInstance().apply { time = date }
        return CalendarDay.from(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1, // MaterialCalendarView month is 1-12
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    /**
     * Called when the view is destroyed.
     * It cleans up the binding.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
