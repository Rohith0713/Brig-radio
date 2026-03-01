package com.campuswave.app.utils

object UrlUtils {
    /**
     * Joins base URL and path safely, ensuring exactly one slash between them.
     * Also handles null or empty inputs.
     */
    fun joinUrl(baseUrl: String, path: String?): String? {
        if (path.isNullOrEmpty()) return null
        
        val cleanBase = baseUrl.trim().removeSuffix("/")
        val cleanPath = path.trim().removePrefix("/")
        
        return "$cleanBase/$cleanPath"
    }
}
