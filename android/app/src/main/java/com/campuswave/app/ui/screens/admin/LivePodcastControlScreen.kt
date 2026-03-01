package com.campuswave.app.ui.screens.admin

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.data.models.HandRaise
import com.campuswave.app.data.models.Podcast
import com.campuswave.app.ui.theme.*
import com.campuswave.app.ui.components.WaveformVisualizer
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivePodcastControlScreen(
    podcast: Podcast,
    viewerCount: Int,
    handRaises: List<HandRaise>,
    onBackClick: () -> Unit,
    onToggleMute: () -> Unit,
    onEndPodcast: () -> Unit,
    onAcceptHandRaise: (Int) -> Unit,
    onIgnoreHandRaise: (Int) -> Unit,
    isSpeaking: Boolean = false,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    val isDark = LocalIsDarkTheme.current
    val surfaceColor = if (isDark) Color(0xFF1E1E1E) else Color.White
    val backgroundColor = if (isDark) Color(0xFF121212) else Color(0xFFF8FAFC)
    val onSurfaceColor = if (isDark) Color.White else Color(0xFF1E293B)
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)

    var selectedTab by remember { mutableIntStateOf(0) }
    
    // Debug info for troubleshooting audio
    val debugInfo = "🎤 Speaking: $isSpeaking"

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Live Session",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurfaceColor
                                )
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color(0xFFFF4444), CircleShape)
                                    )
                                    Text(
                                        "ON AIR",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF4444),
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            // Viewer Count Pill
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF1F5F9),
                                modifier = Modifier.padding(end = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(14.dp), tint = secondaryText)
                                    Text(
                                        viewerCount.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = onSurfaceColor
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF1F5F9)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.KeyboardArrowLeft,
                                        contentDescription = "Back",
                                        tint = onSurfaceColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.MoreHoriz, contentDescription = "More", tint = secondaryText)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                // Removed Agora Error 110 Fix Guide

                // Status Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = surfaceColor.copy(alpha = 0.5f)
                ) {
                    Text(
                        debugInfo,
                        fontSize = 11.sp,
                        color = PrimaryBlue,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().height(70.dp),
                color = surfaceColor,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(Icons.Default.GridView, isSelected = selectedTab == 0, onClick = { selectedTab = 0 })
                    BottomNavItem(Icons.Default.Groups, isSelected = selectedTab == 1, onClick = { selectedTab = 1 })
                    BottomNavItem(Icons.Default.Settings, isSelected = selectedTab == 2, onClick = { selectedTab = 2 })
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else {
                when (selectedTab) {
                    0 -> DashboardContent(
                        podcast = podcast,
                        handRaises = handRaises,
                        onToggleMute = onToggleMute,
                        onEndPodcast = onEndPodcast,
                        onAcceptHandRaise = onAcceptHandRaise,
                        onIgnoreHandRaise = onIgnoreHandRaise,
                        isSpeaking = isSpeaking,
                        onViewQueue = { selectedTab = 1 },
                        debugInfo = debugInfo
                    )
                    1 -> QueueContent(
                        handRaises = handRaises,
                        onAcceptHandRaise = onAcceptHandRaise,
                        onIgnoreHandRaise = onIgnoreHandRaise
                    )
                    2 -> SettingsContent()
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    podcast: Podcast,
    handRaises: List<HandRaise>,
    onToggleMute: () -> Unit,
    onEndPodcast: () -> Unit,
    onAcceptHandRaise: (Int) -> Unit,
    onIgnoreHandRaise: (Int) -> Unit,
    isSpeaking: Boolean,
    onViewQueue: () -> Unit,
    debugInfo: String = ""
) {
    val isDark = LocalIsDarkTheme.current
    val surfaceColor = if (isDark) Color(0xFF1E1E1E) else Color.White
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 30.dp)
    ) {
        // Debug Info Banner
        if (debugInfo.isNotEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2196F3).copy(alpha = 0.1f)
                ) {
                    Text(
                        debugInfo,
                        modifier = Modifier.padding(8.dp),
                        fontSize = 10.sp,
                        color = Color(0xFF2196F3),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        // 1. Controls & Visualizer
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mute/Visualizer Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .clickable { onToggleMute() }, // Clicking anywhere toggles mute for ease
                    shape = RoundedCornerShape(20.dp),
                    color = surfaceColor,
                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF1F5F9)),
                    shadowElevation = if (isDark) 0.dp else 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (podcast.is_muted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mute",
                                tint = if (podcast.is_muted) Color(0xFFFF9800) else PrimaryBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                if (podcast.is_muted) "Resume Audio" else "Pause Audio",
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF1E293B),
                                fontSize = 15.sp
                            )
                        }
                        
                        // Mini Visualizer
                        if (!podcast.is_muted) {
                            WaveformVisualizer(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(30.dp),
                                isSpeaking = isSpeaking,
                                volumeLevel = 50, // Using fixed value for WebRTC
                                color = PrimaryBlue
                            )
                        } else {
                            Text(
                                "MUTED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9800),
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                // End Call Card
                Surface(
                    onClick = onEndPodcast,
                    modifier = Modifier
                        .width(100.dp)
                        .height(80.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFF4444).copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, Color(0xFFFF4444).copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CallEnd,
                            contentDescription = "End",
                            tint = Color(0xFFFF4444),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "End",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF4444),
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // 2. Hand Raise Queue Preview
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "HAND RAISE QUEUE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondaryText,
                        letterSpacing = 1.sp
                    )
                    
                    if (handRaises.isNotEmpty()) {
                        Surface(
                            color = Color(0xFFFF9800).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "${handRaises.size} Pending",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9800)
                            )
                        }
                    }
                }

                if (handRaises.isEmpty()) {
                    // Empty State
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = surfaceColor,
                        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF1F5F9))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.FrontHand, contentDescription = null, tint = secondaryText.copy(alpha = 0.3f), modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No pending requests", color = secondaryText.copy(alpha = 0.7f), fontSize = 13.sp)
                        }
                    }
                } else {
                    // Show top 2 requests
                    handRaises.take(2).forEach { handRaise ->
                        HandRaiseCard(
                            handRaise = handRaise,
                            onAccept = { onAcceptHandRaise(handRaise.user_id) },
                            onIgnore = { onIgnoreHandRaise(handRaise.user_id) }
                        )
                    }
                    if (handRaises.size > 2) {
                        Text(
                            "View ${handRaises.size - 2} more...",
                            fontSize = 13.sp,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .clickable { onViewQueue() }
                                .padding(8.dp)
                        )
                    }
                }
            }
        }

        // 3. Quick Tools Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "QUICK TOOLS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = secondaryText,
                    letterSpacing = 1.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickToolCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.ChatBubbleOutline,
                        label = "Live Chat",
                        color = Color(0xFF5E72E4),
                        onClick = { /* TODO */ }
                    )
                    QuickToolCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.AutoAwesome,
                        label = "Sound Pad",
                        color = Color(0xFF825EE4),
                        onClick = { /* TODO */ }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickToolCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.GraphicEq,
                        label = "Record",
                        color = Color(0xFFFF9800), // Changed to Orange for record like
                        onClick = { /* TODO */ }
                    )
                    QuickToolCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.BarChart,
                        label = "Quick Poll",
                        color = Color(0xFF2DCE89),
                        onClick = { /* TODO */ }
                    )
                }
            }
        }
    }
}

@Composable
fun QueueContent(
    handRaises: List<HandRaise>,
    onAcceptHandRaise: (Int) -> Unit,
    onIgnoreHandRaise: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Hand Raise Queue", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (LocalIsDarkTheme.current) Color.White else Color(0xFF1E293B))
        }
        
        if (handRaises.isEmpty()) {
            item {
                Box(modifier = Modifier.fillParentMaxSize().height(300.dp), contentAlignment = Alignment.Center) {
                    Text("No one has raised their hand yet.", color = CampusGrey)
                }
            }
        } else {
            items(handRaises) { handRaise ->
                HandRaiseCard(
                    handRaise = handRaise,
                    onAccept = { onAcceptHandRaise(handRaise.user_id) },
                    onIgnore = { onIgnoreHandRaise(handRaise.user_id) }
                )
            }
        }
    }
}

@Composable
fun SettingsContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(48.dp), tint = CampusGrey.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Settings Coming Soon", color = CampusGrey)
        }
    }
}

// ================= Components =================

@Composable
fun QuickToolCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    Surface(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isDark) Color(0xFF1E1E1E) else Color.White,
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF1F5F9)),
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                color = color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                label,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isDark) Color.White else Color(0xFF1E293B)
            )
        }
    }
}

@Composable
fun HandRaiseCard(
    handRaise: HandRaise,
    onAccept: () -> Unit,
    onIgnore: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (isDark) Color(0xFF1E1E1E) else Color.White,
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF1F5F9)),
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = Color(0xFFFF9800).copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.FrontHand, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(24.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    handRaise.user_name ?: "Unknown User",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isDark) Color.White else Color(0xFF1E293B)
                )
                Text(
                    "Just now",
                    fontSize = 12.sp,
                    color = CampusGrey
                )
            }
            
            // Allow Button
            Button(
                onClick = onAccept,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Allow", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Close Button
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onIgnore() },
                shape = RoundedCornerShape(10.dp),
                color = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF1F5F9)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Close, contentDescription = "Reject", tint = CampusGrey, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun BottomNavItem(icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            icon, 
            contentDescription = null, 
            tint = if (isSelected) PrimaryBlue else CampusGrey, 
            modifier = Modifier.size(28.dp)
        )
    }
}
