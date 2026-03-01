package com.campuswave.app.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campuswave.app.data.auth.AuthManager
import com.campuswave.app.data.models.*
import com.campuswave.app.data.network.BRIG_RADIOApiService
import com.campuswave.app.data.network.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import com.campuswave.app.data.audio.WebRTCAudioManager

class PodcastViewModel(
    private val authManager: AuthManager,
    private val audioManager: WebRTCAudioManager
) : ViewModel() {

    private val apiService: BRIG_RADIOApiService = RetrofitClient.apiService

    // Podcast state
    private val _livePodcast = MutableStateFlow<Podcast?>(null)
    val livePodcast: StateFlow<Podcast?> = _livePodcast.asStateFlow()

    private val _scheduledPodcasts = MutableStateFlow<List<Podcast>>(emptyList())
    val scheduledPodcasts: StateFlow<List<Podcast>> = _scheduledPodcasts.asStateFlow()

    private val _activePodcast = MutableStateFlow<Podcast?>(null)
    val activePodcast: StateFlow<Podcast?> = _activePodcast.asStateFlow()

    // Viewer state (for admin)
    private val _viewerCount = MutableStateFlow(0)
    val viewerCount: StateFlow<Int> = _viewerCount.asStateFlow()

    // Hand raises (for admin)
    private val _handRaises = MutableStateFlow<List<HandRaise>>(emptyList())
    val handRaises: StateFlow<List<HandRaise>> = _handRaises.asStateFlow()

    // Student hand raise status
    private val _myHandRaiseStatus = MutableStateFlow<String?>(null)
    val myHandRaiseStatus: StateFlow<String?> = _myHandRaiseStatus.asStateFlow()

    // Audio states
    val isSpeaking: StateFlow<Boolean> = audioManager.isSpeaking
    val connectionStatus: StateFlow<String> = audioManager.connectionStatus

    // UI state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Polling state
    private var isPolling = false

    companion object {
        private const val TAG = "PodcastViewModel"
        private const val POLL_INTERVAL_MS = 3000L
    }

    // ==================== Podcast CRUD ====================

    fun createPodcast(title: String, description: String, scheduledStartTime: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val token = "Bearer ${authManager.getToken()}"
                val request = CreatePodcastRequest(title, description, scheduledStartTime)
                val response = apiService.createPodcast(token, request)

                if (response.podcast != null) {
                    _successMessage.value = "Podcast created successfully!"
                    fetchActivePodcasts()
                } else {
                    _errorMessage.value = response.message ?: "Failed to create podcast"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating podcast", e)
                _errorMessage.value = "Failed to create podcast: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchLivePodcast() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = "Bearer ${authManager.getToken()}"
                val response = apiService.getLivePodcast(token)
                _livePodcast.value = response.podcast
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching live podcast", e)
                _livePodcast.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchActivePodcasts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = "Bearer ${authManager.getToken()}"
                val response = apiService.getActivePodcasts(token)
                
                _scheduledPodcasts.value = response.podcasts.filter { it.status == "SCHEDULED" }
                _livePodcast.value = response.podcasts.find { it.status == "LIVE" }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching active podcasts", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchPodcastDetails(podcastId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = "Bearer ${authManager.getToken()}"
                val response = apiService.getPodcastDetails(token, podcastId)
                _activePodcast.value = response.podcast
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching podcast details", e)
                _errorMessage.value = "Failed to load podcast details"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePodcast(podcastId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = "Bearer ${authManager.getToken()}"
                apiService.deletePodcast(token, podcastId)
                _successMessage.value = "Podcast deleted successfully"
                fetchActivePodcasts()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting podcast", e)
                _errorMessage.value = "Failed to delete podcast"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==================== Podcast Control (Admin) ====================

    fun goLive(podcastId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val token = "Bearer ${authManager.getToken()}"
                val response = apiService.goLivePodcast(token, podcastId)

                if (response.podcast != null) {
                    _activePodcast.value = response.podcast
                    _livePodcast.value = response.podcast
                    _successMessage.value = "Podcast is now LIVE!"
                    
                    // Join WebRTC room
                    try {
                        audioManager.connect(podcastId.toString())
                    } catch (e: Exception) {
                        Log.e(TAG, "Error connecting to audio room", e)
                        _errorMessage.value = "Failed to start audio: ${e.message}"
                    }
                    
                    startPolling(podcastId)
                } else {
                    _errorMessage.value = response.message ?: "Failed to go live"
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e(TAG, "Error going live: $errorBody", e)
                _errorMessage.value = "Another podcast is already live"
            } catch (e: Exception) {
                Log.e(TAG, "Error going live", e)
                _errorMessage.value = "Failed to go live: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun endPodcast(podcastId: Int) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val token = "Bearer ${authManager.getToken()}"
                val response = apiService.endPodcast(token, podcastId)

                stopPolling()
                _activePodcast.value = null
                _livePodcast.value = null
                _viewerCount.value = 0
                _handRaises.value = emptyList()
                _successMessage.value = "Podcast ended"
                
                // Leave WebRTC room
                audioManager.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error ending podcast", e)
                _errorMessage.value = "Failed to end podcast"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleMute(podcastId: Int) {
        viewModelScope.launch {
            try {
                val token = "Bearer ${authManager.getToken()}"
                val response = apiService.toggleMutePodcast(token, podcastId)

                _activePodcast.value = _activePodcast.value?.copy(is_muted = response.is_muted)
                
                // CRITICAL: Pass isAdmin=true to broadcast pause/resume via signaling
                audioManager.mute(response.is_muted, isAdmin = true)
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling mute", e)
                _errorMessage.value = "Failed to toggle mute"
            }
        }
    }

    fun clearActivePodcast() {
        _activePodcast.value = null
        _myHandRaiseStatus.value = null
        _handRaises.value = emptyList()
        _viewerCount.value = 0
        _successMessage.value = null
        _errorMessage.value = null
    }

    // ==================== Viewer Management ====================

    fun joinPodcast(podcastId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = "Bearer ${authManager.getToken()}"
                val response = apiService.joinPodcast(token, podcastId)
                _viewerCount.value = response.viewer_count

                // Fetch the podcast details
                val podcastResponse = apiService.getPodcastDetails(token, podcastId)
                _activePodcast.value = podcastResponse.podcast
                
                rejoinChannel(podcastId)
            } catch (e: Exception) {
                Log.e(TAG, "Error joining podcast", e)
                _errorMessage.value = "Failed to join podcast"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun rejoinChannel(podcastId: Int) {
        viewModelScope.launch {
            try {
                audioManager.connect(podcastId.toString())
            } catch (e: Exception) {
                Log.e(TAG, "Error connecting to audio room", e)
                _errorMessage.value = "Failed to join audio room"
            }
        }
    }

    fun leavePodcast(podcastId: Int) {
        viewModelScope.launch {
            try {
                val token = "Bearer ${authManager.getToken()}"
                apiService.leavePodcast(token, podcastId)
                _activePodcast.value = null
                _myHandRaiseStatus.value = null
                
                // Leave WebRTC room
                audioManager.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error leaving podcast", e)
            }
        }
    }

    fun fetchViewerCount(podcastId: Int) {
        viewModelScope.launch {
            try {
                val token = "Bearer ${authManager.getToken()}"
                val response = apiService.getPodcastViewerCount(token, podcastId)
                _viewerCount.value = response.viewer_count
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching viewer count", e)
            }
        }
    }

    // ==================== Hand Raise Management ====================

    fun raiseHand(podcastId: Int) {
        viewModelScope.launch {
            try {
                val token = "Bearer ${authManager.getToken()}"
                val response = apiService.raiseHand(token, podcastId)
                _myHandRaiseStatus.value = "PENDING"
                _successMessage.value = "Hand raised!"
            } catch (e: Exception) {
                Log.e(TAG, "Error raising hand", e)
                _errorMessage.value = "Failed to raise hand"
            }
        }
    }

    fun cancelHandRaise(podcastId: Int) {
        viewModelScope.launch {
            try {
                val token = "Bearer ${authManager.getToken()}"
                apiService.cancelHandRaise(token, podcastId)
                _myHandRaiseStatus.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error canceling hand raise", e)
            }
        }
    }

    fun fetchMyHandRaiseStatus(podcastId: Int) {
        viewModelScope.launch {
            try {
                val token = "Bearer ${authManager.getToken()}"
                val response = apiService.getHandRaiseStatus(token, podcastId)
                _myHandRaiseStatus.value = response.status
                
                // Update Role in 100ms if hand raise accepted
                // In HMS, we usually request a role change
                if (response.status == "ACCEPTED") {
                    // audioManager.requestRoleChange("host") 
                } else {
                    // audioManager.requestRoleChange("viewer")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching hand raise status", e)
            }
        }
    }

    fun fetchHandRaises(podcastId: Int) {
        viewModelScope.launch {
            try {
                val token = "Bearer ${authManager.getToken()}"
                val response = apiService.getHandRaises(token, podcastId)
                _handRaises.value = response.hand_raises
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching hand raises", e)
            }
        }
    }

    fun acceptHandRaise(podcastId: Int, userId: Int) {
        viewModelScope.launch {
            try {
                val token = "Bearer ${authManager.getToken()}"
                apiService.acceptHandRaise(token, podcastId, userId)
                fetchHandRaises(podcastId)
                _successMessage.value = "Hand raise accepted"
            } catch (e: Exception) {
                Log.e(TAG, "Error accepting hand raise", e)
                _errorMessage.value = "Failed to accept hand raise"
            }
        }
    }

    fun ignoreHandRaise(podcastId: Int, userId: Int) {
        viewModelScope.launch {
            try {
                val token = "Bearer ${authManager.getToken()}"
                apiService.ignoreHandRaise(token, podcastId, userId)
                fetchHandRaises(podcastId)
            } catch (e: Exception) {
                Log.e(TAG, "Error ignoring hand raise", e)
            }
        }
    }

    // ==================== Polling ====================

    fun startPolling(podcastId: Int) {
        if (isPolling) return
        isPolling = true

        viewModelScope.launch {
            while (isPolling) {
                try {
                    fetchViewerCount(podcastId)
                    fetchHandRaises(podcastId)
                } catch (e: Exception) {
                    Log.e(TAG, "Polling error", e)
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    fun stopPolling() {
        isPolling = false
    }

    fun startStudentPolling(podcastId: Int) {
        if (isPolling) return
        isPolling = true

        viewModelScope.launch {
            while (isPolling) {
                try {
                    fetchMyHandRaiseStatus(podcastId)
                    
                    // Check if podcast is still live
                    val token = "Bearer ${authManager.getToken()}"
                    val response = apiService.getPodcastDetails(token, podcastId)
                    if (response.podcast?.status != "LIVE") {
                        _activePodcast.value = null
                        stopPolling()
                    } else {
                        val podcast = response.podcast
                        _activePodcast.value = podcast
                        
                        // CRITICAL: Enforce pause state as backup to signaling
                        // This ensures pause state is synced even if signaling fails
                        audioManager.enforcePauseState(podcast.is_muted)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Student polling error", e)
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    // ==================== Utility ====================

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun setActivePodcast(podcast: Podcast) {
        _activePodcast.value = podcast
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
        audioManager.disconnect()
    }
}
