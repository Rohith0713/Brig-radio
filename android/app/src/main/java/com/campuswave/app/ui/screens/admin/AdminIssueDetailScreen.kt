package com.campuswave.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.data.models.Issue
import com.campuswave.app.data.models.IssueMessage
import com.campuswave.app.data.models.IssueStatus
import com.campuswave.app.ui.screens.student.StatusBadge
import com.campuswave.app.ui.theme.*
import com.campuswave.app.ui.viewmodels.IssueViewModel
import com.campuswave.app.utils.DateUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminIssueDetailScreen(
    issueId: Int,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { IssueViewModel(context) }
    val scope = rememberCoroutineScope()
    
    val currentIssue by viewModel.currentIssue.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val isSendingMessage by viewModel.isSendingMessage.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    
    var messageText by remember { mutableStateOf("") }
    var showResolveDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    
    val isDark = LocalIsDarkTheme.current
    val backgroundColor = if (isDark) Color(0xFF0D1117) else Color(0xFFF8F9FC)
    val surfaceColor = if (isDark) Color(0xFF161B22) else Color.White
    val primaryText = if (isDark) Color.White else Color(0xFF1A1A2E)
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
    
    // Load issue details
    LaunchedEffect(issueId) {
        viewModel.loadIssueDetails(issueId)
    }
    
    // Scroll to bottom when messages change
    LaunchedEffect(currentIssue?.messages?.size) {
        currentIssue?.messages?.let {
            if (it.isNotEmpty()) {
                scope.launch {
                    listState.animateScrollToItem(it.size - 1)
                }
            }
        }
    }
    
    // Handle success (for resolve action)
    LaunchedEffect(successMessage) {
        if (successMessage != null && successMessage!!.contains("resolved", ignoreCase = true)) {
            viewModel.clearSuccessMessage()
        }
    }
    
    // Resolve confirmation dialog
    if (showResolveDialog) {
        AlertDialog(
            onDismissRequest = { showResolveDialog = false },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text("Resolve Issue?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "This will mark the issue as resolved and close the chat. The student will be notified.\n\nThis action cannot be undone.",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResolveDialog = false
                        viewModel.resolveIssue(issueId) {
                            // Issue resolved, refresh details
                            viewModel.loadIssueDetails(issueId)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("Mark as Resolved")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResolveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
                                text = currentIssue?.title ?: "Issue Details",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White,
                                maxLines = 1
                            )
                            if (currentIssue != null) {
                                Text(
                                    text = when (currentIssue!!.status) {
                                        IssueStatus.OPEN -> "Open"
                                        IssueStatus.IN_DISCUSSION -> "In Discussion"
                                        IssueStatus.RESOLVED -> "Resolved"
                                    },
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
                    actions = {
                        if (currentIssue != null && currentIssue!!.status != IssueStatus.RESOLVED) {
                            IconButton(onClick = { showResolveDialog = true }) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Resolve",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
        bottomBar = {
            if (currentIssue != null && currentIssue!!.status != IssueStatus.RESOLVED) {
                // Message input bar with resolve button
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = surfaceColor,
                    shadowElevation = 8.dp
                ) {
                    Column {
                        // Quick resolve button
                        if (currentIssue!!.status == IssueStatus.IN_DISCUSSION) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = SuccessGreen.copy(alpha = 0.1f),
                                onClick = { showResolveDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Mark as Resolved",
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                        
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .imePadding(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = messageText,
                                onValueChange = { messageText = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Reply to student...") },
                                maxLines = 4,
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFE2E8F0)
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            FilledIconButton(
                                onClick = {
                                    if (messageText.isNotBlank()) {
                                        viewModel.sendMessage(issueId, messageText)
                                        messageText = ""
                                    }
                                },
                                enabled = messageText.isNotBlank() && !isSendingMessage,
                                modifier = Modifier.size(48.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = PrimaryBlue
                                )
                            ) {
                                if (isSendingMessage) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (currentIssue?.status == IssueStatus.RESOLVED) {
                // Resolved banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SuccessGreen.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Issue resolved on ${currentIssue!!.resolvedAt?.let { DateUtils.formatRelativeTime(it) } ?: "unknown date"}",
                            color = SuccessGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else if (currentIssue != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Student info card
                item {
                    StudentInfoCard(
                        issue = currentIssue!!,
                        surfaceColor = surfaceColor,
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                        isDark = isDark
                    )
                }
                
                // Issue description card
                item {
                    IssueDescriptionCard(
                        issue = currentIssue!!,
                        surfaceColor = surfaceColor,
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                        isDark = isDark
                    )
                }
                
                // Messages section
                if (currentIssue!!.messages?.isNotEmpty() == true) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                color = secondaryText.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "Conversation",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                    fontSize = 12.sp,
                                    color = secondaryText,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    items(currentIssue!!.messages!!) { message ->
                        AdminChatBubble(
                            message = message,
                            isAdmin = message.senderRole == "admin",
                            isDark = isDark
                        )
                    }
                } else {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.QuestionAnswer,
                                contentDescription = null,
                                tint = PrimaryBlue.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Start the conversation",
                                color = primaryText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Reply to help resolve this issue",
                                color = secondaryText,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else if (error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = error ?: "Failed to load issue",
                        color = ErrorRed
                    )
                }
            }
        }
    }
}

@Composable
private fun StudentInfoCard(
    issue: Issue,
    surfaceColor: Color,
    primaryText: Color,
    secondaryText: Color,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = surfaceColor,
        shadowElevation = if (isDark) 0.dp else 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(50.dp),
                shape = CircleShape,
                color = PrimaryBlue.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = issue.studentName ?: "Unknown Student",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = primaryText
                )
                if (issue.studentRollNumber != null) {
                    Text(
                        text = "Roll: ${issue.studentRollNumber}",
                        fontSize = 13.sp,
                        color = secondaryText
                    )
                }
                if (issue.studentEmail != null) {
                    Text(
                        text = issue.studentEmail,
                        fontSize = 12.sp,
                        color = secondaryText
                    )
                }
            }
            StatusBadge(status = issue.status)
        }
    }
}

@Composable
private fun IssueDescriptionCard(
    issue: Issue,
    surfaceColor: Color,
    primaryText: Color,
    secondaryText: Color,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = surfaceColor,
        shadowElevation = if (isDark) 0.dp else 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Issue Description",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = secondaryText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = issue.description,
                fontSize = 15.sp,
                color = primaryText,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = secondaryText,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Submitted ${issue.createdAt?.let { DateUtils.formatRelativeTime(it) } ?: "Unknown"}",
                    fontSize = 12.sp,
                    color = secondaryText
                )
            }
        }
    }
}

@Composable
private fun AdminChatBubble(
    message: IssueMessage,
    isAdmin: Boolean,
    isDark: Boolean
) {
    val bubbleColor = if (isAdmin) {
        PrimaryBlue
    } else {
        if (isDark) Color(0xFF2D3748) else Color(0xFFF1F5F9)
    }
    
    val textColor = if (isAdmin) {
        Color.White
    } else {
        if (isDark) Color.White else Color(0xFF1A1A2E)
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isAdmin) Alignment.End else Alignment.Start
    ) {
        // Sender label
        Text(
            text = if (isAdmin) "You (Admin)" else message.senderName ?: "Student",
            fontSize = 11.sp,
            color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF64748B),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
        
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isAdmin) 16.dp else 4.dp,
                bottomEnd = if (isAdmin) 4.dp else 16.dp
            ),
            color = bubbleColor,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.message,
                    color = textColor,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.createdAt?.let { DateUtils.formatMessageTime(it) } ?: "",
                    color = textColor.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
