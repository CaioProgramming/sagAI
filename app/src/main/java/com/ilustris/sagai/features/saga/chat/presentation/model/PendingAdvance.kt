package com.ilustris.sagai.features.saga.chat.presentation.model

import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.ActMetadata
import com.ilustris.sagai.features.home.data.model.ChapterMetadata
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.home.data.model.TimelineMetadata

sealed class PendingAdvance(
    val titleRes: Int,
    val holdingTextRes: Int,
) {
    data class NewEvent(
        val timeline: TimelineMetadata,
    ) : PendingAdvance(R.string.advance_new_event, R.string.releasing_event)

    data class NewChapter(
        val chapter: ChapterMetadata,
    ) : PendingAdvance(R.string.advance_new_chapter, R.string.releasing_chapter)

    data class NewAct(
        val act: ActMetadata,
    ) : PendingAdvance(R.string.advance_new_act, R.string.releasing_act)

    data object StartAct : PendingAdvance(R.string.advance_new_act, R.string.releasing_act)

    data class NewActIntroduction(
        val act: ActMetadata,
    ) : PendingAdvance(R.string.advance_new_act_introduction, R.string.releasing_act_introduction)

    data class NewChapterIntroduction(
        val chapter: ChapterMetadata,
    ) : PendingAdvance(
            R.string.advance_new_chapter_introduction,
            R.string.releasing_chapter_introduction,
        )

    data class StartChapter(
        val act: ActMetadata,
    ) : PendingAdvance(R.string.advance_new_chapter, R.string.releasing_chapter)

    data class StartStory(
        val chapter: ChapterMetadata,
    ) : PendingAdvance(R.string.story_sheet_title_history_continues, R.string.releasing_start_story)

    data class SagaEnding(
        val saga: SagaMetadata,
    ) : PendingAdvance(R.string.advance_saga_ending, R.string.releasing_ending)
}
