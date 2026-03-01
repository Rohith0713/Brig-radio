package com.campuswave.app.ui.screens.student

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.campuswave.app.ui.theme.*
import com.campuswave.app.data.models.Placement
import com.campuswave.app.data.models.PlacementPoster
import com.campuswave.app.data.models.User
import com.campuswave.app.data.network.ApiConfig
import com.campuswave.app.utils.UrlUtils

@Composable
fun PlacementsScreen(
    placements: List<Placement> = emptyList(),
    posters: List<PlacementPoster> = emptyList(),
    onBackClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onBookmarkClick: (Int) -> Unit = {},
    currentUser: User? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Explore", "Saved", "Achievements")

    // Filtered placements
    val filteredPlacements = placements.filter {
        (it.title.contains(searchQuery, ignoreCase = true) || 
        it.company.contains(searchQuery, ignoreCase = true)) &&
        (selectedTabIndex == 0 || it.isBookmarked)
    }
    
    // Filtered posters
    val visiblePosters = posters.filter { it.isVisible }

    Scaffold(
        containerColor = campusBackground(),
        topBar = {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Placements",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = campusOnBackground()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNotificationClick) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onProfileClick() },
                            contentAlignment = Alignment.Center
                        ) {
                             Icon(Icons.Default.Person, contentDescription = "Profile", tint = campusOnBackground())
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Search Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = androidx.compose.ui.text.TextStyle(color = campusOnBackground(), fontSize = 16.sp),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text("Search roles or companies", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = accentPurple,
                    divider = {},
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = accentPurple
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 15.sp
                                )
                            },
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (selectedTabIndex == 2) {
                // Achievements Tab
                if (visiblePosters.isEmpty()) {
                    EmptyPlacementsState("No achievement posters available yet")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(visiblePosters) { poster ->
                            StudentPosterCard(poster = poster)
                        }
                    }
                }
            } else {
                // Explore / Saved Tab
                if (filteredPlacements.isEmpty()) {
                    EmptyPlacementsState(
                        if (searchQuery.isNotEmpty()) "No matching roles found"
                        else if (selectedTabIndex == 1) "No saved placements yet"
                        else "No opportunities available yet"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(filteredPlacements) { placement ->
                            PlacementItem(
                                placement = placement,
                                onBookmarkClick = { onBookmarkClick(it.id) }
                            )
                            HorizontalDivider(color = campusDivider(), modifier = Modifier.padding(horizontal = 20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyPlacementsState(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

@Composable
fun StudentPosterCard(poster: PlacementPoster) {
    val isDark = LocalIsDarkTheme.current
    val surfaceColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White
    
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = UrlUtils.joinUrl(ApiConfig.BASE_URL.removeSuffix("/api/"), poster.posterImage),
                contentDescription = poster.title,
                modifier = Modifier.fillMaxWidth().height(220.dp),
                contentScale = ContentScale.Crop
            )
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = poster.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = campusOnBackground()
                )
                
                if (!poster.company.isNullOrEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Business, null, modifier = Modifier.size(16.dp), tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = poster.company,
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                if (!poster.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = poster.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PlacementItem(
    placement: Placement,
    onBookmarkClick: (Placement) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                placement.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = campusOnBackground()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${placement.company} • ${placement.location}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                buildString {
                    append(placement.salary ?: "")
                    if (placement.applicantsCount > 0) append(" • ${placement.applicantsCount} applicants")
                    if (placement.deadline != null) append(" • Deadline: ${placement.deadline}")
                },
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            IconButton(
                onClick = { onBookmarkClick(placement) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    if (placement.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (placement.isBookmarked) accentPurple else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "APPLY",
                color = accentPurple,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { 
                    placement.applicationLink?.let { link ->
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(link))
                        context.startActivity(intent)
                    }
                }
            )
        }
    }
}
