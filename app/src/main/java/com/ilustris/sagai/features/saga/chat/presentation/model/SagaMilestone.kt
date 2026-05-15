package com.ilustris.sagai.features.saga.chat.presentation.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.milestone.ui.NewChapterContent
import com.ilustris.sagai.features.milestone.ui.NewCharacterContent
import com.ilustris.sagai.features.milestone.ui.NewEventContent
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.timeline.data.model.Timeline
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

enum class IntroductionType { ACT, CHAPTER, RESUME }

@Immutable
sealed class SagaMilestone(
    val title: Int,
    val subtitle: String,
    val message: String? = null,
    val delay: Duration = 7.seconds,
    val isIntrusive: Boolean = true,
    val extraContent: @Composable () -> Unit = {},
) {
    data class NewEvent(
        val timeline: Timeline,
        val emotionalMascot: String?,
        val messageText: String? = null,
        val sagaContent: SagaContent,
    ) : SagaMilestone(
            R.string.advance_evolve_timeline,
            timeline.title,
            messageText,
            extraContent = { NewEventContent(sagaContent, timeline.id, emotionalMascot) },
        )

    data class ChapterFinished(
        val chapter: Chapter,
        val messageText: String? = null,
        val sagaContent: SagaContent,
    ) : SagaMilestone(
            R.string.notification_new_chapter,
            chapter.title,
            messageText,
            extraContent = { NewChapterContent(sagaContent, chapter.id) },
        )

    data class ActFinished(
        val act: Act,
        val messageText: String? = null,
    ) : SagaMilestone(
            R.string.notification_new_act,
            act.title,
            messageText,
        )

    data class NewCharacter(
        val character: Character,
        private val messageText: String? = null,
        private val saga: Saga,
    ) : SagaMilestone(
            R.string.notification_new_character,
            "${character.name} ${character.lastName ?: emptyString()}",
            messageText,
            delay = 2.seconds,
            extraContent = { NewCharacterContent(saga, character) },
        )

    data class CurrentObjective(
        val timeline: Timeline,
    ) : SagaMilestone(
            R.string.current_objective,
            timeline.currentObjective ?: "",
            delay = 0.seconds,
            isIntrusive = false,
        )

    data class Introduction(
        val type: IntroductionType,
        val titleText: String,
        val introduction: String,
        val number: String,
        val messageText: String? = null,
        val sceneSummary: SceneSummary? = null,
    ) : SagaMilestone(
            R.string.introduction_milestone,
            titleText,
            messageText,
            delay = 0.seconds,
            isIntrusive = true,
        )

    data object Loading : SagaMilestone(
        title = 0,
        subtitle = "",
        delay = 0.seconds,
        isIntrusive = true,
    )

    data object TitleSplash : SagaMilestone(
        title = 0,
        subtitle = "",
        delay = 0.seconds,
        isIntrusive = true,
    )
}

enum class LoadingType {
    ACT,
    CHAPTER,
    EVENT,
    ENDING,
    CHARACTER,
}
