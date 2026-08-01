package com.ilustris.sagai.features.debug.ui

import MessageStatus
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Abilities
import com.ilustris.sagai.features.characters.data.model.BodyFeatures
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterProfile
import com.ilustris.sagai.features.characters.data.model.Clothing
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.data.model.FacialFeatures
import com.ilustris.sagai.features.characters.data.model.PhysicalTraits
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.presentation.model.IntroductionType
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
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

    fun mockSagaContent(genre: Genre) = SagaContent(data = mockSaga(genre))

    fun mockNewEventMilestone(genre: Genre) =
        SagaMilestone.NewEvent(
            timeline =
                Timeline(
                    id = 1,
                    chapterId = 1,
                    title = "The Signal in the Static",
                    emotionalReview = "The Traveler's excitement curdles into dread the moment the static clears — this is the first real crack in their confidence all saga.",
                ),
            emotionalMascot = null,
            messageText = "The static clears just long enough for you to make out a single word before the signal dies again.",
            sagaContent = mockSagaContent(genre),
        )

    fun mockChapterFinishedMilestone(genre: Genre) =
        SagaMilestone.ChapterFinished(
            chapter =
                Chapter(
                    id = 1,
                    actId = 1,
                    title = "Ashes of the Old Guard",
                    emotionalReview = "A chapter defined by loss dressed up as victory — every choice here cost more than it looked like at the time.",
                ),
            messageText = "Every thread from this chapter converges here — nothing that happened was wasted.",
            sagaContent = mockSagaContent(genre),
        )

    fun mockActFinishedMilestone() =
        SagaMilestone.ActFinished(
            act =
                Act(
                    id = 1,
                    title = "The Fracture",
                    emotionalReview = "This act asked what loyalty actually costs, and the Traveler paid every price without flinching once.",
                ),
            messageText = "The dust settles on everything you fought for in this act.",
        )

    fun mockIntroductionMilestone() =
        SagaMilestone.Introduction(
            type = IntroductionType.CHAPTER,
            titleText = "Ashes of the Old Guard",
            introduction = "The city has not forgiven you yet — and it may never get the chance to.",
            number = "Chapter - II",
        )
}
