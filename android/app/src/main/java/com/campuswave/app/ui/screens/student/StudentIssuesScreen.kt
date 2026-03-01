package com.campuswave.app.ui.screens.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.data.models.Issue
import com.campuswave.app.data.models.IssueStatus
import com.campuswave.app.ui.theme.*
import com.campuswave.app.ui.viewmodels.IssueViewModel
import com.campuswave.app.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentIssuesScreen(
    onBackClick: () -> Unit,
    onIssueClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { IssueViewModel(context) }
    
    val issues by viewModel.issues.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("My Issues", "Report Issue")
    
    // Form state
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    val isDark = LocalIsDarkTheme.current
    val backgroundColor = if (isDark) Color(0xFF0D1117) else Color(0xFFF8F9FC)
    val surfaceColor = if (isDark) Color(0xFF161B22) else Color.White
    val primaryText = if (isDark) Color.White else Color(0xFF1A1A2E)
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
    
    // Load issues on entry
    LaunchedEffect(Unit) {
        viewModel.loadMyIssues()
    }
    
    // Handle success
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            title = ""
            description = ""
            selectedTab = 0 // Switch to issues list
            viewModel.clearSuccessMessage()
        }
    }
    
    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(PrimaryBlue, accentPurple)
                        )
                    )
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Issue Center",
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                color = surfaceColor,
                shadowElevation = if (isDark) 0.dp else 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(4.dp)
                ) {
                    tabs.forEachIndexed { index, tabTitle ->
                        val isSelected = selectedTab == index
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = index },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) PrimaryBlue else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (index == 0) Icons.Default.List else Icons.Default.Add,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else secondaryText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tabTitle,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else secondaryText,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
            
            // Error display
            AnimatedVisibility(visible = error != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color = ErrorRed.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error ?: "",
                            color = ErrorRed,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = ErrorRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            
            // Content
            when (selectedTab) {
                0 -> IssuesListTab(
                    issues = issues,
                    isLoading = isLoading,
                    onIssueClick = onIssueClick,
                    onDeleteClick = { issueId ->
                        viewModel.deleteIssue(issueId)
                    },
                    onRefresh = { viewModel.loadMyIssues() },
                    isDark = isDark,
                    surfaceColor = surfaceColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText
                )
                1 -> ReportIssueTab(
                    title = title,
                    onTitleChange = { title = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    isSubmitting = isSubmitting,
                    onSubmit = {
                        viewModel.createIssue(title, description)
                    },
                    isDark = isDark,
                    surfaceColor = surfaceColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText
                )
            }
        }
    }
}

@Composable
private fun IssuesListTab(
    issues: List<Issue>,
    isLoading: Boolean,
    onIssueClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit,
    onRefresh: () -> Unit,
    isDark: Boolean,
    surfaceColor: Color,
    primaryText: Color,
    secondaryText: Color
) {
    var showDeleteDialog by remember { mutableStateOf<Int?>(null) }
    
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Issue") },
            text = { Text("Are you sure you want to delete this issue? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog?.let { onDeleteClick(it) }
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
    } else if (issues.isEmpty()) {
        // ... (empty state content)
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
                color = PrimaryBlue.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "No Issues Reported",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = primaryText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "When you report an issue, it will appear here.\nSwipe to the \"Report Issue\" tab to get started.",
                fontSize = 14.sp,
                color = secondaryText,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(issues) { issue ->
                IssueCard(
                    issue = issue,
                    onClick = { onIssueClick(issue.id) },
                    onDelete = { showDeleteDialog = issue.id },
                    isDark = isDark,
                    surfaceColor = surfaceColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText
                )
            }
        }
    }
}

@Composable
private fun IssueCard(
    issue: Issue,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    isDark: Boolean,
    surfaceColor: Color,
    primaryText: Color,
    secondaryText: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = surfaceColor,
        shadowElevation = if (isDark) 0.dp else 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = issue.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = primaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status = issue.status)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Issue",
                            tint = ErrorRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = issue.description,
                fontSize = 14.sp,
                color = secondaryText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = secondaryText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = issue.createdAt?.let { DateUtils.formatRelativeTime(it) } ?: "Unknown",
                        fontSize = 12.sp,
                        color = secondaryText
                    )
                }
                
                if (issue.messageCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${issue.messageCount} messages",
                            fontSize = 12.sp,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: IssueStatus) {
    val (backgroundColor, textColor, label) = when (status) {
        IssueStatus.OPEN -> Triple(
            Color(0xFF3B82F6).copy(alpha = 0.15f),
            Color(0xFF3B82F6),
            "Open"
        )
        IssueStatus.IN_DISCUSSION -> Triple(
            Color(0xFFF59E0B).copy(alpha = 0.15f),
            Color(0xFFF59E0B),
            "In Discussion"
        )
        IssueStatus.RESOLVED -> Triple(
            Color(0xFF10B981).copy(alpha = 0.15f),
            Color(0xFF10B981),
            "Resolved"
        )
    }
    
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ReportIssueTab(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    isSubmitting: Boolean,
    onSubmit: () -> Unit,
    isDark: Boolean,
    surfaceColor: Color,
    primaryText: Color,
    secondaryText: Color
) {
    val isValid = title.isNotBlank() && description.isNotBlank()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Info card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = PrimaryBlue.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Report College-Related Issues",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = primaryText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Submit any issues you're facing. An admin will review and respond through the chat system.",
                        fontSize = 13.sp,
                        color = secondaryText,
                        lineHeight = 18.sp
                    )
                }
            }
        }
        
        // Title field
        Column {
            Text(
                text = "Issue Title",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = primaryText
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Brief summary of the issue") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFE2E8F0)
                )
            )
        }
        
        // Description field
        Column {
            Text(
                text = "Description",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = primaryText
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                placeholder = { Text("Describe the issue in detail...") },
                minLines = 5,
                maxLines = 8,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFE2E8F0)
                )
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Submit button
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = isValid && !isSubmitting,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue,
                disabledContainerColor = PrimaryBlue.copy(alpha = 0.5f)
            )
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Submit Issue",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
