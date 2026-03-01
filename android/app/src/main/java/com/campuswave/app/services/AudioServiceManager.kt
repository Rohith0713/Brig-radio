package com.campuswave.app.services

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Singleton manager for AudioPlaybackService
 * Provides a clean API for UI components to control background audio
 */
object AudioServiceManager {
    
    private var service: AudioPlaybackService? = null
    private var serviceBound = false
    
    private val _isServiceBound = MutableStateFlow(false)
    val isServiceBound: StateFlow<Boolean> = _isServiceBound.asStateFlow()
    
    private val _playbackState = MutableStateFlow<AudioPlaybackService.PlaybackState>(
        AudioPlaybackService.PlaybackState.Idle
    )
    val playbackState: StateFlow<AudioPlaybackService.PlaybackState> = _playbackState.asStateFlow()
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val audioBinder = binder as? AudioPlaybackService.AudioServiceBinder
            service = audioBinder?.getService()
            serviceBound = true
            _isServiceBound.value = true
            
            // Observe service playback state
            service?.let { audioService ->
                serviceScope.launch {
                    audioService.playbackState.collect { state ->
                        _playbackState.value = state
                    }
                }
            }
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            serviceBound = false
            _isServiceBound.value = false
        }
    }
    
    /**
     * Start playback of a live radio session
     */
    fun startPlayback(
        context: Context,
        mediaUrl: String,
        radioTitle: String,
        radioId: Int,
        eventEndTimeMillis: Long = 0
    ) {
        // Start service with media URL
        val intent = Intent(context, AudioPlaybackService::class.java).apply {
            putExtra(AudioPlaybackService.EXTRA_MEDIA_URL, mediaUrl)
            putExtra(AudioPlaybackService.EXTRA_RADIO_TITLE, radioTitle)
            putExtra(AudioPlaybackService.EXTRA_RADIO_ID, radioId)
            putExtra(AudioPlaybackService.EXTRA_EVENT_END_TIME, eventEndTimeMillis)
        }
        
        context.startForegroundService(intent)
        
        // Bind to service if not already bound
        if (!serviceBound) {
            context.bindService(
                Intent(context, AudioPlaybackService::class.java),
                connection,
                Context.BIND_AUTO_CREATE
            )
        }
        
        _playbackState.value = AudioPlaybackService.PlaybackState.Buffering
    }
    
    /**
     * Toggle play/pause
     */
    fun playPause() {
        service?.playPause()
    }
    
    /**
     * Pause playback (for admin control)
     */
    fun pause() {
        service?.pause()
    }
    
    /**
     * Resume playback (for admin control)
     */
    fun resume(positionMs: Long = -1) {
        if (positionMs >= 0) {
            service?.seekTo(positionMs)
        }
        service?.play()
    }
    
    /**
     * Stop playback and release resources
     */
    fun stop(context: Context) {
        service?.stop()
        unbindService(context)
    }
    
    /**
     * Bind to existing service (if running)
     */
    fun bindService(context: Context) {
        if (!serviceBound) {
            context.bindService(
                Intent(context, AudioPlaybackService::class.java),
                connection,
                Context.BIND_AUTO_CREATE
            )
        }
    }
    
    /**
     * Unbind from service
     */
    fun unbindService(context: Context) {
        if (serviceBound) {
            context.unbindService(connection)
            serviceBound = false
            _isServiceBound.value = false
            _playbackState.value = AudioPlaybackService.PlaybackState.Stopped
        }
    }
    
    /**
     * Check if audio is currently playing
     */
    fun isPlaying(): Boolean {
        return playbackState.value == AudioPlaybackService.PlaybackState.Playing
    }
}
