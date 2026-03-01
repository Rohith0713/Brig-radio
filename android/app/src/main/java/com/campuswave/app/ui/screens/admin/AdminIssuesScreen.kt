package com.campuswave.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.campuswave.app.ui.screens.student.StatusBadge
import com.campuswave.app.ui.theme.*
import com.campuswave.app.ui.viewmodels.IssueViewModel
import com.campuswave.app.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminIssuesScreen(
    onBackClick: () -> Unit,
    onIssueClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { IssueViewModel(context) }
    
    val issues by viewModel.issues.collectAsState()
    val resolvedIssues by viewModel.resolvedIssues.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val issueStats by viewModel.issueStats.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Open Issues", "Resolved")
    
    // Delete confirmation state
    var showDeleteDialog by remember { mutableStateOf(false) }
    var issueToDeleteId by remember { mutableIntStateOf(-1) }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Issue?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove the issue and all its messages. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteIssue(issueToDeleteId) {
                            android.widget.Toast.makeText(context, "Issue deleted", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF4D4D))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    val isDark = LocalIsDarkTheme.current
    val backgroundColor = if (isDark) Color(0xFF0D1117) else Color(0xFFF8F9FC)
    val surfaceColor = if (isDark) Color(0xFF161B22) else Color.White
    val primaryText = if (isDark) Color.White else Color(0xFF1A1A2E)
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
    
    // Load data on entry
    LaunchedEffect(Unit) {
        viewModel.loadAllIssues()
        viewModel.loadResolvedIssues()
        viewModel.loadIssueStats()
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
                        Column {
                            Text(
                                text = "Issue Reports",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                            if (issueStats != null) {
                                Text(
                                    text = "${issueStats!!.totalActive} active issues",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
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
            // Stats cards
            if (issueStats != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMiniCard(
                        label = "Open",
                        value = issueStats!!.openIssues.toString(),
                        color = Color(0xFF3B82F6),
                        modifier = Modifier.weight(1f),
                        surfaceColor = surfaceColor,
                        isDark = isDark
                    )
                    StatMiniCard(
                        label = "In Discussion",
                        value = issueStats!!.inDiscussion.toString(),
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f),
                        surfaceColor = surfaceColor,
                        isDark = isDark
                    )
                    StatMiniCard(
                        label = "Resolved",
                        value = issueStats!!.resolvedIssues.toString(),
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f),
                        surfaceColor = surfaceColor,
                        isDark = isDark
                    )
                }
            }
            
            // Tab Row
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = surfaceColor,
                shadowElevation = if (isDark) 0.dp else 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(4.dp)
                ) {
                    tabs.forEachIndexed { index, tabTitle ->
                        val isSelected = selectedTab == index
                        val count = if (index == 0) issues.size else resolvedIssues.size
                        
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
                                Text(
                                    text = tabTitle,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else secondaryText,
                                    fontSize = 14.sp
                                )
                                if (count > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) Color.White.copy(alpha = 0.2f) else secondaryText.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = count.toString(),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else secondaryText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Content
            val displayedIssues = if (selectedTab == 0) issues else resolvedIssues
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else if (displayedIssues.isEmpty()) {
                EmptyState(
                    isResolved = selectedTab == 1,
                    primaryText = primaryText,
                    secondaryText = secondaryText
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayedIssues) { issue ->
                        AdminIssueCard(
                            issue = issue,
                            onClick = { onIssueClick(issue.id) },
                            onDelete = {
                                issueToDeleteId = issue.id
                                showDeleteDialog = true
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
    }
}

@Composable
private fun StatMiniCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    surfaceColor: Color,
    isDark: Boolean
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = surfaceColor,
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AdminIssueCard(
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
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = issue.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = primaryText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = issue.description,
                        fontSize = 13.sp,
                        color = secondaryText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusBadge(status = issue.status)
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { onDelete() },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFF4D4D).copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Delete",
                                tint = Color(0xFFFF4D4D),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = secondaryText.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))
            
            // Student info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = PrimaryBlue.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = issue.studentName ?: "Unknown Student",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = primaryText
                        )
                        if (issue.studentRollNumber != null) {
                            Text(
                                text = issue.studentRollNumber,
                                fontSize = 11.sp,
                                color = secondaryText
                            )
                        }
                    }
                }
                
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
            }
            
            // Message count
            if (issue.messageCount > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Chat,
                        contentDescription = null,
                        tint = accentPurple,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${issue.messageCount} messages in conversation",
                        fontSize = 12.sp,
                        color = accentPurple,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    isResolved: Boolean,
    primaryText: Color,
    secondaryText: Color
) {
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
            color = if (isResolved) SuccessGreen.copy(alpha = 0.1f) else PrimaryBlue.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (isResolved) Icons.Default.TaskAlt else Icons.Default.Inbox,
                    contentDescription = null,
                    tint = if (isResolved) SuccessGreen else PrimaryBlue,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (isResolved) "No Resolved Issues" else "No Open Issues",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = primaryText
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isResolved) 
                "Resolved issues will appear here" 
            else 
                "Great! There are no pending issues\nto review at this time.",
            fontSize = 14.sp,
            color = secondaryText,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}
