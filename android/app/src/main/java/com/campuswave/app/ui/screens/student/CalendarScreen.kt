package com.campuswave.app.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.data.models.Radio
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    radios: List<Radio>,
    isLoading: Boolean,
    onRadioClick: (Radio) -> Unit,
    onBackClick: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    
    // Group radios by date
    val radiosByDate = remember(radios) {
        radios.groupBy { radio ->
            try {
                LocalDate.parse(radio.start_time.take(10))
            } catch (e: Exception) {
                null
            }
        }.filterKeys { it != null }.mapKeys { it.key!! }
    }
    
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF5E72E4),
                                Color(0xFF825EE4)
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Radio Calendar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F7FA))
        ) {
            // Month Navigation
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Month header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
                        }
                        
                        Text(
                            text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1F2937)
                        )
                        
                        IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Weekday headers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                            Text(
                                text = day,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Calendar grid
                    val daysInMonth = currentMonth.lengthOfMonth()
                    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value % 7
                    val today = LocalDate.now()
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.height(240.dp),
                        userScrollEnabled = false
                    ) {
                        // Empty cells for days before the first day
                        items(firstDayOfMonth) {
                            Box(modifier = Modifier.size(40.dp))
                        }
                        
                        // Days of the month
                        items(daysInMonth) { dayIndex ->
                            val day = dayIndex + 1
                            val date = currentMonth.atDay(day)
                            val hasRadios = radiosByDate.containsKey(date)
                            val isSelected = selectedDate == date
                            val isToday = date == today
                            
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> Color(0xFF5E72E4)
                                            isToday -> Color(0xFF5E72E4).copy(alpha = 0.1f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable { selectedDate = date },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = day.toString(),
                                        fontSize = 14.sp,
                                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> Color.White
                                            isToday -> Color(0xFF5E72E4)
                                            else -> Color(0xFF374151)
                                        }
                                    )
                                    
                                    if (hasRadios) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(
                                                    if (isSelected) Color.White else Color(0xFF10B981),
                                                    CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Radios for selected date
            selectedDate?.let { date ->
                val radiosOnDate = radiosByDate[date] ?: emptyList()
                
                Text(
                    text = "Radios on ${date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                if (radiosOnDate.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventBusy,
                                contentDescription = null,
                                tint = Color(0xFFCBD5E0)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "No radio sessions on this date",
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(radiosOnDate) { radio ->
                            CalendarRadioCard(radio = radio, onClick = { onRadioClick(radio) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarRadioCard(
    radio: Radio,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(60.dp)
            ) {
                Text(
                    text = try {
                        radio.start_time.substring(11, 16)
                    } catch (e: Exception) { "--:--" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF5E72E4)
                )
            }
            
            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp),
                color = Color(0xFFE5E7EB)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = radio.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF1F2937)
                )
                if (radio.location?.isNotEmpty() == true) {
                    Text(
                        text = radio.location,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
            
            // Status badge
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = when (radio.status) {
                    "LIVE" -> Color(0xFF10B981)
                    "UPCOMING" -> Color(0xFF5E72E4)
                    else -> Color(0xFF6B7280)
                }.copy(alpha = 0.1f)
            ) {
                Text(
                    text = radio.status,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = when (radio.status) {
                        "LIVE" -> Color(0xFF10B981)
                        "UPCOMING" -> Color(0xFF5E72E4)
                        else -> Color(0xFF6B7280)
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
