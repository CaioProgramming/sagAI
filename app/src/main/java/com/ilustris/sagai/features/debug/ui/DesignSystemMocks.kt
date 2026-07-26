package com.ilustris.sagai.features.debug.ui

import MessageStatus
import com.ilustris.sagai.features.characters.data.model.Abilities
import com.ilustris.sagai.features.characters.data.model.BodyFeatures
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterProfile
import com.ilustris.sagai.features.characters.data.model.Clothing
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.data.model.FacialFeatures
import com.ilustris.sagai.features.characters.data.model.PhysicalTraits
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent

object DesignSystemMocks {
    fun mockSaga(
        genre: Genre,
        icon: String = "",
    ) = Saga(
        id = 1,
        title = "${genre.name} Saga",
        description = "A mysterious story unfolding in the ${genre.name} universe. Your choices will determine the fate of all.",
        genre = genre,
        icon = icon,
        createdAt = System.currentTimeMillis(),
    )

    fun mockCharacter(
        id: Int,
        name: String,
    ) = Character(
        id = id,
        name = name,
        sagaId = 1,
        backstory = "A hero from the distant lands.",
        details =
            Details(
                physicalTraits =
                    PhysicalTraits(
                        race = "Human",
                        gender = "Neutral",
                        ethnicity = "Cosmic",
                        height = 1.75,
                        weight = 70.0,
                        facialDetails = FacialFeatures("", "", "", "", ""),
                        bodyFeatures = BodyFeatures("", "", ""),
                    ),
                clothing = Clothing("", "", ""),
                abilities = Abilities("", ""),
            ),
        profile = CharacterProfile("Traveler", "Adventurous"),
    )

    fun mockMessageContent(
        id: Int,
        text: String,
        senderType: SenderType,
        character: Character? = null,
        status: MessageStatus = MessageStatus.OK,
    ) = MessageContent(
        message =
            Message(
                id = id,
                text = text,
                senderType = senderType,
                characterId = character?.id,
                sagaId = 1,
                timelineId = 1,
                status = status,
            ),
        character = character,
        reactions = emptyList(),
    )

    fun mockSagaMetadata(
        genre: Genre,
        icon: String = "",
    ): SagaMetadata {
        val traveler = mockCharacter(1, "The Traveler")
        return SagaMetadata(
            data = mockSaga(genre, icon),
            mainCharacter = traveler,
            characters = listOf(traveler, mockCharacter(2, "Narrator")),
            acts = emptyList(),
            wikis = emptyList(),
            messages = emptyList(),
            relationships = emptyList(),
        )
    }

    fun mockTimelineContent() =
        TimelineContent(
            data = Timeline(id = 1, chapterId = 1),
            messages = emptyList(),
        )
}
