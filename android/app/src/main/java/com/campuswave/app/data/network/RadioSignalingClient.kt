package com.campuswave.app.data.network

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * WebSocket client for radio control signaling
 * Handles real-time admin control events (pause/resume/stop) for radio sessions
 */
class RadioSignalingClient {
    
    private val tag = "RadioSignalingClient"
    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Events flow for radio control
    private val _radioControlEvents = MutableSharedFlow<RadioControlEvent>(replay = 0)
    val radioControlEvents: SharedFlow<RadioControlEvent> = _radioControlEvents
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MINUTES) // No timeout for WebSocket
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()
    
    /**
     * Connect to signaling server for a specific radio session
     */
    fun connect(radioId: Int) {
        disconnect() // Clean up any existing connection
        
        val request = Request.Builder()
            .url(getSignalingServerUrl())
            .build()
        
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(tag, "✅ WebSocket connected for radio $radioId")
                
                // Join the radio room
                val joinMessage = JSONObject().apply {
                    put("type", "join")
                    put("room_id", "radio_$radioId")
                }
                webSocket.send(joinMessage.toString())
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(tag, "📨 Received message: $text")
                handleMessage(text)
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(tag, "❌ WebSocket error: ${t.message}", t)
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(tag, "🔌 WebSocket closed: $reason")
            }
        })
    }
    
    /**
     * Disconnect from signaling server
     */
    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
        Log.d(tag, "Disconnected")
    }
    
    /**
     * Send pause command (admin only)
     */
    fun sendPauseCommand(radioId: Int) {
        val message = JSONObject().apply {
            put("type", "radio_pause")
            put("room_id", "radio_$radioId")
        }
        webSocket?.send(message.toString())
        Log.d(tag, "🔴 Sent radio_pause command for radio $radioId")
    }
    
    /**
     * Send resume command (admin only)
     */
    fun sendResumeCommand(radioId: Int, currentPosition: Long = 0) {
        val message = JSONObject().apply {
            put("type", "radio_resume")
            put("room_id", "radio_$radioId")
            put("current_position", currentPosition)
        }
        webSocket?.send(message.toString())
        Log.d(tag, "🟢 Sent radio_resume command for radio $radioId at position $currentPosition")
    }
    
    /**
     * Send stop command (admin only)
     */
    fun sendStopCommand(radioId: Int) {
        val message = JSONObject().apply {
            put("type", "radio_stop")
            put("room_id", "radio_$radioId")
        }
        webSocket?.send(message.toString())
        Log.d(tag, "⛔ Sent radio_stop command for radio $radioId")
    }
    
    /**
     * Handle incoming WebSocket messages
     */
    private fun handleMessage(text: String) {
        try {
            val json = JSONObject(text)
            val type = json.getString("type")
            
            when (type) {
                "radio_paused" -> {
                    val roomId = json.optString("room_id", "")
                    val radioId = roomId.removePrefix("radio_").toIntOrNull() ?: 0
                    val timestamp = json.optLong("timestamp", System.currentTimeMillis())
                    
                    scope.launch {
                        _radioControlEvents.emit(
                            RadioControlEvent.Paused(radioId, timestamp)
                        )
                    }
                    Log.w(tag, "🔴 Radio $radioId PAUSED by admin")
                }
                
                "radio_resumed" -> {
                    val roomId = json.optString("room_id", "")
                    val radioId = roomId.removePrefix("radio_").toIntOrNull() ?: 0
                    val timestamp = json.optLong("timestamp", System.currentTimeMillis())
                    val currentPosition = json.optLong("current_position", 0)
                    
                    scope.launch {
                        _radioControlEvents.emit(
                            RadioControlEvent.Resumed(radioId, timestamp, currentPosition)
                        )
                    }
                    Log.w(tag, "🟢 Radio $radioId RESUMED by admin at $currentPosition")
                }
                
                "radio_stopped" -> {
                    val roomId = json.optString("room_id", "")
                    val radioId = roomId.removePrefix("radio_").toIntOrNull() ?: 0
                    val timestamp = json.optLong("timestamp", System.currentTimeMillis())
                    
                    scope.launch {
                        _radioControlEvents.emit(
                            RadioControlEvent.Stopped(radioId, timestamp)
                        )
                    }
                    Log.w(tag, "⛔ Radio $radioId STOPPED by admin")
                }
                
                "joined" -> {
                    Log.d(tag, "Successfully joined radio room")
                }
                
                else -> {
                    Log.d(tag, "Ignoring message type: $type")
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error parsing message: ${e.message}", e)
        }
    }
    
    /**
     * Get signaling server WebSocket URL
     */
    private fun getSignalingServerUrl(): String {
        return try {
            val uri = java.net.URI.create(com.campuswave.app.data.network.ApiConfig.BASE_URL)
            val host = uri.host ?: "10.0.2.2"
            "ws://$host:8765"
        } catch (e: Exception) {
            // Fallback to a hardcoded IP if URI parsing fails
            "ws://10.36.12.110:8765"
        }
    }
    
    /**
     * Sealed class for radio control events
     */
    sealed class RadioControlEvent {
        data class Paused(val radioId: Int, val timestamp: Long) : RadioControlEvent()
        data class Resumed(val radioId: Int, val timestamp: Long, val currentPosition: Long) : RadioControlEvent()
        data class Stopped(val radioId: Int, val timestamp: Long) : RadioControlEvent()
    }
}
