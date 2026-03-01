package com.campuswave.app.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campuswave.app.data.auth.AuthManager
import com.campuswave.app.data.models.User
import com.campuswave.app.data.network.RetrofitClient
import com.campuswave.app.utils.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class ProfileViewModel(context: Context) : ViewModel() {
    private val authManager = AuthManager(context)
    
    private val _userProfile = MutableStateFlow<ApiResult<User>>(ApiResult.Loading)
    val userProfile: StateFlow<ApiResult<User>> = _userProfile.asStateFlow()

    fun fetchUserProfile() {
        viewModelScope.launch {
            // Only set loading if we don't have data yet
            if (_userProfile.value !is ApiResult.Success) {
                _userProfile.value = ApiResult.Loading
            }
            try {
                val token = authManager.getToken()
                if (token != null) {
                    val user = RetrofitClient.apiService.getCurrentUser("Bearer $token")
                    
                    // Only update if data changed
                    val current = (_userProfile.value as? ApiResult.Success)?.data
                    if (current == null || current != user) {
                        _userProfile.value = ApiResult.Success(user)
                    }
                    
                    // Sync profile picture to local storage for sidebar visibility
                    user.profile_picture?.let { authManager.saveProfilePicture(it) }
                } else {
                    _userProfile.value = ApiResult.Error("Not logged in")
                }
            } catch (e: Exception) {
                _userProfile.value = ApiResult.Error(e.message ?: "Failed to fetch profile")
            }
        }
    }
    
    fun updateProfile(name: String, collegePin: String, department: String, year: String, branch: String) {
        viewModelScope.launch {
            try {
                val token = authManager.getToken()
                if (token != null) {
                    val request = com.campuswave.app.data.models.UpdateProfileRequest(
                        name = name,
                        college_pin = collegePin,
                        department = department,
                        year = year,
                        branch = branch
                    )
                    val updatedUser = RetrofitClient.apiService.updateProfile("Bearer $token", request)
                    _userProfile.value = ApiResult.Success(updatedUser)
                    
                    // Update locally stored data if changed
                    authManager.saveAuthData(
                        token = token,
                        userId = updatedUser.id.toString(),
                        name = updatedUser.name,
                        email = updatedUser.email,
                        role = updatedUser.role,
                        profilePicture = updatedUser.profile_picture,
                        collegePin = updatedUser.college_pin,
                        department = updatedUser.department,
                        year = updatedUser.year,
                        branch = updatedUser.branch
                    )
                }
            } catch (e: Exception) {
                // Handle error (could add a separate error state for actions)
                e.printStackTrace()
            }
        }
    }
    
    fun uploadProfilePicture(uri: android.net.Uri, contentResolver: android.content.ContentResolver) {
        viewModelScope.launch {
            try {
                val token = authManager.getToken()
                if (token != null) {
                    val file = prepareFilePart(uri, contentResolver)
                    if (file != null) {
                        val response = RetrofitClient.apiService.uploadProfilePicture("Bearer $token", file)
                        
                        // Refetch profile to get the new picture URL
                        fetchUserProfile()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun prepareFilePart(uri: android.net.Uri, contentResolver: android.content.ContentResolver): okhttp3.MultipartBody.Part? {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            
            if (bytes != null) {
                val mediaType = (contentResolver.getType(uri) ?: "image/*").toMediaTypeOrNull()
                val requestBody = bytes.toRequestBody(mediaType)
                return okhttp3.MultipartBody.Part.createFormData("picture", "profile_pic.jpg", requestBody)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            authManager.clearAuthData()
            onLogoutComplete()
        }
    }
}
