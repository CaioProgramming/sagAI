package com.ilustris.sagai.core.services

import com.ilustris.sagai.core.services.model.MascotExpression
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fetches and caches the blob mascot eye specs from Firebase Remote Config, under the
 * `default_mascot_expressions` key of the `mascot` group.
 *
 * Remote-only, no compiled fallbacks. A tone missing
 * from the payload, keyed by a name that is not an [EmotionalTone], or carrying values outside
 * the renderer's ranges returns null, and the caller skips drawing the mascot for that tone.
 * An invalid entry drops only itself, never the rest of the table.
 */
@Singleton
class MascotExpressionService
    @Inject
    constructor(
        private val remoteConfigService: RemoteConfigService,
    ) {
        private var cache: Map<EmotionalTone, MascotExpression>? = null

        suspend fun getExpression(tone: EmotionalTone): MascotExpression? = getExpressions()[tone]

        suspend fun getExpressions(): Map<EmotionalTone, MascotExpression> {
            cache?.let { return it }

            val raw =
                remoteConfigService
                    .getJsonMapString(KEY, MascotExpression::class.java)
                    ?: emptyMap()

            val mapped =
                raw
                    .mapNotNull { (key, expression) ->
                        val tone = EmotionalTone.getTone(key)
                        if (tone.name != key.uppercase()) {
                            Timber.tag(TAG).w("Ignoring \"$key\": not an EmotionalTone")
                            return@mapNotNull null
                        }
                        val sanitized = expression.sanitized()
                        if (sanitized == null) {
                            Timber.tag(TAG).w("Ignoring ${tone.name}: values out of range ($expression)")
                            return@mapNotNull null
                        }
                        tone to sanitized
                    }.toMap()

            if (mapped.isEmpty()) {
                Timber.tag(TAG).w("$KEY is missing or empty in Remote Config — mascot will not render")
            } else {
                Timber.tag(TAG).d("Loaded ${mapped.size} expressions: ${mapped.keys.joinToString()}")
            }

            cache = mapped
            return mapped
        }

        companion object {
            private const val KEY = "default_mascot_expressions"
            private const val TAG = "MascotExpressionService"
        }
    }
