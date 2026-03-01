package com.campuswave.app.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import coil.compose.AsyncImage
import com.campuswave.app.data.models.Banner
import com.campuswave.app.utils.FileUtil
import com.campuswave.app.utils.UrlUtils
import com.campuswave.app.data.network.ApiConfig
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBannerScreen(
    banners: List<Banner>,
    onBackClick: () -> Unit,
    onUploadClick: (File) -> Unit,
    onDeleteClick: (Int) -> Unit
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var bannerToDelete by remember { mutableStateOf<Int?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog && bannerToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Banner?") },
            text = { Text("Are you sure you want to delete this banner? The image file will be permanently removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        bannerToDelete?.let { onDeleteClick(it) }
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
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Convert Uri to File and upload immediately
            val file = FileUtil.getFileFromUri(context, it)
            if (file != null) {
                onUploadClick(file)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Banner Management") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent, // Integrate with theme later
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { launcher.launch("image/*") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Banner", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                "Active Banners",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            if (banners.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No banners uploaded yet.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(banners) { banner ->
                        BannerItem(
                            banner = banner,
                            onDelete = { 
                                bannerToDelete = banner.id
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
fun BannerItem(
    banner: Banner,
    onDelete: () -> Unit
) {
    var showPreview by remember { mutableStateOf(false) }
    if (showPreview) {
        val baseUrlForPreview = ApiConfig.BASE_URL.removeSuffix("/api/")
        val previewUrl = UrlUtils.joinUrl(baseUrlForPreview, banner.image_url) ?: ""
        com.campuswave.app.ui.components.ZoomableImageDialog(
            imageUrl = previewUrl,
            onDismiss = { showPreview = false }
        )
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth().height(120.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
             val baseUrl = ApiConfig.BASE_URL.removeSuffix("/api/")
             val imageUrl = UrlUtils.joinUrl(baseUrl, banner.image_url)
            
            AsyncImage(
                model = imageUrl,
                contentDescription = "Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showPreview = true }
            )
            
            // Delete Button
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
            }
        }
    }
}
