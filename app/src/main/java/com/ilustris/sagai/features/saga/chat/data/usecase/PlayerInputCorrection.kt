package com.ilustris.sagai.features.saga.chat.data.usecase

import com.ilustris.sagai.features.saga.chat.data.model.InputMode
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.PlayerInputFeedback

/**
 * Applies what the reply read off the player's message ([PlayerInputFeedback]) back onto that
 * message, behind a guard: the model can still overreach, and a rewritten player line goes
 * straight into the story with no edit step.
 *
 * Pure on purpose so the guard's thresholds are unit-testable.
 */
object PlayerInputCorrection {
    private val KNOWN_TAGS = setOf("action", "think", "narrator")
    private val TAG_REGEX = Regex("</?([a-zA-Z]+)>")

    /** Typed: the corrected prose may grow at most this much over the original (fixing typos, not writing). */
    private const val TYPED_MAX_GROWTH = 1.3f
    private const val TYPED_GROWTH_SLACK_CHARS = 12

    /** Voice: nobody speaks faster than this; more words than that means invented content. */
    private const val VOICE_MAX_WORDS_PER_SECOND = 4f
    private const val VOICE_WORD_SLACK = 4

    data class Result(
        val message: Message,
        /** Voice turn whose correction was missing or rejected: the message still has no text. */
        val needsTranscription: Boolean,
    )

    fun apply(
        original: Message,
        feedback: PlayerInputFeedback?,
        audioDurationMs: Long? = null,
    ): Result {
        val mode = original.inputMode ?: InputMode.TYPED
        val withTone = feedback?.emotionalTone?.let { original.copy(emotionalTone = it) } ?: original
        val corrected = feedback?.correctedText?.trim()?.takeIf { it.isNotBlank() }

        return when (mode) {
            InputMode.TYPED -> {
                val accepted =
                    corrected != null &&
                        corrected != original.text &&
                        isWellFormed(corrected) &&
                        keepsTags(original.text, corrected) &&
                        proseLength(corrected) <= proseLength(original.text) * TYPED_MAX_GROWTH + TYPED_GROWTH_SLACK_CHARS
                Result(
                    message =
                        if (accepted) {
                            withTone.copy(text = corrected!!, originalText = original.originalText ?: original.text)
                        } else {
                            withTone
                        },
                    needsTranscription = false,
                )
            }

            InputMode.VOICE -> {
                val plausible =
                    corrected != null &&
                        isWellFormed(corrected) &&
                        (audioDurationMs == null || wordCount(corrected) <= maxWordsFor(audioDurationMs))
                Result(
                    message = if (plausible) withTone.copy(text = corrected!!) else withTone,
                    needsTranscription = !plausible && original.text.isBlank(),
                )
            }
        }
    }

    /** Every tag is one of ours, tags are flat (never nested) and every opened tag is closed. */
    fun isWellFormed(text: String): Boolean {
        var open: String? = null
        for (match in TAG_REGEX.findAll(text)) {
            val name = match.groupValues[1].lowercase()
            if (name !in KNOWN_TAGS) return false
            val closing = match.value.startsWith("</")
            if (closing) {
                if (open != name) return false
                open = null
            } else {
                if (open != null) return false
                open = name
            }
        }
        return open == null
    }

    private fun keepsTags(
        original: String,
        corrected: String,
    ): Boolean {
        val before = tagCounts(original)
        val after = tagCounts(corrected)
        return before.all { (tag, count) -> (after[tag] ?: 0) >= count }
    }

    private fun tagCounts(text: String): Map<String, Int> =
        TAG_REGEX
            .findAll(text)
            .filterNot { it.value.startsWith("</") }
            .groupingBy { it.groupValues[1].lowercase() }
            .eachCount()

    private fun prose(text: String) = text.replace(TAG_REGEX, "")

    private fun proseLength(text: String) = prose(text).trim().length

    private fun wordCount(text: String) = prose(text).split(Regex("\\s+")).count { it.isNotBlank() }

    private fun maxWordsFor(durationMs: Long) = (durationMs / 1000f * VOICE_MAX_WORDS_PER_SECOND).toInt() + VOICE_WORD_SLACK
}
