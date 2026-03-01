package com.campuswave.app.ui.screens.student

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.data.models.Podcast
import com.campuswave.app.ui.theme.*

import com.campuswave.app.ui.components.WaveformVisualizer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivePodcastViewerScreen(
    podcast: Podcast,
    handRaiseStatus: String?,
    onLeave: () -> Unit,
    onRaiseHand: () -> Unit,
    onCancelHandRaise: () -> Unit,
    isAdminSpeaking: Boolean = false,
    isLoading: Boolean = false,
    successMessage: String? = null,
    errorMessage: String? = null
) {
    val isDark = LocalIsDarkTheme.current
    val onSurfaceColor = if (isDark) Color.White else Color(0xFF1E293B)

    // Pulsing animation for live indicator
    val infiniteTransition = rememberInfiniteTransition(label = "live")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Scaffold(
        containerColor = if (isDark) Color(0xFF0D1117) else Color(0xFFF8FAFC),
        topBar = {
            Column(modifier = Modifier.background(if (isDark) Color(0xFF0D1117) else Color(0xFFF8FAFC))) {
                TopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                podcast.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = onSurfaceColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // LIVE pill
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFFFF4444).copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, Color(0xFFFF4444).copy(alpha = 0.2f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .scale(pulseScale)
                                                .size(6.dp)
                                                .background(Color(0xFFFF4444), CircleShape)
                                        )
                                        Text("LIVE", color = Color(0xFFFF4444), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                    }
                                }
                                
                                // Viewers pill
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFE2E8F0))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(12.dp), tint = CampusGrey)
                                        Text(
                                            podcast.viewer_count.toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = onSurfaceColor
                                        )
                                    }
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onLeave) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Back", tint = onSurfaceColor, modifier = Modifier.size(28.dp))
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.MoreHoriz, contentDescription = "More", tint = onSurfaceColor)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                // Removed Agora Error 110 Fix Guide (Student side)
                
                // Marquee integration
                Surface(
                    modifier = Modifier.fillMaxWidth().height(32.dp),
                    color = if (isDark) Color(0xFF161B22).copy(alpha = 0.5f) else Color(0xFFF1F5F9)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                        Text(
                            "SEMESTER REGISTRATION ENDS FRIDAY! • NEW LIBRARY HOURS A...",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = CampusGrey,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Speaker Profile Section
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(140.dp),
                    shape = CircleShape,
                    border = BorderStroke(4.dp, if (isDark) Color(0xFF161B22) else Color.White),
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            (podcast.creator_name?.firstOrNull() ?: "D").toString().uppercase(),
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    podcast.creator_name ?: "Dr. Sarah Smith",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor
                )
                
                Text(
                    if (isAdminSpeaking) "ADMIN IS SPEAKING" else "ADMIN IS MUTED",
                    fontSize = 14.sp,
                    color = if (isAdminSpeaking) PrimaryBlue else CampusGrey,
                    fontWeight = if (isAdminSpeaking) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Audio Visualization Section
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                WaveformVisualizer(
                    modifier = Modifier
                        .height(60.dp)
                        .fillMaxWidth(0.6f),
                    isSpeaking = isAdminSpeaking,
                    volumeLevel = 50, // Using fixed value for WebRTC
                    color = PrimaryBlue
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "LIVE AUDIO FEED",
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp
                )
            }
            
            // Action Buttons Section
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        if (handRaiseStatus == "PENDING") {
                            onCancelHandRaise()
                        } else if (handRaiseStatus != "ACCEPTED") {
                            onRaiseHand()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = handRaiseStatus != "ACCEPTED" && !isLoading,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (handRaiseStatus) {
                            "PENDING" -> AccentOrange
                            else -> PrimaryBlue
                        }
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.PanTool, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            when (handRaiseStatus) {
                                "PENDING" -> "Raised"
                                else -> "Raise Hand"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        onClick = {},
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFE2E8F0))
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(Icons.Default.ChatBubble, contentDescription = null, tint = onSurfaceColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Chat", fontWeight = FontWeight.Bold, color = onSurfaceColor)
                        }
                    }
                    
                    Surface(
                        onClick = onLeave,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        color = ErrorRed.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.1f))
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Leave", fontWeight = FontWeight.Bold, color = ErrorRed)
                        }
                    }
                }
            }
        }
        
        // CRITICAL: Show "Paused by Admin" overlay when podcast is muted
        if (podcast.is_muted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.PauseCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(80.dp)
                    )
                    Text(
                        "Paused by Admin",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Waiting for host to resume...",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
