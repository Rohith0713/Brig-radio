package com.campuswave.app.data.models

data class CollegeUpdate(
    val id: Int,
    val image_url: String,
    val media_type: String = "IMAGE",
    val caption: String,
    val created_at: String,
    val like_count: Int,
    val is_liked: Boolean,
    val comment_count: Int,
    val view_count: Int,
    val is_saved: Boolean = false
)

data class CollegeUpdatesResponse(
    val updates: List<CollegeUpdate>,
    val total: Int,
    val page: Int,
    val pages: Int
)

data class LikeResponse(
    val message: String,
    val is_liked: Boolean,
    val like_count: Int
)
