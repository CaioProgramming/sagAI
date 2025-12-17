package com.ilustris.sagai.core.analytics

/**
 * Interface for analytics operations following repository pattern.
 */
interface AnalyticsService {
    /**
     * Track an analytics event.
     * The event data class will be automatically converted to Firebase Analytics format.
     *
     * @param event Data class representing the event
     */
    fun trackEvent(event: Any)
}
