package com.campuswave.app.ui.screens.admin

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePodcastScreen(
    onBackClick: () -> Unit,
    onCreatePodcast: (title: String, description: String, scheduledStartTime: String) -> Unit,
    isLoading: Boolean = false,
    successMessage: String? = null,
    errorMessage: String? = null,
    onMessageShown: () -> Unit = {}
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()) }
    val displayDateFormat = remember { SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault()) }

    val isDark = LocalIsDarkTheme.current
    val surfaceColor = if (isDark) Color(0xFF1E1E1E) else Color.White
    val onSurfaceColor = if (isDark) Color.White else Color(0xFF1E293B)
    val cardBackground = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White

    // Success/Error handling
    LaunchedEffect(successMessage, errorMessage) {
        if (successMessage != null || errorMessage != null) {
            if (successMessage != null) {
                android.widget.Toast.makeText(context, "Podcast successfully created", android.widget.Toast.LENGTH_SHORT).show()
                kotlinx.coroutines.delay(1000) // Shorter delay since we have a Toast
                onMessageShown()
                onBackClick()
            } else {
                kotlinx.coroutines.delay(2000)
                onMessageShown()
            }
        }
    }

    // Date picker
    if (showDatePicker) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                showDatePicker = false
                showTimePicker = true
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
            show()
        }
    }

    // Time picker
    if (showTimePicker) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                selectedDate = calendar.time
                showTimePicker = false
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    Scaffold(
        containerColor = campusBackground(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create Podcast",
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = onSurfaceColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = PrimaryBlue.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PrimaryBlue
                    ) {
                        Icon(
                            Icons.Default.Podcasts,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(12.dp).size(28.dp)
                        )
                    }
                    Column {
                        Text(
                            "Schedule Live Podcast",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = onSurfaceColor
                        )
                        Text(
                            "Create a new podcast session for students",
                            fontSize = 13.sp,
                            color = CampusGrey
                        )
                    }
                }
            }

            // Messages
            if (successMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SuccessGreen.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                        Text(successMessage, color = SuccessGreen, fontWeight = FontWeight.Medium)
                    }
                }
            }

            if (errorMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = ErrorRed.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = ErrorRed)
                        Text(errorMessage, color = ErrorRed, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Form
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = cardBackground,
                shadowElevation = if (isDark) 0.dp else 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Title
                    Column {
                        Text(
                            "Podcast Title",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = onSurfaceColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter podcast title") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFE2E8F0),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = onSurfaceColor,
                                unfocusedTextColor = onSurfaceColor
                            )
                        )
                    }

                    // Description
                    Column {
                        Text(
                            "Description",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = onSurfaceColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            placeholder = { Text("What's this podcast about?") },
                            maxLines = 5,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFE2E8F0),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = onSurfaceColor,
                                unfocusedTextColor = onSurfaceColor
                            )
                        )
                    }

                    // Start Time
                    Column {
                        Text(
                            "Scheduled Start Time",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = onSurfaceColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFE2E8F0)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = PrimaryBlue
                                )
                                Text(
                                    text = selectedDate?.let { displayDateFormat.format(it) }
                                        ?: "Select date and time",
                                    color = if (selectedDate != null) onSurfaceColor else CampusGrey,
                                    fontWeight = if (selectedDate != null) FontWeight.Medium else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Create Button
            Button(
                onClick = {
                    if (title.isNotBlank() && selectedDate != null) {
                        val formattedTime = dateFormat.format(selectedDate!!)
                        onCreatePodcast(title, description, formattedTime)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = title.isNotBlank() && selectedDate != null && !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    disabledContainerColor = PrimaryBlue.copy(alpha = 0.5f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Podcasts, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Create Podcast",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
