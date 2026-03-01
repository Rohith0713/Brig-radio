package com.campuswave.app.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campuswave.app.data.auth.AuthManager
import com.campuswave.app.data.models.CreateRadioSuggestionRequest
import com.campuswave.app.data.models.RadioSuggestion
import com.campuswave.app.data.network.RetrofitClient
import com.campuswave.app.data.network.MessageResponse
import com.campuswave.app.utils.ApiResult
import com.campuswave.app.utils.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SuggestionsViewModel(private val context: Context) : ViewModel() {
    
    private val authManager = AuthManager(context)
    private val apiService = RetrofitClient.apiService
    
    private val _allRadioSuggestions = MutableStateFlow<ApiResult<List<RadioSuggestion>>?>(null)
    val allRadioSuggestions: StateFlow<ApiResult<List<RadioSuggestion>>?> = _allRadioSuggestions.asStateFlow()
    
    private val _pendingRadioSuggestions = MutableStateFlow<ApiResult<List<RadioSuggestion>>?>(null)
    val pendingRadioSuggestions: StateFlow<ApiResult<List<RadioSuggestion>>?> = _pendingRadioSuggestions.asStateFlow()
    
    private val _createRadioSuggestionState = MutableStateFlow<ApiResult<RadioSuggestion>?>(null)
    val createRadioSuggestionState: StateFlow<ApiResult<RadioSuggestion>?> = _createRadioSuggestionState.asStateFlow()
    
    private val _myRadioSuggestions = MutableStateFlow<ApiResult<List<RadioSuggestion>>?>(null)
    val myRadioSuggestions: StateFlow<ApiResult<List<RadioSuggestion>>?> = _myRadioSuggestions.asStateFlow()
    
    private val _deleteSuggestionState = MutableStateFlow<ApiResult<MessageResponse>?>(null)
    val deleteSuggestionState: StateFlow<ApiResult<MessageResponse>?> = _deleteSuggestionState.asStateFlow()

    fun fetchMyRadioSuggestions() {
        viewModelScope.launch {
            try {
                _myRadioSuggestions.value = ApiResult.Loading
                val token = authManager.getToken() ?: run {
                    _myRadioSuggestions.value = ApiResult.Error("Not authenticated")
                    return@launch
                }
                val suggestions = apiService.getMySuggestions("Bearer $token")
                _myRadioSuggestions.value = ApiResult.Success(suggestions)
            } catch (e: Exception) {
                _myRadioSuggestions.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun deleteSuggestion(suggestionId: Int) {
        viewModelScope.launch {
            try {
                _deleteSuggestionState.value = ApiResult.Loading
                val token = authManager.getToken() ?: run {
                    _deleteSuggestionState.value = ApiResult.Error("Not authenticated")
                    return@launch
                }
                val response = apiService.deleteSuggestion("Bearer $token", suggestionId)
                _deleteSuggestionState.value = ApiResult.Success(response)
                
                // Refresh list
                fetchMyRadioSuggestions()
            } catch (e: Exception) {
                _deleteSuggestionState.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }
    
    fun resetDeleteSuggestionState() {
        _deleteSuggestionState.value = null
    }

    fun fetchAllRadioSuggestions() {
        viewModelScope.launch {
            try {
                _allRadioSuggestions.value = ApiResult.Loading
                val token = authManager.getToken() ?: run {
                    _allRadioSuggestions.value = ApiResult.Error("Not authenticated")
                    return@launch
                }
                val suggestions = apiService.getAllRadioSuggestions("Bearer $token")
                _allRadioSuggestions.value = ApiResult.Success(suggestions)
            } catch (e: Exception) {
                _allRadioSuggestions.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }
    
    fun fetchPendingRadioSuggestions() {
        viewModelScope.launch {
            try {
                _pendingRadioSuggestions.value = ApiResult.Loading
                val token = authManager.getToken() ?: run {
                    _pendingRadioSuggestions.value = ApiResult.Error("Not authenticated")
                    return@launch
                }
                val suggestions = apiService.getPendingRadioSuggestions("Bearer $token")
                _pendingRadioSuggestions.value = ApiResult.Success(suggestions)
            } catch (e: Exception) {
                _pendingRadioSuggestions.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }
    
    fun createRadioSuggestion(title: String, description: String, category: String) {
        viewModelScope.launch {
            try {
                _createRadioSuggestionState.value = ApiResult.Loading
                val token = authManager.getToken() ?: run {
                    _createRadioSuggestionState.value = ApiResult.Error("Not authenticated")
                    return@launch
                }
                
                val suggestion = apiService.createRadioSuggestion(
                    token = "Bearer $token",
                    suggestion = CreateRadioSuggestionRequest(
                        radio_title = title,
                        description = description,
                        category = category
                    )
                )
                _createRadioSuggestionState.value = ApiResult.Success(suggestion)
                
                // Refresh suggestions after creation
                fetchAllRadioSuggestions()
                
            } catch (e: Exception) {
                _createRadioSuggestionState.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }
    
    fun approveRadioSuggestion(suggestionId: Int) {
        viewModelScope.launch {
            try {
                val token = authManager.getToken() ?: return@launch
                apiService.approveSuggestion("Bearer $token", suggestionId)
                // Refresh pending suggestions
                fetchPendingRadioSuggestions()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun rejectRadioSuggestion(suggestionId: Int) {
        viewModelScope.launch {
            try {
                val token = authManager.getToken() ?: return@launch
                apiService.rejectSuggestion("Bearer $token", suggestionId)
                // Refresh pending suggestions
                fetchPendingRadioSuggestions()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun resetCreateRadioSuggestionState() {
        _createRadioSuggestionState.value = null
    }
}
