package com.ilustris.sagai.core.notifications

import kotlinx.serialization.Serializable

@Serializable
data class ScheduledNotification(
    val sagaId: String,
    val sagaTitle: String,
    val characterId: String,
    val characterName: String,
    val characterAvatarPath: String?,
    val generatedMessage: String,
    val exitTimestamp: Long,
    val scheduledTimestamp: Long,
    val generationTimestamp: Long,
)
