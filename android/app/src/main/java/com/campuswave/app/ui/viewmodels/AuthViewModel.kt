package com.campuswave.app.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campuswave.app.data.auth.AuthManager
import com.campuswave.app.data.models.*
import com.campuswave.app.data.network.RetrofitClient
import com.campuswave.app.utils.ApiResult
import com.campuswave.app.utils.ErrorHandler
import com.campuswave.app.utils.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class AuthViewModel(private val context: Context) : ViewModel() {
    
    private val authManager = AuthManager(context)
    private val apiService = RetrofitClient.apiService
    
    private val _loginState = MutableStateFlow<ApiResult<String>?>(null)
    val loginState: StateFlow<ApiResult<String>?> = _loginState.asStateFlow()
    
    private val _registerState = MutableStateFlow<ApiResult<String>?>(null)
    val registerState: StateFlow<ApiResult<String>?> = _registerState.asStateFlow()

    private val _otpState = MutableStateFlow<ApiResult<String>?>(null)
    val otpState: StateFlow<ApiResult<String>?> = _otpState.asStateFlow()

    private val _verificationNeeded = MutableStateFlow<String?>(null)
    val verificationNeeded: StateFlow<String?> = _verificationNeeded.asStateFlow()

    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _loginState.value = ApiResult.Loading
                
                val response = apiService.login(
                    LoginRequest(email = email.lowercase(), password = password)
                )
                
                if (response.verification_required == true) {
                    _verificationNeeded.value = response.email ?: email
                    _loginState.value = ApiResult.Error("Verification Required") // Or handle differently in UI
                    return@launch
                }

                if (response.access_token != null && response.user != null) {
                    // Save auth data
                    authManager.saveAuthData(
                        token = response.access_token,
                        userId = response.user.id.toString(),
                        name = response.user.name,
                        email = response.user.email,
                        role = response.user.role,
                        profilePicture = response.user.profile_picture
                    )
                    _loginState.value = ApiResult.Success(response.user.role)
                } else {
                     _loginState.value = ApiResult.Error("Invalid response from server")
                }
                
            } catch (e: Exception) {
                // Check if it's a 403 Verification Required error (needs custom error parsing if Retrofit throws)
                // For now, assume standard error handling
                _loginState.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }
    
    fun register(
        name: String, 
        email: String, 
        password: String, 
        role: String, 
        phoneNumber: String?,
        department: String? = null,
        reason: String? = null
    ) {
        viewModelScope.launch {
            try {
                _registerState.value = ApiResult.Loading
                
                val response = apiService.register(
                    RegisterRequest(
                        name = name,
                        email = email.lowercase(),
                        password = password,
                        role = role,
                        phone_number = phoneNumber,
                        department = department,
                        reason_for_access = reason
                    )
                )
                
                if (response.verification_required == true) {
                     _verificationNeeded.value = response.email ?: email
                     // We don't set Success yet, we wait for OTP
                     _registerState.value = ApiResult.Success("VERIFY") 
                } else if (response.access_token != null && response.user != null) {
                     // Save auth data
                    authManager.saveAuthData(
                        token = response.access_token,
                        userId = response.user.id.toString(),
                        name = response.user.name,
                        email = response.user.email,
                        role = response.user.role,
                        profilePicture = response.user.profile_picture
                    )
                    _registerState.value = ApiResult.Success(response.user.role)
                } else if (response.message != null) {
                    // This is for Admin requests which don't login immediately
                    _registerState.value = ApiResult.Success(response.message)
                } else {
                    _registerState.value = ApiResult.Error("Unexpected response")
                }
                
            } catch (e: Exception) {
                _registerState.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun verifyOtp(email: String, otp: String) {
        viewModelScope.launch {
            try {
                _otpState.value = ApiResult.Loading
                
                val response = apiService.verifyOtp(
                    VerifyOtpRequest(email = email.lowercase(), otp = otp)
                )
                
                if (response.access_token != null && response.user != null) {
                    authManager.saveAuthData(
                        token = response.access_token,
                        userId = response.user.id.toString(),
                        name = response.user.name,
                        email = response.user.email,
                        role = response.user.role,
                        profilePicture = response.user.profile_picture
                    )
                    _otpState.value = ApiResult.Success(response.user.role)
                    _verificationNeeded.value = null // Clear needed state
                } else {
                    _otpState.value = ApiResult.Error("Verification failed")
                }
                
            } catch (e: Exception) {
                _otpState.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun resendOtp(email: String) {
        viewModelScope.launch {
             try {
                val response = apiService.resendOtp(ResendOtpRequest(email = email.lowercase()))
                // Optionally show toast or update state
             } catch (e: Exception) {
                 e.printStackTrace()
             }
        }
    }

    fun resetVerificationNeeded() {
        _verificationNeeded.value = null
    }

    fun resetOtpState() {
        _otpState.value = null
    }
    
    private val _forgotPasswordState = MutableStateFlow<ApiResult<String>?>(null)
    val forgotPasswordState: StateFlow<ApiResult<String>?> = _forgotPasswordState.asStateFlow()

    private val _resetPasswordState = MutableStateFlow<ApiResult<String>?>(null)
    val resetPasswordState: StateFlow<ApiResult<String>?> = _resetPasswordState.asStateFlow()

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            try {
                _forgotPasswordState.value = ApiResult.Loading
                val response = apiService.forgotPassword(ForgotPasswordRequest(email.lowercase()))
                _forgotPasswordState.value = ApiResult.Success(response.message)
            } catch (e: Exception) {
                _forgotPasswordState.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun verifyResetOtp(email: String, otp: String) {
        viewModelScope.launch {
            try {
                _otpState.value = ApiResult.Loading
                val response = apiService.verifyResetOtp(VerifyOtpRequest(email.lowercase(), otp))
                _otpState.value = ApiResult.Success(response.message)
            } catch (e: Exception) {
                _otpState.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun resetPassword(email: String, otp: String, password: String) {
        viewModelScope.launch {
            try {
                _resetPasswordState.value = ApiResult.Loading
                val response = apiService.resetPassword(ResetPasswordRequest(email.lowercase(), otp, password))
                _resetPasswordState.value = ApiResult.Success(response.message)
            } catch (e: Exception) {
                _resetPasswordState.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = null
    }

    fun resetResetPasswordState() {
        _resetPasswordState.value = null
    }

    suspend fun logoutSync() {
        authManager.clearAuthData()
    }
    
    fun resetLoginState() {
        _loginState.value = null
    }
    
    fun resetRegisterState() {
        _registerState.value = null
    }

    // Profile Password Reset Flow
    private val _resetRequestState = MutableStateFlow<ApiResult<String>?>(null)
    val resetRequestState: StateFlow<ApiResult<String>?> = _resetRequestState.asStateFlow()

    private val _verifyResetState = MutableStateFlow<ApiResult<String>?>(null) // Success returns reset_token
    val verifyResetState: StateFlow<ApiResult<String>?> = _verifyResetState.asStateFlow()

    private val _completeResetState = MutableStateFlow<ApiResult<String>?>(null)
    val completeResetState: StateFlow<ApiResult<String>?> = _completeResetState.asStateFlow()

    fun startPasswordReset() {
        viewModelScope.launch {
            try {
                val token = authManager.getToken()
                if (token == null) {
                    _resetRequestState.value = ApiResult.Error("Not authenticated")
                    return@launch
                }
                
                _resetRequestState.value = ApiResult.Loading
                val response = apiService.requestPasswordReset("Bearer $token")
                _resetRequestState.value = ApiResult.Success(response.message)
            } catch (e: Exception) {
                _resetRequestState.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun verifyProfileResetOtp(otp: String) {
        viewModelScope.launch {
            try {
                val token = authManager.getToken()
                 if (token == null) {
                    _verifyResetState.value = ApiResult.Error("Not authenticated")
                    return@launch
                }
                
                _verifyResetState.value = ApiResult.Loading
                val response = apiService.verifyProfileResetOtp(
                    "Bearer $token",
                    VerifyResetOtpRequest(otp)
                )
                // Pass the reset token as the success message/value
                _verifyResetState.value = ApiResult.Success(response.reset_token)
            } catch (e: Exception) {
                _verifyResetState.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun completeProfilePasswordReset(resetToken: String, newPassword: String) {
        viewModelScope.launch {
            try {
                _completeResetState.value = ApiResult.Loading
                val response = apiService.completePasswordReset(
                    "Bearer $resetToken",
                    CompleteResetRequest(newPassword)
                )
                _completeResetState.value = ApiResult.Success(response.message)
            } catch (e: Exception) {
                _completeResetState.value = ApiResult.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun clearResetFlowStates() {
        _resetRequestState.value = null
        _verifyResetState.value = null
        _completeResetState.value = null
    }
}
