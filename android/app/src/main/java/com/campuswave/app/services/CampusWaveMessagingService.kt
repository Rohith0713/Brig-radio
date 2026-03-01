package com.campuswave.app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.campuswave.app.MainActivity
import com.campuswave.app.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class BRIG_RADIOMessagingService : FirebaseMessagingService() {
    
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        Log.d(TAG, "From: ${message.from}")
        
        // Check if message contains a data payload
        val data = message.data
        if (data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: $data")
            
            val type = data["type"]
            val id = data["content_id"] ?: data["id"]
            val autoStart = data["auto_start"] == "true"
            
            val title = data["title"] ?: message.notification?.title ?: "BRIG RADIO"
            val body = data["body"] ?: message.notification?.body ?: ""
            
            sendNotification(title, body, type, id, autoStart)
        } else {
            // Fallback for notification only messages
            message.notification?.let {
                sendNotification(it.title ?: "BRIG RADIO", it.body ?: "")
            }
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
    }
    
    private fun sendNotification(
        title: String, 
        messageBody: String, 
        type: String? = null, 
        id: String? = null,
        autoStart: Boolean = false
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            if (type != null) {
                putExtra("nav_type", type)
                putExtra("nav_id", id)
                putExtra("nav_auto_start", autoStart)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            System.currentTimeMillis().toInt(), 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val channelId = when (type) {
            "live_radio" -> "live_radio_channel"
            "suggestion" -> "suggestion_status_channel"
            else -> "college_updates_channel"
        }
        
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = when (type) {
                "live_radio" -> "Live Radio"
                "suggestion" -> "Suggestions"
                else -> "College Updates"
            }
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
    
    companion object {
        private const val TAG = "FCMService"
        private const val NOTIFICATION_ID = 0
    }
}
