package com.campuswave.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Visual indicator for audio playback (audio plays in background service)
 */
@Composable
fun AudioPlaybackIndicator(
    radioTitle: String,
    isPausedByHost: Boolean,
    isActuallyPlaying: Boolean = true,
    hasMediaFile: Boolean = true,
    errorMessage: String? = null,
    currentDuration: Long = 0L,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color(0xFFE91E63)
                )
                Text(
                    text = "Live Audio",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                
                // Live indicator
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (hasMediaFile && errorMessage == null) Color(0xFFE91E63) else Color(0xFF6B7280)
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
            
            // Audio visualization — only animate when actually playing
            if (!hasMediaFile) {
                // No media file — show warning bars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(20) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(15.dp)
                                .background(Color(0xFF4A4A4A), RoundedCornerShape(2.dp))
                        )
                    }
                }
            } else if (isPausedByHost || !isActuallyPlaying) {
                // Static visualization when paused or not yet started
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
                                    Color(0xFF4A4A4A),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            } else {
                // Animated visualization — audio is truly playing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(20) { index ->
                        val animatedHeight by infiniteTransition.animateFloat(
                            initialValue = 20f,
                            targetValue = (40..80).random().toFloat(),
                            animationSpec = infiniteRepeatable(
                                animation = tween(
                                    durationMillis = (300..800).random(),
                                    easing = FastOutSlowInEasing
                                ),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "bar$index"
                        )
                        
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(animatedHeight.dp)
                                .background(
                                    Color(0xFFE91E63),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            }
            
            // Status text — reflects actual state
            val statusText = when {
                !hasMediaFile -> "⚠ No audio file uploaded for this session"
                errorMessage != null -> "⚠ $errorMessage"
                isPausedByHost -> "Paused by host"
                isActuallyPlaying -> "♫ Playing live audio"
                else -> "Tap Resume to start listening"
            }
            val statusColor = when {
                !hasMediaFile || errorMessage != null -> Color(0xFFFF9800)
                else -> Color(0xFF9CA3AF)
            }
            Text(
                text = statusText,
                color = statusColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            if (currentDuration > 0) {
                Text(
                    text = formatIndicatorDuration(currentDuration),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (hasMediaFile && errorMessage == null && isActuallyPlaying) {
                Text(
                    text = "Audio continues if you navigate away",
                    color = Color(0xFF6B7280),
                    fontSize = 12.sp
                )
            }
        }
    }
}

private fun formatIndicatorDuration(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hrs > 0) {
        String.format("%02d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format("%02d:%02d", mins, secs)
    }
}
