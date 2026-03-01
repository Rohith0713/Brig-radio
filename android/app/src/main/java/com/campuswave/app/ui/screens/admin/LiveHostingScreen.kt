package com.campuswave.app.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.data.models.Radio
import com.campuswave.app.data.network.ApiConfig
import com.campuswave.app.services.AudioServiceManager
import com.campuswave.app.services.AudioPlaybackService
import com.campuswave.app.ui.theme.*
import com.campuswave.app.utils.UrlUtils
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveHostingScreen(
    radio: Radio,
    onBackClick: () -> Unit,
    onStartHosting: (String) -> Unit,  // "AUDIO" or "VIDEO"
    onPauseHosting: () -> Unit,
    onResumeHosting: () -> Unit,
    onEndHosting: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    val context = LocalContext.current
    var elapsedTime by remember { mutableLongStateOf(radio.current_duration_seconds.toLong()) }
    
    // Observe playback state for admin audio
    val playbackState by AudioServiceManager.playbackState.collectAsState()
    
    // Sync with server updates
    LaunchedEffect(radio.id, radio.current_duration_seconds) {
        elapsedTime = radio.current_duration_seconds.toLong()
    }
    
    // Parse end time for audio service
    val endTimeMillis = remember(radio.end_time) {
        try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
            inputFormat.timeZone = java.util.TimeZone.getDefault()
            val cleanTimestamp = if (radio.end_time.contains(".")) {
                radio.end_time.substringBefore(".")
            } else {
                radio.end_time
            }
            inputFormat.parse(cleanTimestamp)?.time ?: 0L
        } catch (e: Exception) { 0L }
    }
    
    // Auto-start/pause/resume/stop audio based on host_status
    LaunchedEffect(radio.host_status) {
        val baseUrl = ApiConfig.BASE_URL.removeSuffix("/api/")
        val isVideo = radio.media_type == "VIDEO" || (radio.media_url?.endsWith(".mp4") == true)
        
        when (radio.host_status) {
            "HOSTING" -> {
                // Start or resume local audio playback for admin
                if (!isVideo && !radio.media_url.isNullOrEmpty()) {
                    val mediaUrl = UrlUtils.joinUrl(baseUrl, radio.media_url!!)
                    if (mediaUrl != null) {
                        if (playbackState is AudioPlaybackService.PlaybackState.Idle ||
                            playbackState is AudioPlaybackService.PlaybackState.Stopped) {
                            AudioServiceManager.startPlayback(
                                context = context,
                                mediaUrl = mediaUrl,
                                radioTitle = radio.title ?: "Live Radio",
                                radioId = radio.id,
                                eventEndTimeMillis = endTimeMillis
                            )
                        } else {
                            AudioServiceManager.resume(elapsedTime * 1000)
                        }
                    }
                }
            }
            "PAUSED" -> {
                AudioServiceManager.pause()
            }
            "ENDED" -> {
                AudioServiceManager.stop(context)
            }
        }
    }
    
    // Cleanup audio on screen exit
    DisposableEffect(Unit) {
        onDispose {
            // Don't stop audio on back press if still hosting — let the service run
        }
    }
    
    // Timer for live duration
    LaunchedEffect(radio.host_status) {
        if (radio.host_status == "HOSTING") {
            while (true) {
                delay(1000)
                elapsedTime += 1
            }
        }
    }
    
    Scaffold(
        containerColor = CampusBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Host Radio",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Radio Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CampusSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = radio.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = CampusDark,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = when (radio.host_status) {
                            "HOSTING" -> StatusLive
                            "PAUSED" -> AccentOrange
                            "ENDED" -> CampusGrey
                            else -> PrimaryBlue
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (radio.host_status == "HOSTING") {
                                // Pulsing dot for live
                                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                                val alpha by infiniteTransition.animateFloat(
                                    initialValue = 0.3f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(500),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "alphaAnim"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = alpha))
                                )
                            }
                            Text(
                                text = when (radio.host_status) {
                                    "HOSTING" -> "🔴 LIVE"
                                    "PAUSED" -> "⏸ PAUSED"
                                    "ENDED" -> "✓ ENDED"
                                    else -> "● READY"
                                },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    // Media Type Badge
                    if (radio.media_type != "NONE") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (radio.media_type == "VIDEO") PrimaryPurple else PrimaryBlue
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (radio.media_type == "VIDEO") 
                                        Icons.Default.Videocam else Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = radio.media_type,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    // Timer
                    if (radio.host_status == "HOSTING" || radio.host_status == "PAUSED") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = formatDuration(elapsedTime),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (radio.host_status == "HOSTING") StatusLive else CampusGrey
                        )
                        Text(
                            text = "Duration",
                            color = CampusGrey,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            // Stats Row: Listener Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CampusSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = radio.participant_count.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CampusDark
                        )
                        Text(
                            text = "Listeners",
                            fontSize = 12.sp,
                            color = CampusGrey
                        )
                    }
                }
                
                // Placeholder for Mute/Unmute if needed later
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CampusSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "LIVE",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                        Text(
                            text = "Mic Status",
                            fontSize = 12.sp,
                            color = CampusGrey
                        )
                    }
                }
            }
            
            // Error Message
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f))
                ) {
                    Text(
                        text = error,
                        color = ErrorRed,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Control Buttons
            when (radio.host_status) {
                "NOT_STARTED" -> {
                    // Start Hosting Options
                    Text(
                        text = "Choose how to host this radio session:",
                        color = CampusGrey,
                        fontSize = 16.sp
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Audio Button
                        Button(
                            onClick = { onStartHosting("AUDIO") },
                            modifier = Modifier
                                .weight(1f)
                                .height(120.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            enabled = !isLoading
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Audio",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                        
                        // Video Button
                        Button(
                            onClick = { onStartHosting("VIDEO") },
                            modifier = Modifier
                                .weight(1f)
                                .height(120.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            enabled = !isLoading
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Video",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }
                
                "HOSTING" -> {
                    // Live Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Pause Button
                        Button(
                            onClick = onPauseHosting,
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pause", fontWeight = FontWeight.Bold)
                        }
                        
                        // End Button
                        Button(
                            onClick = onEndHosting,
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("End Radio", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                "PAUSED" -> {
                    // Paused Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Resume Button
                        Button(
                            onClick = onResumeHosting,
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Resume", fontWeight = FontWeight.Bold)
                        }
                        
                        // End Button
                        Button(
                            onClick = onEndHosting,
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("End Radio", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                "ENDED" -> {
                    // Radio ended message
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.1f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Radio has ended",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = CampusDark
                            )
                            Text(
                                text = "Total duration: ${formatDuration(elapsedTime)}",
                                color = CampusGrey
                            )
                        }
                    }
                    
                    Button(
                        onClick = onBackClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Return to Dashboard", fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            // Loading indicator
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp),
                    color = PrimaryBlue
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%02d:%02d", minutes, secs)
    }
}
