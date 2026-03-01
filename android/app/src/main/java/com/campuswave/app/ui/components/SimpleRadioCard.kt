package com.campuswave.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.campuswave.app.data.models.Radio
import com.campuswave.app.data.network.ApiConfig
import com.campuswave.app.ui.theme.*
import com.campuswave.app.utils.DateUtils
import com.campuswave.app.utils.UrlUtils

/**
 * Simple Radio Card with banner image, details below, and like button
 */
@Composable
fun SimpleRadioCard(
    radio: Radio,
    baseUrl: String = ApiConfig.BASE_URL.removeSuffix("/api/"),
    onRadioClick: () -> Unit,
    onLikeClick: () -> Unit,
    onNotifyClick: (() -> Unit)? = null,
    isLiked: Boolean = false,
    isSubscribed: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showPreview by remember { mutableStateOf(false) }
    if (showPreview) {
        val previewUrl = UrlUtils.joinUrl(baseUrl, radio.banner_image ?: "") ?: ""
        ZoomableImageDialog(
            imageUrl = previewUrl,
            onDismiss = { showPreview = false }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onRadioClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (LocalIsDarkTheme.current) DarkSurface else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (LocalIsDarkTheme.current) 0.dp else 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Banner Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .clickable { showPreview = true },
                contentAlignment = Alignment.Center
            ) {
                // Check if banner is available
                val hasBanner = !radio.banner_image.isNullOrEmpty()
                // Construct URL: baseUrl (without /api/) + banner_image path (which starts with /uploads/)
                val imageUrl = if (hasBanner) {
                    val url = UrlUtils.joinUrl(baseUrl, radio.banner_image)
                    android.util.Log.d("SimpleRadioCard", "Banner URL for '${radio.title}': $url")
                    android.util.Log.d("SimpleRadioCard", "baseUrl: $baseUrl, banner_image: ${radio.banner_image}")
                    url
                } else null
                
                if (hasBanner && imageUrl != null) {
                    // Use SubcomposeAsyncImage for better state handling
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .error(android.R.drawable.ic_menu_report_image)
                            .build(),
                        contentDescription = radio.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            // Show loading placeholder
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFF5E72E4), Color(0xFF825EE4))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        },
                        error = {
                            // Show error state with red background
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFD32F2F)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Error",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Image failed to load",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = imageUrl ?: "null",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 8.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    )
                } else {
                    // No banner - show gradient placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF5E72E4), Color(0xFF825EE4))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
                
                // Live badge overlay
                if (radio.status == "LIVE") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFE53935)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Pulsing dot
                                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                                val alpha by infiniteTransition.animateFloat(
                                    initialValue = 1f,
                                    targetValue = 0.3f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(800),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "alpha"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color.White.copy(alpha = alpha), CircleShape)
                                )
                                Text(
                                    text = "LIVE",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
            
            // Radio Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = radio.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (LocalIsDarkTheme.current) Color.White else Color(0xFF1F2937),
                    maxLines = 2,
                )
                
                // Category Badge
                if (radio.category != null) {
                    SuggestionChip(
                        onClick = { },
                        label = { 
                            Text(
                                text = radio.category.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color(android.graphics.Color.parseColor(radio.category.color)).copy(alpha = 0.1f),
                            labelColor = Color(android.graphics.Color.parseColor(radio.category.color))
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            borderColor = Color(android.graphics.Color.parseColor(radio.category.color)).copy(alpha = 0.5f),
                            enabled = true
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Description
                if (!radio.description.isNullOrEmpty()) {
                    Text(
                        text = radio.description,
                        fontSize = 14.sp,
                        color = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.6f) else Color(0xFF6B7280),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Date & Location Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Date
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF5E72E4),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = formatDate(radio.start_time),
                            fontSize = 12.sp,
                            color = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.5f) else Color(0xFF6B7280)
                        )
                    }
                    
                    // Location
                    if (!radio.location.isNullOrEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF5E72E4),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = radio.location,
                                fontSize = 12.sp,
                                color = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.5f) else Color(0xFF6B7280),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Like Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Participants count
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${radio.participant_count} tuned in",
                            fontSize = 13.sp,
                            color = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.5f) else Color(0xFF9CA3AF)
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Notify Button (only if callback provided)
                        if (onNotifyClick != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { onNotifyClick() }
                                    .background(
                                        if (isSubscribed) Color(0xFFE3F2FD) else Color(0xFFF5F5F5)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSubscribed) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                                    contentDescription = "Notify Me",
                                    tint = if (isSubscribed) Color(0xFF2196F3) else Color(0xFF9CA3AF),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSubscribed) "Notified" else "Notify Me",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSubscribed) Color(0xFF2196F3) else Color(0xFF6B7280)
                                )
                            }
                        }

                        // Like Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onLikeClick() }
                                .background(
                                    if (isLiked) Color(0xFFFFEBEE) else Color(0xFFF5F5F5)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (isLiked) Color(0xFFE53935) else Color(0xFF9CA3AF),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (radio.favorite_count > 0) "${radio.favorite_count}" else "Like",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isLiked) Color(0xFFE53935) else Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(isoString: String): String {
    return try {
        val date = isoString.take(10)
        val time = isoString.substring(11, 16)
        "$date • $time"
    } catch (e: Exception) {
        isoString
    }
}
