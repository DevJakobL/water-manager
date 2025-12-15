package com.dev.jakob.watermanager.ui.statistics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dev.jakob.watermanager.R
import com.dev.jakob.watermanager.databinding.FragmentStatisticsBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

/**
 * A [Fragment] that displays water consumption statistics.
 *
 * This fragment features a calendar view that visually represents the user's daily water intake.
 * It observes data from the [StatisticsViewModel] to update the calendar with decorators,
 * providing a quick overview of historical consumption data. The calendar is styled for a dark theme.
 */
class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    // Lazily inject the StatisticsViewModel using Koin for dependency management.
    private val viewModel: StatisticsViewModel by viewModel()

    /**
     * Inflates the fragment's layout using View Binding.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Finalizes the view setup after the view has been created.
     * This method sets up the calendar styling and observes the state from the ViewModel.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCalendarStyling()
        observeViewModelState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }

    /**
     * Applies custom styling to the calendar view to ensure it integrates well with the app's dark theme.
     */
    private fun setupCalendarStyling() {
        binding.calendarView.apply {
            setHeaderTextAppearance(R.style.CalendarHeaderText)
            setWeekDayTextAppearance(R.style.CalendarWeekDayText)
            setDateTextAppearance(R.style.CalendarDateText)
            setBackgroundColor(Color.TRANSPARENT)
        }
    }

    /**
     * Observes the UI state [StateFlow] from the [StatisticsViewModel].
     * When the state changes, it updates the calendar decorators.
     */
    private fun observeViewModelState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { data ->
                    updateCalendarDecorators(data)
                }
            }
        }
    }

    /**
     * Updates the calendar view with decorators that display water intake for specific days.
     */
    private fun updateCalendarDecorators(data: List<CalendarData>) {
        binding.calendarView.apply {
            removeDecorators()
            data.forEach { entry ->
                val calendarDay = getCalendarDay(entry.date)
                addDecorator(WaterIntakeDecorator(calendarDay, entry.amount))
            }
            invalidate()
        }
    }

    /**
     * Converts a standard Java [Date] object into a [CalendarDay] object required by the MaterialCalendarView library.
     */
    private fun getCalendarDay(date: Date): CalendarDay {
        val calendar = Calendar.getInstance().apply { time = date }
        return CalendarDay.from(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1, // MaterialCalendarView months are 1-based (1-12).
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    /**
     * Cleans up the binding reference when the fragment's view is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
