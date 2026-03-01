package com.campuswave.app.ui.screens.admin

import androidx.compose.foundation.layout.FlowRow
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.campuswave.app.ui.viewmodels.RadioViewModel
import com.campuswave.app.utils.ApiResult
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRadioScreen(
    radioViewModel: RadioViewModel,
    onBackClick: () -> Unit,
    onRadioCreated: () -> Unit
) {
    val context = LocalContext.current
    
    var radioName by remember { mutableStateOf("") }
    var radioDescription by remember { mutableStateOf("") }
    
    // Date and time states
    var startDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    
    // Media file state
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var selectedMediaName by remember { mutableStateOf<String?>(null) }
    
    // Banner image state
    var selectedBannerUri by remember { mutableStateOf<Uri?>(null) }
    
    // Category state
    var selectedCategoryId by remember { mutableLongStateOf(0L) } // 0 means not selected or default to 'All'
    val categoriesState by radioViewModel.categories.collectAsState()
    
    // API state
    val createStatus by radioViewModel.createRadioState.collectAsState()
    val uploadStatus by radioViewModel.mediaUploadState.collectAsState()
    val isLoading = createStatus is ApiResult.Loading || uploadStatus is ApiResult.Loading
    val errorMessage = (createStatus as? ApiResult.Error)?.message ?: (uploadStatus as? ApiResult.Error)?.message
    
    // Theme colors
    val isDark = com.campuswave.app.ui.theme.LocalIsDarkTheme.current
    val backgroundColor = if (isDark) com.campuswave.app.ui.theme.DarkBackground else Color.White
    val surfaceColor = if (isDark) com.campuswave.app.ui.theme.DarkSurface else Color(0xFFFAFAFA)
    val textColor = if (isDark) Color.White else Color(0xFF2D3748)
    val secondaryTextColor = if (isDark) Color.Gray else Color(0xFF6B7280)
    val inputBorderColor = if (isDark) Color.Gray.copy(alpha = 0.5f) else Color(0xFFE2E8F0)
    val inputBackgroundColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Transparent
    
    // File picker launcher for media
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedMediaUri = it
            selectedMediaName = it.lastPathSegment ?: "Media file selected"
        }
    }
    
    // Image picker launcher for banner
    val bannerPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedBannerUri = uri
    }
    
    // Handle successful radio creation
    LaunchedEffect(createStatus, uploadStatus) {
        if (createStatus is ApiResult.Success && uploadStatus is ApiResult.Success) {
            android.widget.Toast.makeText(context, "Radio successfully created", android.widget.Toast.LENGTH_SHORT).show()
            radioViewModel.resetCreateRadioState()
            radioViewModel.resetMediaUploadState()
            onRadioCreated()
        }
    }
    
    // Fetch categories on launch
    LaunchedEffect(Unit) {
        radioViewModel.fetchCategories()
    }
    
    // Set default category to 'All' when categories are loaded
    LaunchedEffect(categoriesState) {
        if (categoriesState is ApiResult.Success) {
            val cats = (categoriesState as ApiResult.Success).data
            val allCat = cats.find { it.name.equals("All", ignoreCase = true) }
            if (allCat != null && selectedCategoryId == 0L) {
                selectedCategoryId = allCat.id.toLong()
            }
        }
    }
    
    // Calendar instance for date/time pickers
    val calendar = Calendar.getInstance()
    
    // Date picker dialogs
    val startDatePicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            startDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    
    val endDatePicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            endDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    
    // Time picker dialogs
    val startTimePicker = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            startTime = String.format("%02d:%02d", hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )
    
    val endTimePicker = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            endTime = String.format("%02d:%02d", hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )
    
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Create Radio",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Fill in the details to create a new radio session",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundColor)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error message
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(
                        text = error,
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            // Banner Image Upload
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = textColor
                    )
                    Text(
                        text = "Radio Banner Image",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = textColor
                    )
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 2.dp,
                            color = if (selectedBannerUri != null) Color(0xFF5E72E4) else inputBorderColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            if (selectedBannerUri != null) Color.Transparent else inputBackgroundColor,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { bannerPickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedBannerUri != null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = selectedBannerUri,
                                contentDescription = "Banner preview",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            // Remove button
                            IconButton(
                                onClick = { selectedBannerUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Color.White
                                )
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = secondaryTextColor
                            )
                            Text(
                                text = "Tap to add banner image",
                                fontSize = 14.sp,
                                color = secondaryTextColor,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Recommended: 1080x1080 (square)",
                                fontSize = 12.sp,
                                color = secondaryTextColor
                            )
                        }
                    }
                }
            }
            
            // Radio Name
            FormField(
                label = "Radio Name",
                icon = Icons.Default.Radio,
                value = radioName,
                onValueChange = { radioName = it },
                placeholder = "Enter radio session name",
                textColor = textColor,
                secondaryTextColor = secondaryTextColor,
                inputBorderColor = inputBorderColor,
                inputBackgroundColor = inputBackgroundColor
            )
            
            // Radio Category
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = textColor
                    )
                    Text(
                        text = "Radio Category (Mandatory)",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = textColor
                    )
                }
                
                when (val state = categoriesState) {
                    is ApiResult.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                    is ApiResult.Success -> {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.data.forEach { category ->
                                FilterChip(
                                    selected = selectedCategoryId == category.id.toLong(),
                                    onClick = { selectedCategoryId = category.id.toLong() },
                                    label = { Text(category.name) },
                                    leadingIcon = if (selectedCategoryId == category.id.toLong()) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF5E72E4),
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White,
                                        containerColor = surfaceColor,
                                        labelColor = textColor
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedCategoryId == category.id.toLong(),
                                        borderColor = inputBorderColor,
                                        selectedBorderColor = Color(0xFF5E72E4)
                                    )
                                )
                            }
                        }
                    }
                    is ApiResult.Error -> {
                        Text("Error loading categories: ${state.message}", color = Color.Red, fontSize = 12.sp)
                        Button(onClick = { radioViewModel.fetchCategories() }) {
                            Text("Retry")
                        }
                    }
                    else -> {}
                }
            }
            
            // Start Date & Time
            Text(
                text = "Start Date & Time",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = textColor
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DateTimePickerField(
                    value = startDate,
                    placeholder = "Select date",
                    icon = Icons.Default.CalendarToday,
                    onClick = { startDatePicker.show() },
                    modifier = Modifier.weight(1f),
                    textColor = textColor,
                     inputBorderColor = inputBorderColor,
                     inputBackgroundColor = inputBackgroundColor
                )
                DateTimePickerField(
                    value = startTime,
                    placeholder = "Select time",
                    icon = Icons.Default.AccessTime,
                    onClick = { startTimePicker.show() },
                    modifier = Modifier.weight(1f),
                    textColor = textColor,
                    inputBorderColor = inputBorderColor,
                    inputBackgroundColor = inputBackgroundColor
                )
            }
            
            // End Date & Time
            Text(
                text = "End Date & Time",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = textColor
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DateTimePickerField(
                    value = endDate,
                    placeholder = "Select date",
                    icon = Icons.Default.CalendarToday,
                    onClick = { endDatePicker.show() },
                    modifier = Modifier.weight(1f),
                    textColor = textColor,
                    inputBorderColor = inputBorderColor,
                    inputBackgroundColor = inputBackgroundColor
                )
                DateTimePickerField(
                    value = endTime,
                    placeholder = "Select time",
                    icon = Icons.Default.AccessTime,
                    onClick = { endTimePicker.show() },
                    modifier = Modifier.weight(1f),
                    textColor = textColor,
                    inputBorderColor = inputBorderColor,
                    inputBackgroundColor = inputBackgroundColor
                )
            }
            
            // Radio Description
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = textColor
                    )
                    Text(
                        text = "Radio Description",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = textColor
                    )
                }
                
                OutlinedTextField(
                    value = radioDescription,
                    onValueChange = { radioDescription = it },
                    placeholder = { Text("Enter radio session description", color = secondaryTextColor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        unfocusedBorderColor = inputBorderColor,
                        focusedBorderColor = Color(0xFF5E72E4),
                        unfocusedContainerColor = inputBackgroundColor,
                         focusedContainerColor = inputBackgroundColor
                    )
                )
            }
            
            // Upload Media (MP3/MP4)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = textColor
                    )
                    Text(
                        text = "Radio Media (MP3/MP4)",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = textColor
                    )
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .border(
                            width = 2.dp,
                            color = if (selectedMediaUri != null) Color(0xFF5E72E4) else inputBorderColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            if (selectedMediaUri != null) Color(0xFFF0F4FF).copy(alpha = if (isDark) 0.1f else 1f) else inputBackgroundColor,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { mediaPickerLauncher.launch("*/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedMediaUri != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (selectedMediaName?.endsWith(".mp4") == true) 
                                    Icons.Default.VideoFile else Icons.Default.AudioFile,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = Color(0xFF5E72E4)
                            )
                            Text(
                                text = selectedMediaName ?: "Media selected",
                                fontSize = 14.sp,
                                color = Color(0xFF5E72E4),
                                fontWeight = FontWeight.Medium
                            )
                            TextButton(onClick = { 
                                selectedMediaUri = null
                                selectedMediaName = null
                            }) {
                                Text("Remove", color = Color(0xFFE53935))
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = secondaryTextColor
                            )
                            Text(
                                text = "Tap to select audio/video file",
                                fontSize = 14.sp,
                                color = secondaryTextColor,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "MP3, MP4, WAV up to 100MB",
                                fontSize = 12.sp,
                                color = secondaryTextColor
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Create Radio Button
            Button(
                onClick = {
                    val startDateTime = "${startDate}T${startTime}:00"
                    val endDateTime = "${endDate}T${endTime}:00"
                    
                    // Convert URI to File if media is selected
                    var mediaFile: File? = null
                    selectedMediaUri?.let { uri ->
                        try {
                            val contentResolver = context.contentResolver
                            val mimeType = contentResolver.getType(uri)
                            val extension = when (mimeType) {
                                "audio/mpeg" -> ".mp3"
                                "video/mp4" -> ".mp4"
                                "audio/wav" -> ".wav"
                                "video/webm" -> ".webm"
                                else -> ".tmp"
                            }
                            
                            val inputStream = contentResolver.openInputStream(uri)
                            val tempFile = File.createTempFile("media", extension, context.cacheDir)
                            inputStream?.use { input ->
                                tempFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            mediaFile = tempFile
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    
                    // Convert URI to File if banner is selected
                    var bannerFile: File? = null
                    selectedBannerUri?.let { uri ->
                        try {
                            val contentResolver = context.contentResolver
                            val mimeType = contentResolver.getType(uri)
                            val extension = when (mimeType) {
                                "image/png" -> ".png"
                                "image/gif" -> ".gif"
                                "image/webp" -> ".webp"
                                else -> ".jpg"
                            }
                            
                            val inputStream = contentResolver.openInputStream(uri)
                            val tempFile = File.createTempFile("banner", extension, context.cacheDir)
                            inputStream?.use { input ->
                                tempFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            bannerFile = tempFile
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    
                    radioViewModel.createRadio(
                        title = radioName,
                        description = radioDescription,
                        location = "",
                        startTime = startDateTime,
                        endTime = endDateTime,
                        categoryId = selectedCategoryId.toInt(),
                        mediaFile = mediaFile,
                        bannerFile = bannerFile
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = radioName.isNotEmpty() && startDate.isNotEmpty() && 
                         startTime.isNotEmpty() && endDate.isNotEmpty() && 
                         endTime.isNotEmpty() && selectedCategoryId != 0L && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.Gray
                ),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF5E72E4),
                                    Color(0xFF825EE4)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Create Radio",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateTimePickerField(
    value: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color,
    inputBorderColor: Color,
    inputBackgroundColor: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = { },
        readOnly = true,
        placeholder = { Text(placeholder, color = textColor.copy(alpha = 0.5f), fontSize = 14.sp) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF5E72E4),
                modifier = Modifier.size(20.dp)
            )
        },
        modifier = modifier.clickable { onClick() },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            disabledTextColor = textColor,
            unfocusedBorderColor = inputBorderColor,
            focusedBorderColor = Color(0xFF5E72E4),
            disabledBorderColor = inputBorderColor,
             unfocusedContainerColor = inputBackgroundColor,
            focusedContainerColor = inputBackgroundColor,
             disabledContainerColor = inputBackgroundColor
        ),
        enabled = false
    )
}

@Composable
fun FormField(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    textColor: Color,
    secondaryTextColor: Color,
    inputBorderColor: Color,
    inputBackgroundColor: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = textColor
            )
            Text(
                text = label,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = textColor
            )
        }
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = secondaryTextColor) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                unfocusedBorderColor = inputBorderColor,
                focusedBorderColor = Color(0xFF5E72E4),
                unfocusedContainerColor = inputBackgroundColor,
                focusedContainerColor = inputBackgroundColor
            )
        )
    }
}
