package com.campuswave.app.utils

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

object ErrorHandler {
    
    fun getErrorMessage(exception: Exception): String {
        return when (exception) {
            is HttpException -> {
                parseErrorMessage(exception) ?: when (exception.code()) {
                    401 -> "Authentication failed. Please login again."
                    403 -> "You don't have permission to access this resource."
                    404 -> "Resource not found."
                    500 -> "Server error. Please try again later."
                    else -> "Error: ${exception.message()}"
                }
            }
            is SocketTimeoutException -> {
                "Connection timeout. Please check your internet connection."
            }
            is IOException -> {
                "Network error. Please check your internet connection."
            }
            else -> {
                exception.message ?: "An unexpected error occurred."
            }
        }
    }

    private fun parseErrorMessage(exception: HttpException): String? {
        return try {
            val errorBody = exception.response()?.errorBody()?.string()
            if (errorBody != null) {
                val jsonObject = org.json.JSONObject(errorBody)
                jsonObject.optString("error", null) ?: jsonObject.optString("message", null)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
