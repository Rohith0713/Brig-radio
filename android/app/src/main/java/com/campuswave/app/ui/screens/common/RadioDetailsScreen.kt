package com.campuswave.app.ui.screens.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.data.models.Radio
import com.campuswave.app.data.network.ApiConfig
import com.campuswave.app.services.AudioServiceManager
import com.campuswave.app.services.AudioPlaybackService
import com.campuswave.app.ui.components.AudioPlaybackIndicator
import com.campuswave.app.ui.theme.*
import com.campuswave.app.utils.UrlUtils
import com.campuswave.app.utils.DateUtils
import coil.compose.SubcomposeAsyncImage
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioDetailsScreen(
    radio: Radio,
    userRole: String,
    onBackClick: () -> Unit,
    autoStart: Boolean = false,
    onJoinLiveClick: (Radio) -> Unit = {},
    onEditClick: (Radio) -> Unit = {},
    onDeleteClick: (Radio) -> Unit = {},
    onToggleReminder: (Radio) -> Unit = {},
    onHostClick: () -> Unit = {},
    synchronizedTimeMillis: Long = System.currentTimeMillis()
) {
    val isAdmin = userRole == "ADMIN"
    
    val context = LocalContext.current
    
    // Timer synchronization for live duration
    var elapsedTime by remember { mutableLongStateOf(radio.current_duration_seconds.toLong()) }
    
    // Sync with server updates
    LaunchedEffect(radio.id, radio.current_duration_seconds) {
        elapsedTime = radio.current_duration_seconds.toLong()
    }
    
    // Timer Effect
    LaunchedEffect(radio.host_status) {
        if (radio.host_status == "HOSTING") {
            while (true) {
                delay(1000)
                elapsedTime += 1
            }
        }
    }
    
    // Observe playback state
    val playbackState by AudioServiceManager.playbackState.collectAsState()
    val isPlaying = playbackState is AudioPlaybackService.PlaybackState.Playing
    
    // WebSocket signaling for real-time admin control (students only)
    val signalingClient = remember { com.campuswave.app.data.network.RadioSignalingClient() }
    
    // Connect to WebSocket for live radios (students only)
    LaunchedEffect(radio.id, radio.status, userRole) {
        if (radio.status == "LIVE" && !isAdmin) {
            signalingClient.connect(radio.id)
            
            // Collect events in separate coroutine to not block UI
            launch {
                signalingClient.radioControlEvents.collect { event ->
                    when (event) {
                        is com.campuswave.app.data.network.RadioSignalingClient.RadioControlEvent.Paused -> {
                            android.util.Log.w("RadioDetails", "🔴 PAUSED by admin - stopping playback")
                            Toast.makeText(context, "Paused by admin", Toast.LENGTH_SHORT).show()
                            AudioServiceManager.pause()
                        }
                        is com.campuswave.app.data.network.RadioSignalingClient.RadioControlEvent.Resumed -> {
                            android.util.Log.w("RadioDetails", "🟢 RESUMED by admin - resuming playback from ${event.currentPosition}ms")
                            Toast.makeText(context, "Resumed by admin", Toast.LENGTH_SHORT).show()
                            AudioServiceManager.resume(event.currentPosition)
                        }
                        is com.campuswave.app.data.network.RadioSignalingClient.RadioControlEvent.Stopped -> {
                            android.util.Log.w("RadioDetails", "⛔ STOPPED by admin - ending session")
                            Toast.makeText(context, "Session ended by admin", Toast.LENGTH_SHORT).show()
                            AudioServiceManager.stop(context)
                            onBackClick()
                        }
                    }
                }
            }
        }
    }
    
    // Cleanup WebSocket on screen exit
    DisposableEffect(Unit) {
        onDispose {
            signalingClient.disconnect()
        }
    }
    
    //Parse end time
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
        } catch (e: Exception) {
            0L
        }
    }
    
    LaunchedEffect(radio.host_status, radio.status) {
        if (radio.host_status == "ENDED" || radio.status == "COMPLETED" || radio.status == "MISSED") {
            Toast.makeText(context, "Event ended by admin", Toast.LENGTH_SHORT).show()
            AudioServiceManager.stop(context)
            onBackClick()
        }
    }

    // Effect for Pause/Resume notifications from polling (backup to WebSocket)
    var lastHostStatus by remember { mutableStateOf(radio.host_status) }
    LaunchedEffect(radio.host_status) {
        if (lastHostStatus == "HOSTING" && radio.host_status == "PAUSED") {
            // Only show toast if not already paused (avoid duplicate from WebSocket)
            if (isPlaying) {
                Toast.makeText(context, "Event paused by admin", Toast.LENGTH_SHORT).show()
                AudioServiceManager.pause()
            }
        } else if (lastHostStatus == "PAUSED" && radio.host_status == "HOSTING") {
            Toast.makeText(context, "Event resumed by admin", Toast.LENGTH_SHORT).show()
        }
        lastHostStatus = radio.host_status
    }

    // Auto-start logic for deep links
    var hasAutoStarted by remember { mutableStateOf(false) }
    LaunchedEffect(radio, autoStart) {
        if (autoStart && !hasAutoStarted && radio.status == "LIVE" && radio.host_status == "HOSTING" && !isAdmin) {
            val baseUrl = ApiConfig.BASE_URL.removeSuffix("/api/")
            val isVideo = radio.media_type == "VIDEO" || (radio.media_url?.endsWith(".mp4") == true)
            
            if (!isVideo && !radio.media_url.isNullOrEmpty()) {
                val mediaUrl = UrlUtils.joinUrl(baseUrl, radio.media_url!!)
                if (mediaUrl != null && !isPlaying) {
                    AudioServiceManager.startPlayback(
                        context = context,
                        mediaUrl = mediaUrl,
                        radioTitle = radio.title ?: "Live Radio",
                        radioId = radio.id,
                        eventEndTimeMillis = endTimeMillis
                    )
                    hasAutoStarted = true
                }
            } else if (isVideo) {
                 // Video auto-start is handled by the MediaPlayerComponent itself usually,
                 // or we could trigger a join live event.
                 onJoinLiveClick(radio)
                 hasAutoStarted = true
            }
        }
    }

    Scaffold(
        containerColor = campusBackground(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Radio Session",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (LocalIsDarkTheme.current) Color.White else Color(0xFF1E293B)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.background(if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (LocalIsDarkTheme.current) Color.White else Color(0xFF1E293B)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = campusBackground()
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Media Player / Banner Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(campusSurface())
            ) {
                if (radio.status == "LIVE" && (radio.host_status == "HOSTING" || radio.host_status == "PAUSED")) {
                    val baseUrl = ApiConfig.BASE_URL.removeSuffix("/api/")
                    val isVideo = radio.media_type == "VIDEO" || 
                                 (radio.media_url?.endsWith(".mp4") == true)
                    
                    if (!radio.media_url.isNullOrEmpty()) {
                        // For audio, show visual indicator only (actual playback in background service)
                        // For video, use MediaPlayerComponent
                        if (isVideo) {
                            val mediaUrl = UrlUtils.joinUrl(baseUrl, radio.media_url)
                            com.campuswave.app.ui.components.MediaPlayerComponent(
                                mediaUrl = mediaUrl ?: "",
                                isVideo = true,
                                isPausedByHost = radio.host_status == "PAUSED"
                            )
                        } else {
                            // Audio playback indicator
                            val errorMsg = (playbackState as? AudioPlaybackService.PlaybackState.Error)?.message
                            AudioPlaybackIndicator(
                                radioTitle = radio.title,
                                isPausedByHost = radio.host_status == "PAUSED",
                                isActuallyPlaying = isPlaying,
                                hasMediaFile = !radio.media_url.isNullOrEmpty(),
                                errorMessage = errorMsg,
                                currentDuration = elapsedTime
                            )
                        }
                        
                        if (radio.host_status == "PAUSED") {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = Color.Black.copy(alpha = 0.7f)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Pause, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                                    Text("Broadcast Paused", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Show banner if media_url is missing but it's live (waiting for stream)
                        RadioBanner(radio = radio)
                    }
                } else {
                    // Show banner for scheduled/ended
                    RadioBanner(radio = radio)
                    
                    // Overlay message
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (radio.status == "UPCOMING") Icons.Default.Schedule else Icons.Default.CheckCircle,
                                contentDescription = null, 
                                tint = Color.White, 
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (radio.status == "UPCOMING") "Broadcasting Soon" else "Session Completed",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = radio.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = campusOnBackground()
                        )
                        Text(
                            text = radio.category?.name ?: "General",
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    
                    Surface(
                        color = if (radio.status == "LIVE") Color(0xFFE53935) else campusSurface(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = radio.status,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                
                // Real-time Countdown for Upcoming
                if (radio.status == "UPCOMING") {
                    val statusInfo: DateUtils.RadioStatusInfo = remember(radio.start_time, radio.end_time, synchronizedTimeMillis) {
                        DateUtils.getRadioStatusInfo(radio.start_time, radio.end_time, synchronizedTimeMillis)
                    }
                    
                    if (statusInfo.isStartingSoon) {
                        Surface(
                            color = PrimaryPurple.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = statusInfo.timeInfo ?: "Starting Soon",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryPurple
                                    )
                                    Text(
                                        text = "COUNTDOWN",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = campusGrey(),
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Live Timer Display (Student side)
                if (radio.status == "LIVE" && (radio.host_status == "HOSTING" || radio.host_status == "PAUSED")) {
                    Surface(
                        color = PrimaryBlue.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = formatTimer(elapsedTime),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (radio.host_status == "HOSTING") Color(0xFFE53935) else campusGrey()
                                )
                                Text(
                                    text = if (radio.host_status == "HOSTING") "LIVE DURATION" else "PAUSED",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = campusGrey(),
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
                
                // Description
                Text(
                    text = radio.description ?: "Join us for an exciting campus frequency session.",
                    color = if (LocalIsDarkTheme.current) DarkGrey else LightGrey,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp
                )
                
                HorizontalDivider(color = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f))
                
                // Detail Cards
                InfoCard(
                    icon = Icons.Default.People,
                    label = "Listeners",
                    value = "${radio.participant_count}+",
                    modifier = Modifier.fillMaxWidth()
                )
                
                InfoCard(
                    icon = Icons.Default.Schedule,
                    label = "Time Slot",
                    value = formatFullDateTimeRange(radio.start_time, radio.end_time),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Actions
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                if (isAdmin) {
                    val canHost = radio.status == "LIVE" || radio.host_status in listOf("NOT_STARTED", "HOSTING", "PAUSED")
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (canHost) {
                            Button(
                                onClick = onHostClick,
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (radio.host_status == "HOSTING") Color(0xFFE53935) else PrimaryBlue
                                )
                            ) {
                                Icon(Icons.Default.Podcasts, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Manage Live Broadcast", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { onDeleteClick(radio) },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFE53935)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935))
                            ) {
                                Text("Delete")
                            }
                            Button(
                                onClick = { onEditClick(radio) },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = campusSurface())
                            ) {
                                Text("Edit Details", color = campusOnBackground())
                            }
                        }
                    }
                } else {
                    if (radio.status == "LIVE") {
                        // Student controls during live
                        val baseUrl = ApiConfig.BASE_URL.removeSuffix("/api/")
                        val isVideo = radio.media_type == "VIDEO" || (radio.media_url?.endsWith(".mp4") == true)
                        
                        if (radio.host_status == "PAUSED") {
                             // Admin has paused - student cannot override
                             Button(
                                onClick = { 
                                    Toast.makeText(context, "⏸ Broadcast paused by admin. Please wait...", Toast.LENGTH_SHORT).show() 
                                },
                                modifier = Modifier.fillMaxWidth().height(60.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("PAUSED BY ADMIN", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else if (radio.host_status == "HOSTING") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Play/Pause Button
                                Button(
                                    onClick = {
                                        if (isPlaying) {
                                            AudioServiceManager.pause()
                                        } else {
                                            if (!isVideo && !radio.media_url.isNullOrEmpty()) {
                                                val mediaUrl = UrlUtils.joinUrl(baseUrl, radio.media_url!!)
                                                if (mediaUrl != null) {
                                                    if (playbackState is AudioPlaybackService.PlaybackState.Idle ||
                                                        playbackState is AudioPlaybackService.PlaybackState.Stopped) {
                                                        // First tap: initialize ExoPlayer and start playback
                                                        AudioServiceManager.startPlayback(
                                                            context = context,
                                                            mediaUrl = mediaUrl,
                                                            radioTitle = radio.title ?: "Live Radio",
                                                            radioId = radio.id,
                                                            eventEndTimeMillis = endTimeMillis
                                                        )
                                                    } else {
                                                        // Already initialized: just resume from current position
                                                        AudioServiceManager.resume(elapsedTime * 1000)
                                                    }
                                                }
                                            } else {
                                                onJoinLiveClick(radio)
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(60.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isPlaying) campusSurface() else PrimaryBlue
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    border = if (isPlaying) BorderStroke(1.dp, PrimaryBlue) else null
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Resume",
                                        tint = if (isPlaying) PrimaryBlue else Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isPlaying) "Pause" else "Resume",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = if (isPlaying) PrimaryBlue else Color.White
                                    )
                                }
                                
                                // Leave Button
                                Button(
                                    onClick = {
                                        AudioServiceManager.stop(context)
                                        onBackClick()
                                    },
                                    modifier = Modifier.weight(1f).height(60.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935).copy(alpha = 0.1f)),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE53935))
                                ) {
                                    Icon(Icons.Default.ExitToApp, contentDescription = "Leave", tint = Color(0xFFE53935))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Leave",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFFE53935)
                                    )
                                }
                            }
                        } else {
                             Button(
                                onClick = { 
                                    Toast.makeText(context, "Broadcast not started yet", Toast.LENGTH_SHORT).show() 
                                },
                                modifier = Modifier.fillMaxWidth().height(60.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Waiting for Host...", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    } else {
                         // Upcoming Reminder Button
                         Button(
                            onClick = { onToggleReminder(radio) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .then(
                                    if (radio.is_subscribed) {
                                        Modifier.border(2.dp, PrimaryBlue, RoundedCornerShape(16.dp))
                                    } else {
                                        Modifier.background(
                                            brush = Brush.linearGradient(colors = listOf(PrimaryPurple, PrimaryBlue)),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                    }
                                ),
                            colors = if (radio.is_subscribed) {
                                ButtonDefaults.buttonColors(containerColor = campusSurface())
                            } else {
                                ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                            },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = if (radio.is_subscribed) Icons.Default.CheckCircle else Icons.Default.Notifications,
                                contentDescription = null,
                                tint = if (radio.is_subscribed) PrimaryBlue else Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (radio.is_subscribed) "REMINDER SET" else "SET REMINDER",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = if (radio.is_subscribed) PrimaryBlue else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = campusSurface(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
            Column {
                Text(text = label, color = if (LocalIsDarkTheme.current) DarkGrey else LightGrey, fontSize = 12.sp)
                Text(text = value, color = campusOnBackground(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RadioBanner(radio: Radio) {
    val baseUrl = ApiConfig.BASE_URL.removeSuffix("/api/")
    val imageUrl = UrlUtils.joinUrl(baseUrl, radio.banner_image)
    
    var showPreview by remember { mutableStateOf(false) }
    if (showPreview) {
        com.campuswave.app.ui.components.ZoomableImageDialog(
            imageUrl = imageUrl ?: "",
            onDismiss = { showPreview = false }
        )
    }
    
    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = radio.title,
        modifier = Modifier
            .fillMaxSize()
            .clickable { showPreview = true },
        contentScale = ContentScale.Crop,
        loading = {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(PrimaryPurple, PrimaryBlue)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Radio, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
            }
        }
    )
}

fun formatDateTime(dateTime: String): String {
    try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
        val outputFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
        val date = inputFormat.parse(dateTime)
        return if (date != null) outputFormat.format(date) else dateTime
    } catch (e: Exception) {
        return dateTime
    }
}

fun formatFullDateTimeRange(start: String, end: String): String {
    try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
        val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US)
        val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
        
        val startDate = inputFormat.parse(start)
        val endDate = inputFormat.parse(end)
        
        if (startDate != null && endDate != null) {
            return "${dateFormat.format(startDate)} | ${timeFormat.format(startDate)} - ${timeFormat.format(endDate)}"
        }
        return "$start - $end"
    } catch (e: Exception) {
        return "$start - $end"
    }
}
fun formatTimer(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hrs > 0) {
        String.format("%02d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format("%02d:%02d", mins, secs)
    }
}
