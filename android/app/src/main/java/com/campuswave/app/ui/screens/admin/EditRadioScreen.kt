package com.campuswave.app.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.data.models.Radio
import com.campuswave.app.utils.ApiResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRadioScreen(
    radio: Radio,
    radioViewModel: com.campuswave.app.ui.viewmodels.RadioViewModel,
    onBackClick: () -> Unit,
    onUpdateClick: () -> Unit
) {
    var title by remember { mutableStateOf(radio.title) }
    var description by remember { mutableStateOf(radio.description ?: "") }
    var location by remember { mutableStateOf(radio.location ?: "") }
    var startDate by remember { mutableStateOf(radio.start_time.substringBefore("T")) }
    var startTime by remember { mutableStateOf(radio.start_time.substringAfter("T").take(5)) }
    var endDate by remember { mutableStateOf(radio.end_time.substringBefore("T")) }
    var endTime by remember { mutableStateOf(radio.end_time.substringAfter("T").take(5)) }
    
    // Category state
    var selectedCategoryId by remember { mutableLongStateOf(radio.category_id?.toLong() ?: 0L) }
    val categoriesState by radioViewModel.categories.collectAsState()
    
    val updateStatus by radioViewModel.updateRadioState.collectAsState()
    
    LaunchedEffect(updateStatus) {
        if (updateStatus is ApiResult.Success) {
            radioViewModel.resetUpdateRadioState()
            onUpdateClick()
        }
    }
    
    // Fetch categories on launch
    LaunchedEffect(Unit) {
        radioViewModel.fetchCategories()
    }
    
    // Set default category if not set
    LaunchedEffect(categoriesState) {
        if (categoriesState is ApiResult.Success && selectedCategoryId == 0L) {
            val cats = (categoriesState as ApiResult.Success).data
            val allCat = cats.find { it.name.equals("All", ignoreCase = true) }
            if (allCat != null) {
                selectedCategoryId = allCat.id.toLong()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Radio",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Radio Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Radio Title") },
                placeholder = { Text("Enter radio title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            
            // Radio Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Enter radio description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 5
            )
            
            // Location
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                placeholder = { Text("Enter radio location") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            
            // Radio Category
            Text(
                text = "Radio Category (Mandatory)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            
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
                                } else null
                            )
                        }
                    }
                }
                is ApiResult.Error -> {
                    Text("Error loading categories: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
                else -> {}
            }
            
            // Date & Time Section
            Text(
                text = "Start Date & Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Date") },
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Time") },
                    placeholder = { Text("HH:MM") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
            
            Text(
                text = "End Date & Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("Date") },
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("Time") },
                    placeholder = { Text("HH:MM") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
            
            if (updateStatus is ApiResult.Error) {
                Text(
                    text = (updateStatus as ApiResult.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Update Button
            Button(
                onClick = {
                     radioViewModel.updateRadio(
                        radioId = radio.id,
                        title = title,
                        description = description,
                        location = location,
                        startTime = "${startDate}T${startTime}:00",
                        endTime = "${endDate}T${endTime}:00",
                        categoryId = selectedCategoryId.toInt()
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = title.isNotEmpty() && location.isNotEmpty() && selectedCategoryId != 0L && updateStatus !is ApiResult.Loading
            ) {
                if (updateStatus is ApiResult.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Update Radio",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
