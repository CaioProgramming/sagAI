package com.ilustris.sagai.core.analytics

import android.content.Context
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val TAG = "AnalyticsServiceImpl"

/**
 * Implementation of AnalyticsService using Firebase Analytics.
 */
class AnalyticsServiceImpl
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : AnalyticsService {
        // Lazy initialization to avoid upfront performance cost
        private val firebaseAnalytics: FirebaseAnalytics by lazy {
            FirebaseAnalytics.getInstance(context)
        }

        /**
         * Track an analytics event with automatic error handling.
         * All failures are logged to Crashlytics without breaking app functionality.
         */
        override fun trackEvent(event: Any) {
            try {
                val eventName =
                    event::class.simpleName?.toEventName() ?: run {
                        val error = AnalyticsEventException("Event class has no name")
                        Log.e(TAG, error.message, error)
                        FirebaseCrashlytics.getInstance().recordException(error)
                        return
                    }

                val bundle = event.toAnalyticsBundle()

                // Log event for debugging
                Log.d(TAG, "Tracking event: $eventName with ${bundle.size()} properties")

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

                Log.e(TAG, error.message, error)
                FirebaseCrashlytics.getInstance().recordException(error)
            }
        }
    }
