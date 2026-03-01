package com.campuswave.app.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.campuswave.app.utils.NotificationHelper

/**
 * Worker to show radio reminder notifications at scheduled times
 */
class RadioReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        const val KEY_RADIO_ID = "radio_id"
        const val KEY_RADIO_TITLE = "radio_title"
        const val KEY_RADIO_START_TIME = "radio_start_time"
        const val KEY_NOTIFICATION_TYPE = "notification_type"
        
        const val TYPE_30_MINUTES = "30_minutes"
        const val TYPE_5_MINUTES = "5_minutes"
        const val TYPE_2_MINUTES = "2_minutes"
        const val TYPE_LIVE = "live"
    }
    
    override suspend fun doWork(): Result {
        val radioId = inputData.getInt(KEY_RADIO_ID, -1)
        val radioTitle = inputData.getString(KEY_RADIO_TITLE) ?: return Result.failure()
        val startTimeStr = inputData.getString(KEY_RADIO_START_TIME)
        val notificationType = inputData.getString(KEY_NOTIFICATION_TYPE) ?: return Result.failure()
        
        if (radioId == -1) return Result.failure()
        
        // Ensure notification channel exists
        NotificationHelper.createNotificationChannel(applicationContext)
        
        // Show appropriate notification based on type
        when (notificationType) {
            TYPE_30_MINUTES -> {
                NotificationHelper.showThirtyMinuteReminder(
                    context = applicationContext,
                    radioId = radioId,
                    radioTitle = radioTitle
                )
            }
            TYPE_5_MINUTES -> {
                NotificationHelper.showFiveMinuteReminder(
                    context = applicationContext,
                    radioId = radioId,
                    radioTitle = radioTitle,
                    startTimeStr = startTimeStr
                )
            }
            TYPE_2_MINUTES -> {
                NotificationHelper.showTwoMinuteReminder(
                    context = applicationContext,
                    radioId = radioId,
                    radioTitle = radioTitle
                )
            }
            TYPE_LIVE -> {
                NotificationHelper.showRadioLiveNotification(
                    context = applicationContext,
                    radioId = radioId,
                    radioTitle = radioTitle
                )
            }
        }
        
        return Result.success()
    }
}
