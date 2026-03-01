package com.campuswave.app.data.models

data class Notification(
    val id: Int,
    val title: String,
    val message: String,
    val type: String, // 'RADIO_LIVE', 'SUGGESTION_APPROVED', 'GENERAL'
    val related_id: Int?,
    val is_read: Boolean,
    val created_at: String,
    val time_ago: String
)

data class NotificationsResponse(
    val notifications: List<Notification>,
    val total: Int,
    val page: Int,
    val pages: Int
)
