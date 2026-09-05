package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.ai.model.SafeGuard

class GuardrailsException(
    val status: SafeGuard,
) : Exception("AI Guardrail triggered: ${status.name}")

/**
 * Maps Gemini's own platform-level `promptFeedback.blockReason` onto [SafeGuard].
 *
 * A different vocabulary from [SafeGuard]'s names on purpose: those are values our own blueprints
 * are instructed to write into `AIError.type`, self-reported by the model about its own would-be
 * answer. `blockReason` is Google's classification of the *prompt*, decided before the model ever
 * runs, so nothing here is guaranteed to line up name-for-name — hence a mapping rather than the
 * direct enum decode `AIError.type` gets away with.
 *
 * [SafeGuard.BLOCKED] already reads right for this ("Unable to Process... violates security
 * policies. Rephrase your message.") without new copy, so every recognized reason folds into it;
 * an unrecognized or absent reason falls back to [SafeGuard.UNKNOWN] rather than guessing.
 */
fun promptBlockReasonToSafeGuard(blockReason: String?): SafeGuard =
    when (blockReason) {
        "SAFETY", "PROHIBITED_CONTENT", "BLOCKLIST" -> SafeGuard.BLOCKED
        else -> SafeGuard.UNKNOWN
    }
