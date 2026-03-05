package com.campuswave.app.ui.screens.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.data.models.Podcast
import com.campuswave.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastListScreen(
    livePodcast: Podcast? = null,
    scheduledPodcasts: List<Podcast> = emptyList(),
    isLoading: Boolean = false,
    userRole: String = "STUDENT",
    onBackClick: () -> Unit = {},
    onPodcastClick: (Int) -> Unit = {},
    onGoLiveClick: (Int) -> Unit = {},
    onCreatePodcastClick: () -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    val isDark = LocalIsDarkTheme.current
    val onSurfaceColor = if (isDark) Color.White else Color(0xFF1E293B)
    val isAdmin = userRole == "ADMIN" || userRole == "MAIN_ADMIN"

    Scaffold(
        containerColor = campusBackground(),
        topBar = {
            TopAppBar(
                title = { 
                    Text("Podcasts", fontWeight = FontWeight.Bold, color = onSurfaceColor) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = onSurfaceColor)
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = onSurfaceColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {}
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = PrimaryBlue.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Podcasts,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Feature Coming Soon",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "The Podcasts feature is currently under development and will be available in a future update.",
                    fontSize = 14.sp,
                    color = CampusGrey,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun PodcastItem(
    podcast: Podcast,
    isLive: Boolean,
    isAdmin: Boolean,
    onClick: () -> Unit,
    onActionClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val surfaceColor = if (isDark) Color(0xFF1E1E1E) else Color.White
    val onSurfaceColor = if (isDark) Color.White else Color(0xFF1E293B)
    
    val displayDateFormat = remember { SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()) }
    val scheduledTime = try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        isoFormat.parse(podcast.scheduled_start_time)
    } catch (e: Exception) {
        null
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = surfaceColor,
        border = BorderStroke(
            1.dp, 
            if (isLive) Color(0xFFFF4444).copy(alpha = 0.5f) 
            else if (isDark) Color.White.copy(alpha = 0.1f) 
            else Color(0xFFE2E8F0)
        ),
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon / Avatar
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isLive) Color(0xFFFF4444).copy(alpha = 0.1f) else PrimaryBlue.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (isLive) Icons.Default.LiveTv else Icons.Default.Podcasts,
                        contentDescription = null,
                        tint = if (isLive) Color(0xFFFF4444) else PrimaryBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = podcast.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = onSurfaceColor
                )
                Text(
                    text = scheduledTime?.let { displayDateFormat.format(it) } ?: podcast.scheduled_start_time,
                    fontSize = 12.sp,
                    color = CampusGrey
                )
                if (podcast.creator_name != null) {
                    Text(
                        text = "By ${podcast.creator_name}",
                        fontSize = 11.sp,
                        color = CampusGrey.copy(alpha = 0.8f)
                    )
                }
            }

            // Action Button
            if (isLive) {
                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(if (isAdmin) "Manage" else "Join", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            } else if (isAdmin) {
                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Go Live", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
