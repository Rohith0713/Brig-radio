package com.campuswave.app.data.network

import com.campuswave.app.data.models.*
import com.campuswave.app.utils.ApiResult
import retrofit2.http.*
import retrofit2.Response

interface BRIG_RADIOApiService {
    
    // ==================== Authentication ====================
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): LoginResponse
    
    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): LoginResponse
    
    @POST("auth/resend-otp")
    suspend fun resendOtp(@Body request: ResendOtpRequest): OtpResponse
    
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): OtpResponse
    
    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): MessageResponse

    // Password Reset Flow
    
    @POST("auth/request-password-reset")
    suspend fun requestPasswordReset(@Header("Authorization") token: String): MessageResponse
    
    @POST("auth/verify-reset-otp")
    suspend fun verifyResetOtp(@Body request: VerifyOtpRequest): MessageResponse
    
    @POST("auth/verify-profile-reset-otp")
    suspend fun verifyProfileResetOtp(
        @Header("Authorization") token: String,
        @Body request: VerifyResetOtpRequest
    ): VerifyResetOtpResponse
    
    @POST("auth/complete-password-reset")
    suspend fun completePasswordReset(
        @Header("Authorization") resetToken: String,
        @Body request: CompleteResetRequest
    ): MessageResponse
    
    @GET("auth/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): User
    
    @PATCH("auth/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): User
    
    @Multipart
    @POST("auth/profile/picture")
    suspend fun uploadProfilePicture(
        @Header("Authorization") token: String,
        @Part picture: okhttp3.MultipartBody.Part
    ): ProfilePictureResponse

    @GET("auth/admin-requests")
    suspend fun getAdminRequests(
        @Header("Authorization") token: String
    ): List<User>

    @POST("auth/approve-admin/{admin_id}")
    suspend fun approveAdmin(
        @Header("Authorization") token: String,
        @Path("admin_id") adminId: Int,
        @Body request: Map<String, String>
    ): MessageResponse

    @GET("admin/analytics")
    suspend fun getAnalytics(
        @Header("Authorization") token: String
    ): AdminAnalyticsResponse

    @GET("college-updates/{id}/analytics")
    suspend fun getUpdateAnalytics(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): UpdateAnalyticsResponse
    
    // ==================== Reports ====================
    @POST("reports/")
    suspend fun createReport(@Body report: ReportRequest): ApiResult<ReportResponse>
    
    // ==================== Radios ====================
    @GET("radios")
    suspend fun getAllRadios(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("category_id") categoryId: Int? = null,
        @Query("status") status: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null
    ): RadiosResponse
    
    @GET("radios/live")
    suspend fun getLiveRadios(): List<Radio>
    
    @GET("radios/upcoming")
    suspend fun getUpcomingRadios(): List<Radio>
    
    @GET("radios/{id}")
    suspend fun getRadioDetails(@Path("id") radioId: Int): Radio
    
    @POST("radios")
    suspend fun createRadio(
        @Header("Authorization") token: String,
        @Body radio: CreateRadioRequest
    ): Radio
    
    @PUT("radios/{id}")
    suspend fun updateRadio(
        @Header("Authorization") token: String,
        @Path("id") radioId: Int,
        @Body radio: CreateRadioRequest
    ): Radio
    
    @DELETE("radios/{id}")
    suspend fun deleteRadio(
        @Header("Authorization") token: String,
        @Path("id") radioId: Int
    ): MessageResponse
    
    @GET("radios/missed")
    suspend fun getPastRadios(): List<Radio>
    
    @Multipart
    @POST("radios/{id}/upload-media")
    suspend fun uploadRadioMedia(
        @Header("Authorization") token: String,
        @Path("id") radioId: Int,
        @Part media: okhttp3.MultipartBody.Part
    ): MediaUploadResponse
    
    @Multipart
    @POST("radios/{id}/upload-banner")
    suspend fun uploadRadioBanner(
        @Header("Authorization") token: String,
        @Path("id") radioId: Int,
        @Part banner: okhttp3.MultipartBody.Part
    ): BannerUploadResponse
    
    // ==================== Categories ====================
    @GET("categories")
    suspend fun getCategories(): List<Category>
    
    @GET("categories/{id}")
    suspend fun getCategory(@Path("id") categoryId: Int): Category
    
    @POST("categories")
    suspend fun createCategory(
        @Header("Authorization") token: String,
        @Body category: Category
    ): Category
    
    // ==================== Favorites ====================
    @GET("favorites")
    suspend fun getFavorites(
        @Header("Authorization") token: String
    ): List<Radio>
    
    @POST("radios/{id}/favorite/toggle")
    suspend fun toggleFavorite(
        @Header("Authorization") token: String,
        @Path("id") radioId: Int
    ): FavoriteToggleResponse
    
    @POST("radios/{id}/subscribe")
    suspend fun toggleSubscription(
        @Header("Authorization") token: String,
        @Path("id") radioId: Int
    ): SubscriptionToggleResponse
    
    @POST("radios/{id}/favorite")
    suspend fun addFavorite(
        @Header("Authorization") token: String,
        @Path("id") radioId: Int
    ): MessageResponse
    
    @DELETE("radios/{id}/favorite")
    suspend fun removeFavorite(
        @Header("Authorization") token: String,
        @Path("id") radioId: Int
    ): MessageResponse
    
    // ==================== Comments ====================
    @GET("radios/{id}/comments")
    suspend fun getComments(
        @Path("id") radioId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): CommentsResponse
    
    @GET("radios/{id}/comments/recent")
    suspend fun getRecentComments(
        @Path("id") radioId: Int,
        @Query("limit") limit: Int = 20,
        @Query("since") since: String? = null
    ): List<Comment>
    
    @POST("radios/{id}/comments")
    suspend fun addComment(
        @Header("Authorization") token: String,
        @Path("id") radioId: Int,
        @Body comment: CommentRequest
    ): Comment
    
    @DELETE("comments/{id}")
    suspend fun deleteComment(
        @Header("Authorization") token: String,
        @Path("id") commentId: Int
    ): MessageResponse
    
    // ==================== Analytics ====================
    @GET("analytics/overview")
    suspend fun getAnalyticsOverview(
        @Header("Authorization") token: String
    ): AnalyticsOverview
    
    @GET("analytics/radios")
    suspend fun getRadioAnalytics(
        @Header("Authorization") token: String
    ): RadioAnalytics
    
    @GET("analytics/trends")
    suspend fun getTrends(
        @Header("Authorization") token: String
    ): TrendsData
    
    // ==================== Suggestions ====================
    @GET("suggestions")
    suspend fun getAllRadioSuggestions(
        @Header("Authorization") token: String
    ): List<RadioSuggestion>
    
    @GET("suggestions/pending")
    suspend fun getPendingRadioSuggestions(
        @Header("Authorization") token: String
    ): List<RadioSuggestion>
    
    @POST("suggestions")
    suspend fun createRadioSuggestion(
        @Header("Authorization") token: String,
        @Body suggestion: CreateRadioSuggestionRequest
    ): RadioSuggestion
    
    @PUT("suggestions/{id}/approve")
    suspend fun approveSuggestion(
        @Header("Authorization") token: String,
        @Path("id") suggestionId: Int
    ): ApproveResponse
    
    @PUT("suggestions/{id}/reject")
    suspend fun rejectSuggestion(
        @Header("Authorization") token: String,
        @Path("id") suggestionId: Int
    ): MessageResponse
    
    @GET("suggestions/my")
    suspend fun getMySuggestions(
        @Header("Authorization") token: String
    ): List<RadioSuggestion>
    
    @DELETE("suggestions/{id}")
    suspend fun deleteSuggestion(
        @Header("Authorization") token: String,
        @Path("id") suggestionId: Int
    ): MessageResponse
    
    // ==================== Dashboard ====================
    @GET("dashboard/stats")
    suspend fun getDashboardStats(
        @Header("Authorization") token: String
    ): DashboardStats
    
    // ==================== Live Hosting ====================
    @POST("radios/{id}/start-hosting")
    suspend fun startHosting(
        @Header("Authorization") token: String,
        @Path("id") radioId: Int,
        @Body request: StartHostingRequest
    ): HostingResponse
    
    @PUT("radios/{id}/pause-hosting")
    suspend fun pauseHosting(
        @Header("Authorization") token: String,
        @Path("id") radioId: Int
    ): HostingResponse
    
    @PUT("radios/{id}/resume-hosting")
    suspend fun resumeHosting(
        @Header("Authorization") token: String,
        @Path("id") radioId: Int
    ): HostingResponse
    
    @PUT("radios/{id}/end-hosting")
    suspend fun endHosting(
        @Header("Authorization") token: String,
        @Path("id") radioId: Int
    ): HostingResponse
    
    @GET("radios/{id}/stream-info")
    suspend fun getStreamInfo(
        @Path("id") radioId: Int
    ): StreamInfoResponse

    // ==================== College Updates ====================
    @Multipart
    @POST("college-updates")
    suspend fun createCollegeUpdate(
        @Header("Authorization") token: String,
        @Part media: okhttp3.MultipartBody.Part,
        @Part("caption") caption: okhttp3.RequestBody
    ): Response<Map<String, Any>>

    @GET("college-updates")
    suspend fun getCollegeUpdates(
        @Header("Authorization") token: String?,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): CollegeUpdatesResponse

    @DELETE("college-updates/{id}")
    suspend fun deleteCollegeUpdate(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): MessageResponse

    @POST("college-updates/{id}/like")
    suspend fun toggleLike(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): LikeResponse

    @POST("college-updates/{id}/view")
    suspend fun recordUpdateView(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Map<String, Any>

    @GET("college-updates/{id}/comments")
    suspend fun getUpdateComments(
        @Header("Authorization") token: String?,
        @Path("id") id: Int
    ): CollegeUpdateCommentsResponse

    @POST("college-updates/{id}/comment")
    suspend fun addUpdateComment(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: CommentRequest
    ): CollegeUpdateComment

    // ==================== Notifications ====================
    @GET("notifications")
    suspend fun getNotifications(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): NotificationsResponse
    
    @PUT("notifications/{id}/read")
    suspend fun markNotificationRead(
         @Header("Authorization") token: String,
         @Path("id") notificationId: Int
    ): MessageResponse
    
    @PUT("notifications/read-all")
    suspend fun markAllNotificationsRead(
         @Header("Authorization") token: String
    ): MessageResponse

    @DELETE("notifications/clear-all")
    suspend fun deleteAllNotifications(
         @Header("Authorization") token: String
    ): MessageResponse

    // ==================== Marquee ====================
    @GET("marquee/active")
    suspend fun getActiveMarquee(): Marquee

    @GET("marquee")
    suspend fun getAllMarquees(
        @Header("Authorization") token: String
    ): List<Marquee>

    @POST("marquee")
    suspend fun createOrUpdateMarquee(
        @Header("Authorization") token: String,
        @Body request: Marquee
    ): Marquee

    @PATCH("marquee/{id}/toggle")
    suspend fun toggleMarquee(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Marquee

    @DELETE("marquee/{id}")
    suspend fun deleteMarquee(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): MessageResponse

    // ==================== Live Podcast ====================
    
    @POST("podcasts")
    suspend fun createPodcast(
        @Header("Authorization") token: String,
        @Body request: CreatePodcastRequest
    ): PodcastResponse
    
    @GET("podcasts/active")
    suspend fun getActivePodcasts(
        @Header("Authorization") token: String
    ): PodcastListResponse
    
    @GET("podcasts/live")
    suspend fun getLivePodcast(
        @Header("Authorization") token: String
    ): PodcastResponse
    
    @GET("podcasts/{id}")
    suspend fun getPodcastDetails(
        @Header("Authorization") token: String,
        @Path("id") podcastId: Int
    ): PodcastResponse
    
    @DELETE("podcasts/{id}")
    suspend fun deletePodcast(
        @Header("Authorization") token: String,
        @Path("id") podcastId: Int
    ): MessageResponse
    
    @POST("podcasts/{id}/go-live")
    suspend fun goLivePodcast(
        @Header("Authorization") token: String,
        @Path("id") podcastId: Int
    ): PodcastResponse
    
    @POST("podcasts/{id}/end")
    suspend fun endPodcast(
        @Header("Authorization") token: String,
        @Path("id") podcastId: Int
    ): PodcastResponse
    
    @POST("podcasts/{id}/toggle-mute")
    suspend fun toggleMutePodcast(
        @Header("Authorization") token: String,
        @Path("id") podcastId: Int
    ): MuteToggleResponse
    
    @POST("podcasts/{id}/join")
    suspend fun joinPodcast(
        @Header("Authorization") token: String,
        @Path("id") podcastId: Int
    ): ViewerCountResponse
    
    @POST("podcasts/{id}/leave")
    suspend fun leavePodcast(
        @Header("Authorization") token: String,
        @Path("id") podcastId: Int
    ): ViewerCountResponse
    
    @GET("podcasts/{id}/viewers")
    suspend fun getPodcastViewerCount(
        @Header("Authorization") token: String,
        @Path("id") podcastId: Int
    ): ViewerCountResponse
    
    @POST("podcasts/{id}/hand-raise")
    suspend fun raiseHand(
        @Header("Authorization") token: String,
        @Path("id") podcastId: Int
    ): HandRaiseResponse
    
    @DELETE("podcasts/{id}/hand-raise")
    suspend fun cancelHandRaise(
        @Header("Authorization") token: String,
        @Path("id") podcastId: Int
    ): MessageResponse
    
    @GET("podcasts/{id}/hand-raise/status")
    suspend fun getHandRaiseStatus(
        @Header("Authorization") token: String,
        @Path("id") podcastId: Int
    ): HandRaiseStatusResponse
    
    @GET("podcasts/{id}/hand-raises")
    suspend fun getHandRaises(
        @Header("Authorization") token: String,
        @Path("id") podcastId: Int
    ): HandRaiseListResponse
    
    @POST("podcasts/{id}/hand-raises/{userId}/accept")
    suspend fun acceptHandRaise(
        @Header("Authorization") token: String,
        @Path("id") podcastId: Int,
        @Path("userId") userId: Int
    ): HandRaiseResponse
    
    @POST("podcasts/{id}/hand-raises/{userId}/ignore")
    suspend fun ignoreHandRaise(
        @Header("Authorization") token: String,
        @Path("id") podcastId: Int,
        @Path("userId") userId: Int
    ): HandRaiseResponse

    // ==================== Placements ====================
    @GET("placements")
    suspend fun getPlacements(
        @Header("Authorization") token: String
    ): List<Placement>

    @POST("placements")
    suspend fun createPlacement(
        @Header("Authorization") token: String,
        @Body placement: PlacementRequest
    ): Placement

    @DELETE("placements/{id}")
    suspend fun deletePlacement(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): MessageResponse

    @GET("placements/saved")
    suspend fun getSavedPlacements(
        @Header("Authorization") token: String
    ): List<Placement>

    @POST("placements/{id}/bookmark")
    suspend fun togglePlacementBookmark(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Map<String, Any>

    // ==================== Placement Posters ====================
    @GET("placements/posters")
    suspend fun getPlacementPosters(
        @Header("Authorization") token: String
    ): List<PlacementPoster>

    @Multipart
    @POST("placements/posters")
    suspend fun uploadPlacementPoster(
        @Header("Authorization") token: String,
        @Part poster: okhttp3.MultipartBody.Part,
        @Part("title") title: okhttp3.RequestBody,
        @Part("company") company: okhttp3.RequestBody?,
        @Part("description") description: okhttp3.RequestBody?
    ): PlacementPoster

    @PATCH("placements/posters/{id}")
    suspend fun updatePlacementPoster(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): PlacementPoster

    @DELETE("placements/posters/{id}")
    suspend fun deletePlacementPoster(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): MessageResponse

    // ==================== Agora ====================
    @GET("agora/token")
    suspend fun getAgoraToken(
        @Header("Authorization") token: String,
        @Query("channelName") channelName: String,
        @Query("uid") uid: Int,
        @Query("role") role: String = "publisher"
    ): AgoraTokenResponse

    // ==================== Issues ====================
    @POST("issues")
    suspend fun createIssue(
        @Header("Authorization") token: String,
        @Body request: CreateIssueRequest
    ): IssueResponse

    @GET("issues")
    suspend fun getAllIssues(
        @Header("Authorization") token: String
    ): List<Issue>

    @GET("issues/resolved")
    suspend fun getResolvedIssues(
        @Header("Authorization") token: String
    ): List<Issue>

    @GET("issues/my")
    suspend fun getMyIssues(
        @Header("Authorization") token: String
    ): List<Issue>

    @GET("issues/{id}")
    suspend fun getIssueDetails(
        @Header("Authorization") token: String,
        @Path("id") issueId: Int
    ): Issue

    @POST("issues/{id}/message")
    suspend fun sendIssueMessage(
        @Header("Authorization") token: String,
        @Path("id") issueId: Int,
        @Body request: SendIssueMessageRequest
    ): IssueMessageResponse

    @PUT("issues/{id}/resolve")
    suspend fun resolveIssue(
        @Header("Authorization") token: String,
        @Path("id") issueId: Int
    ): IssueResponse

    @DELETE("issues/{id}")
    suspend fun deleteIssue(
        @Header("Authorization") token: String,
        @Path("id") issueId: Int
    ): MessageResponse

    @GET("issues/stats")
    suspend fun getIssueStats(
        @Header("Authorization") token: String
    ): IssueStats

    // ==================== Banners ====================
    @GET("banners")
    suspend fun getBanners(): BannerResponse

    @Multipart
    @POST("banners")
    suspend fun uploadBanner(
        @Header("Authorization") token: String,
        @Part banner: okhttp3.MultipartBody.Part
    ): AdminBannerUploadResponse

    @DELETE("banners/{id}")
    suspend fun deleteBanner(
        @Header("Authorization") token: String,
        @Path("id") bannerId: Int
    ): MessageResponse
}

// Helper response models
data class RadiosResponse(
    val radios: List<Radio>,
    val total: Int,
    val page: Int,
    val pages: Int
)

data class MessageResponse(
    val message: String
)

data class ApproveResponse(
    val message: String,
    val suggestion: RadioSuggestion,
    val radio_id: Int
)

data class MediaUploadResponse(
    val message: String,
    val media_url: String
)

data class BannerUploadResponse(
    val message: String,
    val banner_image: String
)

// Hosting request/response models
data class StartHostingRequest(
    val media_type: String  // "AUDIO" or "VIDEO"
)

data class HostingResponse(
    val message: String,
    val radio: Radio
)

data class StreamInfoResponse(
    val radio_id: Int,
    val title: String,
    val status: String,
    val host_status: String,
    val media_type: String,
    val media_url: String?,
    val is_live: Boolean,
    val is_paused: Boolean
)

// Podcast response models
data class MuteToggleResponse(
    val message: String,
    val is_muted: Boolean
)

data class HandRaiseResponse(
    val message: String,
    val hand_raise: HandRaise? = null
)

data class AgoraTokenResponse(
    val token: String?,
    val appId: String? = null
)

