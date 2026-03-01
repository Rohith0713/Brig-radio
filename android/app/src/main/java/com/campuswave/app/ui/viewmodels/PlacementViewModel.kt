package com.campuswave.app.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campuswave.app.data.auth.AuthManager
import com.campuswave.app.data.models.Placement
import com.campuswave.app.data.models.PlacementPoster
import com.campuswave.app.data.models.PlacementRequest
import com.campuswave.app.data.network.RetrofitClient
import com.campuswave.app.utils.ApiResult
import com.campuswave.app.utils.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlacementViewModel(context: Context) : ViewModel() {
    private val apiService = RetrofitClient.apiService
    private val authManager = AuthManager(context)

    private val _placements = MutableStateFlow<ApiResult<List<Placement>>>(ApiResult.Loading)
    val placements: StateFlow<ApiResult<List<Placement>>> = _placements.asStateFlow()

    private val _posters = MutableStateFlow<ApiResult<List<PlacementPoster>>>(ApiResult.Loading)
    val posters: StateFlow<ApiResult<List<PlacementPoster>>> = _posters.asStateFlow()

    private val _createState = MutableStateFlow<ApiResult<Placement>?>(null)
    val createState: StateFlow<ApiResult<Placement>?> = _createState.asStateFlow()

    private val _uploadPosterState = MutableStateFlow<ApiResult<PlacementPoster>?>(null)
    val uploadPosterState: StateFlow<ApiResult<PlacementPoster>?> = _uploadPosterState.asStateFlow()

    init {
        fetchPlacements()
        fetchPosters()
    }

    fun fetchPlacements() {
        viewModelScope.launch {
            _placements.value = ApiResult.Loading
            try {
                val token = authManager.getToken() ?: "guest"
                val response = apiService.getPlacements("Bearer $token")
                _placements.value = ApiResult.Success(response)
            } catch (e: Exception) {
                _placements.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun fetchPosters() {
        viewModelScope.launch {
            _posters.value = ApiResult.Loading
            try {
                val token = authManager.getToken() ?: "guest"
                val response = apiService.getPlacementPosters("Bearer $token")
                _posters.value = ApiResult.Success(response)
            } catch (e: Exception) {
                _posters.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun addPlacement(request: PlacementRequest) {
        viewModelScope.launch {
            _createState.value = ApiResult.Loading
            try {
                val token = authManager.getToken() ?: return@launch
                val response = apiService.createPlacement("Bearer $token", request)
                _createState.value = ApiResult.Success(response)
                fetchPlacements() // Refresh list
            } catch (e: Exception) {
                _createState.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun uploadPoster(
        title: String,
        company: String?,
        description: String?,
        posterPart: okhttp3.MultipartBody.Part
    ) {
        viewModelScope.launch {
            _uploadPosterState.value = ApiResult.Loading
            try {
                val token = authManager.getToken() ?: return@launch
                val titleBody = okhttp3.RequestBody.create(okhttp3.MultipartBody.FORM, title)
                val companyBody = company?.let { okhttp3.RequestBody.create(okhttp3.MultipartBody.FORM, it) }
                val descriptionBody = description?.let { okhttp3.RequestBody.create(okhttp3.MultipartBody.FORM, it) }

                val response = apiService.uploadPlacementPoster(
                    token = "Bearer $token",
                    poster = posterPart,
                    title = titleBody,
                    company = companyBody,
                    description = descriptionBody
                )
                _uploadPosterState.value = ApiResult.Success(response)
                fetchPosters() // Refresh list
            } catch (e: Exception) {
                _uploadPosterState.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun togglePosterVisibility(id: Int, isVisible: Boolean) {
        viewModelScope.launch {
            try {
                val token = authManager.getToken() ?: return@launch
                apiService.updatePlacementPoster(
                    "Bearer $token",
                    id,
                    mapOf("isVisible" to isVisible)
                )
                fetchPosters()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deletePlacementPoster(id: Int) {
        viewModelScope.launch {
            try {
                val token = authManager.getToken() ?: return@launch
                apiService.deletePlacementPoster("Bearer $token", id)
                fetchPosters()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun resetCreateState() {
        _createState.value = null
    }

    fun resetUploadPosterState() {
        _uploadPosterState.value = null
    }

    fun toggleBookmark(id: Int) {
        viewModelScope.launch {
            try {
                val token = authManager.getToken() ?: return@launch
                apiService.togglePlacementBookmark("Bearer $token", id)
                
                // Refresh both lists to update UI
                fetchPlacements()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deletePlacement(id: Int) {
        viewModelScope.launch {
            try {
                val token = authManager.getToken() ?: return@launch
                apiService.deletePlacement("Bearer $token", id)
                fetchPlacements() // Refresh list
            } catch (e: Exception) {
                // Handle deletion error
            }
        }
    }
}
