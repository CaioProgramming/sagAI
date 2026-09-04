package com.ilustris.sagai.core.services.model

/**
 * Eye spec for the blob mascot, one per [com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone].
 *
 * All values are multipliers over the base eye, not absolute sizes — the geometry itself
 * (capsule shape, orbit radius, blink cadence) lives in the composable, not in Remote Config.
 *
 * Fields are populated by Gson, which bypasses constructors: a field missing from the payload
 * arrives as `0f`, so an incomplete entry fails [sanitized] and the tone is treated as not
 * configured. That is intentional — there are no compiled fallbacks.
 */
data class MascotExpression(
    /** Eye width multiplier. */
    val w: Float = 1f,
    /** Eye height multiplier. Low values read as a squint. */
    val h: Float = 1f,
    /** Lid tilt in radians. Positive drops the inner corner (angry), negative the outer (sad). */
    val tilt: Float = 0f,
    /** Vertical offset of the pair, in latitude radians. */
    val dy: Float = 0f,
    /** Height difference between the two eyes. */
    val asym: Float = 0f,
    /** Draws the eye as a smiling arc instead of a capsule. */
    val arc: Boolean = false,
    /** Breathing and blink rhythm multiplier. `0` means unset and resolves to `1`. */
    val tempo: Float = 1f,
    /** Amount of nervous shake applied to the body. */
    val jitter: Float = 0f,
) {
    /**
     * Returns this spec with [tempo] resolved, or null when any value is outside the
     * ranges the renderer can draw. An invalid entry drops that single tone, never the table.
     */
    fun sanitized(): MascotExpression? {
        if (w !in W_RANGE) return null
        if (h !in H_RANGE) return null
        if (tilt !in TILT_RANGE) return null
        if (dy !in DY_RANGE) return null
        if (asym !in UNIT_RANGE) return null
        if (jitter !in UNIT_RANGE) return null
        val resolvedTempo = if (tempo <= 0f) 1f else tempo
        if (resolvedTempo !in TEMPO_RANGE) return null
        return copy(tempo = resolvedTempo)
    }

    companion object {
        private val W_RANGE = 0.2f..2.5f
        private val H_RANGE = 0.05f..2.5f
        private val TILT_RANGE = -1.2f..1.2f
        private val DY_RANGE = -0.6f..0.6f
        private val UNIT_RANGE = 0f..1f
        private val TEMPO_RANGE = 0.2f..3f
    }
}
