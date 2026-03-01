package com.campuswave.app.ui.screens.student

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.data.models.Radio
import com.campuswave.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    favorites: List<Radio>,
    isLoading: Boolean,
    onRadioClick: (Radio) -> Unit,
    onRemoveFavorite: (Radio) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        containerColor = campusBackground(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                            
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(
                                    text = "Saved Frequency",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    color = if (LocalIsDarkTheme.current) Color.White else Color(0xFF1E293B)
                                )
                                Text(
                                    text = "${favorites.size} STATION SIGNAL(S)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (LocalIsDarkTheme.current) DarkGrey else LightGrey,
                                    letterSpacing = 1.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = AccentPink,
                                modifier = Modifier.padding(end = 16.dp).size(24.dp)
                            )
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
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else if (favorites.isEmpty()) {
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
                                imageVector = Icons.Default.HeartBroken,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = if (LocalIsDarkTheme.current) DarkGrey else LightGrey
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Silent Frequency",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (LocalIsDarkTheme.current) Color.White else Color(0xFF1E293B)
                    )
                    Text(
                        text = "Your favorite station signals will appear here once tuned in.",
                        fontSize = 14.sp,
                        color = if (LocalIsDarkTheme.current) DarkGrey else LightGrey,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(favorites) { radio ->
                        FavoriteRadioCard(
                            radio = radio,
                            onClick = { onRadioClick(radio) },
                            onRemove = { onRemoveFavorite(radio) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteRadioCard(
    radio: Radio,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.05f) else Color.White,
        border = BorderStroke(1.dp, if (radio.status == "LIVE") PrimaryBlue.copy(alpha = 0.3f) else (if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.05f) else Color(0xFFE2E8F0))),
        shadowElevation = if (LocalIsDarkTheme.current) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category/Status Icon
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = campusSurface()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (radio.status) {
                            "LIVE" -> Icons.Default.Sensors
                            "UPCOMING" -> Icons.Default.Schedule
                            else -> Icons.Default.RadioButtonChecked
                        },
                        contentDescription = null,
                        tint = when (radio.status) {
                            "LIVE" -> PrimaryBlue
                            "UPCOMING" -> AccentOrange
                            else -> CampusGrey
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = radio.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = campusOnBackground()
                    )
                    
                    if (radio.status == "LIVE") {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PrimaryBlue
                        ) {
                            Text(
                                text = "LIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = CampusGrey
                    )
                    Text(
                        text = radio.category?.name ?: "General Frequency",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = CampusGrey
                    )
                    Text(
                        text = radio.start_time.take(16).replace("T", " "),
                        fontSize = 11.sp,
                        color = CampusGrey
                    )
                }
            }
            
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Remove",
                    tint = AccentPink,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
