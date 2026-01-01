package com.dev.jakob.watermanager.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dev.jakob.watermanager.MainActivity
import com.dev.jakob.watermanager.R
import com.dev.jakob.watermanager.data.repository.WaterRepository
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Worker class responsible for checking hydration status and sending reminders.
 *
 * It runs periodically and checks if the user has drunk enough water based on the
 * time of day and their daily goal.
 */
class HydrationReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val waterRepository: WaterRepository by inject()

    override suspend fun doWork(): Result {
        val isTest = inputData.getBoolean("IS_TEST", false)

        val notificationsEnabled = waterRepository.getNotificationsEnabled().first()
        // If not test, respect the enabled setting
        if (!isTest && !notificationsEnabled) {
            return Result.success()
        }

        val startHour = waterRepository.getNotificationStartHour().first()
        val endHour = waterRepository.getNotificationEndHour().first()
        
        val now = LocalTime.now()
        val currentMinuteOfDay = now.hour * 60 + now.minute
        val startMinuteOfDay = startHour * 60
        val endMinuteOfDay = endHour * 60

        // Only run within the specified time window (unless it's a test)
        if (!isTest && (currentMinuteOfDay < startMinuteOfDay || currentMinuteOfDay > endMinuteOfDay)) {
            return Result.success()
        }

        // Check hydration status
        val dailyGoal = waterRepository.getDailyGoal().first()
        val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val waterIntakeList = waterRepository.getAllWaterIntake().first()
        
        val todayIntake = waterIntakeList
            .filter { it.timestamp >= today }
            .sumOf { it.amount }

        // Calculate expected intake based on time of day (linear distribution)
        val totalMinutes = endMinuteOfDay - startMinuteOfDay
        
        if (totalMinutes <= 0) return Result.success()

        val minutesPassed = currentMinuteOfDay - startMinuteOfDay
        
        // Clamp progress between 0.0 and 1.0 to avoid negative values (before start)
        // or values > 1.0 (after end)
        val progressPercentage = (minutesPassed.toDouble() / totalMinutes.toDouble()).coerceIn(0.0, 1.0)
        val expectedIntake = dailyGoal * progressPercentage

        // Calculate deficit
        val deficit = expectedIntake - todayIntake

        if (isTest) {
            // In test mode, always send notification with actual values
            sendNotification(deficit.roundToInt(), isTest = true)
        } else {
            // In normal mode, only send if deficit is significant (> 100ml)
            // and we are actually "in" the day (progress > 0)
            if (deficit > 100 && progressPercentage > 0) {
                sendNotification(deficit.roundToInt(), isTest = false)
            }
        }

        return Result.success()
    }

    private fun sendNotification(missingAmountMl: Int, isTest: Boolean) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "hydration_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Hydration Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminds you to drink water"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val title: String
        val text: String

        val amountLiters = abs(missingAmountMl) / 1000.0
        val formattedAmount = String.format("%.1f", amountLiters)

        if (missingAmountMl > 0) {
            // Deficit
            title = if (isTest) "Test: Trinken nicht vergessen!" else "Trinken nicht vergessen!"
            text = "Du liegst ca. $formattedAmount L unter deinem Zeitplan. Trink einen Schluck!"
        } else {
            // Surplus (only shown in test mode usually)
            title = "Test: Alles im grünen Bereich!"
            text = "Du bist aktuell $formattedAmount L über deinem Zeitplan. Weiter so!"
        }

        // Create intent to open MainActivity
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) 
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent) // Set the intent
            .setAutoCancel(true) // Remove notification on click
            .build()

        notificationManager.notify(1, notification)
    }
}
