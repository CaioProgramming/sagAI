package com.ilustris.sagai.features.saga.chat.presentation.model

import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.timeline.data.model.Timeline
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

sealed class SagaMilestone(
    val title: Int,
    val subtitle: String,
    val delay: Duration = 7.seconds,
    val isIntrusive: Boolean = true,
) {
    class NewEvent(
        val timeline: Timeline,
    ) : SagaMilestone(
            R.string.new_event_milestone_title,
            timeline.title,
        )

    class ChapterFinished(
        val chapter: Chapter,
    ) : SagaMilestone(
            R.string.notification_new_chapter,
            chapter.title,
        )

    class ActFinished(
        val act: Act,
    ) : SagaMilestone(
            R.string.notification_new_act,
            act.title,
        )

    class NewCharacter(
        val character: Character,
    ) : SagaMilestone(
            R.string.notification_new_character,
            "${character.name} ${character.lastName ?: emptyString()}",
            delay = 2.seconds,
    )

    class CurrentObjective(
        val timeline: Timeline,
    ) : SagaMilestone(
            R.string.current_objective,
            timeline.currentObjective ?: "",
            delay = 0.seconds,
            isIntrusive = false,
    )
}
