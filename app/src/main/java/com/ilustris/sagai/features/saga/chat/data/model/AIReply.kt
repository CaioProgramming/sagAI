package com.ilustris.sagai.features.saga.chat.data.model

/**
 * Structured reply from the chat AI: the message itself, the scene state it leaves behind, and
 * [NewCharacterDiscovery] when a genuinely new character enters.
 *
 * Reactions and the notification hook deliberately live in [ReplyFallout] instead — they are
 * responses to a turn that already happened, and keeping them here made every reply prompt carry
 * their schema and rules. [sceneSummary] stays: the model that just wrote the scene is the one best
 * placed to say where it left off, and continuity depends on that being right. Both tones stay too
 * — they cost an enum each and feed the review's expressiveness pages.
 */
data class AIReply(
    val message: Message,
    val sceneSummary: SceneSummary? = null,
    val newCharacter: NewCharacterDiscovery? = null,
    /**
     * Evolved read of how the player prefers to engage (e.g. combat vs. relationship/introspection
     * depth), persisted onto [com.ilustris.sagai.features.home.data.model.Saga.playerCompass] —
     * saga-wide and turn-by-turn, unlike [sceneSummary] which resets per scene. The model rewrites
     * it from its current value every turn rather than restating the immediate objective, so a
     * sustained pattern outweighs a single message.
     */
    val playerCompass: String? = null,
    /** Everything the reply read off the player's latest message. See [PlayerInputFeedback]. */
    val playerInput: PlayerInputFeedback? = null,
)

/**
 * What the reply model derived from *this* player input, grouped so anything else about the
 * player's turn has one place to go. [playerCompass][AIReply.playerCompass] stays outside: it's a
 * saga-wide read built across turns, not about one message.
 *
 * @property correctedText The player's message as they meant to send it. Typed turns: typos and
 * names fixed, or null when nothing needed fixing. Voice turns: the only text of what the player
 * said (the reply hears the audio directly), sorted into dialogue and expressive tags.
 * @property understood False when a voice turn was noise or unintelligible; the reply then reacts
 * in scene instead of advancing the plot.
 * @property emotionalTone The player's tone for this turn (was `AIReply.userTone`).
 */
data class PlayerInputFeedback(
    val correctedText: String? = null,
    val understood: Boolean = true,
    val emotionalTone: EmotionalTone? = null,
)

/**
 * A saved reply plus the player's message as it stands after the reply was applied to it
 * (corrected text, tone). Consumers downstream of the reply — the fallout, live mode — need that
 * updated message, not the one the generation started from.
 *
 * @property needsTranscription A voice turn came back without a usable [PlayerInputFeedback.correctedText],
 * so [userMessage] still has no text and has to be transcribed from its audio.
 */
data class GeneratedReply(
    val reply: AIReply,
    val userMessage: Message,
    val needsTranscription: Boolean = false,
)
