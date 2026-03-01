package com.campuswave.app.data.network

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject

class SignalingClient {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    
    // For emulator use 10.0.2.2, for real device use your PC's IP address
    // PRO TIP: Change this IP to your computer's local IP (e.g., 192.168.1.X) if testing on real device
    private val signalingUrl = "ws://10.0.2.2:8765" 
    
    private val _signalingEvents = MutableSharedFlow<JSONObject>()
    val signalingEvents: SharedFlow<JSONObject> = _signalingEvents

    private val socketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket connected")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "Message received: $text")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val json = JSONObject(text)
                    _signalingEvents.emit(json)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing message", e)
                }
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closed: $reason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "WebSocket failure", t)
        }
    }

    fun connect() {
        if (webSocket != null) return
        val request = Request.Builder().url(signalingUrl).build()
        webSocket = client.newWebSocket(request, socketListener)
    }

    fun sendJoin(roomId: String) {
        val json = JSONObject().apply {
            put("type", "join")
            put("room_id", roomId)
        }
        send(json)
    }

    fun sendOffer(roomId: String, offerSdp: String) {
        val json = JSONObject().apply {
            put("type", "offer")
            put("room_id", roomId)
            put("payload", JSONObject().apply {
                put("type", "offer")
                put("sdp", offerSdp)
            })
        }
        send(json)
    }

    fun sendAnswer(roomId: String, answerSdp: String) {
        val json = JSONObject().apply {
            put("type", "answer")
            put("room_id", roomId)
            put("payload", JSONObject().apply {
                put("type", "answer")
                put("sdp", answerSdp)
            })
        }
        send(json)
    }

    fun sendIceCandidate(roomId: String, sdpMid: String, sdpMLineIndex: Int, sdpCandidate: String) {
        val json = JSONObject().apply {
            put("type", "ice_candidate")
            put("room_id", roomId)
            put("payload", JSONObject().apply {
                put("candidate", sdpCandidate)
                put("sdpMid", sdpMid)
                put("sdpMLineIndex", sdpMLineIndex)
            })
        }
        send(json)
    }
    
    /**
     * CRITICAL: Send admin pause event to all students in room
     */
    fun sendAdminPause(roomId: String) {
        val json = JSONObject().apply {
            put("type", "admin_pause")
            put("room_id", roomId)
        }
        send(json)
        Log.d(TAG, "🔴 Admin pause event sent for room: $roomId")
    }
    
    /**
     * CRITICAL: Send admin resume event to all students in room
     */
    fun sendAdminResume(roomId: String) {
        val json = JSONObject().apply {
            put("type", "admin_resume")
            put("room_id", roomId)
        }
        send(json)
        Log.d(TAG, "🟢 Admin resume event sent for room: $roomId")
    }
    
    private fun send(json: JSONObject) {
        webSocket?.send(json.toString())
    }

    fun close() {
        webSocket?.close(1000, "User closing")
        webSocket = null
    }

    companion object {
        private const val TAG = "SignalingClient"
    }
}
