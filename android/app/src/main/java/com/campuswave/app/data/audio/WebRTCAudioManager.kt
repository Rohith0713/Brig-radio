package com.campuswave.app.data.audio

import android.content.Context
import android.util.Log
import com.campuswave.app.data.network.SignalingClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.webrtc.*
import java.util.concurrent.ConcurrentHashMap

class WebRTCAudioManager(
    private val context: Context,
    private val signalingClient: SignalingClient
) {

    private val peerConnectionFactory: PeerConnectionFactory
    private val audioSource: AudioSource
    private val localAudioTrack: AudioTrack
    private val peerConnections = ConcurrentHashMap<String, PeerConnection>()
    
    private var localPeerId: String? = null
    private var currentRoomId: String? = null
    private var isPaused = false // Track pause state

    // States for UI
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _connectionStatus = MutableStateFlow("Disconnected")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    // ICE Servers
    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
    )

    init {
        // Initialize WebRTC
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(PeerConnectionFactory.Options())
            .createPeerConnectionFactory()

        // Create Audio Source and Track
        val audioConstraints = MediaConstraints()
        audioSource = peerConnectionFactory.createAudioSource(audioConstraints)
        localAudioTrack = peerConnectionFactory.createAudioTrack("ARDAMSa0", audioSource)
        
        // Listen for signaling events
        CoroutineScope(Dispatchers.Main).launch {
            signalingClient.signalingEvents.collect { json ->
                handleSignalingEvent(json)
            }
        }
    }

    fun connect(roomId: String) {
        currentRoomId = roomId
        signalingClient.connect()
        signalingClient.sendJoin(roomId)
        _connectionStatus.value = "Connecting..."
    }
    
    fun disconnect() {
        peerConnections.values.forEach { it.close() }
        peerConnections.clear()
        signalingClient.close()
        _connectionStatus.value = "Disconnected"
    }

    /**
     * Mute/unmute audio
     * @param isMuted Whether audio should be muted
     * @param isAdmin If true, broadcasts pause/resume to students via signaling
     */
    fun mute(isMuted: Boolean, isAdmin: Boolean = false) {
        localAudioTrack.setEnabled(!isMuted)
        _isSpeaking.value = !isMuted
        isPaused = isMuted
        
        // Admin broadcasts pause/resume events to all students
        if (isAdmin) {
            currentRoomId?.let { roomId ->
                if (isMuted) {
                    Log.w(TAG, "🔴 Admin pausing podcast - broadcasting to students")
                    signalingClient.sendAdminPause(roomId)
                } else {
                    Log.w(TAG, "🟢 Admin resuming podcast - broadcasting to students")
                    signalingClient.sendAdminResume(roomId)
                }
            }
        }
    }
    
    /**
     * Enforce pause state (backup to signaling)
     * Called by student polling to ensure pause state is synced with backend
     */
    fun enforcePauseState(shouldBePaused: Boolean) {
        if (shouldBePaused != isPaused) {
            Log.w(TAG, "⚠️ Pause state mismatch detected. Forcing sync to: $shouldBePaused")
            // Force all remote audio tracks to match backend state
            peerConnections.values.forEach { pc ->
                try {
                    pc.receivers.forEach { receiver ->
                        receiver.track()?.setEnabled(!shouldBePaused)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error enforcing pause state", e)
                }
            }
            isPaused = shouldBePaused
            _isSpeaking.value = !shouldBePaused
        }
    }

    private fun handleSignalingEvent(json: JSONObject) {
        val type = json.optString("type")
        
        when (type) {
            "peer_joined" -> {
                val peerId = json.getString("peer_id")
                Log.d(TAG, "Peer joined: $peerId")
                createPeerConnection(peerId, isInitiator = true)
            }
            "offer" -> {
                val peerId = json.getString("from")
                val payload = json.getJSONObject("payload")
                val sdp = payload.getString("sdp")
                Log.d(TAG, "Received offer from: $peerId")
                
                val pc = createPeerConnection(peerId, isInitiator = false)
                pc?.setRemoteDescription(SimpleSdpObserver(), SessionDescription(SessionDescription.Type.OFFER, sdp))
                
                // Create Answer
                val constraints = MediaConstraints()
                pc?.createAnswer(object : SimpleSdpObserver() {
                    override fun onCreateSuccess(desc: SessionDescription?) {
                        desc?.let {
                            pc.setLocalDescription(SimpleSdpObserver(), it)
                            currentRoomId?.let { roomId ->
                                signalingClient.sendAnswer(roomId, it.description)
                            }
                        }
                    }
                }, constraints)
            }
            "answer" -> {
                val peerId = json.getString("from")
                val payload = json.getJSONObject("payload")
                val sdp = payload.getString("sdp")
                Log.d(TAG, "Received answer from: $peerId")
                
                val pc = peerConnections[peerId]
                pc?.setRemoteDescription(SimpleSdpObserver(), SessionDescription(SessionDescription.Type.ANSWER, sdp))
            }
            "ice_candidate" -> {
                val peerId = json.getString("from")
                val payload = json.getJSONObject("payload")
                
                val pc = peerConnections[peerId]
                val candidate = IceCandidate(
                    payload.getString("sdpMid"),
                    payload.getInt("sdpMLineIndex"),
                    payload.getString("candidate")
                )
                pc?.addIceCandidate(candidate)
            }
            "peer_left" -> {
                val peerId = json.getString("peer_id")
                peerConnections[peerId]?.close()
                peerConnections.remove(peerId)
            }
            
            "admin_paused" -> {
                Log.w(TAG, "🔴 ADMIN PAUSED - Disabling all remote audio tracks")
                // CRITICAL: Disable ALL remote audio tracks to enforce silence
                peerConnections.values.forEach { pc ->
                    try {
                        pc.receivers.forEach { receiver ->
                            receiver.track()?.let { track ->
                                Log.d(TAG, "Disabling track: ${track.id()}")
                                track.setEnabled(false)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error disabling track on pause", e)
                    }
                }
                isPaused = true
                _isSpeaking.value = false // Update UI to show paused state
            }
            
            "admin_resumed" -> {
                Log.w(TAG, "🟢 ADMIN RESUMED - Re-enabling all remote audio tracks")
                // Re-enable remote audio tracks
                peerConnections.values.forEach { pc ->
                    try {
                        pc.receivers.forEach { receiver ->
                            receiver.track()?.let { track ->
                                Log.d(TAG, "Enabling track: ${track.id()}")
                                track.setEnabled(true)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error enabling track on resume", e)
                    }
                }
                isPaused = false
            }
        }
    }

    private fun createPeerConnection(peerId: String, isInitiator: Boolean): PeerConnection? {
        if (peerConnections.containsKey(peerId)) return peerConnections[peerId]

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        
        val pc = peerConnectionFactory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate?) {
                candidate?.let {
                    currentRoomId?.let { roomId -> 
                        signalingClient.sendIceCandidate(roomId, it.sdpMid, it.sdpMLineIndex, it.sdp)
                    }
                }
            }

            override fun onAddStream(stream: MediaStream?) {
                Log.d(TAG, "Received remote stream from $peerId")
                // In a pure audio app, standard WebRTC handles audio playback automatically 
                // via the native ADM (Audio Device Module) once the track is added.
                // We just need to ensure the track is enabled.
                stream?.audioTracks?.firstOrNull()?.setEnabled(true)
            }

            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                Log.d(TAG, "ICE Connection State for $peerId: $state")
                if (state == PeerConnection.IceConnectionState.CONNECTED) {
                    _connectionStatus.value = "Connected"
                }
            }

            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {}
        })

        if (pc != null) {
            pc.addStream(peerConnectionFactory.createLocalMediaStream("ARDAMS").apply {
                addTrack(localAudioTrack)
            })
            peerConnections[peerId] = pc
            
            if (isInitiator) {
                val constraints = MediaConstraints()
                pc.createOffer(object : SimpleSdpObserver() {
                    override fun onCreateSuccess(desc: SessionDescription?) {
                        desc?.let {
                            pc.setLocalDescription(SimpleSdpObserver(), it)
                            currentRoomId?.let { roomId ->
                                signalingClient.sendOffer(roomId, it.description)
                            }
                        }
                    }
                }, constraints)
            }
        }

        return pc
    }

    companion object {
        private const val TAG = "WebRTCAudioManager"
    }
    
    // Helper for SdpObserver to reduce boilerplate
    open class SimpleSdpObserver : SdpObserver {
        override fun onCreateSuccess(p0: SessionDescription?) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(p0: String?) { Log.e(TAG, "SDP Create Failure: $p0") }
        override fun onSetFailure(p0: String?) { Log.e(TAG, "SDP Set Failure: $p0") }
    }
}
