package com.ilustris.sagai.features.saga.chat.data.model

/**
 * Everything a chat turn produces that isn't the reply itself.
 *
 * These used to ride along inside [AIReply], which meant the HIGH-tier reply prompt carried their
 * schema and their rules on every single turn — and generated them while it was busy writing prose.
 * They are all reactions *to* a turn that has already happened, so nothing is lost by resolving them
 * a beat later on a cheaper model, and the reply prompt gets that budget back.
 *
 * Generated after the reply is persisted and on screen, so a slow or failed fallout never delays the
 * bubble. Every field is optional for the same reason: a turn without reactions is degraded, not
 * broken.
 */
data class ReplyFallout(
    /** NPC reactions to the player's message. Excludes the player's own speaker. */
    val userReactions: List<AIReaction>? = null,
    /** NPC reactions to the character reply that just landed. */
    val replyReactions: List<AIReaction>? = null,
    /**
     * Character-voiced teaser used verbatim as a push notification if the player doesn't return.
     * Patched onto the persisted [SceneSummary] so [com.ilustris.sagai.core.notifications.NotificationGenerationWorker]
     * can stay network-free — it only ever reads what's already in the timeline.
     */
    val notificationHook: String? = null,
    /** Display name of who "says" [notificationHook]; null means a narrator-voiced hook. */
    val notificationCharacterName: String? = null,
)
