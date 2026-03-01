package com.campuswave.app.data.models

data class Placement(
    val id: Int,
    val title: String,
    val company: String,
    val location: String,
    val salary: String? = null,
    val applicantsCount: Int = 0,
    val deadline: String? = null,
    val isBookmarked: Boolean = false,
    val description: String? = null,
    val postedAt: String? = null,
    val applicationLink: String? = null
)

data class PlacementPoster(
    val id: Int,
    val title: String,
    val company: String? = null,
    val description: String? = null,
    val posterImage: String,
    val isVisible: Boolean = true,
    val createdAt: String? = null
)
