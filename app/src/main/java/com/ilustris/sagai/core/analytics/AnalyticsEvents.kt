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

/** Ad lifecycle events — `tier` is [com.ilustris.sagai.core.services.AdTier]'s name (EVENT / CHAPTER_OR_ACT). */
data class AdRequestedEvent(
    val tier: String,
)

data class AdLoadedEvent(
    val tier: String,
)

data class AdFailedToLoadEvent(
    val errorCode: Int,
    val errorMessage: String,
)

data class AdShownEvent(
    val tier: String,
)

data class AdDismissedEvent(
    val tier: String,
)

data class AdClickedEvent(
    val tier: String,
)

data class AdFailedToShowEvent(
    val errorCode: Int,
    val errorMessage: String,
)
