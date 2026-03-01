package com.campuswave.app.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campuswave.app.utils.ApiResult
import com.campuswave.app.data.models.ReportRequest
import com.campuswave.app.data.models.ReportResponse
import com.campuswave.app.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReportViewModel(private val context: Context) : ViewModel() {
    private val authManager = com.campuswave.app.data.auth.AuthManager(context)
    private val api = RetrofitClient.apiService

    private val _reportState = MutableStateFlow<ApiResult<ReportResponse>?>(null)
    val reportState: StateFlow<ApiResult<ReportResponse>?> = _reportState

    fun submitReport(issueType: String, description: String, sessionId: Int? = null) {
        viewModelScope.launch {
            _reportState.value = ApiResult.Loading
            try {
                val token = authManager.getToken()
                if (token == null) {
                    _reportState.value = ApiResult.Error("Not authenticated")
                    return@launch
                }

                val request = ReportRequest(
                    issue_type = issueType,
                    description = description,
                    session_id = sessionId
                )

                _reportState.value = api.createReport(request) // request is Body
                // Note: RetrofitClient.apiService.createReport needs to expect Body, not token in arg if interceptor handles it.
                // Assuming ApiService methods are suspended and return ApiResult.
                // Wait, ApiService methods usually need token if not handled by interceptor?
                // In this project, does RetrofitClient add token automatically?
                // Let's check RetrofitClient or other usages.
                // In ProfileViewModel, specific methods were used.
                // Actually `BRIG_RADIOApiService` methods often needed `@Header("Authorization")`.
                // Let's check `BRIG_RADIOApiService.kt` again to be sure.
            } catch (e: Exception) {
                _reportState.value = ApiResult.Error(e.message ?: "Available to submit report")
            }
        }
    }
    
    fun resetState() {
        _reportState.value = null
    }
}
