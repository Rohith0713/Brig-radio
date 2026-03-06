package com.campuswave.app.ui.screens.admin

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.data.models.Radio
import com.campuswave.app.data.models.DashboardStats
import com.campuswave.app.data.models.RadioSuggestion
import com.campuswave.app.data.models.User
import com.campuswave.app.utils.DateUtils
import com.campuswave.app.ui.theme.*
import com.campuswave.app.ui.components.SimpleRadioCard
import coil.compose.SubcomposeAsyncImage
import com.campuswave.app.data.network.ApiConfig
import com.campuswave.app.utils.UrlUtils
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.campuswave.app.data.models.Marquee
import com.campuswave.app.ui.components.MarqueeComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    stats: DashboardStats? = null,
    liveRadios: List<Radio> = emptyList(),
    upcomingRadios: List<Radio> = emptyList(),
    isLoadingStats: Boolean = false,
    onCreateRadioClick: () -> Unit = {},
    onRadioSuggestionsClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onUserDetailsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onRadioClick: (Int) -> Unit = {},
    onLikeClick: (Int) -> Unit = {},
    onHostRadioClick: (Int) -> Unit = {},
    onPastRadioClick: () -> Unit = {},
    onAdminRequestsClick: () -> Unit = {},
    onInviteAdminClick: () -> Unit = {},
    onCollegeUpdatesClick: () -> Unit = {},
    onAdminPostsClick: () -> Unit = {},
    onManageMarqueeClick: () -> Unit = {},
    onManageBannersClick: () -> Unit = {},
    onLivePodcastClick: () -> Unit = {},
    onUploadPlacementClick: () -> Unit = {},
    onIssueReportsClick: () -> Unit = {},
    onRefresh: () -> Unit = {},
    activeMarquee: Marquee? = null,
    currentUser: User? = null,
    synchronizedTimeMillis: Long = System.currentTimeMillis()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Home", "Live Radio", "Upcoming Radio")
    val scope = rememberCoroutineScope()
    
    val isDark = LocalIsDarkTheme.current
    val surfaceColor = if (isDark) Color(0xFF1E1E1E) else Color.White
    val onSurfaceColor = if (isDark) Color.White else Color(0xFF1E293B)
    val secondaryTextColor = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF64748B)

    Scaffold(
        containerColor = campusBackground(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 4.dp)
            ) {
                // Station Control Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Branding Logo
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = com.campuswave.app.R.drawable.brig_logo),
                            contentDescription = "BRIG Logo",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(2.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = "Station Control",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = onSurfaceColor,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "ADMIN ACCESS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                    
                    // Profile Icon for details
                    Surface(
                        modifier = Modifier.size(40.dp).clickable { onUserDetailsClick() },
                        shape = CircleShape,
                        color = if (isDark) Color(0xFF2D3748) else Color(0xFFE2E8F0),
                        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.3f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (currentUser?.profile_picture != null) {
                                SubcomposeAsyncImage(
                                    model = UrlUtils.joinUrl(ApiConfig.BASE_URL.removeSuffix("/api/"), currentUser.profile_picture),
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    loading = { CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp)) },
                                    error = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = onSurfaceColor) }
                                )
                            } else {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
                
                // Modern Tab Switcher
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF1F5F9)
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clickable { 
                                        selectedTabIndex = index
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedTabIndex == index) {
                                    if (isDark) Color.White.copy(alpha = 0.1f) else Color.White
                                } else Color.Transparent,
                                shadowElevation = if (selectedTabIndex == index && !isDark) 2.dp else 0.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val icon = when (index) {
                                        1 -> Icons.Default.Podcasts
                                        2 -> Icons.Default.CalendarMonth
                                        else -> Icons.Default.Home
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (selectedTabIndex == index) {
                                            if (isDark) Color.White else Color(0xFF1E293B)
                                        } else secondaryTextColor
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedTabIndex == index) {
                                            if (isDark) Color.White else Color(0xFF1E293B)
                                        } else secondaryTextColor,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTabIndex) {
                0 -> AdminHomeTab(
                    stats = stats,
                    liveRadios = liveRadios,
                    upcomingRadios = upcomingRadios.filter { it.status != "DRAFT" },
                    onCreateRadioClick = onCreateRadioClick,
                    onRadioSuggestionsClick = onRadioSuggestionsClick,
                    onAnalyticsClick = onAnalyticsClick,
                    onPastRadioClick = onPastRadioClick,
                    onAdminRequestsClick = onAdminRequestsClick,
                    onInviteAdminClick = onInviteAdminClick,
                    onCollegeUpdatesClick = onCollegeUpdatesClick,
                    onAdminPostsClick = onAdminPostsClick,
                    onManageMarqueeClick = onManageMarqueeClick,
                    onManageBannersClick = onManageBannersClick,
                    onLivePodcastClick = onLivePodcastClick,
                    onUploadPlacementClick = onUploadPlacementClick,
                    onIssueReportsClick = onIssueReportsClick,
                    onRadioClick = onRadioClick,
                    onHostRadioClick = onHostRadioClick,
                    onTabChange = { selectedTabIndex = it },
                    isMainAdmin = stats?.role == "MAIN_ADMIN",
                    currentUser = currentUser,
                    synchronizedTimeMillis = synchronizedTimeMillis
                )
                1 -> AdminRadiosTab(
                    radios = liveRadios, 
                    isLive = true, 
                    onRadioClick = onRadioClick,
                    onLikeClick = onLikeClick,
                    onHostRadioClick = onHostRadioClick
                )
                2 -> AdminRadiosTab(
                    radios = upcomingRadios.filter { it.status != "DRAFT" }, 
                    isLive = false, 
                    onRadioClick = onRadioClick,
                    onLikeClick = onLikeClick,
                    onHostRadioClick = onHostRadioClick
                )
            }
        }
    }
}

@Composable
fun AdminHomeTab(
    stats: DashboardStats? = null,
    liveRadios: List<Radio> = emptyList(),
    upcomingRadios: List<Radio> = emptyList(),
    onCreateRadioClick: () -> Unit,
    onRadioSuggestionsClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onPastRadioClick: () -> Unit,
    onAdminRequestsClick: () -> Unit,
    onInviteAdminClick: () -> Unit,
    onCollegeUpdatesClick: () -> Unit,
    onAdminPostsClick: () -> Unit,
    onManageMarqueeClick: () -> Unit,
    onManageBannersClick: () -> Unit,
    onLivePodcastClick: () -> Unit = {},
    onUploadPlacementClick: () -> Unit = {},
    onIssueReportsClick: () -> Unit = {},
    onRadioClick: (Int) -> Unit = {},
    onHostRadioClick: (Int) -> Unit = {},
    onTabChange: (Int) -> Unit = {},
    isMainAdmin: Boolean = false,
    currentUser: User? = null,
    synchronizedTimeMillis: Long = System.currentTimeMillis()
) {
    val greeting = remember { getTimeBasedGreeting() }
    val greetingTagline = remember { getGreetingTagline() }
    val displayFirstName = currentUser?.name?.substringBefore(" ") ?: "Admin"
    
    val isDark = LocalIsDarkTheme.current
    val onSurfaceColor = if (isDark) Color.White else Color(0xFF1E293B)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 14.dp, bottom = 4.dp)) {
                Text(
                    text = "$greeting, $displayFirstName",
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp,
                        brush = Brush.horizontalGradient(
                            colors = if (isDark) listOf(
                                Color(0xFFE0C3FC),
                                Color(0xFF8EC5FC)
                            ) else listOf(
                                Color(0xFF667EEA),
                                Color(0xFF764BA2)
                            )
                        )
                    )
                )
                Text(
                    text = greetingTagline,
                    color = if (isDark) Color(0xFF8B949E) else Color(0xFF6B7280),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.2.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Dashboard Overview",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = onSurfaceColor,
                    letterSpacing = (-0.5).sp
                )
            }
        }
        
        // Stats Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    icon = Icons.Default.Radio,
                    label = "Total Radio",
                    value = stats?.total_radios?.toString() ?: "0",
                    iconColor = PrimaryBlue
                )
                StatCard(
                    icon = Icons.Default.Groups,
                    label = "Active Participants",
                    value = stats?.active_participants?.toString() ?: "0",
                    iconColor = Color(0xFF8B5CF6)
                )
                StatCard(
                    icon = Icons.Default.TipsAndUpdates,
                    label = "Pending Suggestions",
                    value = stats?.pending_suggestions?.toString() ?: "0",
                    iconColor = Color(0xFFF97316)
                )
            }
        }
        
        // Management Actions Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(
                    text = "Management Actions",
                    color = onSurfaceColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                
                // Actions Grid — uniform 4-column layout
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Row 1: 4 items
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionButton(
                            icon = Icons.Default.Add,
                            label = "Create Radio",
                            backgroundColor = PrimaryBlue,
                            onClick = onCreateRadioClick,
                            modifier = Modifier.weight(1f)
                        )
                        ActionButton(
                            icon = Icons.Default.Lightbulb,
                            label = "Radio Suggestions",
                            backgroundColor = AccentOrange,
                            onClick = onRadioSuggestionsClick,
                            modifier = Modifier.weight(1f),
                            badgeCount = stats?.pending_suggestions ?: 0
                        )
                        ActionButton(
                            icon = Icons.Default.BarChart,
                            label = "Analytics",
                            backgroundColor = SuccessGreen,
                            onClick = onAnalyticsClick,
                            modifier = Modifier.weight(1f)
                        )
                        ActionButton(
                            icon = Icons.Default.Article,
                            label = "Posts",
                            backgroundColor = Color(0xFF9C27B0),
                            onClick = onAdminPostsClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Row 2: 4 items
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionButton(
                            icon = Icons.Default.Campaign,
                            label = "Manage Marquee",
                            backgroundColor = Color(0xFF009688),
                            onClick = onManageMarqueeClick,
                            modifier = Modifier.weight(1f)
                        )
                        ActionButton(
                            icon = Icons.Default.Work,
                            label = "Upload Placement",
                            backgroundColor = Color(0xFF607D8B),
                            onClick = onUploadPlacementClick,
                            modifier = Modifier.weight(1f)
                        )
                        ActionButton(
                            icon = Icons.Default.ViewCarousel,
                            label = "Banners",
                            backgroundColor = Color(0xFF795548),
                            onClick = onManageBannersClick,
                            modifier = Modifier.weight(1f)
                        )
                        ActionButton(
                            icon = Icons.Default.History,
                            label = "Past Radio",
                            backgroundColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White,
                            iconColor = CampusGrey,
                            labelColor = if (isDark) Color.White else Color(0xFF1E293B),
                            onClick = onPastRadioClick,
                            modifier = Modifier.weight(1f),
                            hasShadow = !isDark
                        )
                    }

                    // Row 3: 1 item + spacers for alignment
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionButton(
                            icon = Icons.Default.Report,
                            label = "Issue Reports",
                            backgroundColor = Color(0xFFDC2626),
                            onClick = onIssueReportsClick,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    
                    if (isMainAdmin) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ActionButton(
                                icon = Icons.Default.GroupAdd,
                                label = "Admin Requests",
                                backgroundColor = PrimaryPurple,
                                onClick = onAdminRequestsClick,
                                modifier = Modifier.weight(1f),
                                badgeCount = stats?.pending_admin_requests ?: 0
                            )
                            ActionButton(
                                icon = Icons.Default.PersonAdd,
                                label = "Invite Admin",
                                backgroundColor = AccentPink,
                                onClick = onInviteAdminClick,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        
        // Live Session Waveform Highlight
        item {
            val liveSession = liveRadios.firstOrNull()
            if (liveSession != null) {
                LiveSessionHighlight(
                    radio = liveSession,
                    onClick = { onTabChange(1) }
                )
            }
        }
        
        // Upcoming List Preview
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Upcoming", color = onSurfaceColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = "SEE ALL", 
                        color = accentPurple, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { onPastRadioClick() }
                    )
                }
                
                upcomingRadios.take(3).forEach { radio ->
                    UpcomingPreviewCard(
                        radio = radio, 
                        synchronizedTimeMillis = synchronizedTimeMillis,
                        onClick = { onTabChange(2) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    val isDark = LocalIsDarkTheme.current
    val onSurfaceColor = if (isDark) Color.White else Color(0xFF1E293B)
    val cardBackground = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White
    val cardBorder = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF1F5F9)
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = cardBackground,
        border = BorderStroke(1.dp, cardBorder),
        shadowElevation = if (isDark) 0.dp else 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(54.dp),
                shape = RoundedCornerShape(14.dp),
                color = iconColor.copy(alpha = 0.08f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon, 
                        contentDescription = null, 
                        tint = iconColor, 
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Column {
                Text(
                    text = label.uppercase(), 
                    color = CampusGrey, 
                    fontSize = 11.sp, 
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = value, 
                    color = onSurfaceColor, 
                    fontSize = 26.sp, 
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
    iconColor: Color = Color.White,
    labelColor: Color = Color.White,
    hasShadow: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(100), label = "scale"
    )

    val isDark = LocalIsDarkTheme.current

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            shape = RoundedCornerShape(20.dp),
            color = backgroundColor,
            shadowElevation = if (hasShadow && !isDark) 6.dp else 0.dp,
            border = if (backgroundColor == Color.White || backgroundColor == Color.White.copy(alpha = 0.05f)) 
                    BorderStroke(1.dp, Color(0xFFF1F5F9)) else null
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = iconColor, 
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = label,
                    color = labelColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 14.sp
                )
            }
        }

        if (badgeCount > 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(20.dp),
                shape = CircleShape,
                color = Color(0xFFFF5252),
                border = BorderStroke(2.dp, if (isDark) Color(0xFF121212) else Color.White)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun LiveSessionHighlight(
    radio: Radio,
    onClick: () -> Unit = {}
) {
    val statusInfo = remember(radio.start_time, radio.end_time) {
        DateUtils.getRadioStatusInfo(radio.start_time, radio.end_time)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = (if (statusInfo.isLive) Color(0xFFE53935) else Color.Gray).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, if (statusInfo.isLive) Color(0xFFE53935) else Color.Gray)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(if (statusInfo.isLive) Color(0xFFE53935) else Color.Gray, CircleShape))
                        Text(statusInfo.statusLabel, color = if (statusInfo.isLive) Color(0xFFE53935) else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Mock Waveform
            Row(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val heights = listOf(0.4f, 0.6f, 0.3f, 0.8f, 0.5f, 0.9f, 0.4f, 0.7f, 0.3f, 0.6f, 0.8f, 0.4f, 0.5f, 0.7f, 0.3f, 0.9f, 0.5f)
                heights.forEach { h ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(h * (if (statusInfo.isLive) 1f else 0.2f)) // Static waveform if not live
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = if (statusInfo.isLive) listOf(Color(0xFF8B5CF6), Color(0xFF6366F1)) else listOf(Color.Gray, Color.DarkGray)
                                ),
                                shape = RoundedCornerShape(50)
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = radio.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(text = "with Station Admin", color = CampusGrey, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = if (statusInfo.isLive) "Remaining" else "Schedule", color = CampusGrey, fontSize = 10.sp)
                    Text(
                        text = statusInfo.timeInfo ?: "${statusInfo.startTimeFormatted} - ${statusInfo.endTimeFormatted}", 
                        color = Color.White, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { /* Settings */ },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Settings", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { /* End or Start */ },
                    modifier = Modifier.weight(1.3f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (statusInfo.isLive) Color(0xFFE53935) else PrimaryBlue)
                ) {
                    Icon(if (statusInfo.isLive) Icons.Default.StopCircle else Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (statusInfo.isLive) "End Broadcast" else "Go Live", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun UpcomingPreviewCard(
    radio: Radio, 
    synchronizedTimeMillis: Long = System.currentTimeMillis(),
    onClick: () -> Unit = {}
) {
    val statusInfo = remember(radio.start_time, radio.end_time, synchronizedTimeMillis) {
        DateUtils.getRadioStatusInfo(radio.start_time, radio.end_time, synchronizedTimeMillis)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.03f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(10.dp),
                color = CampusSurface
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = CampusGrey)
                }
            }
            
            Column {
                Text(text = radio.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = "${radio.category?.name ?: "General"} • ${statusInfo.timeInfo ?: statusInfo.startTimeFormatted}",
                    color = if (statusInfo.isLive) AccentPink else CampusGrey,
                    fontSize = 12.sp,
                    fontWeight = if (statusInfo.isLive) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun AdminRadiosTab(
    radios: List<Radio>,
    isLive: Boolean,
    onRadioClick: (Int) -> Unit,
    onLikeClick: (Int) -> Unit = {},
    onHostRadioClick: (Int) -> Unit = {},
    onDeleteClick: (Int) -> Unit = {},
    synchronizedTimeMillis: Long = System.currentTimeMillis()
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (radios.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text(text = if (isLive) "No active frequencies" else "No upcoming sessions", color = CampusGrey)
                }
            }
        }
        items(radios.size) { index ->
            AdminRadioCard(
                radio = radios[index],
                isLive = isLive,
                onClick = { onHostRadioClick(radios[index].id) },
                onHostClick = { onHostRadioClick(radios[index].id) },
                onDeleteClick = { onDeleteClick(radios[index].id) },
                synchronizedTimeMillis = synchronizedTimeMillis
            )
        }
    }
}

@Composable
fun AdminRadioCard(
    radio: Radio,
    isLive: Boolean,
    onClick: () -> Unit,
    onHostClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    synchronizedTimeMillis: Long = System.currentTimeMillis()
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = if (isLive) BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.3f)) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(modifier = Modifier.size(60.dp), shape = RoundedCornerShape(12.dp), color = CampusSurface) {
                val imageUrl = UrlUtils.joinUrl(ApiConfig.BASE_URL.removeSuffix("/api/"), radio.banner_image)
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = radio.title,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    loading = { CircularProgressIndicator(color = PrimaryBlue, modifier = Modifier.padding(16.dp), strokeWidth = 2.dp) },
                    error = { 
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Podcasts, contentDescription = null, tint = if (isLive) PrimaryBlue else CampusGrey)
                        }
                    }
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = radio.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = radio.category?.name ?: "General", color = PrimaryBlue, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                
                val statusInfo = remember(radio.start_time, radio.end_time, synchronizedTimeMillis) {
                    DateUtils.getRadioStatusInfo(radio.start_time, radio.end_time, synchronizedTimeMillis)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (statusInfo.isLive) Icons.Default.Podcasts else Icons.Default.Schedule, 
                        contentDescription = null, 
                        modifier = Modifier.size(12.dp), 
                        tint = if (statusInfo.isLive) AccentPink else CampusGrey
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (statusInfo.isLive) "LIVE" else if (statusInfo.isStartingSoon) statusInfo.timeInfo ?: "" else DateUtils.formatRadioDate(radio.start_time), 
                        color = if (statusInfo.isLive) AccentPink else if (statusInfo.isStartingSoon) PrimaryPurple else CampusGrey, 
                        fontSize = 11.sp,
                        fontWeight = if (statusInfo.isLive || statusInfo.isStartingSoon) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444), // Red color
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onHostClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = null,
                        tint = CampusGrey,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun getTimeBasedGreeting(): String {
    val calendar = java.util.Calendar.getInstance()
    return when (calendar.get(java.util.Calendar.HOUR_OF_DAY)) {
        in 0..4 -> "Burning the midnight oil"
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..20 -> "Good Evening"
        else -> "Good Night"
    }
}

private fun getGreetingTagline(): String {
    val calendar = java.util.Calendar.getInstance()
    return when (calendar.get(java.util.Calendar.HOUR_OF_DAY)) {
        in 0..4 -> "Dedication at its finest."
        in 5..11 -> "Here's what's happening on your station today."
        in 12..16 -> "Your station is running smoothly."
        in 17..20 -> "Your station awaits — let's wrap up strong."
        else -> "Winding down — great work today."
    }
}

