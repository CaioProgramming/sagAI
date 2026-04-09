package com.ilustris.sagai.features.saga.chat.presentation.model

import com.ilustris.sagai.R
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.TimelineContent

sealed class PendingAdvance(
    val titleRes: Int,
    val holdingTextRes: Int,
) {
    data class NewEvent(
        val timeline: TimelineContent,
    ) : PendingAdvance(R.string.advance_new_event, R.string.releasing_event)

    data class NewChapter(
        val chapter: ChapterContent,
    ) : PendingAdvance(R.string.advance_new_chapter, R.string.releasing_chapter)

    data class NewAct(
        val act: ActContent,
    ) : PendingAdvance(R.string.advance_new_act, R.string.releasing_act)

    data class NewActIntroduction(
        val act: ActContent,
    ) : PendingAdvance(R.string.advance_new_act_introduction, R.string.releasing_act_introduction)

    data class NewChapterIntroduction(
        val chapter: ChapterContent,
    ) : PendingAdvance(
            R.string.advance_new_chapter_introduction,
            R.string.releasing_chapter_introduction,
        )

    data class StartStory(
        val chapter: ChapterContent,
    ) : PendingAdvance(R.string.story_sheet_title_history_continues, R.string.releasing_start_story)

    data class SagaEnding(
        val saga: SagaContent,
    ) : PendingAdvance(R.string.advance_saga_ending, R.string.releasing_ending)
}
