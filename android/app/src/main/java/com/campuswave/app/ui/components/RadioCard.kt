package com.campuswave.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.data.models.Radio
import com.campuswave.app.ui.theme.*

@Composable
fun RadioCard(
    radio: Radio,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Animate scale on press for tactile feel
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "scale"
    )

    // Animate elevation
    val elevation by animateFloatAsState(
        targetValue = if (isPressed) 2.dp.value else 6.dp.value,
        animationSpec = tween(durationMillis = 100),
        label = "elevation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        colors = CardDefaults.cardColors(containerColor = CampusSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = androidx.compose.foundation.LocalIndication.current,
                    onClick = onClick
                )
                .padding(16.dp)
        ) {
            // Header Row: Status & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Badge
                StatusBadge(status = radio.status)
                
                // Date/Time hint (if needed, or just keep simple)
//                 Text(
//                     text = TimeUtils.formatDate(event.start_time),
//                     style = MaterialTheme.typography.labelSmall,
//                     color = CampusGrey
//                 )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Radio Title
            Text(
                text = radio.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = CampusDark,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Radio Description
            if (radio.description != null) {
                Text(
                    text = radio.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CampusGrey,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Divider(color = CampusLight.copy(alpha = 0.5f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))
            
            // Footer: Location & Participants
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Location
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📍",
                        modifier = Modifier.padding(end = 4.dp),
                        fontSize = 14.sp
                    )
                    Text(
                        text = radio.location ?: "Unknown Location",
                        style = MaterialTheme.typography.bodySmall,
                        color = CampusDark.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Participants
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CampusBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CampusLight)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👥",
                            modifier = Modifier.padding(end = 4.dp),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${radio.participant_count}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (backgroundColor, textColor, text) = when (status) {
        "LIVE" -> Triple(StatusLive.copy(alpha = 0.1f), StatusLive, "● LIVE")
        "UPCOMING" -> Triple(StatusUpcoming.copy(alpha = 0.1f), StatusUpcoming, "UPCOMING")
        "MISSED" -> Triple(StatusMissed.copy(alpha = 0.1f), StatusMissed, "ENDED")
        else -> Triple(CampusGrey.copy(alpha = 0.1f), CampusGrey, status)
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}
