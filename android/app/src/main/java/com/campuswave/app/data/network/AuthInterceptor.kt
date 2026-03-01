package com.campuswave.app.data.network

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenProvider: suspend () -> String?) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Get token synchronously (required by Interceptor interface)
        val token = runBlocking {
            tokenProvider()
        }
        
        // If no token, proceed without auth header
        if (token == null) {
            return chain.proceed(originalRequest)
        }
        
        // Add Authorization header with Bearer token
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        
        return chain.proceed(authenticatedRequest)
    }
}
