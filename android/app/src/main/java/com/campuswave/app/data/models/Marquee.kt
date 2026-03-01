package com.campuswave.app.data.models

data class Marquee(
    val id: Int? = null,
    val message: String? = null,
    val is_active: Boolean = false,
    val text_color: String? = "#6366F1",
    val font_style: String? = "Bold",
    val font_size: String? = "Medium",
    val bg_color: String? = "#6366F1",
    val bg_gradient_end: String? = null,
    val scroll_speed: String? = "Normal",
    val text_alignment: String? = "Left",
    val created_at: String? = null,
    val updated_at: String? = null
)

data class MarqueeResponse(
    val id: Int? = null,
    val message: String? = null,
    val is_active: Boolean = false
)
