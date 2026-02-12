package com.ilustris.sagai.core.analytics

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import com.ilustris.sagai.core.utils.toJsonFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val TAG = "AnalyticsServiceImpl"

/**
 * Implementation of AnalyticsService using Firebase Analytics.
 */
class AnalyticsService
    @Inject
    constructor(
        @ApplicationContext
        private val context: Context,
    ) {
        // Lazy initialization to avoid upfront performance cost
        private val firebaseAnalytics: FirebaseAnalytics by lazy {
            FirebaseAnalytics.getInstance(context)
        }

        /**
         * Track an analytics event with automatic error handling.
         * All failures are logged to Crashlytics without breaking app functionality.
         */
        fun trackEvent(event: Any) {
            try {
                val eventName =
                    event::class.simpleName?.toEventName() ?: run {
                        val error = AnalyticsEventException("Event class has no name")
                        Timber.tag(TAG).e(error, error.message)
                        FirebaseCrashlytics.getInstance().recordException(error)
                        return
                    }

                val bundle = event.toAnalyticsBundle()

                // Log event for debugging
                Timber.tag(TAG).d("Tracking event: $eventName: ${event.toJsonFormat()}")

                firebaseAnalytics.logEvent(eventName, bundle)
            } catch (e: Exception) {
                // Wrap in analytics exception if not already
                val error =
                    if (e is AnalyticsEventException ||
                        e is AnalyticsBundleException ||
                        e is AnalyticsPropertyException
                    ) {
                        e
                    } else {
                        AnalyticsEventException("Failed to track event: ${event::class.simpleName}", e)
                    }

                Timber.tag(TAG).e(error, error.message)
                FirebaseCrashlytics.getInstance().recordException(error)
            }
        }
    }
