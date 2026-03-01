package com.campuswave.app.ui.screens.student

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.campuswave.app.data.models.CollegeUpdate
import com.campuswave.app.data.network.RetrofitClient
import com.campuswave.app.data.auth.AuthManager
import com.campuswave.app.utils.UrlUtils
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import android.content.Intent
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import com.campuswave.app.ui.components.CollegeUpdateSkeleton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollegeUpdatesScreen(
    navController: NavController,
    targetUpdateId: Int? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var updates by remember { mutableStateOf<List<CollegeUpdate>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentUserId by remember { mutableStateOf<Int?>(null) }
    var isAdmin by remember { mutableStateOf(false) }

    // Analytics State
    var showAnalyticsSheet by remember { mutableStateOf(false) }
    var selectedUpdateForAnalytics by remember { mutableStateOf<CollegeUpdate?>(null) }
    var analyticsData by remember { mutableStateOf<com.campuswave.app.data.models.UpdateAnalyticsResponse?>(null) }
    var isLoadingAnalytics by remember { mutableStateOf(false) }
    val analyticsSheetState = rememberModalBottomSheetState()
    val listState = rememberLazyListState()

    // Pagination & Refresh State
    var currentPage by remember { mutableIntStateOf(1) }
    var hasMore by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isNextPageLoading by remember { mutableStateOf(false) }

    // Auto-scroll logic for deep links
    LaunchedEffect(updates, targetUpdateId) {
        if (targetUpdateId != null && updates.isNotEmpty()) {
            val index = updates.indexOfFirst { it.id == targetUpdateId }
            if (index != -1) {
                listState.animateScrollToItem(index)
            }
        }
    }

    // Fetch data function for refresh
    fun fetchUpdates(refresh: Boolean = false) {
        scope.launch {
            if (refresh) {
                isRefreshing = true
                currentPage = 1
                hasMore = true
            } else if (currentPage > 1) {
                isNextPageLoading = true
            } else {
                isLoading = true
            }

            val authManager = AuthManager(context)
            val token = authManager.getToken()
            val role = authManager.getUserRole()
            isAdmin = role == "ADMIN" || role == "MAIN_ADMIN"
            
            if (token != null) {
                try {
                    val response = RetrofitClient.apiService.getCollegeUpdates(
                        token = "Bearer $token",
                        page = currentPage,
                        limit = 10
                    )
                    
                    if (refresh) {
                        updates = response.updates
                    } else {
                        updates = updates + response.updates
                    }
                    
                    hasMore = response.page < response.pages
                    if (hasMore) currentPage++
                    
                } catch (e: Exception) {
                    Toast.makeText(context, "Error loading updates: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                } finally {
                    isLoading = false
                    isRefreshing = false
                    isNextPageLoading = false
                }
            } else {
                isLoading = false
                isRefreshing = false
                isNextPageLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchUpdates()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("College Updates") },
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
        }
    ) { paddingValues ->
        // Pull to Refresh implementation
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { fetchUpdates(refresh = true) },
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (isLoading) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(5) { CollegeUpdateSkeleton() }
                }
            } else if (updates.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Feed,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No updates yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Stay tuned for college news!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { fetchUpdates() }) {
                        Text("Refresh")
                    }
                }
            } else {
                // Video Autoplay Logic: Find the center-most visible item
                val currentlyPlayingIndex by remember {
                    derivedStateOf {
                        val layoutInfo = listState.layoutInfo
                        val visibleItemsInfo = layoutInfo.visibleItemsInfo
                        if (visibleItemsInfo.isEmpty()) return@derivedStateOf -1
                        
                        // Find the item closest to the center of the viewport
                        val viewportCenter = layoutInfo.viewportStartOffset + (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
                        
                        visibleItemsInfo.minByOrNull { 
                            kotlin.math.abs((it.offset + it.size / 2) - viewportCenter) 
                        }?.index ?: -1
                    }
                }

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(updates) { index, update ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + expandVertically()
                        ) {
                            CollegeUpdateCard(
                                update = update,
                                isAdmin = isAdmin,
                                shouldPlay = index == currentlyPlayingIndex,
                                onLikeClick = {
                                    scope.launch {
                                        try {
                                            val authManager = AuthManager(context)
                                            val token = authManager.getToken() ?: return@launch
                                            val response = RetrofitClient.apiService.toggleLike("Bearer $token", update.id)
                                            
                                            // Update local state
                                            updates = updates.map { 
                                                if (it.id == update.id) {
                                                    it.copy(
                                                        is_liked = response.is_liked,
                                                        like_count = response.like_count
                                                    )
                                                } else it
                                            }
                                        } catch (e: Exception) {
                                            // Handle error
                                        }
                                    }
                                },
                                onDeleteClick = {
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
                                },
                                onAnalyticsClick = {
                                    selectedUpdateForAnalytics = update
                                    showAnalyticsSheet = true
                                    scope.launch {
                                        isLoadingAnalytics = true
                                        try {
                                            val authManager = AuthManager(context)
                                            val token = authManager.getToken() ?: return@launch
                                            val response = RetrofitClient.apiService.getUpdateAnalytics("Bearer $token", update.id)
                                            analyticsData = response
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Failed to load analytics", Toast.LENGTH_SHORT).show()
                                        } finally {
                                            isLoadingAnalytics = false
                                        }
                                    }
                                },
                                onSaveClick = {
                                    updates = updates.map { 
                                        if (it.id == update.id) it.copy(is_saved = !it.is_saved) else it 
                                    }
                                    Toast.makeText(context, if (!update.is_saved) "Saved to bookmarks" else "Removed from bookmarks", Toast.LENGTH_SHORT).show()
                                },
                                onShareClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "College Update")
                                        putExtra(Intent.EXTRA_TEXT, "${update.caption}\n\nShared via BRIG RADIO")
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                                }
                            )
                        }
                    }

                    if (isNextPageLoading) {
                        item {
                            CollegeUpdateSkeleton()
                        }
                    }

                    // Pagination trigger
                    item {
                        LaunchedEffect(Unit) {
                            if (hasMore && !isNextPageLoading) {
                                fetchUpdates()
                            }
                        }
                    }
                }
            }
        }
    }

    // Analytics Bottom Sheet
    if (showAnalyticsSheet && selectedUpdateForAnalytics != null) {
        ModalBottomSheet(
            onDismissRequest = { 
                showAnalyticsSheet = false
                selectedUpdateForAnalytics = null
                analyticsData = null
            },
            sheetState = analyticsSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    "Post Analytics",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Engagement metrics for this update",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                if (isLoadingAnalytics) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (analyticsData != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AnalyticsStatCard("Views", analyticsData!!.views.toString(), Icons.Default.Visibility, Color(0xFF6366F1))
                        AnalyticsStatCard("Likes", analyticsData!!.likes.toString(), Icons.Default.Favorite, Color(0xFFEF4444))
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Post Details", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Posted on: ${formatUpdateDate(analyticsData!!.created_at)}", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(analyticsData!!.caption, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { showAnalyticsSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun AnalyticsStatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun CollegeUpdateCard(
    update: CollegeUpdate,
    isAdmin: Boolean,
    shouldPlay: Boolean,
    onLikeClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    
    // Heart animation state
    var showHeartOverlay by remember { mutableStateOf(false) }
    val heartAlpha by animateFloatAsState(
        targetValue = if (showHeartOverlay) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "HeartAlpha"
    )
    val heartScale by animateFloatAsState(
        targetValue = if (showHeartOverlay) 1.5f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "HeartScale"
    )

    // Like button bounce animation
    val likeScale by animateFloatAsState(
        targetValue = if (update.is_liked) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "LikeScale"
    )

    // Record view when card is displayed
    LaunchedEffect(update.id) {
        scope.launch {
            try {
                val authManager = AuthManager(context)
                val token = authManager.getToken() ?: return@launch
                RetrofitClient.apiService.recordUpdateView("Bearer $token", update.id)
            } catch (e: Exception) { }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(0.dp) // Keep it full width but with card behavior
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Campus Admin",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        formatUpdateDate(update.created_at),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
                
                if (isAdmin) {
                    Row {
                        IconButton(onClick = onAnalyticsClick) {
                            Icon(Icons.Default.BarChart, contentDescription = "Analytics", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onDeleteClick) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            
            // Immersive Media with Interactions
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center
            ) {
                val mediaUrl = UrlUtils.joinUrl(
                    com.campuswave.app.data.network.ApiConfig.BASE_URL.removeSuffix("/api/"),
                    update.image_url
                ) ?: ""

                if (update.media_type == "VIDEO") {
                    com.campuswave.app.ui.components.FeedVideoPlayer(
                        mediaUrl = mediaUrl,
                        shouldPlay = shouldPlay,
                        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                        onDoubleTap = {
                            if (!update.is_liked) {
                                onLikeClick()
                            }
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showHeartOverlay = true
                            scope.launch {
                                kotlinx.coroutines.delay(600)
                                showHeartOverlay = false
                            }
                        }
                    )
                } else {
                    var showPreview by remember { mutableStateOf(false) }
                    if (showPreview) {
                        com.campuswave.app.ui.components.ZoomableImageDialog(
                            imageUrl = mediaUrl,
                            onDismiss = { showPreview = false }
                        )
                    }

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(mediaUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .pointerInput(mediaUrl) {
                                detectTapGestures(
                                    onTap = { showPreview = true },
                                    onDoubleTap = {
                                        if (!update.is_liked) {
                                            onLikeClick()
                                        }
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showHeartOverlay = true
                                        scope.launch {
                                            kotlinx.coroutines.delay(600)
                                            showHeartOverlay = false
                                        }
                                    }
                                )
                            },
                        contentScale = ContentScale.FillWidth
                    )
                }

                // Heart Overlay
                if (heartAlpha > 0f) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = heartAlpha),
                        modifier = Modifier
                            .size(120.dp)
                            .scale(heartScale)
                            .graphicsLayer {
                                rotationZ = -10f // Slight tilt for style
                            }
                    )
                }
            }
            
            // Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like button with count below
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    IconButton(
                        onClick = { 
                            onLikeClick()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        modifier = Modifier.scale(likeScale)
                    ) {
                        Icon(
                            if (update.is_liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (update.is_liked) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        "${update.like_count} likes",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                IconButton(onClick = onShareClick) {
                    Icon(Icons.Outlined.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurface)
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(onClick = onSaveClick) {
                    Icon(
                        if (update.is_saved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (update.is_saved) Color(0xFF3B82F6) else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Row(
                    modifier = Modifier.padding(end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${update.view_count}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Caption with Read More logic
            if (update.caption.isNotBlank()) {
                var isExpanded by remember { mutableStateOf(false) }
                val hasMore = update.caption.length > 100 || update.caption.count { it == '\n' } > 2

                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .padding(bottom = 12.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = buildString {
                            append("Campus Admin  ")
                            append(update.caption)
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (hasMore && !isExpanded) {
                        Text(
                            text = "read more",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { isExpanded = true }
                        )
                    } else if (isExpanded) {
                        Text(
                            text = "show less",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { isExpanded = false }
                        )
                    }
                }
            }
        }
    }
}


private fun formatUpdateDate(isoString: String): String {
    return try {
        // Simple parser, adjust to your backend format if needed
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault())
        val date = inputFormat.parse(isoString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        isoString // Fallback
    }
}
