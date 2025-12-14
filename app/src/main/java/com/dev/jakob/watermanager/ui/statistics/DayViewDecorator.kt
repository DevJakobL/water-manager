package com.dev.jakob.watermanager.ui.statistics

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.style.LineBackgroundSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

/**
 * A [DayViewDecorator] that displays the water intake amount below a specific date.
 * @param day The specific day to decorate.
 * @param amount The water intake amount for that day.
 */
class WaterIntakeDecorator(private val day: CalendarDay, private val amount: Float) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        // Decorate only if the day matches and there's an amount to show.
        return day == this.day && amount > 0
    }

    override fun decorate(view: DayViewFacade) {
        // Add the span to draw the text.
        view.addSpan(AmountSpan(amount))
    }

    /**
     * A [LineBackgroundSpan] that draws the water intake amount below the date number.
     */
    private class AmountSpan(private val amount: Float) : LineBackgroundSpan {
        override fun drawBackground(
            canvas: Canvas,
            paint: Paint,
            left: Int,
            right: Int,
            top: Int,
            baseline: Int,
            bottom: Int,
            text: CharSequence,
            start: Int,
            end: Int,
            lineNumber: Int
        ) {
            val textPaint = Paint(paint).apply {
                textSize = 28f // Adjust text size as needed
                textAlign = Paint.Align.CENTER
                color = Color.WHITE
            }
            val amountInLiters = amount / 1000
            val textToDraw = "%.1fL".format(amountInLiters)
            // Adjust the y-position to draw below the date number
            val y = bottom + 30f
            canvas.drawText(textToDraw, ((left + right) / 2).toFloat(), y, textPaint)
        }
    }
}
