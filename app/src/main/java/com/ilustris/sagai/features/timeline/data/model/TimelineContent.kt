package com.ilustris.sagai.features.timeline.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.features.characters.events.data.model.CharacterEvent
import com.ilustris.sagai.features.characters.events.data.model.CharacterEventDetails
import com.ilustris.sagai.features.saga.chat.domain.model.Message
import com.ilustris.sagai.features.saga.chat.domain.model.MessageContent

data class TimelineContent(
    @Embedded
    val data: Timeline,
    @Relation(
        parentColumn = "id",
        entityColumn = "timelineId",
        entity = Message::class,
    )
    val messages: List<MessageContent> = emptyList(),
    @Relation(
        parentColumn = "id",
        entity = CharacterEvent::class,
        entityColumn = "gameTimelineId",
    )
    val characterEventDetails: List<CharacterEventDetails> = emptyList(),
) {
    fun isFull(): Boolean = messages.size >= UpdateRules.LORE_UPDATE_LIMIT

    fun isComplete(): Boolean =
        isFull() &&
            data.title.isNotEmpty() &&
            data.content.isNotEmpty()
}
