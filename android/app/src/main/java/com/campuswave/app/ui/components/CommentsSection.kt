package com.campuswave.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.data.models.Comment
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun CommentsSection(
    comments: List<Comment>,
    isLoading: Boolean,
    onSendComment: (String) -> Unit,
    onDeleteComment: (Comment) -> Unit,
    currentUserId: Int?,
    isAdmin: Boolean = false,
    modifier: Modifier = Modifier
) {
    var commentText by remember { mutableStateOf("") }
    
    Column(modifier = modifier) {
        // Comments List
        Text(
            text = "Comments (${comments.size})",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color(0xFF1F2937),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF5E72E4),
                    modifier = Modifier.size(32.dp)
                )
            }
        } else if (comments.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = Color(0xFFCBD5E0),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No comments yet",
                        fontSize = 14.sp,
                        color = Color(0xFF9CA3AF)
                    )
                    Text(
                        text = "Be the first to comment!",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E0)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(comments) { comment ->
                    CommentItem(
                        comment = comment,
                        canDelete = currentUserId == comment.user_id || isAdmin,
                        onDelete = { onDeleteComment(comment) }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Comment Input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("Write a comment...", color = Color(0xFF9CA3AF)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF1F2937),
                    unfocusedTextColor = Color(0xFF374151),
                    focusedBorderColor = Color(0xFF5E72E4),
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (commentText.isNotBlank()) {
                            onSendComment(commentText)
                            commentText = ""
                        }
                    }
                )
            )
            
            IconButton(
                onClick = {
                    if (commentText.isNotBlank()) {
                        onSendComment(commentText)
                        commentText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF5E72E4), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF5E72E4).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = comment.user_name.firstOrNull()?.uppercase() ?: "?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF5E72E4)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = comment.user_name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF1F2937)
                    )
                    
                    Text(
                        text = formatCommentTime(comment.created_at),
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
                
                Text(
                    text = comment.content,
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            if (canDelete) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun formatCommentTime(isoString: String?): String {
    if (isoString == null) return ""
    return try {
        val dateTime = ZonedDateTime.parse(isoString)
        dateTime.format(DateTimeFormatter.ofPattern("MMM d, HH:mm"))
    } catch (e: Exception) {
        try {
            isoString.take(16).replace("T", " ")
        } catch (e: Exception) {
            ""
        }
    }
}
