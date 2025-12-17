package com.ilustris.sagai.core.analytics

/**
 * Data classes representing analytics events.
 * Class names are automatically converted to event names in Firebase Analytics.
 */

/**
 * Triggered when a user successfully creates a new saga
 */
data class SagaCreationEvent(
    val messageCount: Int,
    val genre: String,
)

/**
 * Triggered when a user clicks on premium features
 */
data class PremiumClickEvent(
    val source: String,
)

/**
 * Triggered after image generation to track quality metrics
 */
data class ImageQualityEvent(
    val genre: String,
    val imageType: String,
    val quality: String,
    val violations: Int,
    val violationTypes: String? = null,
)
