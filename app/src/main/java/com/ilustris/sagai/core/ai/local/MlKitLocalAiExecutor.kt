package com.ilustris.sagai.core.ai.local

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MlKitLocalAiExecutor
    @Inject
    constructor() : LocalAiExecutor {
        private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private val generativeModel by lazy { Generation.getClient() }

        @Volatile
        private var cachedAvailability: Pair<LocalAiAvailability, Long>? = null

        @Volatile
        private var downloadDispatched = false

        override suspend fun availability(): LocalAiAvailability {
            val cached = cachedAvailability
            val now = System.currentTimeMillis()
            if (cached != null && now - cached.second < CACHE_TTL_MS) {
                return cached.first
            }
            val mapped =
                runCatching { mapFeatureStatus(generativeModel.checkStatus()) }
                    .getOrElse {
                        Timber.w(it, "Local AI status check failed")
                        LocalAiAvailability.UNAVAILABLE
                    }
            cachedAvailability = mapped to now
            return mapped
        }

        override fun ensureModelDownloaded() {
            if (downloadDispatched) return
            downloadDispatched = true
            applicationScope.launch {
                try {
                    val status = generativeModel.checkStatus()
                    if (status != FeatureStatus.DOWNLOADABLE) {
                        if (status == FeatureStatus.AVAILABLE) {
                            invalidateAvailabilityCache()
                        }
                        downloadDispatched = false
                        return@launch
                    }
                    Timber.i("Local AI model download started")
                    generativeModel.download().collect { downloadStatus ->
                        when (downloadStatus) {
                            is DownloadStatus.DownloadStarted -> {
                                Unit
                            }

                            is DownloadStatus.DownloadProgress -> {
                                Unit
                            }

                            DownloadStatus.DownloadCompleted -> {
                                Timber.i("Local AI model download completed")
                                invalidateAvailabilityCache()
                                downloadDispatched = false
                            }

                            is DownloadStatus.DownloadFailed -> {
                                Timber.e(downloadStatus.e, "Local AI model download failed")
                                LocalAiTelemetry.recordDownloadFailed(downloadStatus.e)
                                downloadDispatched = false
                            }
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Local AI model download failed")
                    LocalAiTelemetry.recordDownloadFailed(e)
                    downloadDispatched = false
                }
            }
        }

        override suspend fun generate(
            prompt: String,
            systemInstruction: String,
            maxOutputTokens: Int,
        ): Result<String> =
            runCatching {
                val combinedPrompt = combinePrompt(systemInstruction, prompt)
                val response =
                    generativeModel.generateContent(
                        generateContentRequest(TextPart(combinedPrompt)) {
                            temperature = 0.1f
                            this.maxOutputTokens = maxOutputTokens
                        },
                    )
                response.candidates
                    .firstOrNull()
                    ?.text
                    ?.takeIf { it.isNotBlank() }
                    ?: error("Local AI returned empty response")
            }

        fun warmup() {
            applicationScope.launch {
                runCatching { generativeModel.warmup() }
            }
        }

        private fun invalidateAvailabilityCache() {
            cachedAvailability = null
        }

        private fun mapFeatureStatus(status: Int): LocalAiAvailability =
            when (status) {
                FeatureStatus.AVAILABLE -> LocalAiAvailability.AVAILABLE
                FeatureStatus.DOWNLOADABLE -> LocalAiAvailability.DOWNLOADABLE
                FeatureStatus.DOWNLOADING -> LocalAiAvailability.DOWNLOADING
                else -> LocalAiAvailability.UNAVAILABLE
            }

        private fun combinePrompt(
            systemInstruction: String,
            prompt: String,
        ): String =
            buildString {
                if (systemInstruction.isNotBlank()) {
                    append(systemInstruction.trim())
                    append("\n\n")
                }
                append(prompt.trim())
            }

        companion object {
            private const val CACHE_TTL_MS = 5 * 60 * 1000L
        }
    }

object LocalAiTelemetry {
    fun recordLocalHit(responseTimeMs: Long) {
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("ai_inference_source", "LOCAL")
            log("Local AI hit in ${responseTimeMs}ms")
        }
        Timber.i("Local AI hit (${responseTimeMs}ms)")
    }

    fun recordLocalMiss(
        reason: String,
        availability: LocalAiAvailability? = null,
    ) {
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("ai_inference_source", "CLOUD")
            setCustomKey("ai_local_fallback_reason", reason)
            availability?.let { setCustomKey("ai_local_availability", it.name) }
            log("Local AI miss -> cloud ($reason)")
        }
        Timber.i("Local AI miss -> cloud ($reason)")
    }

    fun recordDownloadFailed(throwable: Throwable) {
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("ai_local_download_failed", true)
            log("Local AI model download failed: ${throwable.message}")
            recordException(throwable)
        }
    }
}
