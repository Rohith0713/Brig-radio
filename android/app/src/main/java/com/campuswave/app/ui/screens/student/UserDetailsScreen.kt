package com.campuswave.app.ui.screens.student

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campuswave.app.data.models.User
import com.campuswave.app.ui.theme.*
import com.campuswave.app.ui.viewmodels.ProfileViewModel
import com.campuswave.app.utils.ApiResult
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.campuswave.app.utils.UrlUtils
import com.campuswave.app.data.network.ApiConfig
import java.text.SimpleDateFormat
import java.util.*
import com.campuswave.app.data.theme.ThemeManager
import com.campuswave.app.data.theme.ThemeMode
import kotlinx.coroutines.launch
import coil.compose.SubcomposeAsyncImage
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import com.campuswave.app.ui.components.EditProfileDialog
import com.campuswave.app.ui.components.PersonalDetailRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailsScreen(
    userName: String = "",
    userEmail: String = "",
    userId: String = "",
    userRole: String = "",
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onResetPasswordClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel = remember { ProfileViewModel(context) }
    val profileState by viewModel.userProfile.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: android.net.Uri? ->
            if (uri != null) {
                viewModel.uploadProfilePicture(uri, context.contentResolver)
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile()
    }

    Scaffold(
        containerColor = campusBackground(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(campusBackground())
            ) {
                // Glassmorphism Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.03f) else Color.White,
                    border = BorderStroke(1.dp, if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.05f) else Color(0xFFE2E8F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = if (LocalIsDarkTheme.current) Color.White else Color(0xFF1E293B)
                                )
                            }
                            
                            Text(
                                text = "My Frequency",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = if (LocalIsDarkTheme.current) Color.White else Color(0xFF1E293B),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = accentPurple.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, accentPurple.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "PROFILE",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentPurple,
                                    letterSpacing = 1.sp
                                )
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
            when (val state = profileState) {
                is ApiResult.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
                is ApiResult.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CloudOff, contentDescription = null, tint = CampusGrey, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Failed to sync profile", color = CampusGrey, fontWeight = FontWeight.Medium)
                        Button(
                            onClick = { viewModel.fetchUserProfile() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Retry Sync")
                        }
                    }
                }
                is ApiResult.Success<*> -> {
                    val user = state.data as User
                    UserProfileContent(
                        user = user,
                        onImageClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onEditProfileClick = { showEditDialog = true },
                        onLogoutClick = onLogoutClick,
                        onResetPasswordClick = onResetPasswordClick
                    )
                    
                    if (showEditDialog) {
                        EditProfileDialog(
                            currentName = user.name,
                            currentPin = user.college_pin,
                            currentDepartment = user.department,
                            currentYear = user.year,
                            currentBranch = user.branch,
                            onDismiss = { showEditDialog = false },
                            onConfirm = { name, pin, department, year, branch ->
                                viewModel.updateProfile(name, pin, department, year, branch)
                                showEditDialog = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserProfileContent(
    user: User,
    onImageClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onResetPasswordClick: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(24.dp)
    ) {
        // Avatar Section
        item {
            Box(
                modifier = Modifier
                    .size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer Glow/Border
                Surface(
                    modifier = Modifier.size(130.dp),
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(4.dp, Brush.sweepGradient(listOf(accentPurple, Color(0xFF8B5CF6), accentPurple)))
                ) {
                    Box(
                        modifier = Modifier.padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = CircleShape,
                            color = campusSurface()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (user.profile_picture != null) {
                                    coil.compose.SubcomposeAsyncImage(
                                        model = UrlUtils.joinUrl(ApiConfig.BASE_URL.removeSuffix("/api/"), user.profile_picture),
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier.fillMaxSize().clickable(onClick = onImageClick),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        loading = {
                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = accentPurple)
                                            }
                                        },
                                        error = {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = CampusGrey, modifier = Modifier.size(50.dp))
                                        }
                                    )
                                } else {
                                    Text(
                                        text = user.name.take(1).uppercase(),
                                        fontSize = 54.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (LocalIsDarkTheme.current) Color.White else Color(0xFF1E293B),
                                        modifier = Modifier.clickable(onClick = onImageClick)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Camera Badge overlay
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-8).dp, y = (-8).dp)
                        .size(36.dp)
                        .clickable(onClick = onImageClick),
                    shape = CircleShape,
                    color = accentPurple,
                    border = BorderStroke(2.dp, campusBackground())
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Picture",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Header Info
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (LocalIsDarkTheme.current) Color.White else Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        shape = CircleShape,
                        color = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f),
                        modifier = Modifier.size(34.dp).clickable(onClick = onEditProfileClick)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.6f) else Color(0xFF1E293B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accentPurple.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, accentPurple.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (user.role == "ADMIN") Icons.Default.VerifiedUser else Icons.Default.School,
                            contentDescription = null,
                            tint = accentPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = user.role,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = accentPurple,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // PERSONAL DETAILS Card
        item {
            var isVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { isVisible = true }

            androidx.compose.animation.AnimatedVisibility(
                visible = isVisible,
                enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(600)) + 
                        androidx.compose.animation.slideInVertically(initialOffsetY = { it / 2 }, animationSpec = androidx.compose.animation.core.tween(600))
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = campusSurface(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "PERSONAL DETAILS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B949E),
                        letterSpacing = 1.sp
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    PersonalDetailRow(icon = Icons.Default.Email, label = "Email Address", value = user.email)
                    HorizontalDivider(color = campusDivider().copy(alpha = 0.5f))
                    PersonalDetailRow(icon = Icons.Default.Category, label = "Department", value = user.department ?: "Not Set")
                    HorizontalDivider(color = campusDivider().copy(alpha = 0.5f))
                    PersonalDetailRow(icon = Icons.Default.CalendarToday, label = "Year", value = user.year ?: "Not Set")
                    HorizontalDivider(color = campusDivider().copy(alpha = 0.5f))
                    PersonalDetailRow(icon = Icons.Default.AccountTree, label = "Branch", value = user.branch ?: "Not Set")
                    if (user.college_pin != null) {
                        HorizontalDivider(color = campusDivider().copy(alpha = 0.5f))
                        PersonalDetailRow(icon = Icons.Default.Fingerprint, label = "College PIN", value = user.college_pin)
                    }
                }
            }
        }
    }
        
        // Appearance Settings
        item {
            Spacer(modifier = Modifier.height(8.dp))
            ThemeSettingsSection()
        }


        // Institutional & Account Settings
        item {
            Spacer(modifier = Modifier.height(8.dp))
            InstitutionalSettingsSection(onResetPasswordClick = onResetPasswordClick)
        }

        // Logout Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = ErrorRed
                ),
                border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sign Out from Frequency",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ProfileDetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = campusSurface(),
        border = BorderStroke(1.dp, campusDivider())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.05f) else accentColor.copy(alpha = 0.05f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (LocalIsDarkTheme.current) accentColor.copy(alpha = 0.8f) else accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            
            Column {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (LocalIsDarkTheme.current) DarkGrey.copy(alpha = 0.7f) else LightGrey,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    color = if (LocalIsDarkTheme.current) Color.White else Color(0xFF1E293B),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun formatProfileDate(dateString: String): String {
    return try {
        if (dateString.contains(",")) {
            dateString.substringBeforeLast(" ")
        } else {
            dateString.take(10)
        }
    } catch (e: Exception) {
        dateString
    }
}



@Composable
fun ThemeSettingsSection() {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.05f) else accentPurple.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = accentPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column {
                Text(
                    text = "Broadcast Aesthetics",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = campusOnBackground()
                )
                Text(
                    text = "Control your UI resonance",
                    fontSize = 12.sp,
                    color = if (LocalIsDarkTheme.current) DarkGrey.copy(alpha = 0.7f) else LightGrey
                )
            }
        }
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = if (LocalIsDarkTheme.current) Color(0xFF161B22) else Color(0xFFF3F4F6)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOptionButton(
                    modifier = Modifier.weight(1f),
                    title = "System",
                    isSelected = currentTheme == ThemeMode.SYSTEM,
                    onClick = { coroutineScope.launch { themeManager.saveThemeMode(ThemeMode.SYSTEM) } }
                )
                
                ThemeOptionButton(
                    modifier = Modifier.weight(1f),
                    title = "Light",
                    isSelected = currentTheme == ThemeMode.LIGHT,
                    onClick = { coroutineScope.launch { themeManager.saveThemeMode(ThemeMode.LIGHT) } }
                )
                
                ThemeOptionButton(
                    modifier = Modifier.weight(1f),
                    title = "Dark",
                    isSelected = currentTheme == ThemeMode.DARK,
                    onClick = { coroutineScope.launch { themeManager.saveThemeMode(ThemeMode.DARK) } }
                )
            }
        }
    }
}

@Composable
fun InstitutionalSettingsSection(onResetPasswordClick: () -> Unit) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.05f) else Color(0xFFF0F9FF),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = Color(0xFF0EA5E9),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column {
                Text(
                    text = "Institution & Security",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = campusOnBackground()
                )
                Text(
                    text = "Manage your college presence",
                    fontSize = 12.sp,
                    color = if (LocalIsDarkTheme.current) DarkGrey.copy(alpha = 0.7f) else LightGrey
                )
            }
        }
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = if (LocalIsDarkTheme.current) Color(0xFF161B22) else Color(0xFFF3F4F6)
        ) {
            Column(modifier = Modifier.padding(6.dp)) {
                SettingsOptionButton(
                    title = "About BGIT",
                    icon = Icons.Default.Language,
                    onClick = { uriHandler.openUri("https://www.bgiic.ac.in/") }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                SettingsOptionButton(
                    title = "Reset Password",
                    icon = Icons.Default.LockReset,
                    onClick = onResetPasswordClick
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                SettingsOptionButton(
                    title = "Developed by",
                    icon = Icons.Default.Code,
                    onClick = { uriHandler.openUri("http://brigradio.jo3.org/") }
                )
            }
        }
    }
}

@Composable
fun SettingsOptionButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (LocalIsDarkTheme.current) campusOnBackground().copy(alpha = 0.7f) else Color(0xFF1E293B),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = campusOnBackground()
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (LocalIsDarkTheme.current) campusOnBackground().copy(alpha = 0.3f) else Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ThemeOptionButton(
    modifier: Modifier = Modifier,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) accentPurple else Color.Transparent,
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else if (LocalIsDarkTheme.current) DarkGrey else LightGrey
            )
        }
    }
}


