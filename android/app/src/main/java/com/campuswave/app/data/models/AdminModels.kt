package com.campuswave.app.data.models

data class AdminAnalyticsResponse(
    val total_users: Int,
    val users_by_role: Map<String, Int>,
    val total_radios: Int,
    val total_participants: Int,
    val total_favorites: Int,
    val total_update_views: Int = 0,
    val total_update_likes: Int = 0,
    val total_update_comments: Int = 0,
    val total_placements: Int = 0,
    val registration_trend: List<TrendItem>,
    val top_radios: List<TopRadioItem>,
    val top_updates: List<TopUpdateItem> = emptyList(),
    val departmental_context: String?
)

data class TopUpdateItem(
    val id: Int,
    val caption: String,
    val view_count: Int,
    val like_count: Int,
    val comment_count: Int
)

data class TrendItem(
    val date: String,
    val count: Int
)

data class TopRadioItem(
    val id: Int,
    val title: String,
    val participant_count: Int,
    val favorite_count: Int
)

data class UpdateAnalyticsResponse(
    val id: Int,
    val caption: String,
    val created_at: String,
    val views: Int,
    val likes: Int,
    val comments: Int
)
