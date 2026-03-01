package com.campuswave.app.ui.screens.admin

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.campuswave.app.data.network.RetrofitClient
import com.campuswave.app.data.auth.AuthManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCollegeUpdateScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var caption by remember { mutableStateOf("") }
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var isVideo by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            selectedMediaUri = it
            isVideo = context.contentResolver.getType(it)?.startsWith("video") == true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Update") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Media Picker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { 
                        mediaPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                        ) 
                    }
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (selectedMediaUri != null) {
                    if (isVideo) {
                        // Video Preview (Icon + Filename or Thumbnail)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.VideoLibrary, // Changed from MusicNote or similar
                                contentDescription = "Selected Video",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Video Selected", fontWeight = FontWeight.Bold)
                            Text(
                                "Tap to change",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        AsyncImage(
                            model = selectedMediaUri,
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    IconButton(
                        onClick = { 
                            selectedMediaUri = null
                            isVideo = false
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            contentDescription = "Add Media",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Add Photo or Video *",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Caption Input
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                label = { Text("Write a caption...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Allow it to expand
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                )
            )

            // Post Button
            Button(
                onClick = {
                    if (selectedMediaUri == null) {
                        Toast.makeText(context, "Please select an image or video", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (caption.isBlank()) {
                        Toast.makeText(context, "Please write a caption", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    isLoading = true
                    scope.launch {
                        try {
                            val authManager = AuthManager(context)
                            val token = authManager.getToken()
                            if (token == null) {
                                Toast.makeText(context, "Session expired", Toast.LENGTH_SHORT).show()
                                isLoading = false
                                return@launch
                            }

                            // Prepare File
                            val file = uriToFile(context, selectedMediaUri!!, if (isVideo) ".mp4" else ".jpg")
                            val mimeType = context.contentResolver.getType(selectedMediaUri!!) ?: if (isVideo) "video/mp4" else "image/jpeg"
                            val requestFile = file.readBytes().toRequestBody(mimeType.toMediaTypeOrNull())
                            
                            // Send part name 'media' for everything
                            val mediaPart = MultipartBody.Part.createFormData("media", file.name, requestFile)
                            val captionPart = caption.toRequestBody("text/plain".toMediaTypeOrNull())

                            val response = RetrofitClient.apiService.createCollegeUpdate("Bearer $token", mediaPart, captionPart)
                            
                            if (response.isSuccessful) {
                                Toast.makeText(context, "College update posted successfully.", Toast.LENGTH_LONG).show()
                                navController.navigateUp()
                            } else {
                                val errorBody = response.errorBody()?.string()
                                Toast.makeText(context, "Failed: $errorBody", Toast.LENGTH_LONG).show()
                            }
                            
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Post Update")
                }
            }
        }
    }
}

// Helper to convert URI to File
private fun uriToFile(context: Context, uri: Uri, extension: String): File {
    val contentResolver = context.contentResolver
    val myFile = File.createTempFile("temp_media", extension, context.cacheDir)
    val inputStream = contentResolver.openInputStream(uri)
    val outputStream = FileOutputStream(myFile)
    
    inputStream?.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }
    return myFile
}
