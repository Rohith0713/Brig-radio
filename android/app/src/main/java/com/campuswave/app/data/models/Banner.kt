package com.campuswave.app.data.models

data class Banner(
    val id: Int,
    val image_url: String,
    val created_at: String
)

data class BannerResponse(
    val banners: List<Banner>
)

data class AdminBannerUploadResponse(
    val message: String,
    val banner: Banner
)
