package com.ilustris.sagai.core.ai.model

import androidx.annotation.Keep

/**
 * Holding lines written for the request that is currently running.
 *
 * A list rather than one line because they are consumed by rotation: a single sentence sitting
 * still for the length of a HIGH-tier generation reads as a frozen screen, and a pool small enough
 * to memorise reads as a canned animation. Both are the thing this replaces.
 */
@Keep
data class LoadingLines(
    val lines: List<String> = emptyList(),
)
