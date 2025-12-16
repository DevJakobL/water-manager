package com.dev.jakob.watermanager.ui.statistics

import android.graphics.*
import android.text.style.LineBackgroundSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.util.*

/**
 * A [DayViewDecorator] that displays the water intake amount below the date.
 * The amount is shown inside a black, rounded square.
 * @param day The specific day to decorate.
 * @param amount The water intake amount for that day.
 */
class WaterIntakeDecorator(private val day: CalendarDay, private val amount: Float) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        // Decorate only if the day matches and there's an amount to show.
        return day == this.day && amount > 0
    }

    override fun decorate(view: DayViewFacade) {
        // Add the custom span to draw the background and text below the date.
        view.addSpan(AmountSpan(amount))
    }

    /**
     * A [LineBackgroundSpan] that draws the water intake amount below the date number,
     * inside a black, rounded square.
     */
    private class AmountSpan(private val amount: Float) : LineBackgroundSpan {

        private val backgroundPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK // Black background
        }
        private val textPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = 28f // Adjust text size as needed
            textAlign = Paint.Align.CENTER
        }


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
            // Format the text to be drawn
            val amountInLiters = amount / 1000
            val textToDraw = String.format(Locale.US, "%.1fL", amountInLiters)

            // Measure the text to calculate the background size
            val textBounds = Rect()
            textPaint.getTextBounds(textToDraw, 0, textToDraw.length, textBounds)

            // Define padding for the background
            val backgroundPadding = 8f
            val cornerRadius = 10f

            // Calculate position for the background rectangle, BELOW the date
            val backgroundHeight = textBounds.height() + 2 * backgroundPadding
            val backgroundWidth = textBounds.width() + 2 * backgroundPadding
            val backgroundTop = bottom + 10f // Position it below the original bottom of the date line
            val backgroundLeft = (left + right) / 2f - backgroundWidth / 2f
            val backgroundRect = RectF(
                backgroundLeft,
                backgroundTop,
                backgroundLeft + backgroundWidth,
                backgroundTop + backgroundHeight
            )

            // Draw the background
            canvas.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, backgroundPaint)

            // Calculate position for the text and draw it on top of the new background
            val textY = backgroundRect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText(textToDraw, backgroundRect.centerX(), textY, textPaint)
        }
    }
}
