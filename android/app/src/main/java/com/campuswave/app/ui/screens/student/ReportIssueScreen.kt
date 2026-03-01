package com.campuswave.app.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.ui.theme.CampusBackground
import com.campuswave.app.ui.theme.PrimaryGradient
import com.campuswave.app.ui.theme.PrimaryBlue
import com.campuswave.app.ui.theme.CampusSurface
import com.campuswave.app.ui.theme.CampusDark
import androidx.compose.ui.platform.LocalContext
import com.campuswave.app.ui.viewmodels.ProfileViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportIssueScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { com.campuswave.app.ui.viewmodels.ReportViewModel(context) }
    val reportState by viewModel.reportState.collectAsState()
    val scope = rememberCoroutineScope()
    
    var issueType by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }
    
    // Reset state on entry
    LaunchedEffect(Unit) {
        viewModel.resetState()
    }
    
    // Handle API result
    LaunchedEffect(reportState) {
        if (reportState is com.campuswave.app.utils.ApiResult.Success) {
            showSnackbar = true
            issueType = ""
            description = ""
            viewModel.resetState()
        }
    }
    
    val issueTypes = listOf("Audio issue", "Wrong content", "Offensive content", "Not playing", "Other")

    Scaffold(
        containerColor = CampusBackground,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryGradient)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Report Session",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
        snackbarHost = {
             if (showSnackbar) {
                 Snackbar(
                     modifier = Modifier.padding(16.dp),
                     action = {
                         TextButton(onClick = { showSnackbar = false }) {
                             Text("Dismiss", color = Color.White)
                         }
                     }
                 ) {
                     Text("Report submitted successfully!")
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
            Text(
                text = "Report an Issue",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = CampusDark
            )
            
            Text(
                text = "Please provide details about the issue you encountered. Your feedback helps us improve the radio experience.",
                style = MaterialTheme.typography.bodyMedium,
                color = com.campuswave.app.ui.theme.CampusGrey
            )
            
            // Error Message Display
            if (reportState is com.campuswave.app.utils.ApiResult.Error) {
                Text(
                    text = (reportState as com.campuswave.app.utils.ApiResult.Error).message,
                    color = com.campuswave.app.ui.theme.ErrorRed,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Issue Type Dropdown
            Box {
                OutlinedTextField(
                    value = issueType,
                    onValueChange = { },
                    label = { Text("Issue Type") },
                    modifier = Modifier.fillMaxWidth().clickable { isExpanded = true },
                    enabled = false, // Disable typing, force selection
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, "Dropdown")
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = CampusDark,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                
                // Overlay clickable box because TextField is disabled
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { isExpanded = true }
                )

                DropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false },
                    modifier = Modifier.fillMaxWidth().background(CampusSurface)
                ) {
                    issueTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type, color = CampusDark) },
                            onClick = {
                                issueType = type
                                isExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Description Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                minLines = 3,
                maxLines = 5
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Submit Button
            Button(
                onClick = {
                    viewModel.submitReport(issueType, description)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = issueType.isNotEmpty() && reportState !is com.campuswave.app.utils.ApiResult.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (reportState is com.campuswave.app.utils.ApiResult.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Submit Report")
                }
            }
        }
    }
}
