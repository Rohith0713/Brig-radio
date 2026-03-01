@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.campuswave.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.rememberNavController
import com.campuswave.app.navigation.AppNavigation
import com.campuswave.app.ui.viewmodels.*
import com.campuswave.app.data.auth.AuthManager
import com.campuswave.app.ui.theme.BRIG_RADIOThemeWithManager

class MainActivity : ComponentActivity() {
    private var deepLinkDestination = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        handleIntent(intent)
        
        val authManager = AuthManager(this)
        val authInterceptor = com.campuswave.app.data.network.AuthInterceptor {
            authManager.getToken()
        }
        com.campuswave.app.data.network.RetrofitClient.setAuthInterceptor(authInterceptor)
        
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("students")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) Log.d("FCM", "Subscribed to students topic")
                }
        } catch (e: Exception) {
            Log.e("FCM", "Firebase not configured", e)
        }
        
        com.campuswave.app.utils.NotificationHelper.createNotificationChannel(this)
        requestNotificationPermission()
        requestMicrophonePermission()
        
        enableEdgeToEdge()
        setContent {
            BRIG_RADIOThemeWithManager {
                BRIG_RADIOApp(deepLinkDestination = deepLinkDestination.value, onDeepLinkConsumed = { deepLinkDestination.value = null })
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            val type = it.getStringExtra("nav_type")
            val id = it.getStringExtra("nav_id")
            val autoStart = it.getBooleanExtra("nav_auto_start", false)

            if (type != null) {
                val route = when (type) {
                    "live_radio" -> id?.toIntOrNull()?.let { Screen.RadioDetails.createRoute(it, autoStart) }
                    "college_update" -> id?.toIntOrNull()?.let { Screen.CollegeUpdates.createRoute(it) }
                    "suggestion" -> id?.toIntOrNull()?.let { Screen.StudentIssueDetail.createRoute(it) }
                    "podcast_live" -> id?.toIntOrNull()?.let { "live_podcast_viewer/$it" }
                    "report_reply" -> id?.toIntOrNull()?.let { Screen.StudentIssueDetail.createRoute(it) }
                    "announcement" -> Screen.Notifications.route
                    else -> null
                }
                
                if (route != null) {
                    deepLinkDestination.value = route
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { }.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    fun requestMicrophonePermission() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS)
        }

        val missingPermissions = permissions.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 1001)
        }
    }
}

@Composable
fun BRIG_RADIOApp(
    deepLinkDestination: String? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    val authManager = remember { AuthManager(context) }
    val authViewModel = remember { AuthViewModel(context) }
    val adminViewModel = remember { AdminViewModel(context) }
    val radioViewModel = remember { RadioViewModel(context) }
    val suggestionsViewModel = remember { SuggestionsViewModel(context) }
    val placementViewModel = remember { PlacementViewModel(context) }
    
    val signalingClient = remember { com.campuswave.app.data.network.SignalingClient() }
    val audioManager = remember { com.campuswave.app.data.audio.WebRTCAudioManager(context, signalingClient) }
    val podcastViewModel = remember { PodcastViewModel(authManager, audioManager) }
    
    AppNavigation(
        navController = navController,
        authManager = authManager,
        adminViewModel = adminViewModel,
        radioViewModel = radioViewModel,
        suggestionsViewModel = suggestionsViewModel,
        podcastViewModel = podcastViewModel,
        placementViewModel = placementViewModel,
        authViewModel = authViewModel,
        deepLinkDestination = deepLinkDestination,
        onDeepLinkConsumed = onDeepLinkConsumed
    )
}
