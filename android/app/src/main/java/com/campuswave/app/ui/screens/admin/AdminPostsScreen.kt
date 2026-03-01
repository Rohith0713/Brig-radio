package com.campuswave.app.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.campuswave.app.data.auth.AuthManager
import com.campuswave.app.data.models.CollegeUpdate
import com.campuswave.app.data.network.RetrofitClient
import com.campuswave.app.utils.UrlUtils
import kotlinx.coroutines.launch
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPostsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var updates by remember { mutableStateOf<List<CollegeUpdate>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch admin's posts
    LaunchedEffect(Unit) {
        val authManager = AuthManager(context)
        val token = authManager.getToken()
        
        if (token != null) {
            try {
                val response = RetrofitClient.apiService.getCollegeUpdates("Bearer $token")
                // Filter to show only current admin's posts
                val authManager2 = AuthManager(context)
                val userId = authManager2.getToken() // We'll need to get user ID properly
                updates = response.updates // Backend should ideally filter, but for now show all
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var updateToDelete by remember { mutableStateOf<CollegeUpdate?>(null) }

    if (showDeleteDialog && updateToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Post?") },
            text = { Text("Are you sure you want to delete this? This will permanently remove the media file from the server.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        updateToDelete?.let { update ->
                            scope.launch {
                                try {
                                    val authManager = AuthManager(context)
                                    val token = authManager.getToken() ?: return@launch
                                    RetrofitClient.apiService.deleteCollegeUpdate("Bearer $token", update.id)
                                    updates = updates.filter { it.id != update.id }
                                    Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Posts") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(com.campuswave.app.Screen.CreateCollegeUpdate.route) },
                containerColor = Color(0xFF3B82F6),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Post")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (updates.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No posts yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Create your first college update",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(updates) { update ->
                        AdminPostCard(
                            update = update,
                            onDeleteClick = {
                                updateToDelete = update
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPostCard(
    update: CollegeUpdate,
    onDeleteClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎓", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "Your Post",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${update.like_count} likes",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Media Content
            var showPreview by remember { mutableStateOf(false) }
            val mediaUrl = UrlUtils.joinUrl(
                com.campuswave.app.data.network.ApiConfig.BASE_URL.removeSuffix("/api/"),
                update.image_url
            ) ?: ""
            
            if (showPreview && update.media_type != "VIDEO") {
                com.campuswave.app.ui.components.ZoomableImageDialog(
                    imageUrl = mediaUrl,
                    onDismiss = { showPreview = false }
                )
            }
            
            if (update.media_type == "VIDEO") {
                com.campuswave.app.ui.components.FeedVideoPlayer(
                    mediaUrl = mediaUrl,
                    shouldPlay = true,
                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                )
            } else {
                AsyncImage(
                    model = mediaUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .pointerInput(mediaUrl) {
                            detectTapGestures(
                                onTap = { showPreview = true }
                            )
                        },
                    contentScale = ContentScale.FillWidth
                )
            }
            
            // Caption
            if (update.caption.isNotBlank()) {
                Text(
                    update.caption,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
