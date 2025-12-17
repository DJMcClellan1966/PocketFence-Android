package com.pocketfence.android.model

data class BlockedWebsite(
    val url: String,
    val category: WebsiteCategory = WebsiteCategory.CUSTOM,
    val addedTime: Long = System.currentTimeMillis(),
    val timesBlocked: Int = 0
)

enum class WebsiteCategory {
    CUSTOM,
    SOCIAL_MEDIA,
    ADULT_CONTENT,
    GAMBLING,
    GAMING,
    VIOLENCE,
    DRUGS
}
