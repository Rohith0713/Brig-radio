package com.campuswave.app.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.campuswave.app.MainActivity
import com.campuswave.app.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Foreground Service for background audio playback of live radio sessions
 */
class AudioPlaybackService : Service() {
    
    companion object {
        const val CHANNEL_ID = "audio_playback_channel"
        const val NOTIFICATION_ID = 1001
        
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"
        
        const val EXTRA_MEDIA_URL = "EXTRA_MEDIA_URL"
        const val EXTRA_RADIO_TITLE = "EXTRA_RADIO_TITLE"
        const val EXTRA_RADIO_ID = "EXTRA_RADIO_ID"
        const val EXTRA_EVENT_END_TIME = "EXTRA_EVENT_END_TIME"
    }
    
    private val binder = AudioServiceBinder()
    
    private var exoPlayer: ExoPlayer? = null
    
    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    
    private var currentRadioTitle: String = "Live Radio"
    private var currentRadioId: Int = 0
    private var eventEndTimeMillis: Long = 0
    private var endTimeCheckRunnable: Runnable? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    
    inner class AudioServiceBinder : Binder() {
        fun getService(): AudioPlaybackService = this@AudioPlaybackService
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> handlePlay(intent)
            ACTION_PAUSE -> handlePause()
            ACTION_STOP -> handleStop()
            else -> {
                // Initial service start with media URL
                val mediaUrl = intent?.getStringExtra(EXTRA_MEDIA_URL)
                val radioTitle = intent?.getStringExtra(EXTRA_RADIO_TITLE)
                val radioId = intent?.getIntExtra(EXTRA_RADIO_ID, 0)
                val endTime = intent?.getLongExtra(EXTRA_EVENT_END_TIME, 0)
                
                if (mediaUrl != null) {
                    currentRadioTitle = radioTitle ?: "Live Radio"
                    currentRadioId = radioId ?: 0
                    eventEndTimeMillis = endTime ?: 0
                    initializePlayer(mediaUrl)
                    startEndTimeMonitoring()
                }
            }
        }
        
        return START_STICKY
    }
    
    private fun initializePlayer(mediaUrl: String) {
        // Release existing player if any
        exoPlayer?.release()
        
        // Ensure audio routes to speaker/headphones in foreground
        ensureSpeakerOutput()
        
        // Create AudioAttributes for proper audio routing
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        // Create new ExoPlayer with audio attributes and focus handling
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true)
            val mediaItem = MediaItem.fromUri(Uri.parse(mediaUrl))
            setMediaItem(mediaItem)
            volume = 1f  // Ensure max player volume for audible output
            playWhenReady = true
            prepare()
            
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            _playbackState.value = PlaybackState.Buffering
                            updateNotification(isPlaying = false)
                        }
                        Player.STATE_READY -> {
                            _playbackState.value = PlaybackState.Playing
                            updateNotification(isPlaying = true)
                        }
                        Player.STATE_ENDED -> {
                            _playbackState.value = PlaybackState.Stopped
                            stopSelf()
                        }
                        Player.STATE_IDLE -> {
                            _playbackState.value = PlaybackState.Idle
                        }
                    }
                }
                
                override fun onIsPlayingChanged(playing: Boolean) {
                    _playbackState.value = if (playing) PlaybackState.Playing else PlaybackState.Paused
                    updateNotification(isPlaying = playing)
                }

                override fun onPlayerError(error: PlaybackException) {
                    android.util.Log.e("AudioPlayback", "ExoPlayer error: ${error.message}", error)
                    android.util.Log.e("AudioPlayback", "Error code: ${error.errorCode}, cause: ${error.cause}")
                    _playbackState.value = PlaybackState.Error(error.message ?: "Playback error")
                    updateNotification(isPlaying = false)
                }
            })
        }
        
        // Start foreground service
        val notification = buildNotification(isPlaying = true)
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun handlePlay(intent: Intent? = null) {
        if (intent != null) {
            // New playback request
            val mediaUrl = intent.getStringExtra(EXTRA_MEDIA_URL)
            val radioTitle = intent.getStringExtra(EXTRA_RADIO_TITLE)
            val radioId = intent.getIntExtra(EXTRA_RADIO_ID, 0)
            
            if (mediaUrl != null) {
                currentRadioTitle = radioTitle ?: "Live Radio"
                currentRadioId = radioId
                initializePlayer(mediaUrl)
            }
        } else {
            // Resume existing playback
            exoPlayer?.play()
        }
    }
    
    private fun handlePause() {
        exoPlayer?.pause()
    }
    
    
    private fun startEndTimeMonitoring() {
        if (eventEndTimeMillis > 0) {
            endTimeCheckRunnable = object : Runnable {
                override fun run() {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime >= eventEndTimeMillis) {
                        // Event has ended, stop playback
                        handleStop()
                    } else {
                        // Check again in 1 second
                        handler.postDelayed(this, 1000)
                    }
                }
            }
            handler.post(endTimeCheckRunnable!!)
        }
    }
    
    private fun handleStop() {
        _playbackState.value = PlaybackState.Stopped
        endTimeCheckRunnable?.let { handler.removeCallbacks(it) }
        resetAudioRouting()
        stopSelf()
    }
    
    /**
     * Configure the Android AudioManager for speaker output.
     * Ensures audio plays through the loudspeaker (or headphones if connected)
     * rather than staying silent or routing to the earpiece.
     */
    private fun ensureSpeakerOutput() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        audioManager.mode = android.media.AudioManager.MODE_NORMAL
        audioManager.isSpeakerphoneOn = true
        android.util.Log.d("AudioPlayback", "Speaker output enabled, mode=MODE_NORMAL")
    }
    
    /**
     * Reset audio routing to defaults when playback stops.
     */
    private fun resetAudioRouting() {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
            audioManager.mode = android.media.AudioManager.MODE_NORMAL
            audioManager.isSpeakerphoneOn = false
        } catch (e: Exception) {
            android.util.Log.e("AudioPlayback", "Error resetting audio routing", e)
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audio Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Playback controls for live radio"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun buildNotification(isPlaying: Boolean): Notification {
        // Intent to open app when notification is clicked
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Play/Pause action
        val playPauseIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, AudioPlaybackService::class.java).apply {
                action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Stop action
        val stopIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, AudioPlaybackService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(currentRadioTitle)
            .setContentText("Live Radio • Campus Wave")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(contentIntent)
            .setOngoing(isPlaying)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                if (isPlaying) "Pause" else "Play",
                playPauseIntent
            )
            .addAction(
                R.drawable.ic_stop,
                "Stop",
                stopIntent
            )
            .build()
    }
    
    private fun updateNotification(isPlaying: Boolean) {
        val notification = buildNotification(isPlaying)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    fun playPause() {
        exoPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }
    
    fun pause() {
        exoPlayer?.pause()
    }
    
    fun play() {
        exoPlayer?.play()
    }
    
    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }
    
    fun stop() {
        handleStop()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        endTimeCheckRunnable?.let { handler.removeCallbacks(it) }
        exoPlayer?.release()
        resetAudioRouting()
        _playbackState.value = PlaybackState.Idle
    }
    
    sealed class PlaybackState {
        object Idle : PlaybackState()
        object Buffering : PlaybackState()
        object Playing : PlaybackState()
        object Paused : PlaybackState()
        object Stopped : PlaybackState()
        data class Error(val message: String) : PlaybackState()
    }
}
