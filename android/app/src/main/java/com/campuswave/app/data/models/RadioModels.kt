package com.campuswave.app.data.models

data class Radio(
    val id: Int,
    val title: String,
    val description: String?,
    val banner_image: String?,
    val media_url: String? = null,
    val recording_url: String? = null,
    val location: String?,
    val start_time: String,
    val end_time: String,
    val status: String,
    val created_by: Int,
    val participant_count: Int = 0,
    val favorite_count: Int = 0,
    val created_at: String? = null,
    val updated_at: String? = null,
    // Live hosting fields
    val media_type: String = "NONE",
    val host_status: String = "NOT_STARTED",
    val hosted_by: Int? = null,
    val stream_started_at: String? = null,
    // Category fields
    val category_id: Int? = null,
    val category: Category? = null,
    val is_favorited: Boolean = false,
    val is_subscribed: Boolean = false,
    // Timer synchronization fields
    val last_resumed_at: String? = null,
    val accumulated_duration: Int = 0,
    val current_duration_seconds: Int = 0,
    val server_time: String? = null
)

data class CreateRadioRequest(
    val title: String,
    val description: String,
    val location: String,
    val start_time: String,
    val end_time: String,
    val status: String = "UPCOMING",
    val media_url: String? = null,
    val category_id: Int? = null
)

data class RadioSuggestion(
    val id: Int,
    val radio_title: String,
    val description: String?,
    val category: String? = null,
    val suggested_by: Int,
    val status: String,
    val reviewed_by: Int?,
    val created_at: String,
    val reviewed_at: String?,
    val rejection_reason: String? = null,
    val student_name: String? = null,
    val student_email: String? = null
)

data class CreateRadioSuggestionRequest(
    val radio_title: String,
    val description: String,
    val category: String? = null
)

data class DashboardStats(
    val total_radios: Int,
    val active_participants: Int,
    val pending_suggestions: Int,
    val pending_admin_requests: Int = 0,
    val role: String? = null
)

// ==================== New Feature Models ====================

data class Category(
    val id: Int,
    val name: String,
    val color: String = "#5E72E4",
    val icon: String = "radio"
)

data class Comment(
    val id: Int,
    val radio_id: Int,
    val user_id: Int,
    val user_name: String,
    val content: String,
    val created_at: String?
)

data class CommentRequest(
    val content: String
)

data class CommentsResponse(
    val comments: List<Comment>,
    val total: Int,
    val page: Int,
    val pages: Int
)

data class CollegeUpdateComment(
    val id: Int,
    val update_id: Int,
    val user_id: Int,
    val user_name: String,
    val content: String,
    val created_at: String?
)

data class CollegeUpdateCommentsResponse(
    val comments: List<CollegeUpdateComment>
)

data class FavoriteToggleResponse(
    val message: String,
    val is_favorited: Boolean
)

// Analytics models
data class AnalyticsOverview(
    val totals: TotalStats,
    val radios: RadioStats,
    val recent: RecentStats
)

data class TotalStats(
    val radios: Int,
    val users: Int,
    val students: Int,
    val admins: Int,
    val comments: Int,
    val favorites: Int
)

data class RadioStats(
    val live: Int,
    val upcoming: Int,
    val completed: Int
)

data class RecentStats(
    val radios_this_week: Int,
    val radios_this_month: Int,
    val users_this_week: Int,
    val comments_this_week: Int
)

data class RadioAnalytics(
    val top_favorited: List<TopRadio>,
    val top_commented: List<TopRadio>,
    val by_category: List<CategoryCount>,
    val by_status: List<StatusCount>
)

data class TopRadio(
    val id: Int,
    val title: String,
    val favorites: Int? = null,
    val comments: Int? = null
)

data class CategoryCount(
    val category: String,
    val count: Int
)

data class StatusCount(
    val status: String,
    val count: Int
)

data class TrendsData(
    val daily_radios: List<DailyCount>,
    val daily_users: List<DailyCount>
)

data class DailyCount(
    val date: String,
    val count: Int
)

// ==================== Podcast Models ====================

data class Podcast(
    val id: Int,
    val title: String,
    val description: String?,
    val scheduled_start_time: String,
    val status: String,  // SCHEDULED, LIVE, ENDED
    val is_muted: Boolean = false,
    val created_by: Int,
    val started_at: String? = null,
    val ended_at: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val viewer_count: Int = 0,
    val creator_name: String? = null,
    val creator_email: String? = null
)

data class CreatePodcastRequest(
    val title: String,
    val description: String,
    val scheduled_start_time: String
)

data class HandRaise(
    val id: Int,
    val podcast_id: Int,
    val user_id: Int,
    val user_name: String? = null,
    val user_email: String? = null,
    val status: String,  // PENDING, ACCEPTED, IGNORED
    val created_at: String?,
    val responded_at: String? = null
)

// Podcast API responses
data class PodcastResponse(
    val message: String? = null,
    val podcast: Podcast? = null
)

data class PodcastListResponse(
    val podcasts: List<Podcast>
)

data class ViewerCountResponse(
    val viewer_count: Int
)

data class HandRaiseListResponse(
    val hand_raises: List<HandRaise>,
    val count: Int
)

data class HandRaiseStatusResponse(
    val status: String?,
    val hand_raise: HandRaise?
)

