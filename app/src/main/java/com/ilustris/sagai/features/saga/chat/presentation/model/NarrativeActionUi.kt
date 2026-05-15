package com.ilustris.sagai.features.saga.chat.presentation.model

import com.ilustris.sagai.R
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeAction
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeExecutionMode

data class NarrativeActionUi(
    val titleRes: Int,
    val holdingTextRes: Int,
    val executionMode: NarrativeExecutionMode,
)

fun NarrativeAction.toUi(): NarrativeActionUi =
    when (this) {
        is NarrativeAction.EvolveTimeline -> {
            NarrativeActionUi(
                R.string.advance_evolve_timeline,
                R.string.releasing_evolve_timeline,
                NarrativeExecutionMode.UserTriggered,
            )
        }

        is NarrativeAction.GenerateChapter -> {
            NarrativeActionUi(
                R.string.advance_close_chapter,
                R.string.releasing_close_chapter,
                NarrativeExecutionMode.UserTriggered,
            )
        }

        is NarrativeAction.CreateChapter -> {
            NarrativeActionUi(
                R.string.advance_open_chapter,
                R.string.releasing_open_chapter,
                NarrativeExecutionMode.UserTriggered,
            )
        }

        is NarrativeAction.GenerateAct -> {
            NarrativeActionUi(
                R.string.advance_close_act,
                R.string.releasing_close_act,
                NarrativeExecutionMode.UserTriggered,
            )
        }

        NarrativeAction.CreateAct -> {
            NarrativeActionUi(
                R.string.advance_create_act,
                R.string.releasing_create_act,
                NarrativeExecutionMode.UserTriggered,
            )
        }

        is NarrativeAction.GenerateActIntro -> {
            NarrativeActionUi(
                R.string.advance_new_act_introduction,
                R.string.releasing_act_introduction,
                NarrativeExecutionMode.UserTriggered,
            )
        }

        is NarrativeAction.GenerateChapterIntro -> {
            NarrativeActionUi(
                R.string.advance_new_chapter_introduction,
                R.string.releasing_chapter_introduction,
                NarrativeExecutionMode.UserTriggered,
            )
        }

        is NarrativeAction.CreateTimeline -> {
            NarrativeActionUi(
                R.string.advance_start_story,
                R.string.releasing_start_story,
                NarrativeExecutionMode.UserTriggered,
            )
        }

        is NarrativeAction.GenerateEnding -> {
            NarrativeActionUi(
                R.string.advance_saga_ending,
                R.string.releasing_ending,
                NarrativeExecutionMode.UserTriggered,
            )
        }

        is NarrativeAction.CloseTimeline -> {
            NarrativeActionUi(
                0,
                R.string.releasing_close_scene,
                NarrativeExecutionMode.Automatic,
            )
        }
    }
