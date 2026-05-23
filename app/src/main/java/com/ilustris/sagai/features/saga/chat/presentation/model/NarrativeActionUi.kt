package com.ilustris.sagai.features.saga.chat.presentation.model

import com.ilustris.sagai.R
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeAction
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeExecutionMode
import com.ilustris.sagai.features.saga.chat.domain.manager.executionMode

data class NarrativeActionUi(
    val titleRes: Int,
    val holdingTextRes: Int,
    val executionMode: NarrativeExecutionMode,
)

fun NarrativeAction.toUi(): NarrativeActionUi {
    val mode = executionMode()
    val (titleRes, holdingTextRes) =
        when (this) {
            is NarrativeAction.EvolveTimeline ->
                R.string.advance_evolve_timeline to R.string.releasing_evolve_timeline

            is NarrativeAction.GenerateChapter ->
                R.string.advance_close_chapter to R.string.releasing_close_chapter

            is NarrativeAction.CreateChapter ->
                R.string.advance_open_chapter to R.string.releasing_open_chapter

            is NarrativeAction.GenerateAct ->
                R.string.advance_close_act to R.string.releasing_close_act

            NarrativeAction.CreateAct ->
                R.string.advance_create_act to R.string.releasing_create_act

            is NarrativeAction.GenerateActIntro ->
                R.string.advance_new_act_introduction to R.string.releasing_act_introduction

            is NarrativeAction.GenerateChapterIntro ->
                R.string.advance_new_chapter_introduction to R.string.releasing_chapter_introduction

            is NarrativeAction.CreateTimeline ->
                R.string.advance_start_story to R.string.releasing_start_story

            is NarrativeAction.GenerateEnding ->
                R.string.advance_saga_ending to R.string.releasing_ending

            is NarrativeAction.CloseTimeline ->
                0 to R.string.releasing_close_scene
        }
    return NarrativeActionUi(titleRes, holdingTextRes, mode)
}
