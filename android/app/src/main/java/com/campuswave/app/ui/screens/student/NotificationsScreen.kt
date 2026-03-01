package com.campuswave.app.ui.screens.student

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.data.models.Notification
import com.campuswave.app.ui.viewmodels.RadioViewModel
import com.campuswave.app.utils.ApiResult
import com.campuswave.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    radioViewModel: RadioViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        radioViewModel.fetchNotifications()
    }

    val notificationsState by radioViewModel.notifications.collectAsState()
    
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Alerts?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove all alerts at once? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        radioViewModel.clearAllNotifications()
                        showClearDialog = false
                        Toast.makeText(context, "All alerts cleared successfully", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("CLEAR ALL", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("CANCEL")
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = if (LocalIsDarkTheme.current) Color(0xFF1E1E1E) else Color.White
        )
    }

    Scaffold(
        containerColor = campusBackground(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(campusBackground())
            ) {
                // Glassmorphism Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.03f) else Color.White,
                    border = BorderStroke(1.dp, if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.05f) else Color(0xFFE2E8F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = if (LocalIsDarkTheme.current) Color.White else Color(0xFF1E293B)
                                )
                            }
                            
                            Text(
                                text = "Station Alerts",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = if (LocalIsDarkTheme.current) Color.White else Color(0xFF1E293B),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            val result = notificationsState
                            if (result is ApiResult.Success && result.data.isNotEmpty()) {
                                TextButton(
                                    onClick = { showClearDialog = true },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red.copy(alpha = 0.8f))
                                ) {
                                    Text("CLEAR ALL", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val result = notificationsState) {
                is ApiResult.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
                is ApiResult.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CloudOff, contentDescription = null, tint = CampusGrey, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Failed to sync alerts", color = CampusGrey)
                    }
                }
                is ApiResult.Success -> {
                    val notifications = result.data
                    if (notifications.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                modifier = Modifier.size(100.dp),
                                shape = CircleShape,
                                color = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f),
                                border = BorderStroke(1.dp, if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsNone,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = if (LocalIsDarkTheme.current) DarkGrey else LightGrey
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("All Clear", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (LocalIsDarkTheme.current) Color.White else Color(0xFF1E293B))
                            Text("No new signals or alerts at the moment.", color = if (LocalIsDarkTheme.current) DarkGrey else LightGrey, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(notifications.size) { index ->
                                NotificationCard(notifications[index])
                            }
                        }
                    }
                }
                null -> {}
            }
        }
    }
}

@Composable
fun NotificationCard(notification: Notification) {
    val accentColor = when (notification.type) {
        "SUGGESTION_APPROVED" -> SuccessGreen
        "RADIO_START" -> PrimaryBlue
        "ADMIN_ALERT" -> AccentPink
        else -> PrimaryPurple
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (LocalIsDarkTheme.current) (if (notification.is_read) Color.White.copy(alpha = 0.03f) else Color.White.copy(alpha = 0.08f)) else (if (notification.is_read) Color.Black.copy(alpha = 0.03f) else Color.White),
        border = BorderStroke(1.dp, if (notification.is_read) (if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.03f) else Color(0xFFE2E8F0)) else accentColor.copy(alpha = 0.3f)),
        shadowElevation = if (LocalIsDarkTheme.current || notification.is_read) 0.dp else 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Notification Icon
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accentColor.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (notification.type) {
                            "SUGGESTION_APPROVED" -> Icons.Default.FactCheck
                            "RADIO_START" -> Icons.Default.Sensors
                            "ADMIN_ALERT" -> Icons.Default.Emergency
                            else -> Icons.Default.Notifications
                        },
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Notification Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (LocalIsDarkTheme.current) (if (notification.is_read) Color.White.copy(alpha = 0.7f) else Color.White) else (if (notification.is_read) Color(0xFF64748B) else Color(0xFF1E293B))
                    )
                    
                    if (!notification.is_read) {
                        Surface(
                            shape = CircleShape,
                            color = accentColor,
                            modifier = Modifier.size(8.dp)
                        ) {}
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = if (LocalIsDarkTheme.current) DarkGrey else LightGrey,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(12.dp), tint = CampusGrey.copy(alpha = 0.5f))
                    Text(
                        text = notification.time_ago.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = CampusGrey.copy(alpha = 0.5f),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
