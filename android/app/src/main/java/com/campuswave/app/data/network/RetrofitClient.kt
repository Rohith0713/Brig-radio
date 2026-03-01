package com.campuswave.app.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private var authInterceptor: Interceptor? = null
    
    fun setAuthInterceptor(interceptor: Interceptor) {
        authInterceptor = interceptor
    }
    
    private fun getOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
        
        // Add auth interceptor if available
        authInterceptor?.let {
            builder.addInterceptor(it)
        }
        
        return builder.build()
    }
    
    private val retrofit: Retrofit
        get() = Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    
    val apiService: BRIG_RADIOApiService
        get() = retrofit.create(BRIG_RADIOApiService::class.java)
}
