package com.campuswave.app.ui.screens.student

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.R
import com.campuswave.app.data.models.Radio
import com.campuswave.app.data.models.User
import com.campuswave.app.data.models.Marquee
import com.campuswave.app.ui.components.MarqueeComponent
import com.campuswave.app.ui.theme.*
import com.campuswave.app.ui.components.SimpleRadioCard
import com.campuswave.app.ui.components.ProfileSidebar
import com.campuswave.app.utils.DateUtils
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import coil.compose.SubcomposeAsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
    liveRadios: List<Radio> = emptyList(),
    upcomingRadios: List<Radio> = emptyList(),
    pastRadios: List<Radio> = emptyList(),
    isLoadingLive: Boolean = false,
    isLoadingUpcoming: Boolean = false,
    isLoadingPast: Boolean = false,
    currentUser: User? = null,
    baseUrl: String = "",
    onNotificationsClick: () -> Unit = {},
    onSuggestRadioClick: () -> Unit = {},
    onUserDetailsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onRadioClick: (Int) -> Unit = {},
    onLikeClick: (Int) -> Unit = {},
    onReminderToggle: (Int) -> Unit = {},
    onCollegeUpdatesClick: () -> Unit = {},
    onPodcastsClick: () -> Unit = {},
    onPlacementsClick: () -> Unit = {},
    onIssuesClick: () -> Unit = {},
    onRefresh: () -> Unit = {},
    activeMarquee: Marquee? = null,
    banners: List<com.campuswave.app.data.models.Banner> = emptyList(),
    livePodcast: com.campuswave.app.data.models.Podcast? = null,
    onJoinPodcastClick: (Int) -> Unit = {},
    synchronizedTimeMillis: Long = System.currentTimeMillis()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf("All") }
    val tabs = listOf("Live Radio", "Upcoming", "Past Radio")
    val categories = listOf("All", "Academic", "Co-Curricular", "Extra-Curricular", "Music", "Talk Show", "Guest Lecture", "Branch Sponsored")
    
    val isDark = LocalIsDarkTheme.current
    val backgroundColor = if (isDark) Color(0xFF0D1117) else Color(0xFFF8F9FC)
    val surfaceColor = if (isDark) Color(0xFF161B22) else Color.White
    val primaryText = if (isDark) Color.White else Color(0xFF1A1A2E)
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF8E8E93)

    val filteredRadios = remember(selectedTabIndex, selectedCategory, liveRadios, upcomingRadios, pastRadios) {
        val currentList = when (selectedTabIndex) {
            0 -> liveRadios
            1 -> upcomingRadios.filter { it.status != "DRAFT" }
            2 -> pastRadios
            else -> liveRadios
        }
        if (selectedCategory == "All") currentList
        else currentList.filter { it.category?.name?.contains(selectedCategory, ignoreCase = true) == true }
    }
    
    Scaffold(
        containerColor = backgroundColor,
        bottomBar = {
            // Bottom Navigation Bar
            Surface(
                modifier = Modifier.fillMaxWidth().height(85.dp),
                color = surfaceColor,
                shadowElevation = if (isDark) 0.dp else 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 30.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Placements (Left)
                    Column(
                        modifier = Modifier.clickable { onPlacementsClick() },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.BusinessCenter,
                            contentDescription = "Placements",
                            tint = secondaryText,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text("Placements", fontSize = 9.sp, color = secondaryText, fontWeight = FontWeight.Medium)
                    }

                    // Podcasts
                    Column(
                        modifier = Modifier.clickable { onPodcastsClick() },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Podcasts,
                            contentDescription = "Podcasts",
                            tint = secondaryText,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text("Podcasts", fontSize = 9.sp, color = secondaryText, fontWeight = FontWeight.Medium)
                    }

                    // Floating Action Button (Middle - Suggest)
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .offset(y = (-10).dp)
                            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(listOf(accentPurple, Color(0xFF8B5CF6))),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { onSuggestRadioClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Suggest", tint = Color.White, modifier = Modifier.size(26.dp))
                    }

                    // Issues
                    Column(
                        modifier = Modifier.clickable { onIssuesClick() },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Report,
                            contentDescription = "Issues",
                            tint = secondaryText,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text("Issues", fontSize = 9.sp, color = secondaryText, fontWeight = FontWeight.Medium)
                    }

                    // Updates (Right)
                    Column(
                        modifier = Modifier.clickable { onCollegeUpdatesClick() },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Newspaper,
                            contentDescription = "Updates",
                            tint = secondaryText,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text("Updates", fontSize = 9.sp, color = secondaryText, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header (Fixed)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Logo Icon (Replacement)
                    Image(
                        painter = painterResource(id = R.drawable.brig_logo),
                        contentDescription = "BRIG Logo",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp),
                        contentScale = ContentScale.Fit
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "BRIG RADIO",
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                        fontSize = 22.sp
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Alerts Icon
                    IconButton(
                        onClick = { onNotificationsClick() },
                        modifier = Modifier
                            .size(44.dp)
                            .padding(4.dp)
                    ) {
                        BadgedBox(
                            badge = {}
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Alerts",
                                tint = primaryText,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Profile with Status Dot
                    Box(
                        modifier = Modifier.size(44.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize().clickable { onUserDetailsClick() },
                            shape = CircleShape,
                            color = accentPurple.copy(alpha = 0.1f),
                            border = BorderStroke(2.dp, accentPurple.copy(alpha = 0.3f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (currentUser?.profile_picture != null) {
                                    coil.compose.SubcomposeAsyncImage(
                                        model = com.campuswave.app.utils.UrlUtils.joinUrl(com.campuswave.app.data.network.ApiConfig.BASE_URL.removeSuffix("/api/"), currentUser.profile_picture),
                                        contentDescription = "Profile",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                        loading = { CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) },
                                        error = { Icon(Icons.Default.Person, contentDescription = null, tint = accentPurple) }
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Profile",
                                        tint = accentPurple,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                        
                        // Status Dot
                        Surface(
                            modifier = Modifier
                                .size(12.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = 2.dp, y = 2.dp),
                            shape = CircleShape,
                            color = Color(0xFF4ADE80), // Green dot
                            border = BorderStroke(2.dp, backgroundColor)
                        ) {}
                    }
                }
            }

            if (activeMarquee != null) {
                val parsedTextColor = try { Color(android.graphics.Color.parseColor(activeMarquee.text_color ?: "#6366F1")) } catch (_: Exception) { if (isDark) Color.White else Color(0xFF1A1A2E) }
                val parsedBgColor = try { Color(android.graphics.Color.parseColor(activeMarquee.bg_color ?: "#6366F1")).copy(alpha = 0.15f) } catch (_: Exception) { if (isDark) Color(0xFF1E1E2E).copy(alpha = 0.5f) else Color(0xFFEEF2FF).copy(alpha = 0.5f) }
                val parsedGradientEnd = activeMarquee.bg_gradient_end?.let { hex -> try { Color(android.graphics.Color.parseColor(hex)).copy(alpha = 0.15f) } catch (_: Exception) { null } }
                val mFontWeight = when (activeMarquee.font_style) {
                    "Regular" -> androidx.compose.ui.text.font.FontWeight.Normal
                    "Italic" -> androidx.compose.ui.text.font.FontWeight.Normal
                    "Semi-Bold" -> androidx.compose.ui.text.font.FontWeight.SemiBold
                    else -> androidx.compose.ui.text.font.FontWeight.Bold
                }
                val mFontStyle = if (activeMarquee.font_style == "Italic") androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                val mFontSize = when (activeMarquee.font_size) { "Small" -> 10f; "Large" -> 16f; else -> 12f }
                val mVelocity = when (activeMarquee.scroll_speed) { "Slow" -> 50; "Fast" -> 180; else -> 100 }
                val mTextAlign = if (activeMarquee.text_alignment == "Center") androidx.compose.ui.text.style.TextAlign.Center else androidx.compose.ui.text.style.TextAlign.Left

                MarqueeComponent(
                    message = activeMarquee.message ?: "",
                    backgroundColor = parsedBgColor,
                    textColor = parsedTextColor,
                    velocity = mVelocity,
                    fontWeight = mFontWeight,
                    fontStyle = mFontStyle,
                    fontSize = mFontSize,
                    gradientEndColor = parsedGradientEnd,
                    textAlignment = mTextAlign
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                // Auto Scrolling Banner
                if (banners.isNotEmpty()) {
                    item {
                        com.campuswave.app.ui.components.AutoScrollingBanner(
                            banners = banners,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 20.dp, top = 12.dp)
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
                
                // Live Podcast Banner
                if (livePodcast != null) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFF4444).copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, Color(0xFFFF4444).copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Pulsing dot
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(Color(0xFFFF4444), CircleShape)
                                    )
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                "LIVE NOW",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFF4444)
                                            )
                                        }
                                        Text(
                                            livePodcast.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = primaryText
                                        )
                                    }
                                }
                                
                                Surface(
                                    onClick = { onJoinPodcastClick(livePodcast.id) },
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFFFF4444)
                                ) {
                                    Text(
                                        "Join",
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Premium Interactive Tabs
                item {
                    val density = LocalDensity.current
                    var tabRowWidth by remember { mutableStateOf(0) }
                    val tabWidth = if (tabRowWidth > 0) with(density) { (tabRowWidth / tabs.size).toDp() } else 0.dp
                    val indicatorOffset by animateDpAsState(
                        targetValue = tabWidth * selectedTabIndex,
                        animationSpec = spring(
                            dampingRatio = 0.7f,
                            stiffness = 300f
                        ),
                        label = "tab_indicator"
                    )

                    // Tab icons
                    val tabIcons = listOf(
                        Icons.Default.CellTower,
                        Icons.Default.EventNote,
                        Icons.Default.History
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(52.dp),
                        shape = RoundedCornerShape(28.dp),
                        color = if (isDark) Color(0xFF151520) else Color(0xFFEEF2FF),
                        border = BorderStroke(
                            1.dp,
                            if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                                .onGloballyPositioned { tabRowWidth = it.size.width }
                        ) {
                            // Sliding pill indicator with glow
                            if (tabWidth > 0.dp) {
                                // Outer glow layer
                                Box(
                                    modifier = Modifier
                                        .offset(x = indicatorOffset)
                                        .width(tabWidth)
                                        .fillMaxHeight()
                                        .shadow(
                                            elevation = 12.dp,
                                            shape = RoundedCornerShape(24.dp),
                                            ambientColor = accentPurple.copy(alpha = 0.5f),
                                            spotColor = accentPurple.copy(alpha = 0.6f)
                                        )
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFF4338CA),
                                                    accentPurple,
                                                    Color(0xFF8B5CF6)
                                                )
                                            )
                                        )
                                        .border(
                                            width = 1.dp,
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = 0.25f),
                                                    Color.White.copy(alpha = 0.05f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                )
                            }

                            // Tab labels with icons
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                tabs.forEachIndexed { index, title ->
                                    val isSelected = selectedTabIndex == index
                                    val interactionSource = remember { MutableInteractionSource() }
                                    val isPressed by interactionSource.collectIsPressedAsState()

                                    val tabScale by animateFloatAsState(
                                        targetValue = when {
                                            isPressed -> 0.92f
                                            isSelected -> 1.02f
                                            else -> 1f
                                        },
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        ),
                                        label = "tab_scale_$index"
                                    )
                                    val textColor by animateColorAsState(
                                        targetValue = if (isSelected) Color.White else secondaryText,
                                        animationSpec = tween(300, easing = EaseInOut),
                                        label = "tab_color_$index"
                                    )
                                    val iconAlpha by animateFloatAsState(
                                        targetValue = if (isSelected) 1f else 0.4f,
                                        animationSpec = tween(300),
                                        label = "icon_alpha_$index"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .graphicsLayer {
                                                scaleX = tabScale
                                                scaleY = tabScale
                                            }
                                            .clip(RoundedCornerShape(24.dp))
                                            .clickable(
                                                interactionSource = interactionSource,
                                                indication = null
                                            ) { selectedTabIndex = index },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = tabIcons[index],
                                                contentDescription = null,
                                                tint = textColor.copy(alpha = iconAlpha),
                                                modifier = Modifier.size(if (isSelected) 16.dp else 14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = title,
                                                fontSize = if (isSelected) 13.sp else 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = textColor,
                                                letterSpacing = if (isSelected) 0.3.sp else 0.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Category Chips
                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(categories.size) { index ->
                            val category = categories[index]
                            val isSelected = selectedCategory == category
                            Surface(
                                modifier = Modifier.clickable { selectedCategory = category },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) accentPurple else Color.Transparent,
                                border = BorderStroke(1.5.dp, if (isSelected) accentPurple else secondaryText.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = category,
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                    color = if (isSelected) Color.White else secondaryText,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
                if (isLoadingLive || isLoadingUpcoming || isLoadingPast) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(bottom = 100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = accentPurple)
                        }
                    }
                } else if (filteredRadios.isEmpty()) {
                    item {
                        // Empty State
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 60.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Animated Signal Icon
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                accentPurple.copy(alpha = 0.1f),
                                                Color.Transparent
                                            )
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    accentPurple.copy(alpha = 0.15f),
                                                    Color.Transparent
                                                )
                                            ),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.SignalCellularAlt,
                                        contentDescription = null,
                                        tint = accentPurple.copy(alpha = 0.6f),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                text = "No signals detected",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = when(selectedTabIndex) {
                                    0 -> "Looks like there are no live broadcasts\nin this category right now."
                                    1 -> "No upcoming radio sessions scheduled\nfor this category."
                                    else -> "No past recordings found\nfor this category."
                                },
                                fontSize = 14.sp,
                                color = secondaryText,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // View Past Recordings Link
                            Row(
                                modifier = Modifier.clickable { selectedTabIndex = 2 },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    tint = accentPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "View Past Recordings",
                                    fontSize = 14.sp,
                                    color = accentPurple,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
                
                items(filteredRadios.size) { index ->
                    val radio = filteredRadios[index]
                    if (selectedTabIndex == 0) {
                        com.campuswave.app.ui.components.PremiumRadioCard(
                            radio = radio,
                            onClick = { onRadioClick(radio.id) },
                            onJoinClick = { onRadioClick(radio.id) },
                            synchronizedTimeMillis = synchronizedTimeMillis
                        )
                    } else {
                        com.campuswave.app.ui.components.PremiumUpcomingCard(
                            radio = radio,
                            isReminderSet = radio.is_subscribed,
                            showReminder = selectedTabIndex == 1,
                            onReminderToggle = { onReminderToggle(radio.id) },
                            onClick = { onRadioClick(radio.id) },
                            synchronizedTimeMillis = synchronizedTimeMillis
                        )
                    }
                }
            }
        }
    }
}
