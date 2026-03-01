package com.campuswave.app.data.models

data class User(
    val id: Int,
    val email: String,
    val name: String,
    val role: String,
    val created_at: String? = null,
    val profile_picture: String? = null,
    val college_pin: String? = null,
    val department: String? = null,
    val reason_for_access: String? = null,
    val year: String? = null,
    val branch: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val role: String = "STUDENT",
    val phone_number: String? = null,
    val department: String? = null,
    val reason_for_access: String? = null
)

data class LoginResponse(
    val access_token: String? = null,
    val user: User? = null,
    val verification_required: Boolean = false,
    val email: String? = null,
    val message: String? = null,
    val error: String? = null
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class ResendOtpRequest(
    val email: String
)

data class OtpResponse(
    val message: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val password: String
)

data class UpdateProfileRequest(
    val name: String? = null,
    val college_pin: String? = null,
    val department: String? = null,
    val year: String? = null,
    val branch: String? = null
)

data class ProfilePictureResponse(
    val message: String,
    val profile_picture: String
)

data class ReportRequest(
    val issue_type: String,
    val description: String?,
    val session_id: Int? = null
)

data class ReportResponse(
    val message: String,
    val report: Map<String, Any>?
)

data class VerifyResetOtpRequest(
    val otp: String
)

data class VerifyResetOtpResponse(
    val message: String,
    val reset_token: String
)

data class CompleteResetRequest(
    val new_password: String
)
