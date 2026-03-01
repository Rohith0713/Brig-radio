package com.campuswave.app.utils

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.campuswave.app.workers.RadioReminderWorker
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Utility to schedule notification reminders for radio sessions
 */
object NotificationScheduler {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    
    /**
     * Schedule all reminders for a radio session (30 min, 5 min, 2 min, and live)
     */
    fun scheduleRadioReminders(
        context: Context,
        radioId: Int,
        radioTitle: String,
        startTimeStr: String
    ) {
        try {
            val startTime = dateFormat.parse(startTimeStr) ?: return
            val now = Date()
            
            // Calculate delays
            val timeUntilRadio = startTime.time - now.time
            val thirtyMinBefore = timeUntilRadio - (30 * 60 * 1000)
            val fiveMinBefore = timeUntilRadio - (5 * 60 * 1000)
            val twoMinBefore = timeUntilRadio - (2 * 60 * 1000)
            
            // Schedule 30 minutes before (if more than 30 min away)
            if (thirtyMinBefore > 0) {
                scheduleReminder(
                    context = context,
                    radioId = radioId,
                    radioTitle = radioTitle,
                    startTimeStr = startTimeStr,
                    delayMillis = thirtyMinBefore,
                    notificationType = RadioReminderWorker.TYPE_30_MINUTES
                )
            }
            
            // Schedule 5 minutes before (if more than 5 min away)
            if (fiveMinBefore > 0) {
                scheduleReminder(
                    context = context,
                    radioId = radioId,
                    radioTitle = radioTitle,
                    startTimeStr = startTimeStr,
                    delayMillis = fiveMinBefore,
                    notificationType = RadioReminderWorker.TYPE_5_MINUTES
                )
            }

            // Schedule 2 minutes before (if more than 2 min away)
            if (twoMinBefore > 0) {
                scheduleReminder(
                    context = context,
                    radioId = radioId,
                    radioTitle = radioTitle,
                    startTimeStr = startTimeStr,
                    delayMillis = twoMinBefore,
                    notificationType = RadioReminderWorker.TYPE_2_MINUTES
                )
            }
            
            // Schedule live notification (at radio start time)
            if (timeUntilRadio > 0) {
                scheduleReminder(
                    context = context,
                    radioId = radioId,
                    radioTitle = radioTitle,
                    startTimeStr = startTimeStr,
                    delayMillis = timeUntilRadio,
                    notificationType = RadioReminderWorker.TYPE_LIVE
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun scheduleReminder(
        context: Context,
        radioId: Int,
        radioTitle: String,
        startTimeStr: String,
        delayMillis: Long,
        notificationType: String
    ) {
        val inputData = Data.Builder()
            .putInt(RadioReminderWorker.KEY_RADIO_ID, radioId)
            .putString(RadioReminderWorker.KEY_RADIO_TITLE, radioTitle)
            .putString(RadioReminderWorker.KEY_RADIO_START_TIME, startTimeStr)
            .putString(RadioReminderWorker.KEY_NOTIFICATION_TYPE, notificationType)
            .build()
        
        val workTag = "radio_${radioId}_${notificationType}"
        val workRequest = OneTimeWorkRequestBuilder<RadioReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(workTag)
            .build()
        
        WorkManager.getInstance(context).enqueueUniqueWork(
            workTag,
            androidx.work.ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    /**
     * Cancel all reminders for a specific radio session
     */
    fun cancelRadioReminders(context: Context, radioId: Int) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag("radio_${radioId}_${RadioReminderWorker.TYPE_30_MINUTES}")
        workManager.cancelAllWorkByTag("radio_${radioId}_${RadioReminderWorker.TYPE_5_MINUTES}")
        workManager.cancelAllWorkByTag("radio_${radioId}_${RadioReminderWorker.TYPE_2_MINUTES}")
        workManager.cancelAllWorkByTag("radio_${radioId}_${RadioReminderWorker.TYPE_LIVE}")
    }
}
