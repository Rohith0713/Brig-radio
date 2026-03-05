package com.campuswave.app.ui.screens.student

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitSuggestionScreen(
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onSubmit: (String, String, String) -> Unit,
    onBackClick: () -> Unit,
    onSubmitted: () -> Unit
) {
    var radioTitle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    
    val isDark = LocalIsDarkTheme.current
    val surfaceColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White
    val onSurfaceColor = if (isDark) Color.White else Color(0xFF1E293B)
    val inputTextColor = if (isDark) Color.White else Color(0xFF1E293B)
    val inputBackgroundColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF1F5F9)
    val inputBorderColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFE2E8F0)
    
    Scaffold(
        containerColor = campusBackground(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(campusBackground())
                    .statusBarsPadding()
            ) {
                // Glassmorphism Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (isDark) Color.White.copy(alpha = 0.03f) else Color.White,
                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFE2E8F0))
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
                                    tint = onSurfaceColor
                                )
                            }
                            
                            Text(
                                text = "Propose Session",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = onSurfaceColor,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Creative Brief Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = PrimaryBlue.copy(alpha = if (isDark) 0.1f else 0.05f),
                border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PrimaryBlue,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Creative Brief",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceColor
                        )
                        Text(
                            text = "Suggest a concept that resonates with the campus spirit.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) DarkGrey else LightGrey
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(
                    text = "Broadcast Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = onSurfaceColor
                )
                
                // Radio Title
                OutlinedTextField(
                    value = radioTitle,
                    onValueChange = { radioTitle = it },
                    label = { Text("Session Title", color = CampusGrey) },
                    placeholder = { Text("Ex: The Midnight Frequency", color = CampusGrey.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = inputBorderColor,
                        focusedTextColor = inputTextColor,
                        unfocusedTextColor = inputTextColor,
                        cursorColor = PrimaryBlue,
                        unfocusedContainerColor = inputBackgroundColor,
                        focusedContainerColor = inputBackgroundColor
                    )
                )

                // Category Selection Redesigned
                var expanded by remember { mutableStateOf(false) }
                val categories = listOf(
                    "Academic", "Co-Curricular", "Extra-Curricular",
                    "Music", "Talk Show", "Guest Lecture", "Branch Sponsored"
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Vibe / Category", color = CampusGrey) },
                        placeholder = { Text("Choose a category", color = CampusGrey.copy(alpha = 0.5f)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = inputBorderColor,
                            focusedTextColor = inputTextColor,
                            unfocusedTextColor = inputTextColor,
                            unfocusedTrailingIconColor = if (isDark) DarkGrey else LightGrey,
                            focusedTrailingIconColor = PrimaryBlue,
                            unfocusedContainerColor = inputBackgroundColor,
                            focusedContainerColor = inputBackgroundColor
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(campusSurface())
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(text = category, color = campusOnBackground()) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("The Narrative", color = CampusGrey) },
                    placeholder = { Text("Pitch your idea, explain the Flow, and detail any specific tech or talent needs...", color = CampusGrey.copy(alpha = 0.5f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = inputBorderColor,
                        focusedTextColor = inputTextColor,
                        unfocusedTextColor = inputTextColor,
                        cursorColor = PrimaryBlue,
                        unfocusedContainerColor = inputBackgroundColor,
                        focusedContainerColor = inputBackgroundColor
                    )
                )
            }
            
            // Error Message
            errorMessage?.let {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ErrorRed.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp),
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Button(
                onClick = {
                    if (radioTitle.isNotEmpty() && description.isNotEmpty() && selectedCategory.isNotEmpty()) {
                        onSubmit(radioTitle, description, selectedCategory)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    disabledContainerColor = CampusSurface,
                    disabledContentColor = CampusGrey
                ),
                enabled = radioTitle.isNotEmpty() && description.isNotEmpty() && selectedCategory.isNotEmpty() && !isLoading,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TRANSMIT PROPOSAL",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Icon(Icons.Default.Podcasts, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
