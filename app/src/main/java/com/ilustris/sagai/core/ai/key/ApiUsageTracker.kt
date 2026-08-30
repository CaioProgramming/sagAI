package com.ilustris.sagai.core.ai.key

import com.ilustris.sagai.core.ai.model.GeminiUsageMetadata
import com.ilustris.sagai.core.database.model.ApiUsageDay
import com.ilustris.sagai.core.database.source.ApiUsageDao
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Counts what this app spends of the user's API key.
 *
 * The Gemini API has no endpoint for reading a key's consumption — it names a limit only inside the
 * 429 that breaks it. But `usageMetadata` rides along on every successful response, so the app can
 * account for its own calls even if it can never see the account's true total.
 *
 * Unlike the audit log this runs in release builds: it is the data behind a screen the user is
 * meant to see, not a debugging aid. It stores counters only, never prompts or responses.
 */
@Singleton
class ApiUsageTracker
    @Inject
    constructor(
        private val apiUsageDao: ApiUsageDao,
    ) {
        fun observeToday(): Flow<List<ApiUsageDay>> = apiUsageDao.observeDay(pacificToday())

        /** Never throws: accounting must not be what breaks a generation that already succeeded. */
        suspend fun record(
            model: String,
            usage: GeminiUsageMetadata?,
        ) {
            try {
                apiUsageDao.record(
                    day = pacificToday(),
                    model = model.replace("models/", ""),
                    promptTokens = (usage?.promptTokenCount ?: 0).toLong(),
                    candidatesTokens = (usage?.candidatesTokenCount ?: 0).toLong(),
                    thoughtsTokens = (usage?.thoughtsTokenCount ?: 0).toLong(),
                )
                apiUsageDao.prune(pacificToday(minusDays = RETENTION_DAYS))
            } catch (e: Exception) {
                Timber.tag(TAG).w("Could not record API usage: ${e.message}")
            }
        }

        /**
         * Google's daily counters roll over at midnight Pacific, so a "day" here is a Pacific date
         * regardless of where the user is. Only the display converts to local time.
         */
        private fun pacificToday(minusDays: Long = 0): String =
            LocalDate
                .now(ZoneId.of("America/Los_Angeles"))
                .minusDays(minusDays)
                .toString()

        companion object {
            private const val TAG = "📈 ApiUsage"
            private const val RETENTION_DAYS = 7L
        }
    }
