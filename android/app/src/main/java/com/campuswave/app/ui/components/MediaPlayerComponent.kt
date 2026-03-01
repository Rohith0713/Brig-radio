package com.campuswave.app.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.animation.*

/**
 * Composable for playing audio/video media in live radio sessions
 */
@Composable
fun MediaPlayerComponent(
    mediaUrl: String,
    isVideo: Boolean = false,
    showLiveHeader: Boolean = true,
    isPausedByHost: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Create ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.parse(mediaUrl))
            setMediaItem(mediaItem)
            playWhenReady = true
            prepare()
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    isLoading = playbackState == Player.STATE_BUFFERING
                    if (playbackState == Player.STATE_READY) {
                        isLoading = false
                    }
                }
                
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }
            })
        }
    }
    
    // Auto-pause/resume based on host status
    LaunchedEffect(isPausedByHost) {
        if (isPausedByHost) {
            exoPlayer.pause()
        } else {
            // Resume if it was paused by host
            exoPlayer.play()
        }
    }
    
    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (showLiveHeader) 16.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(if (showLiveHeader) 12.dp else 0.dp)
        ) {
            // Header
            if (showLiveHeader) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isVideo) Icons.Default.VideoLibrary else Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color(0xFFE91E63)
                    )
                    Text(
                        text = if (isVideo) "Live Video" else "Live Audio",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    
                    // Live indicator
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFE91E63)
                    ) {
                        Text(
                            text = "LIVE",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            if (isVideo) {
                // Video player view - larger and with proper controls
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = true
                            setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                            controllerAutoShow = true
                        }
                    },
                    update = { playerView ->
                        playerView.player = exoPlayer
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                )
            } else {
                // Audio player with custom controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2D2D2D), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Audio visualization placeholder
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(20) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height((20..50).random().dp)
                                    .background(
                                        if (isPlaying) Color(0xFFE91E63) else Color(0xFF4A4A4A),
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }
                    
                    // Playback controls
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play/Pause button
                        FilledIconButton(
                            onClick = {
                                if (isPlaying) {
                                    exoPlayer.pause()
                                } else {
                                    exoPlayer.play()
                                }
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color(0xFFE91E63)
                            ),
                            modifier = Modifier.size(56.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        
                        // Stop button
                        IconButton(
                            onClick = {
                                exoPlayer.stop()
                                exoPlayer.seekTo(0)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop",
                                tint = Color.White
                            )
                        }
                    }
                    
                    Text(
                        text = if (isPlaying) "Now Playing..." else "Tap play to listen",
                        color = Color(0xFF9CA3AF),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * Simple audio player for non-live content
 */
@Composable
fun SimpleAudioPlayer(
    mediaUrl: String,
    modifier: Modifier = Modifier
) {
    MediaPlayerComponent(
        mediaUrl = mediaUrl,
        isVideo = false,
        showLiveHeader = false,
        modifier = modifier
    )
}

/**
 * Simple video player for non-live content
 */
@Composable
fun SimpleVideoPlayer(
    mediaUrl: String,
    modifier: Modifier = Modifier
) {
    FeedVideoPlayer(mediaUrl = mediaUrl, shouldPlay = true, modifier = modifier)
}

/**
 * Optimized video player for Social Feeds (College Updates)
 * Automatically adjusts height based on video aspect ratio to avoid black bars and cropping.
 * Supports Instagram-like behavior: autoplay on scroll (via shouldPlay), mute/unmute, 2x speed hold.
 */
@Composable
fun FeedVideoPlayer(
    mediaUrl: String,
    shouldPlay: Boolean, // Controlled by parent scroll position
    modifier: Modifier = Modifier,
    onDoubleTap: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // Internal playback states
    var isMuted by remember { mutableStateOf(true) }
    var isPausedManually by remember { mutableStateOf(false) }
    var isHoldingFastPlayback by remember { mutableStateOf(false) }
    var showOverlayControls by remember { mutableStateOf(false) }
    
    // Create ExoPlayer with auto-play and loop mode
    val exoPlayer = remember(mediaUrl) {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.parse(mediaUrl))
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ALL
            prepare()
        }
    }
    
    // Update player based on external shouldPlay and internal manual controls
    LaunchedEffect(shouldPlay, isPausedManually) {
        if (shouldPlay && !isPausedManually) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    // Sync volume with isMuted
    LaunchedEffect(isMuted) {
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    // Handle 2x speed playback
    LaunchedEffect(isHoldingFastPlayback) {
        exoPlayer.setPlaybackSpeed(if (isHoldingFastPlayback) 2.0f else 1.0f)
    }

    // Auto-hide overlay controls after some time
    LaunchedEffect(showOverlayControls) {
        if (showOverlayControls) {
            kotlinx.coroutines.delay(3000)
            showOverlayControls = false
        }
    }
    
    // Cleanup on dispose
    DisposableEffect(mediaUrl) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .pointerInput(mediaUrl) {
                detectTapGestures(
                    onTap = { showOverlayControls = !showOverlayControls },
                    onDoubleTap = { onDoubleTap() },
                    onLongPress = { offset ->
                        // Long press on right side triggers 2x speed
                        if (offset.x > size.width / 2) {
                            isHoldingFastPlayback = true
                        }
                    },
                    onPress = {
                        tryAwaitRelease()
                        isHoldingFastPlayback = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
            },
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
        )

        // Overlay Controls
        androidx.compose.animation.AnimatedVisibility(
            visible = showOverlayControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.align(Alignment.TopEnd),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Mute Toggle
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isMuted) "Unmute" else "Mute",
                            tint = Color.White
                        )
                    }

                    // Pause/Resume Toggle
                    IconButton(
                        onClick = { isPausedManually = !isPausedManually },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = if (isPausedManually) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isPausedManually) "Play" else "Pause",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Fast Forward visual indicator (2x)
        if (isHoldingFastPlayback) {
            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.FastForward, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("2x", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

