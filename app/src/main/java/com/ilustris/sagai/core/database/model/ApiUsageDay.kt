package com.ilustris.sagai.core.database.model

import androidx.annotation.Keep
import androidx.room.Entity

/**
 * One day of this app's own consumption of the user's API key, per model.
 *
 * Exists because the Gemini API has no endpoint for reading a key's usage — it reports a limit only
 * in the body of the 429 that breaks it. Everything needed to count locally already arrives on
 * every response, so the app can at least be honest about its own share.
 *
 * Two things it can never be: complete, since the same key used anywhere else is invisible here;
 * and authoritative, since the limits it is compared against are reference values for the free
 * tier, not read from the user's account. Both belong on screen, not just in this comment.
 *
 * [day] is keyed to the Pacific date, not the device's, because that is when Google's daily
 * counters roll over. Display converts to local time.
 */
@Keep
@Entity(tableName = "api_usage_days", primaryKeys = ["day", "model"])
data class ApiUsageDay(
    val day: String,
    val model: String,
    val requests: Int = 0,
    val promptTokens: Long = 0,
    val candidatesTokens: Long = 0,
    val thoughtsTokens: Long = 0,
)
