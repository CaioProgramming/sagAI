package com.ilustris.sagai.core.services.model

/**
 * Face spec for the dragon mascot, one per [com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone].
 *
 * Eye and lid values are multipliers or angles over the base face — the geometry itself (silhouette,
 * eye shape, motion) lives in the composable, not in Remote Config. Angles are in radians.
 *
 * Fields are populated by Gson, which may bypass constructors: a required field missing from the
 * payload arrives as `0f`, so an incomplete entry fails [sanitized] and the tone is treated as not
 * configured. That is intentional — there are no compiled fallbacks. Optional fields (everything
 * from [cheek] down except [tempo] and [glance]) are neutral at `0`.
 */
data class MascotExpression(
    /** Eye width multiplier. */
    val w: Float = 1f,
    /** Eye height multiplier. Low values read as a squint. */
    val h: Float = 1f,
    /** Absolute eye rotation. Negative drops the inner corner (angry), positive the outer (sad). */
    val rot: Float = 0f,
    /** Upper lid: fraction of the eye height it covers, as a straight cut. */
    val lid: Float = 0f,
    /** Lid slope. Positive raises the inner corner (worried, sad), negative lowers it (angry). */
    val lidTilt: Float = 0f,
    /** Height of the eyes on the face. */
    val dy: Float = 0f,
    /** Size and lid difference between the two eyes. */
    val asym: Float = 0f,
    /** Closed-eye arc: `1` smiling (n), `-1` serene (u), `0` open. */
    val arc: Float = 0f,
    /** Lower lid rising in the middle, so the eye smiles. */
    val cheek: Float = 0f,
    /** Crest and frills: `-1` drooping and closed, `1` raised and open. */
    val posture: Float = 0f,
    /** Head roll. */
    val headTilt: Float = 0f,
    /** Head lowered (`+`) or raised (`-`). */
    val headDip: Float = 0f,
    /** Nose height. */
    val noseDy: Float = 0f,
    /** Nose scale. `0` means unset and resolves to `1`. */
    val noseS: Float = 1f,
    /** Where this tone looks when the gaze is free. */
    val gx: Float = 0f,
    val gy: Float = 0f,
    /** How much the free gaze wanders around that bias. `0` means unset and resolves to `1`. */
    val glance: Float = 1f,
    /** Breathing and blink rhythm multiplier. `0` means unset and resolves to `1`. */
    val tempo: Float = 1f,
    /** Amount of nervous shake applied to the body. */
    val jit: Float = 0f,
) {
    /**
     * Returns this spec with the unset defaults resolved, or null when any value is outside the
     * ranges the renderer can draw. An invalid entry drops that single tone, never the table.
     */
    fun sanitized(): MascotExpression? {
        if (w !in W_RANGE) return null
        if (h !in H_RANGE) return null
        if (rot !in ANGLE_RANGE) return null
        if (lidTilt !in ANGLE_RANGE) return null
        if (lid !in LID_RANGE) return null
        if (dy !in DY_RANGE) return null
        if (asym !in UNIT_RANGE) return null
        if (arc !in SIGNED_RANGE) return null
        if (cheek !in UNIT_RANGE) return null
        if (posture !in SIGNED_RANGE) return null
        if (headTilt !in HEAD_RANGE) return null
        if (headDip !in HEAD_RANGE) return null
        if (noseDy !in NOSE_DY_RANGE) return null
        if (gx !in SIGNED_RANGE || gy !in SIGNED_RANGE) return null
        if (jit !in UNIT_RANGE) return null
        val resolvedNose = if (noseS <= 0f) 1f else noseS
        if (resolvedNose !in NOSE_SCALE_RANGE) return null
        val resolvedGlance = if (glance <= 0f) 1f else glance
        if (resolvedGlance !in GLANCE_RANGE) return null
        val resolvedTempo = if (tempo <= 0f) 1f else tempo
        if (resolvedTempo !in TEMPO_RANGE) return null
        return copy(noseS = resolvedNose, glance = resolvedGlance, tempo = resolvedTempo)
    }

    companion object {
        private val W_RANGE = 0.2f..2.5f
        private val H_RANGE = 0.05f..2.5f
        private val ANGLE_RANGE = -1.2f..1.2f
        private val LID_RANGE = 0f..0.72f
        private val DY_RANGE = -0.3f..0.3f
        private val HEAD_RANGE = -0.4f..0.4f
        private val NOSE_DY_RANGE = -0.2f..0.2f
        private val NOSE_SCALE_RANGE = 0.5f..1.6f
        private val GLANCE_RANGE = 0.05f..2.5f
        private val UNIT_RANGE = 0f..1f
        private val SIGNED_RANGE = -1f..1f
        private val TEMPO_RANGE = 0.2f..3f
    }
}
