package com.campuswave.app.data.models

data class PlacementRequest(
    val title: String,
    val company: String,
    val location: String,
    val salary: String? = null,
    val deadline: String? = null,
    val description: String? = null,
    val applicationLink: String? = null
)
