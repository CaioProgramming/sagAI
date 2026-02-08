package com.ilustris.sagai.core.analytics

/**
 * Custom exceptions for analytics operations to provide better debugging.
 */

/**
 * Thrown when an analytics event has naming or structure issues
 */
class AnalyticsEventException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

/**
 * Thrown when bundle conversion fails
 */
class AnalyticsBundleException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

/**
 * Thrown when property mapping fails
 */
class AnalyticsPropertyException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
