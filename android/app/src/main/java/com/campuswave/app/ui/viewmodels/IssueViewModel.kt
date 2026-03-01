package com.campuswave.app.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campuswave.app.data.auth.AuthManager
import com.campuswave.app.data.models.*
import com.campuswave.app.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing issues (student reporting, admin management, chat)
 */
class IssueViewModel(private val context: Context) : ViewModel() {
    
    private val authManager = AuthManager(context)
    private val api = RetrofitClient.apiService
    
    // Issue list state
    private val _issues = MutableStateFlow<List<Issue>>(emptyList())
    val issues: StateFlow<List<Issue>> = _issues.asStateFlow()
    
    private val _resolvedIssues = MutableStateFlow<List<Issue>>(emptyList())
    val resolvedIssues: StateFlow<List<Issue>> = _resolvedIssues.asStateFlow()
    
    // Current issue details (with messages)
    private val _currentIssue = MutableStateFlow<Issue?>(null)
    val currentIssue: StateFlow<Issue?> = _currentIssue.asStateFlow()
    
    // Stats for admin dashboard
    private val _issueStats = MutableStateFlow<IssueStats?>(null)
    val issueStats: StateFlow<IssueStats?> = _issueStats.asStateFlow()
    
    // Loading states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()
    
    private val _isSendingMessage = MutableStateFlow(false)
    val isSendingMessage: StateFlow<Boolean> = _isSendingMessage.asStateFlow()
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Success message
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    /**
     * Create a new issue (student only)
     */
    fun createIssue(title: String, description: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null
            
            try {
                val token = authManager.getToken()
                if (token == null) {
                    _error.value = "Not authenticated"
                    return@launch
                }
                
                val request = CreateIssueRequest(title.trim(), description.trim())
                val response = api.createIssue("Bearer $token", request)
                
                _successMessage.value = response.message ?: "Issue submitted successfully"
                // Refresh issues list
                loadMyIssues()
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to submit issue"
            } finally {
                _isSubmitting.value = false
            }
        }
    }
    
    /**
     * Load student's own issues
     */
    fun loadMyIssues() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val token = authManager.getToken()
                if (token == null) {
                    _error.value = "Not authenticated"
                    return@launch
                }
                
                val issuesList = api.getMyIssues("Bearer $token")
                _issues.value = issuesList
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load issues"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load all open/in-discussion issues (admin only)
     */
    fun loadAllIssues() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val token = authManager.getToken()
                if (token == null) {
                    _error.value = "Not authenticated"
                    return@launch
                }
                
                val issuesList = api.getAllIssues("Bearer $token")
                _issues.value = issuesList
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load issues"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load resolved issues (admin only)
     */
    fun loadResolvedIssues() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val token = authManager.getToken()
                if (token == null) {
                    _error.value = "Not authenticated"
                    return@launch
                }
                
                val issuesList = api.getResolvedIssues("Bearer $token")
                _resolvedIssues.value = issuesList
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load resolved issues"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load issue details with messages
     */
    fun loadIssueDetails(issueId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val token = authManager.getToken()
                if (token == null) {
                    _error.value = "Not authenticated"
                    return@launch
                }
                
                val issue = api.getIssueDetails("Bearer $token", issueId)
                _currentIssue.value = issue
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load issue details"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Send a message in the issue chat
     */
    fun sendMessage(issueId: Int, message: String) {
        viewModelScope.launch {
            _isSendingMessage.value = true
            _error.value = null
            
            try {
                val token = authManager.getToken()
                if (token == null) {
                    _error.value = "Not authenticated"
                    return@launch
                }
                
                val request = SendIssueMessageRequest(message.trim())
                api.sendIssueMessage("Bearer $token", issueId, request)
                
                // Refresh issue details to get updated messages
                loadIssueDetails(issueId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to send message"
            } finally {
                _isSendingMessage.value = false
            }
        }
    }
    
    /**
     * Mark issue as resolved (admin only)
     */
    fun resolveIssue(issueId: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null
            
            try {
                val token = authManager.getToken()
                if (token == null) {
                    _error.value = "Not authenticated"
                    return@launch
                }
                
                val response = api.resolveIssue("Bearer $token", issueId)
                _successMessage.value = response.message ?: "Issue resolved successfully"
                
                // Update current issue if it's the one being resolved
                if (_currentIssue.value?.id == issueId) {
                    _currentIssue.value = response.issue
                }
                
                // Refresh lists
                loadAllIssues()
                loadResolvedIssues()
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to resolve issue"
            } finally {
                _isSubmitting.value = false
            }
        }
    }
    
    /**
     * Load issue stats for admin dashboard
     */
    fun loadIssueStats() {
        viewModelScope.launch {
            try {
                val token = authManager.getToken()
                if (token != null) {
                    val stats = api.getIssueStats("Bearer $token")
                    _issueStats.value = stats
                }
            } catch (e: Exception) {
                // Stats loading failure is non-critical
            }
        }
    }
    
    /**
     * Delete an issue (admin or student)
     */
    fun deleteIssue(issueId: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val token = authManager.getToken()
                if (token == null) {
                    _error.value = "Not authenticated"
                    return@launch
                }
                
                val response = api.deleteIssue("Bearer $token", issueId)
                _successMessage.value = response.message
                // Refresh all issue lists and stats
                loadMyIssues()
                loadAllIssues()
                loadResolvedIssues()
                loadIssueStats()
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete issue"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear current issue (when navigating away)
     */
    fun clearCurrentIssue() {
        _currentIssue.value = null
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}
