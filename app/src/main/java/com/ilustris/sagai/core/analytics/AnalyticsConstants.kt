package com.ilustris.sagai.core.analytics

/**
 * Analytics constants organized in nested objects for better readability.
 * Events are defined as data classes and their class names are used as event identifiers.
 */
object AnalyticsConstants {
    /**
     * Analytics properties for events
     */
    object Properties {
        const val MESSAGE_COUNT = "message_count"
        const val GENRE = "genre"
        const val IMAGE_TYPE = "image_type"
        const val QUALITY = "quality"
        const val VIOLATIONS = "violations"
        const val VIOLATION_TYPES = "violation_types"
        const val SOURCE = "source"
    }

    /**
     * Image types for analytics
     */
    object ImageType {
        const val AVATAR = "avatar"
        const val ICON = "icon"
        const val COVER = "cover"
    }

    /**
     * Quality levels for image generation
     */
    object Quality {
        const val GOOD = "good"
        const val MEDIUM = "medium"
        const val BAD = "bad"
    }
}
