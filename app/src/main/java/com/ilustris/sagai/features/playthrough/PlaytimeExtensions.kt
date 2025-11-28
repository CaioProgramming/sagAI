package com.ilustris.sagai.features.playthrough

/**
 * Formats playtime in milliseconds to a human-readable string.
 * Always shows both hours and minutes (e.g., "0h 15m" or "2h 30m").
 */
fun Long.toPlaytimeFormat(): String {
    val hours = this / 3600000
    val minutes = (this % 3600000) / 60000
    return "${hours}h ${minutes}m"
}
