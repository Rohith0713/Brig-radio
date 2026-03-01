package com.campuswave.app.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campuswave.app.data.auth.AuthManager
import com.campuswave.app.data.models.User
import com.campuswave.app.data.models.AdminAnalyticsResponse
import com.campuswave.app.data.network.RetrofitClient
import com.campuswave.app.utils.ApiResult
import com.campuswave.app.utils.ErrorHandler
import com.campuswave.app.data.models.Marquee
import com.campuswave.app.data.models.UpdateAnalyticsResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(context: Context) : ViewModel() {
    private val authManager = AuthManager(context)
    private val apiService = RetrofitClient.apiService

    private val _adminRequests = MutableStateFlow<ApiResult<List<User>>>(ApiResult.Loading)
    val adminRequests: StateFlow<ApiResult<List<User>>> = _adminRequests.asStateFlow()

    private val _approvalState = MutableStateFlow<ApiResult<String>?>(null)
    val approvalState: StateFlow<ApiResult<String>?> = _approvalState.asStateFlow()

    private val _analyticsState = MutableStateFlow<ApiResult<AdminAnalyticsResponse>>(ApiResult.Loading)
    val analyticsState: StateFlow<ApiResult<AdminAnalyticsResponse>> = _analyticsState.asStateFlow()

    private val _updateAnalytics = MutableStateFlow<ApiResult<UpdateAnalyticsResponse>?>(null)
    val updateAnalytics: StateFlow<ApiResult<UpdateAnalyticsResponse>?> = _updateAnalytics.asStateFlow()

    private val _marqueeList = MutableStateFlow<ApiResult<List<Marquee>>>(ApiResult.Loading)
    val marqueeList: StateFlow<ApiResult<List<Marquee>>> = _marqueeList.asStateFlow()

    private val _marqueeActionState = MutableStateFlow<ApiResult<Marquee>?>(null)
    val marqueeActionState: StateFlow<ApiResult<Marquee>?> = _marqueeActionState.asStateFlow()

    init {
        fetchAdminRequests()
        fetchAnalytics()
        fetchMarquees()
    }

    fun fetchAdminRequests() {
        viewModelScope.launch {
            try {
                _adminRequests.value = ApiResult.Loading
                val token = authManager.getToken() ?: throw Exception("Auth token missing")
                val requests = apiService.getAdminRequests("Bearer $token")
                _adminRequests.value = ApiResult.Success(requests)
            } catch (e: Exception) {
                _adminRequests.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun approveAdmin(adminId: Int, isApprove: Boolean) {
        viewModelScope.launch {
            try {
                _approvalState.value = ApiResult.Loading
                val token = authManager.getToken() ?: throw Exception("Auth token missing")
                val action = if (isApprove) "APPROVE" else "REJECT"
                val response = apiService.approveAdmin(
                    token = "Bearer $token",
                    adminId = adminId,
                    request = mapOf("action" to action)
                )
                _approvalState.value = ApiResult.Success(response.message)
                // Refresh list
                fetchAdminRequests()
            } catch (e: Exception) {
                _approvalState.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun fetchAnalytics() {
        viewModelScope.launch {
            try {
                _analyticsState.value = ApiResult.Loading
                val token = authManager.getToken() ?: throw Exception("Auth token missing")
                val response = apiService.getAnalytics("Bearer $token")
                _analyticsState.value = ApiResult.Success(response)
            } catch (e: Exception) {
                _analyticsState.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun fetchUpdateAnalytics(updateId: Int) {
        viewModelScope.launch {
            try {
                _updateAnalytics.value = ApiResult.Loading
                val token = authManager.getToken() ?: throw Exception("Auth token missing")
                val response = apiService.getUpdateAnalytics("Bearer $token", updateId)
                _updateAnalytics.value = ApiResult.Success(response)
            } catch (e: Exception) {
                _updateAnalytics.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun resetUpdateAnalytics() {
        _updateAnalytics.value = null
    }

    fun resetApprovalState() {
        _approvalState.value = null
    }

    // ==================== Marquee Management ====================
    fun fetchMarquees() {
        viewModelScope.launch {
            try {
                _marqueeList.value = ApiResult.Loading
                val token = authManager.getToken() ?: throw Exception("Auth token missing")
                val marquees = apiService.getAllMarquees("Bearer $token")
                _marqueeList.value = ApiResult.Success(marquees)
            } catch (e: Exception) {
                _marqueeList.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun saveMarquee(marquee: Marquee) {
        viewModelScope.launch {
            try {
                _marqueeActionState.value = ApiResult.Loading
                val token = authManager.getToken() ?: throw Exception("Auth token missing")
                val response = apiService.createOrUpdateMarquee("Bearer $token", marquee)
                _marqueeActionState.value = ApiResult.Success(response)
                fetchMarquees()
            } catch (e: Exception) {
                _marqueeActionState.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun toggleMarquee(marqueeId: Int) {
        viewModelScope.launch {
            try {
                val token = authManager.getToken() ?: throw Exception("Auth token missing")
                apiService.toggleMarquee("Bearer $token", marqueeId)
                fetchMarquees()
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    fun deleteMarquee(marqueeId: Int) {
        viewModelScope.launch {
            try {
                val token = authManager.getToken() ?: throw Exception("Auth token missing")
                apiService.deleteMarquee("Bearer $token", marqueeId)
                fetchMarquees()
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    fun resetMarqueeActionState() {
        _marqueeActionState.value = null
    }
}
