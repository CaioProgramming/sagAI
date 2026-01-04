package com.ilustris.sagai.features.saga.chat.presentation.model

import com.ilustris.sagai.R
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.timeline.data.model.Timeline

sealed class SagaMilestone(
    val title: Int,
    val subtitle: String,
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
            character.name,
        )
}
