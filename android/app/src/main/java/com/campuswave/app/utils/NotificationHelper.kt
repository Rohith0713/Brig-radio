package com.campuswave.app.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.campuswave.app.MainActivity
import com.campuswave.app.R

object NotificationHelper {
    
    private const val CHANNEL_ID = "campuswave_radios"
    private const val CHANNEL_NAME = "Radio Notifications"
    private const val CHANNEL_DESCRIPTION = "Notifications for upcoming and live radio sessions"
    
    /**
     * Create the notification channel (required for Android 8.0+)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Check if notification permission is granted (Android 13+)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
    
    /**
     * Show a notification for a radio reminder
     */
    fun showRadioReminder(
        context: Context,
        radioId: Int,
        radioTitle: String,
        message: String,
        notificationType: NotificationType
    ) {
        if (!hasNotificationPermission(context)) return
        
        // Create intent to open app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("radioId", radioId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            radioId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val (icon, priority) = when (notificationType) {
            NotificationType.THIRTY_MINUTES_BEFORE -> Pair(R.drawable.ic_launcher_foreground, NotificationCompat.PRIORITY_DEFAULT)
            NotificationType.FIVE_MINUTES_BEFORE -> Pair(R.drawable.ic_launcher_foreground, NotificationCompat.PRIORITY_HIGH)
            NotificationType.TWO_MINUTES_BEFORE -> Pair(R.drawable.ic_launcher_foreground, NotificationCompat.PRIORITY_HIGH)
            NotificationType.RADIO_LIVE -> Pair(R.drawable.ic_launcher_foreground, NotificationCompat.PRIORITY_MAX)
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(radioTitle)
            .setContentText(message)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .build()
        
        val notificationId = radioId * 10 + notificationType.ordinal
        
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
    
    /**
     * Show 30 minutes before reminder
     */
    fun showThirtyMinuteReminder(context: Context, radioId: Int, radioTitle: String) {
        showRadioReminder(
            context = context,
            radioId = radioId,
            radioTitle = "⏰ $radioTitle",
            message = "Radio session starts in 30 minutes! Get ready to join.",
            notificationType = NotificationType.THIRTY_MINUTES_BEFORE
        )
    }
    
    /**
     * Show 5 minutes before reminder
     */
    fun showFiveMinuteReminder(context: Context, radioId: Int, radioTitle: String, startTimeStr: String?) {
        val timeMessage = if (startTimeStr != null) {
            try {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                val outputFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                val date = inputFormat.parse(startTimeStr)
                val timeParams = outputFormat.format(date)
                "Radio session starts at $timeParams! Don't miss it!"
            } catch (e: Exception) {
                "Radio session starts in 5 minutes! Don't miss it!"
            }
        } else {
            "Radio session starts in 5 minutes! Don't miss it!"
        }
        
        showRadioReminder(
            context = context,
            radioId = radioId,
            radioTitle = "⚡ $radioTitle",
            message = timeMessage,
            notificationType = NotificationType.FIVE_MINUTES_BEFORE
        )
    }
    
    /**
     * Show 2 minutes before reminder
     */
    fun showTwoMinuteReminder(context: Context, radioId: Int, radioTitle: String) {
        showRadioReminder(
            context = context,
            radioId = radioId,
            radioTitle = "⏳ $radioTitle",
            message = "Starts in 2 minutes! Time to tune in.",
            notificationType = NotificationType.TWO_MINUTES_BEFORE
        )
    }

    /**
     * Show radio is live notification
     */
    fun showRadioLiveNotification(context: Context, radioId: Int, radioTitle: String) {
        showRadioReminder(
            context = context,
            radioId = radioId,
            radioTitle = "🔴 LIVE: $radioTitle",
            message = "The radio session is now live! Tap to join.",
            notificationType = NotificationType.RADIO_LIVE
        )
    }

    
    enum class NotificationType {
        THIRTY_MINUTES_BEFORE,
        FIVE_MINUTES_BEFORE,
        TWO_MINUTES_BEFORE,
        RADIO_LIVE
    }
}
