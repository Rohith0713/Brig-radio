package com.campuswave.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.campuswave.app.data.models.Radio
import com.campuswave.app.data.network.ApiConfig
import com.campuswave.app.ui.theme.*
import com.campuswave.app.utils.DateUtils
import com.campuswave.app.utils.UrlUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Premium Upcoming Radio Card matching the horizontal compact design
 * Now with proper light/dark theme support and live countdown
 */
@Composable
fun PremiumUpcomingCard(
    radio: Radio,
    baseUrl: String = ApiConfig.BASE_URL.removeSuffix("/api/"),
    isReminderSet: Boolean = false,
    showReminder: Boolean = true,
    onReminderToggle: () -> Unit,
    onClick: () -> Unit,
    synchronizedTimeMillis: Long = System.currentTimeMillis(),
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    
    // Theme-aware colors
    val cardBackground = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White
    val cardBorder = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f)
    val titleColor = if (isDark) Color.White else Color(0xFF1A1A2E)
    val subtitleColor = if (isDark) CampusGrey else Color(0xFF64748B)
    
    // Get Status Info from DateUtils using synchronized time
    val statusInfo = remember(radio.start_time, radio.end_time, synchronizedTimeMillis) {
        DateUtils.getRadioStatusInfo(radio.start_time, radio.end_time, synchronizedTimeMillis)
    }
    
    val hasStarted = statusInfo.isLive || statusInfo.isEnded
    
    // Formatted Display String for Date/Time range
    val formattedDateTimeString = remember(statusInfo) {
        "${radio.start_time.substringBefore("T")} • ${statusInfo.startTimeFormatted} - ${statusInfo.endTimeFormatted}"
    }
    
    // Determine border color based on state
    val activeBorderColor = if (statusInfo.isStartingSoon) PrimaryPurple else cardBorder
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = cardBackground,
        shadowElevation = if (isDark) 0.dp else 4.dp,
        border = androidx.compose.foundation.BorderStroke(if (statusInfo.isStartingSoon) 2.dp else 1.dp, activeBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info Content (Left)
            Column(modifier = Modifier.weight(1f)) {
                // Date & Time Row (Always Visible - Source of Truth)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = formattedDateTimeString,
                        color = PrimaryPurple,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = radio.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${radio.category?.name ?: "General"} • ${radio.description?.take(30) ?: ""}",
                    color = subtitleColor,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Section: Reminder Button + Countdown (if applicable)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Reminder Button (Flexible width)
                    if (showReminder && !hasStarted) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onReminderToggle() },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isReminderSet) Color.Transparent else PrimaryBlue,
                            border = if (isReminderSet) androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f)) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isReminderSet) Icons.Default.CheckCircle else Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = if (isReminderSet) (if (isDark) Color.White else PrimaryBlue) else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isReminderSet) "Reminder Set" else "Set Reminder",
                                    color = if (isReminderSet) (if (isDark) Color.White else PrimaryBlue) else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else if (hasStarted) {
                        val statusColor = if (statusInfo.isLive) Color.Red else (if (isDark) Color.White else Color.Black)
                         Text(
                            text = statusInfo.statusLabel,
                            color = statusColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // Countdown Timer (Only if starting soon or live)
                    if (statusInfo.timeInfo != null) {
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = if (statusInfo.isLive) "ENDING" else "STARTING",
                                color = subtitleColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = statusInfo.timeInfo!!,
                                color = if (statusInfo.isLive) AccentPink else PrimaryPurple,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            // Thumbnail (Right)
            var showPreview by remember { mutableStateOf(false) }
            if (showPreview) {
                val previewUrl = UrlUtils.joinUrl(baseUrl, radio.banner_image ?: "") ?: ""
                ZoomableImageDialog(
                    imageUrl = previewUrl,
                    onDismiss = { showPreview = false }
                )
            }

            val imageUrl = UrlUtils.joinUrl(baseUrl, radio.banner_image ?: "")
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .error(android.R.drawable.ic_menu_gallery)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { showPreview = true },
                contentScale = ContentScale.Crop
            )
        }
    }
}
