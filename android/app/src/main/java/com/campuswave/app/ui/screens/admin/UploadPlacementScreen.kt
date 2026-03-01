package com.campuswave.app.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import coil.compose.AsyncImage
import com.campuswave.app.data.models.Placement
import com.campuswave.app.data.models.PlacementPoster
import com.campuswave.app.data.models.PlacementRequest
import com.campuswave.app.data.network.ApiConfig
import com.campuswave.app.ui.theme.LocalIsDarkTheme
import com.campuswave.app.ui.theme.PrimaryBlue
import com.campuswave.app.utils.UrlUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadPlacementScreen(
    placements: List<Placement> = emptyList(),
    posters: List<PlacementPoster> = emptyList(),
    onBackClick: () -> Unit,
    onPlacementPosted: (PlacementRequest) -> Unit,
    onDeletePlacement: (Int) -> Unit = {},
    onPosterUpload: (title: String, company: String?, description: String?, MultipartBody.Part) -> Unit = { _, _, _, _ -> },
    onDeletePoster: (Int) -> Unit = {},
    onTogglePosterVisibility: (Int, Boolean) -> Unit = { _, _ -> },
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Jobs, 1: Posters
    var isAddingNew by remember { mutableStateOf(false) }
    
    // Deletion states
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDeleteId by remember { mutableIntStateOf(-1) }
    var deleteType by remember { mutableStateOf("JOB") } // "JOB" or "POSTER"

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(if (deleteType == "JOB") "Delete Placement?" else "Delete Poster?") },
            text = { Text("Are you sure you want to delete this? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        if (deleteType == "JOB") {
                            onDeletePlacement(itemToDeleteId)
                        } else {
                            onDeletePoster(itemToDeleteId)
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

    val isDark = LocalIsDarkTheme.current
    val onSurfaceColor = if (isDark) Color.White else Color(0xFF1E293B)
    
    Scaffold(
        containerColor = if (isDark) Color.Black else Color(0xFFF8FAFC),
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        Text(
                            if (isAddingNew) {
                                if (selectedTab == 0) "New Placement" else "New Achievement Poster"
                            } else "Manage Placements", 
                            fontWeight = FontWeight.ExtraBold,
                            color = onSurfaceColor
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (isAddingNew) isAddingNew = false else onBackClick()
                        }) {
                            Icon(
                                if (isAddingNew) Icons.Default.Close else Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = onSurfaceColor
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (isDark) Color.Black else Color.White
                    )
                )
                
                if (!isAddingNew) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = if (isDark) Color.Black else Color.White,
                        contentColor = PrimaryBlue,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = PrimaryBlue
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Jobs & Internships") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Posters & Achievements") }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (!isAddingNew) {
                FloatingActionButton(
                    onClick = { isAddingNew = true },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(28.dp))
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (isAddingNew) {
                if (selectedTab == 0) {
                    NewPlacementForm(
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onPost = onPlacementPosted
                    )
                } else {
                    NewPosterForm(
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onUpload = onPosterUpload
                    )
                }
            } else {
                if (selectedTab == 0) {
                    ManagementList(
                        items = placements,
                        emptyIcon = Icons.Default.WorkOutline,
                        emptyText = "No placements posted yet",
                        onDeleteItem = { 
                            itemToDeleteId = it.id
                            deleteType = "JOB"
                            showDeleteDialog = true
                        }
                    ) { placement ->
                        AdminPlacementCard(
                            placement = placement, 
                            onDelete = { 
                                itemToDeleteId = placement.id
                                deleteType = "JOB"
                                showDeleteDialog = true
                            }
                        )
                    }
                } else {
                    ManagementList(
                        items = posters,
                        emptyIcon = Icons.Default.Image,
                        emptyText = "No achievement posters yet",
                        onDeleteItem = { 
                            itemToDeleteId = it.id
                            deleteType = "POSTER"
                            showDeleteDialog = true
                        }
                    ) { poster ->
                        AdminPosterCard(
                            poster = poster,
                            onDelete = { 
                                itemToDeleteId = poster.id
                                deleteType = "POSTER"
                                showDeleteDialog = true
                            },
                            onToggleVisibility = { onTogglePosterVisibility(poster.id, it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NewPlacementForm(
    isLoading: Boolean,
    errorMessage: String?,
    onPost: (PlacementRequest) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var applicationLink by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (errorMessage != null) {
            ErrorCard(errorMessage)
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Job Title") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = company,
            onValueChange = { company = it },
            label = { Text("Company Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = salary,
            onValueChange = { salary = it },
            label = { Text("Salary (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = deadline,
            onValueChange = { deadline = it },
            label = { Text("Deadline") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = applicationLink,
            onValueChange = { applicationLink = it },
            label = { Text("Application Link") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = {
                onPost(PlacementRequest(title, company, location, salary, deadline, applicationLink, description))
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading && title.isNotBlank() && company.isNotBlank()
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            else Text("Post Opportunity")
        }
    }
}

@Composable
fun NewPosterForm(
    isLoading: Boolean,
    errorMessage: String?,
    onUpload: (title: String, company: String?, description: String?, MultipartBody.Part) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (errorMessage != null) {
            ErrorCard(errorMessage)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(48.dp))
                    Text("Select Poster Image")
                }
            }
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Poster Title / Achievement") },
            placeholder = { Text("e.g. 50+ Students Selected in Google") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = company,
            onValueChange = { company = it },
            label = { Text("Company (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description (Optional)") },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = {
                imageUri?.let { uri ->
                    val file = com.campuswave.app.utils.FileUtil.getFileFromUri(context, uri) ?: return@let
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("poster", file.name, requestFile)
                    onUpload(title, company.ifEmpty { null }, description.ifEmpty { null }, part)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading && title.isNotBlank() && imageUri != null
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            else Text("Upload Poster")
        }
    }
}

@Composable
fun <T> ManagementList(
    items: List<T>,
    emptyIcon: androidx.compose.ui.graphics.vector.ImageVector,
    emptyText: String,
    onDeleteItem: (T) -> Unit,
    cardContent: @Composable (T) -> Unit
) {
    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(emptyIcon, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                Text(emptyText, color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) { item -> cardContent(item) }
        }
    }
}

@Composable
fun AdminPosterCard(
    poster: PlacementPoster,
    onDelete: () -> Unit,
    onToggleVisibility: (Boolean) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val surfaceColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(150.dp)) {
                AsyncImage(
                    model = UrlUtils.joinUrl(ApiConfig.BASE_URL.removeSuffix("/api/"), poster.posterImage),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, null, tint = Color.White)
                    }
                }
            }
            
            Column(Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(poster.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (!poster.company.isNullOrEmpty()) {
                            Text(poster.company, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    
                    Switch(
                        checked = poster.isVisible,
                        onCheckedChange = onToggleVisibility,
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryBlue)
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(message, color = Color.Red, modifier = Modifier.padding(12.dp), fontSize = 12.sp)
    }
}

@Composable
fun AdminPlacementCard(
    placement: Placement,
    onDelete: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val surfaceColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White
    val onSurfaceColor = if (isDark) Color.White else Color(0xFF1E293B)
    val secondaryTextColor = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = surfaceColor,
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF1F5F9)),
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = placement.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor,
                    letterSpacing = (-0.3).sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Business, 
                        contentDescription = null, 
                        modifier = Modifier.size(14.dp),
                        tint = secondaryTextColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = placement.company,
                        fontSize = 14.sp,
                        color = secondaryTextColor,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        Icons.Default.LocationOn, 
                        contentDescription = null, 
                        modifier = Modifier.size(14.dp),
                        tint = secondaryTextColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = placement.location,
                        fontSize = 14.sp,
                        color = secondaryTextColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onDelete() },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFF4D4D).copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = Color(0xFFFF4D4D),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
