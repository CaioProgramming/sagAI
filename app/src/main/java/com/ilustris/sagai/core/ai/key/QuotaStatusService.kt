package com.ilustris.sagai.core.ai.key

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Where the user's key stands against the Gemini quota, for every surface that wants to say so.
 *
 * The two 429s have opposite remedies and this is what keeps them apart:
 *
 * - per-minute (RPM/TPM) clears in seconds and the existing backoff already handles it, so it is
 *   transient state only — the point is to show the user *why* they are waiting instead of a mute
 *   spinner. The in-flight request is never dropped.
 * - per-day (RPD) kills the key until midnight Pacific, where retrying is pure waste, so it is
 *   persisted and the generation entry points hide their input.
 *
 * This exists as a service rather than a `StreamingState.Reasoning` emission because the backoff
 * `delay()` lives inside `executeSyncGenerationWithRetry`, which has no emission channel at all —
 * pushing it through would mean threading it into every use case. One `StateFlow` covers the sync
 * and streaming paths alike.
 */
@Singleton
class QuotaStatusService
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val transient = MutableStateFlow<QuotaStatus>(QuotaStatus.Clear)

        /**
         * Persisted daily exhaustion wins over a transient per-minute wait: if the key is done for
         * the day, a countdown promising a retry in 24 seconds would be a lie.
         */
        val status: Flow<QuotaStatus> =
            combine(
                context.byokDataStore.data.map { it[DAILY_LIMITS_KEY] },
                transient,
            ) { storedJson, transientStatus ->
                activeDailyExhaustion(storedJson) ?: transientStatus
            }

        /**
         * The active daily block, if any — the pre-flight check every generation runs through.
         *
         * Reading it costs one DataStore lookup and saves a request that is already known to come
         * back 429, which on the free tier is not a rounding error: the refused call would still
         * count against the per-minute bucket.
         */
        suspend fun activeDailyBlock(): QuotaStatus.DailyExhausted? =
            activeDailyExhaustion(
                context.byokDataStore.data.first()[DAILY_LIMITS_KEY],
            )

        /** Per-minute or per-token throttle: publish the window, keep the request alive. */
        fun reportCoolingDown(
            model: String,
            retryDelaySeconds: Long,
        ) {
            transient.value =
                QuotaStatus.CoolingDown(
                    until = System.currentTimeMillis() + retryDelaySeconds * 1000L,
                    model = model,
                )
        }

        fun reportRecovered() {
            transient.value = QuotaStatus.Clear
        }

        /** Daily free-tier quota is spent for [model]; blocked until the Pacific day rolls over. */
        suspend fun reportDailyExhausted(model: String) {
            val until = nextPacificMidnight()
            Timber
                .tag(TAG)
                .w("Daily quota exhausted for $model — blocked until ${java.util.Date(until)}")
            context.byokDataStore.edit { preferences ->
                val current = decodeLimits(preferences[DAILY_LIMITS_KEY])
                preferences[DAILY_LIMITS_KEY] =
                    Gson().toJson(current + (model to until))
            }
            transient.value = QuotaStatus.Clear
        }

        /**
         * Wipes every cooldown. Called when the key is replaced — quota is billed per Google Cloud
         * project, and a key from a different project has a quota of its own, so holding the old
         * block would strand a user who legitimately brought a fresh one. Being wrong costs a
         * single failed request, which re-establishes the block.
         */
        suspend fun clearAll() {
            transient.value = QuotaStatus.Clear
            context.byokDataStore.edit { it.remove(DAILY_LIMITS_KEY) }
        }

        private fun activeDailyExhaustion(storedJson: String?): QuotaStatus.DailyExhausted? {
            val now = System.currentTimeMillis()
            // Surfaces a single aggregate state even though the map is per-model: the free tier
            // quotas tend to run out together, and a partially-blocked UI reads as a bug. The map
            // stays per-model so a future tier downgrade has something to read.
            return decodeLimits(storedJson)
                .filterValues { it > now }
                .minByOrNull { it.value }
                ?.let { (model, until) -> QuotaStatus.DailyExhausted(until, model) }
        }

        private fun decodeLimits(json: String?): Map<String, Long> {
            if (json.isNullOrBlank()) return emptyMap()
            return try {
                val parsed = Gson().fromJson(json, Map::class.java) ?: return emptyMap()
                parsed.entries.mapNotNull { entry ->
                    val model = entry.key as? String ?: return@mapNotNull null
                    // Gson decodes bare JSON numbers as Double — go through Number, not Long.
                    val until = (entry.value as? Number)?.toLong() ?: return@mapNotNull null
                    model to until
                }.toMap()
            } catch (e: Exception) {
                Timber.tag(TAG).e("Failed to parse stored quota limits: ${e.message}")
                emptyMap()
            }
        }

        /** The Gemini free tier's daily counters reset at midnight Pacific, not device-local. */
        private fun nextPacificMidnight(): Long {
            val pacific = ZoneId.of("America/Los_Angeles")
            val tomorrow = ZonedDateTime.now(pacific).toLocalDate().plusDays(1)
            return tomorrow
                .atStartOfDay(pacific)
                .toInstant()
                .toEpochMilli()
        }

        companion object {
            private const val TAG = "📊 QuotaStatus"
            private val DAILY_LIMITS_KEY = stringPreferencesKey("quota_daily_limits")
        }
    }

sealed class QuotaStatus {
    object Clear : QuotaStatus()

    /** Short throttle; the request is still in flight behind the existing backoff. */
    data class CoolingDown(
        val until: Long,
        val model: String,
    ) : QuotaStatus()

    /** Key is spent for the day; generation entry points should stop accepting input. */
    data class DailyExhausted(
        val until: Long,
        val model: String,
    ) : QuotaStatus()
}
