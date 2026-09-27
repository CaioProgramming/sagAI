package com.ilustris.sagai.features.live.presentation

import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.saga.chat.data.voicing.MessageBlock

/** Where the live turn stands. See docs/feature_planning/live_conversation/plan.md. */
sealed interface LivePhase {
    data object Idle : LivePhase

    /** Holding the button; the blob follows the mic level. */
    data object Listening : LivePhase

    /** Recording sent; the sender stays in focus while the reply is written. */
    data object Thinking : LivePhase

    /** Reply text landed; the player's corrected line is on stage while the audio is made. */
    data object Voicing : LivePhase

    /** The reply playing. [silent] when there's no audio (voice quota spent, TTS failed): captions only. */
    data class Speaking(
        val silent: Boolean,
    ) : LivePhase

    /** A short beat after an error before going back to Idle. */
    data object Recovering : LivePhase
}

/** What the hint line under the blob says. */
enum class LiveHint {
    HOLD_TO_TALK,
    RELEASE_TO_SEND,
    RELEASE_TO_CANCEL,
    CANCELLED,
    TOO_SHORT,
    WAITING_REPLY,
    PREPARING_VOICE,
    INTERRUPT,
    SPEAKING_SILENTLY,
    REPLY_FAILED,
    GUARDRAIL,
    MIC_UNAVAILABLE,
    PERMISSION_NEEDED,
    MILESTONE,
}

/** Whoever is inside the blob: a character, or the narrator (the saga's own art). */
data class LiveFocus(
    val character: Character?,
) {
    val isNarrator get() = character == null
}

/**
 * The line under the blob, split into typed blocks. [current] is the block being spoken (-1 when
 * none yet); earlier blocks read as done, later ones as pending.
 */
data class LiveCaption(
    val blocks: List<MessageBlock>,
    val current: Int = -1,
    val isPlayerLine: Boolean = false,
    val allDone: Boolean = false,
)

/** A reaction that just landed, to orbit the portrait in focus. */
data class LiveReaction(
    val id: Int,
    val emoji: String,
    val thought: String?,
    val character: Character,
)

data class LiveUiState(
    val saga: SagaMetadata? = null,
    val speakers: List<Character> = emptyList(),
    val selectedSpeakerId: Int? = null,
    val phase: LivePhase = LivePhase.Idle,
    val focus: LiveFocus? = null,
    val caption: LiveCaption? = null,
    /** The player's line before the reply corrected it: shown as "transcribing…". */
    val playerLinePending: Boolean = false,
    val reasoning: String? = null,
    val hint: LiveHint = LiveHint.HOLD_TO_TALK,
    /** Set when the voice (TTS) quota is spent: the session speaks through captions until then. */
    val voicesRestingUntil: Long? = null,
    /** An intrusive milestone is up: no new turns. */
    val inputBlocked: Boolean = false,
    val canRetry: Boolean = false,
) {
    val selectedSpeaker get() = speakers.find { it.id == selectedSpeakerId } ?: speakers.firstOrNull()
}
