package com.campuswave.app.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campuswave.app.data.auth.AuthManager
import com.campuswave.app.data.models.CreateRadioRequest
import com.campuswave.app.data.models.DashboardStats
import com.campuswave.app.data.models.Radio
import com.campuswave.app.data.network.MediaUploadResponse
import com.campuswave.app.data.network.RetrofitClient
import com.campuswave.app.utils.ApiResult
import com.campuswave.app.utils.ErrorHandler
import com.campuswave.app.utils.NotificationScheduler
import com.campuswave.app.data.models.Marquee
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class RadioViewModel(private val context: Context) : ViewModel() {
    
    private val authManager = AuthManager(context)
    private val apiService = RetrofitClient.apiService
    private val radioSignalingClient = com.campuswave.app.data.network.RadioSignalingClient()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    private val formatWithMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US)
    private val formatWithMicro = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.US)
    private val formatIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US)

    private val dateCache = mutableMapOf<String, Date>()
    
    // Time Synchronization
    private var serverTimeOffset: Long = 0L
    private val _synchronizedTime = MutableStateFlow(System.currentTimeMillis())
    val synchronizedTime: StateFlow<Long> = _synchronizedTime.asStateFlow()

    init {
        startAutoRefresh()
        startSynchronizedTimer()
    }
    
    private fun startSynchronizedTimer() {
        viewModelScope.launch {
            while (isActive) {
                _synchronizedTime.value = System.currentTimeMillis() + serverTimeOffset
                delay(1000)
            }
        }
    }

    private fun updateServerTimeOffset(serverTimeStr: String?) {
        val serverTimeMillis = com.campuswave.app.utils.DateUtils.parseIsoToMillis(serverTimeStr) ?: return
        val deviceTimeMillis = System.currentTimeMillis()
        serverTimeOffset = serverTimeMillis - deviceTimeMillis
    }
    
    private val _liveRadios = MutableStateFlow<ApiResult<List<Radio>>?>(null)
    val liveRadios: StateFlow<ApiResult<List<Radio>>?> = _liveRadios.asStateFlow()
    
    private val _upcomingRadios = MutableStateFlow<ApiResult<List<Radio>>?>(null)
    val upcomingRadios: StateFlow<ApiResult<List<Radio>>?> = _upcomingRadios.asStateFlow()
    
    private val _missedRadios = MutableStateFlow<ApiResult<List<Radio>>?>(null)
    val missedRadios: StateFlow<ApiResult<List<Radio>>?> = _missedRadios.asStateFlow()
    
    private val _allRadios = MutableStateFlow<ApiResult<List<Radio>>?>(null)
    val allRadios: StateFlow<ApiResult<List<Radio>>?> = _allRadios.asStateFlow()
    
    private val _dashboardStats = MutableStateFlow<ApiResult<DashboardStats>?>(null)
    val dashboardStats: StateFlow<ApiResult<DashboardStats>?> = _dashboardStats.asStateFlow()
    
    private val _createRadioState = MutableStateFlow<ApiResult<Radio>?>(null)
    val createRadioState: StateFlow<ApiResult<Radio>?> = _createRadioState.asStateFlow()
    
    private val _mediaUploadState = MutableStateFlow<ApiResult<MediaUploadResponse>?>(null)
    val mediaUploadState: StateFlow<ApiResult<MediaUploadResponse>?> = _mediaUploadState.asStateFlow()
    
    private val _categories = MutableStateFlow<ApiResult<List<com.campuswave.app.data.models.Category>>?>(null)
    val categories: StateFlow<ApiResult<List<com.campuswave.app.data.models.Category>>?> = _categories.asStateFlow()
    
    private val _radioDetails = MutableStateFlow<ApiResult<Radio>?>(null)
    val radioDetails: StateFlow<ApiResult<Radio>?> = _radioDetails.asStateFlow()
    
    private val _updateRadioState = MutableStateFlow<ApiResult<Radio>?>(null)
    val updateRadioState: StateFlow<ApiResult<Radio>?> = _updateRadioState.asStateFlow()
    
    private val _activeMarquee = MutableStateFlow<ApiResult<Marquee>?>(null)
    val activeMarquee: StateFlow<ApiResult<Marquee>?> = _activeMarquee.asStateFlow()
    
    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (isActive) {
                delay(10000) // Increased to 10s to reduce battery/CPU usage
                refreshAllRadiosSmart()
                fetchActiveMarquee()
            }
        }
    }

    fun refreshAllRadiosSmart() {
        viewModelScope.launch {
            try {
                // Fetch all radios in one go to reduce network requests
                val response = try { apiService.getAllRadios(1, 100) } catch (e: Exception) { 
                    android.util.Log.e("RadioViewModel", "Error calling getAllRadios", e)
                    null 
                }
                if (response == null) return@launch
                
                val allFetched = response.radios
                val now = Date()
                
                val liveList = mutableListOf<Radio>()
                val upcomingList = mutableListOf<Radio>()
                val missedList = mutableListOf<Radio>()
                
                allFetched.forEach { radio ->
                    val start = parseDate(radio.start_time)
                    val end = parseDate(radio.end_time)
                    
                    if (radio.host_status == "ENDED") {
                        missedList.add(radio.copy(status = "MISSED"))
                    } else if (radio.status == "DRAFT") {
                        // Keep DRAFT status, don't override with LIVE/UPCOMING based on time
                        // Add to upcoming list for admin visibility, but status remains DRAFT
                        upcomingList.add(radio)
                    } else if (start != null && end != null) {
                        if (now.after(start) && now.before(end)) {
                            liveList.add(radio.copy(status = "LIVE"))
                        } else if (now.before(start)) {
                            upcomingList.add(radio.copy(status = "UPCOMING"))
                        } else {
                            missedList.add(radio.copy(status = "MISSED"))
                        }
                    } else {
                        when(radio.status.uppercase()) {
                            "LIVE" -> liveList.add(radio)
                            "UPCOMING" -> upcomingList.add(radio)
                            else -> missedList.add(radio)
                        }
                    }
                }
                
                // Only update if lists have changed to avoid unnecessary recompositions
                if (_liveRadios.value !is ApiResult.Success || (_liveRadios.value as ApiResult.Success).data != liveList) {
                    _liveRadios.value = ApiResult.Success(liveList)
                }
                if (_upcomingRadios.value !is ApiResult.Success || (_upcomingRadios.value as ApiResult.Success).data != upcomingList) {
                    _upcomingRadios.value = ApiResult.Success(upcomingList)
                }
                if (_missedRadios.value !is ApiResult.Success || (_missedRadios.value as ApiResult.Success).data != missedList) {
                    _missedRadios.value = ApiResult.Success(missedList)
                }
                
                // Update synchronization offset from the first radio's server_time if available
                allFetched.firstOrNull()?.server_time?.let { updateServerTimeOffset(it) }

            } catch (e: Exception) {
                // Keep old state
                android.util.Log.e("RadioViewModel", "Error in refreshAllRadiosSmart", e)
            }
        }
    }
    
    private fun parseDate(dateStr: String): Date? {
        if (dateStr.isEmpty()) return null
        
        // Return from cache if available to avoid parsing overhead
        dateCache[dateStr]?.let { return it }
        
        val parsedDate = try {
            dateFormat.parse(dateStr) ?: 
            formatWithMillis.parse(dateStr) ?:
            formatWithMicro.parse(dateStr) ?:
            formatIso.parse(dateStr)
        } catch (e: Exception) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                try {
                    Date.from(java.time.Instant.parse(dateStr))
                } catch (e2: Exception) {
                    null
                }
            } else {
                null
            }
        }
        
        if (parsedDate != null) {
            // Keep cache size reasonable
            if (dateCache.size > 200) dateCache.clear()
            dateCache[dateStr] = parsedDate
        } else {
            android.util.Log.e("RadioViewModel", "Failed to parse date: $dateStr")
        }
        
        return parsedDate
    }

    fun fetchLiveRadios() {
        refreshAllRadiosSmart()
    }
    
    fun fetchUpcomingRadios() {
        refreshAllRadiosSmart()
    }
    
    fun fetchMissedRadios() {
        refreshAllRadiosSmart()
    }
    
    fun fetchRadioDetails(radioId: Int) {
        viewModelScope.launch {
            // Only set loading if we don't have data yet to avoid flicker during polling
            if (_radioDetails.value !is ApiResult.Success) {
                _radioDetails.value = ApiResult.Loading
            }
            
            try {
                val response = apiService.getRadioDetails(radioId)
                // Only update if data is different to avoid recompositions
                val current = (_radioDetails.value as? ApiResult.Success)?.data
                if (current == null || current != response) {
                    _radioDetails.value = ApiResult.Success(response)
                    // Update offset from details response as well
                    response.server_time?.let { updateServerTimeOffset(it) }
                }
            } catch (e: Exception) {
                // If we already have data, don't show error state for a single failed poll
                if (_radioDetails.value !is ApiResult.Success) {
                    _radioDetails.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
                }
            }
        }
    }
    
    fun fetchDashboardStats() {
        viewModelScope.launch {
            if (_dashboardStats.value !is ApiResult.Success) {
                _dashboardStats.value = ApiResult.Loading
            }
            try {
                val token = authManager.getToken() ?: run {
                    _dashboardStats.value = ApiResult.Error("Not authenticated")
                    return@launch
                }
                val stats = apiService.getDashboardStats("Bearer $token")
                _dashboardStats.value = ApiResult.Success(stats)
            } catch (e: Exception) {
                if (_dashboardStats.value !is ApiResult.Success) {
                    _dashboardStats.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
                }
            }
        }
    }
    
    fun fetchAllRadios(page: Int = 1, limit: Int = 10) {
        viewModelScope.launch {
            if (_allRadios.value !is ApiResult.Success) {
                _allRadios.value = ApiResult.Loading
            }
            try {
                val response = apiService.getAllRadios(page, limit)
                _allRadios.value = ApiResult.Success(response.radios)
            } catch (e: Exception) {
                if (_allRadios.value !is ApiResult.Success) {
                    _allRadios.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
                }
            }
        }
    }

    private var pollingJob: kotlinx.coroutines.Job? = null

    fun startRadioPolling(radioId: Int) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                fetchRadioDetails(radioId)
                delay(5000) // Increased from 2s to 5s to reduce lag
            }
        }
    }
    
    fun stopRadioPolling() {
        pollingJob?.cancel()
    }

    fun createRadio(
        title: String,
        description: String,
        location: String,
        startTime: String,
        endTime: String,
        categoryId: Int,
        status: String = "UPCOMING",
        mediaFile: File? = null,
        bannerFile: File? = null
    ) {
        viewModelScope.launch {
            try {
                _createRadioState.value = ApiResult.Loading
                val token = authManager.getToken() ?: run {
                    _createRadioState.value = ApiResult.Error("Not authenticated")
                    return@launch
                }
                
                val radio = apiService.createRadio(
                    token = "Bearer $token",
                    radio = CreateRadioRequest(
                        title = title,
                        description = description,
                        location = location,
                        start_time = startTime,
                        end_time = endTime,
                        category_id = categoryId,
                        status = status
                    )
                )
                
                _createRadioState.value = ApiResult.Success(radio)
                _mediaUploadState.value = ApiResult.Loading

                var bannerSuccess = true
                var mediaSuccess = true

                // Upload banner image if provided
                if (bannerFile != null && bannerFile.exists()) {
                    android.util.Log.d("RadioViewModel", "Banner file provided for radio ${radio.id}, triggering upload")
                    bannerSuccess = uploadBannerForRadio(radio.id, bannerFile, token)
                }
                
                // Upload media if provided
                if (mediaFile != null && mediaFile.exists()) {
                    mediaSuccess = uploadMediaForRadio(radio.id, mediaFile, token)
                }
                
                if (bannerSuccess && mediaSuccess) {
                    _mediaUploadState.value = ApiResult.Success(MediaUploadResponse("Uploads completed", ""))
                } else {
                    _mediaUploadState.value = ApiResult.Error("One or more uploads failed")
                }
                
                // Schedule notifications for the new radio session
                com.campuswave.app.utils.NotificationScheduler.scheduleRadioReminders(
                    context = context,
                    radioId = radio.id,
                    radioTitle = radio.title,
                    startTimeStr = startTime
                )
                
                // Refresh radios after creation
                fetchLiveRadios()
                fetchUpcomingRadios()
                fetchMissedRadios()
                
            } catch (e: Exception) {
                _createRadioState.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
                _mediaUploadState.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }
    
    private suspend fun uploadMediaForRadio(radioId: Int, file: File, token: String): Boolean {
        return try {
            val extension = file.extension.lowercase()
            val mediaType = when (extension) {
                "mp3" -> "audio/mpeg"
                "mp4" -> "video/mp4"
                "wav" -> "audio/wav"
                "webm" -> "video/webm"
                else -> "application/octet-stream"
            }.toMediaTypeOrNull()
            
            val requestBody = file.asRequestBody(mediaType)
            val part = MultipartBody.Part.createFormData("media", file.name, requestBody)
            
            apiService.uploadRadioMedia("Bearer $token", radioId, part)
            true
        } catch (e: Exception) {
            // Log error but don't fail radio creation
            e.printStackTrace()
            false
        }
    }
    
    private suspend fun uploadBannerForRadio(radioId: Int, file: File, token: String): Boolean {
        return try {
            android.util.Log.d("RadioViewModel", "Starting banner upload for radio $radioId, file: ${file.absolutePath}, size: ${file.length()}")
            
            val extension = file.extension.lowercase()
            val mediaType = when (extension) {
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "gif" -> "image/gif"
                "webp" -> "image/webp"
                else -> "image/jpeg"
            }.toMediaTypeOrNull()
            
            android.util.Log.d("RadioViewModel", "Banner media type: $mediaType, extension: $extension")
            
            val requestBody = file.asRequestBody(mediaType)
            val part = MultipartBody.Part.createFormData("banner", file.name, requestBody)
            
            val response = apiService.uploadRadioBanner("Bearer $token", radioId, part)
            android.util.Log.d("RadioViewModel", "Banner upload successful for radio $radioId, response: ${response.banner_image}")
            true
        } catch (e: Exception) {
            // Log error but don't fail radio creation
            android.util.Log.e("RadioViewModel", "Banner upload FAILED for radio $radioId: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    fun updateRadio(
        radioId: Int,
        title: String,
        description: String,
        location: String,
        startTime: String,
        endTime: String,
        categoryId: Int,
        status: String = "UPCOMING"
    ) {
        viewModelScope.launch {
            try {
                _updateRadioState.value = ApiResult.Loading
                val token = authManager.getToken() ?: run {
                    _updateRadioState.value = ApiResult.Error("Not authenticated")
                    return@launch
                }
                
                val radio = apiService.updateRadio(
                    token = "Bearer $token",
                    radioId = radioId,
                    radio = CreateRadioRequest(
                        title = title,
                        description = description,
                        location = location,
                        start_time = startTime,
                        end_time = endTime,
                        category_id = categoryId,
                        status = status
                    )
                )
                
                _updateRadioState.value = ApiResult.Success(radio)
                
                // Refresh radio details
                fetchRadioDetails(radioId)
                fetchLiveRadios()
                fetchUpcomingRadios()
                fetchMissedRadios()
                
            } catch (e: Exception) {
                _updateRadioState.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun deleteRadio(radioId: Int) {
        viewModelScope.launch {
            try {
                val token = authManager.getToken()
                if (token != null) {
                    apiService.deleteRadio("Bearer $token", radioId)
                    // Refresh radios after deletion
                    fetchLiveRadios()
                    fetchUpcomingRadios()
                    fetchMissedRadios()
                }
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    fun fetchCategories() {
        viewModelScope.launch {
            try {
                _categories.value = ApiResult.Loading
                val response = apiService.getCategories()
                _categories.value = ApiResult.Success(response)
            } catch (e: Exception) {
                _categories.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun resetCreateRadioState() {
        _createRadioState.value = null
    }
    
    fun resetUpdateRadioState() {
        _updateRadioState.value = null
    }
    
    fun resetMediaUploadState() {
        _mediaUploadState.value = null
    }
    
    // ==================== Live Hosting ====================
    
    private val _hostingState = MutableStateFlow<ApiResult<Radio>?>(null)
    val hostingState: StateFlow<ApiResult<Radio>?> = _hostingState.asStateFlow()
    
    fun startHosting(radioId: Int, mediaType: String) {
        viewModelScope.launch {
            try {
                _hostingState.value = ApiResult.Loading
                val token = authManager.getToken() ?: run {
                    _hostingState.value = ApiResult.Error("Not authenticated")
                    return@launch
                }
                
                val response = apiService.startHosting(
                    token = "Bearer $token",
                    radioId = radioId,
                    request = com.campuswave.app.data.network.StartHostingRequest(media_type = mediaType)
                )
                
                _hostingState.value = ApiResult.Success(response.radio)
                _radioDetails.value = ApiResult.Success(response.radio)
                refreshAllRadiosSmart()
                
            } catch (e: Exception) {
                _hostingState.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }
    
    fun pauseHosting(radioId: Int) {
        viewModelScope.launch {
            try {
                _hostingState.value = ApiResult.Loading
                val token = authManager.getToken() ?: run {
                    _hostingState.value = ApiResult.Error("Not authenticated")
                    return@launch
                }
                
                val response = apiService.pauseHosting(
                    token = "Bearer $token",
                    radioId = radioId
                )
                
                // Broadcast pause event via WebSocket
                radioSignalingClient.sendPauseCommand(radioId)
                
                _hostingState.value = ApiResult.Success(response.radio)
                _radioDetails.value = ApiResult.Success(response.radio)
                
            } catch (e: Exception) {
                _hostingState.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }
    
    fun resumeHosting(radioId: Int) {
        viewModelScope.launch {
            try {
                _hostingState.value = ApiResult.Loading
                val token = authManager.getToken() ?: run {
                    _hostingState.value = ApiResult.Error("Not authenticated")
                    return@launch
                }
                
                val response = apiService.resumeHosting(
                    token = "Bearer $token",
                    radioId = radioId
                )
                
                // Broadcast resume event via WebSocket
                radioSignalingClient.sendResumeCommand(radioId)
                
                _hostingState.value = ApiResult.Success(response.radio)
                _radioDetails.value = ApiResult.Success(response.radio)
                
            } catch (e: Exception) {
                _hostingState.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }
    
    fun endHosting(radioId: Int) {
        viewModelScope.launch {
            try {
                _hostingState.value = ApiResult.Loading
                val token = authManager.getToken() ?: run {
                    _hostingState.value = ApiResult.Error("Not authenticated")
                    return@launch
                }
                
                val response = apiService.endHosting(
                    token = "Bearer $token",
                    radioId = radioId
                )
                
                // Broadcast stop event via WebSocket
                radioSignalingClient.sendStopCommand(radioId)
                
                _hostingState.value = ApiResult.Success(response.radio)
                _radioDetails.value = ApiResult.Success(response.radio)
                refreshAllRadiosSmart()
                
            } catch (e: Exception) {
                _hostingState.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }
    
    fun resetHostingState() {
        _hostingState.value = null
    }
    
    // ==================== Favorites ====================
    fun toggleFavorite(radioId: Int) {
        viewModelScope.launch {
            val token = authManager.getToken() ?: return@launch
            try {
                val response = apiService.toggleFavorite("Bearer $token", radioId)
                
                // Update the radio in all our radio lists
                val updateRadio = { radio: Radio ->
                    if (radio.id == radioId) {
                        radio.copy(
                            is_favorited = response.is_favorited,
                            favorite_count = if (response.is_favorited) radio.favorite_count + 1 else maxOf(0, radio.favorite_count - 1)
                        )
                    } else radio
                }
                
                // Update live radios
                val currentLiveResult = _liveRadios.value
                if (currentLiveResult is ApiResult.Success) {
                    _liveRadios.value = ApiResult.Success(currentLiveResult.data.map(updateRadio))
                }
                
                // Update upcoming radios
                val currentUpcomingResult = _upcomingRadios.value
                if (currentUpcomingResult is ApiResult.Success) {
                    _upcomingRadios.value = ApiResult.Success(currentUpcomingResult.data.map(updateRadio))
                }
                
                // Update missed radios
                val currentMissedResult = _missedRadios.value
                if (currentMissedResult is ApiResult.Success) {
                    _missedRadios.value = ApiResult.Success(currentMissedResult.data.map(updateRadio))
                }
                
                // Update radio details if viewing this radio
                val currentDetailsResult = _radioDetails.value
                if (currentDetailsResult is ApiResult.Success && currentDetailsResult.data.id == radioId) {
                    _radioDetails.value = ApiResult.Success(updateRadio(currentDetailsResult.data))
                }
                
                android.util.Log.d("RadioViewModel", "Toggled favorite for radio $radioId: is_favorited=${response.is_favorited}")
                
            } catch (e: Exception) {
                android.util.Log.e("RadioViewModel", "Failed to toggle favorite: ${e.message}")
            }
        }
    }

    fun toggleSubscription(radioId: Int) {
        viewModelScope.launch {
            val token = authManager.getToken() ?: return@launch
            try {

                val response = apiService.toggleSubscription("Bearer $token", radioId)
                
                val updateRadio = { radio: Radio ->
                    if (radio.id == radioId) {
                        val newRadio = radio.copy(is_subscribed = response.is_subscribed)
                        
                        // Local notification scheduling
                        if (response.is_subscribed) {
                            NotificationScheduler.scheduleRadioReminders(
                                context = context,
                                radioId = newRadio.id,
                                radioTitle = newRadio.title,
                                startTimeStr = newRadio.start_time
                            )
                        } else {
                            NotificationScheduler.cancelRadioReminders(context, newRadio.id)
                        }
                        
                        newRadio
                    } else radio
                }
                
                val currentUpcomingResult = _upcomingRadios.value
                if (currentUpcomingResult is ApiResult.Success) {
                    _upcomingRadios.value = ApiResult.Success(currentUpcomingResult.data.map(updateRadio))
                }
                
                // Also update details if open
                val currentDetailsResult = _radioDetails.value
                if (currentDetailsResult is ApiResult.Success && currentDetailsResult.data.id == radioId) {
                     _radioDetails.value = ApiResult.Success(updateRadio(currentDetailsResult.data))
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ==================== Notifications ====================
    
    private val _notifications = MutableStateFlow<ApiResult<List<com.campuswave.app.data.models.Notification>>?>(null)
    val notifications: StateFlow<ApiResult<List<com.campuswave.app.data.models.Notification>>?> = _notifications.asStateFlow()
    
    fun fetchNotifications() {
        viewModelScope.launch {
            val token = authManager.getToken() ?: return@launch
            try {
                _notifications.value = ApiResult.Loading
                val response = apiService.getNotifications("Bearer $token")
                _notifications.value = ApiResult.Success(response.notifications)
            } catch (e: Exception) {
                _notifications.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }
    
    fun markAllRead() {
        viewModelScope.launch {
            val token = authManager.getToken() ?: return@launch
            try {
                apiService.markAllNotificationsRead("Bearer $token")
                fetchNotifications()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            val token = authManager.getToken() ?: return@launch
            try {
                apiService.deleteAllNotifications("Bearer $token")
                fetchNotifications()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun markAsRead(notificationId: Int) {
        viewModelScope.launch {
            val token = authManager.getToken() ?: return@launch
            try {
                apiService.markNotificationRead("Bearer $token", notificationId)
                fetchNotifications()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ==================== Marquee ====================
    fun fetchActiveMarquee() {
        viewModelScope.launch {
            try {
                val marquee = apiService.getActiveMarquee()
                _activeMarquee.value = ApiResult.Success(marquee)
            } catch (e: Exception) {
                // If it fails, just hide it or keep old one
                android.util.Log.e("RadioViewModel", "Error fetching active marquee: ${e.message}")
            }
        }
    }

    // ==================== Banners ====================
    private val _banners = MutableStateFlow<ApiResult<List<com.campuswave.app.data.models.Banner>>?>(null)
    val banners: StateFlow<ApiResult<List<com.campuswave.app.data.models.Banner>>?> = _banners.asStateFlow()
    
    fun fetchBanners() {
        viewModelScope.launch {
            try {
                // Only set loading if null to avoid flicker
                if (_banners.value == null) {
                    _banners.value = ApiResult.Loading
                }
                val response = apiService.getBanners()
                _banners.value = ApiResult.Success(response.banners)
            } catch (e: Exception) {
                if (_banners.value == null) {
                    _banners.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
                }
            }
        }
    }
    
    fun uploadBanner(file: File) {
        viewModelScope.launch {
             try {
                val token = authManager.getToken() ?: return@launch
                
                 val extension = file.extension.lowercase()
                val mediaType = when (extension) {
                    "jpg", "jpeg" -> "image/jpeg"
                    "png" -> "image/png"
                    "webp" -> "image/webp"
                    else -> "image/jpeg"
                }.toMediaTypeOrNull()
                
                val requestBody = file.asRequestBody(mediaType)
                val part = MultipartBody.Part.createFormData("banner", file.name, requestBody)
                
                apiService.uploadBanner("Bearer $token", part)
                fetchBanners() // Refresh list
             } catch (e: Exception) {
                 e.printStackTrace()
             }
        }
    }
    
    fun joinRadio(radioId: Int) {
        viewModelScope.launch {
            try {
                val token = authManager.getToken() ?: return@launch
                val response = apiService.joinRadio("Bearer $token", radioId)
                
                // Update specific radio details if open
                val currentDetailsResult = _radioDetails.value
                if (currentDetailsResult is ApiResult.Success && currentDetailsResult.data.id == radioId) {
                    _radioDetails.value = ApiResult.Success(
                        currentDetailsResult.data.copy(participant_count = response.participant_count)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun leaveRadio(radioId: Int) {
        viewModelScope.launch {
            try {
                val token = authManager.getToken() ?: return@launch
                val response = apiService.leaveRadio("Bearer $token", radioId)
                
                // Update specific radio details if open
                val currentDetailsResult = _radioDetails.value
                if (currentDetailsResult is ApiResult.Success && currentDetailsResult.data.id == radioId) {
                    _radioDetails.value = ApiResult.Success(
                        currentDetailsResult.data.copy(participant_count = response.participant_count)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteBanner(bannerId: Int) {
        viewModelScope.launch {
            try {
                val token = authManager.getToken() ?: return@launch
                apiService.deleteBanner("Bearer $token", bannerId)
                fetchBanners() // Refresh list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
