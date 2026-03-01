package com.campuswave.app.ui.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.data.models.AdminAnalyticsResponse
import com.campuswave.app.data.models.TrendItem
import com.campuswave.app.ui.theme.*
import com.campuswave.app.ui.viewmodels.AdminViewModel
import com.campuswave.app.utils.ApiResult
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.Offset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnalyticsScreen(
    analyticsState: ApiResult<AdminAnalyticsResponse>,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit
) {

    Scaffold(
        containerColor = CampusBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "System Analytics",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val result = analyticsState) {
                is ApiResult.Loading -> {
                    AnalyticsShimmerLoading()
                }
                is ApiResult.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = result.message,
                            color = CampusGrey,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onRefresh,
                            modifier = Modifier.padding(top = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Retry")
                        }
                    }
                }
                is ApiResult.Success -> {
                    AnalyticsContent(result.data)
                }
            }
        }
    }
}

@Composable
fun AnalyticsContent(data: AdminAnalyticsResponse) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Context Banner
        data.departmental_context?.let { dept ->
            item {
                Surface(
                    color = PrimaryPurple.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = PrimaryPurple)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Showing data for $dept Department",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryPurple
                        )
                    }
                }
            }
        }

        // Key Stats Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMiniCard(
                    modifier = Modifier.weight(1f),
                    label = "Total Users",
                    value = data.total_users.toString(),
                    icon = Icons.Default.People,
                    color = PrimaryBlue,
                    index = 0
                )
                StatMiniCard(
                    modifier = Modifier.weight(1f),
                    label = "Total Radios",
                    value = data.total_radios.toString(),
                    icon = Icons.Default.Radio,
                    color = PrimaryPurple,
                    index = 1
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMiniCard(
                    modifier = Modifier.weight(1f),
                    label = "Participants",
                    value = data.total_participants.toString(),
                    icon = Icons.Default.Headset,
                    color = SuccessGreen,
                    index = 2
                )
                StatMiniCard(
                    modifier = Modifier.weight(1f),
                    label = "Favorites",
                    value = data.total_favorites.toString(),
                    icon = Icons.Default.Favorite,
                    color = ErrorRed,
                    index = 3
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMiniCard(
                    modifier = Modifier.weight(1f),
                    label = "Total Placements",
                    value = data.total_placements.toString(),
                    icon = Icons.Default.Work,
                    color = Color(0xFF607D8B),
                    index = 4
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Registration Trend Chart (Simplified Bar Chart)
        item {
            ChartSection(
                title = "Registration Trend (Last 7 Days)",
                icon = Icons.Default.TrendingUp
            ) {
                RegistrationBarChart(data.registration_trend)
            }
        }

        // User Roles Distribution
        item {
            ChartSection(
                title = "User Role Distribution",
                icon = Icons.Default.PieChart
            ) {
                RoleDistributionList(data.users_by_role)
            }
        }

        // College Updates Engagement
        item {
            ChartSection(
                title = "College Updates Engagement",
                icon = Icons.Default.Newspaper
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        EngagementSmallStat("Views", data.total_update_views.toString(), Icons.Default.Visibility, 0)
                        EngagementSmallStat("Likes", data.total_update_likes.toString(), Icons.Default.Favorite, 1)
                        EngagementSmallStat("Comments", data.total_update_comments.toString(), Icons.Default.Comment, 2)
                    }
                    
                    if (data.top_updates.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Top Performing Updates",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CampusGrey
                        )
                        data.top_updates.forEachIndexed { index, update ->
                            TopUpdateCard(update = update, index = index)
                        }
                    }
                }
            }
        }

        // Top Performing Radios
        item {
            ChartSection(
                title = "Top Radio Sessions",
                icon = Icons.Default.Star
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    data.top_radios.forEachIndexed { index, radio ->
                        TopRadioCard(radio = radio, index = index)
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun StatMiniCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    index: Int = 0
) {
    val animatedValue by animateIntAsState(
        targetValue = value.filter { it.isDigit() }.toIntOrNull() ?: 0,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "CountUp"
    )

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(600, delayMillis = index * 100)) + 
                slideInVertically(tween(600, delayMillis = index * 100)) { it / 2 }
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CampusSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.1f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                    }
                }
                Text(text = label, fontSize = 12.sp, color = CampusGrey)
                Text(
                    text = if (value.any { it.isDigit() }) {
                        value.replace(Regex("\\d+"), animatedValue.toString())
                    } else value,
                    fontSize = 20.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = CampusDark
                )
            }
        }
    }
}

@Composable
fun ChartSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = CampusDark
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CampusSurface,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 2.dp
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun RegistrationBarChart(trend: List<TrendItem>) {
    val maxCount = trend.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        trend.forEachIndexed { index, item ->
            val animatedHeightPercent by animateFloatAsState(
                targetValue = item.count.toFloat() / maxCount,
                animationSpec = tween(durationMillis = 1000, delayMillis = index * 50, easing = FastOutSlowInEasing),
                label = "BarHeight"
            )
            
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(animatedHeightPercent.coerceAtLeast(0.05f))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(PrimaryBlue, PrimaryBlue.copy(alpha = 0.6f))
                            ),
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                        )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.date.split("-").let { "${it.last()}" },
                    fontSize = 10.sp,
                    color = CampusGrey
                )
            }
        }
    }
}

@Composable
fun RoleDistributionList(roles: Map<String, Int>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val total = roles.values.sum().coerceAtLeast(1)
        roles.entries.sortedByDescending { it.value }.forEach { entry ->
            val color = when(entry.key.uppercase()) {
                "STUDENT" -> PrimaryBlue
                "ADMIN_APPROVED" -> PrimaryPurple
                "MAIN_ADMIN" -> SuccessGreen
                else -> CampusGrey
            }
            
            val animatedProgress by animateFloatAsState(
                targetValue = entry.value.toFloat() / total,
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                label = "ProgressFill"
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = entry.key, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text(text = "${entry.value}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = color,
                    trackColor = color.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
fun TopRadioCard(radio: com.campuswave.app.data.models.TopRadioItem, index: Int = 0) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(tween(500, delayMillis = index * 100)) { -it } + fadeIn()
    ) {
        Surface(
            color = CampusBackground.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryBlue.copy(alpha = 0.1f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Radio, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = radio.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "${radio.participant_count} Listeners", fontSize = 12.sp, color = CampusGrey)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${radio.favorite_count}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun EngagementSmallStat(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, index: Int = 0) {
    val animatedValue by animateIntAsState(
        targetValue = value.filter { it.isDigit() }.toIntOrNull() ?: 0,
        animationSpec = tween(durationMillis = 1500, delayMillis = index * 100, easing = FastOutSlowInEasing),
        label = "CountUp"
    )

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(tween(500, delayMillis = index * 200)) + fadeIn()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
            Text(
                text = if (value.any { it.isDigit() }) animatedValue.toString() else value,
                fontWeight = FontWeight.ExtraBold, 
                fontSize = 16.sp, 
                color = CampusDark
            )
            Text(text = label, fontSize = 10.sp, color = CampusGrey)
        }
    }
}

@Composable
fun TopUpdateCard(update: com.campuswave.app.data.models.TopUpdateItem, index: Int = 0) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(tween(500, delayMillis = index * 100)) { it } + fadeIn()
    ) {
        Surface(
            color = CampusBackground.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SuccessGreen.copy(alpha = 0.1f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Newspaper, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = update.caption, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                    Text(text = "${update.view_count} views", fontSize = 12.sp, color = CampusGrey)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${update.like_count}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AnalyticsShimmerLoading() {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(3) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f).height(100.dp).clip(RoundedCornerShape(16.dp)).background(brush))
                Box(modifier = Modifier.weight(1f).height(100.dp).clip(RoundedCornerShape(16.dp)).background(brush))
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)).background(brush))
    }
}
