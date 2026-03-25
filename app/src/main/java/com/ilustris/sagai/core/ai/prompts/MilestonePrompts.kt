package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone

data class CongratsMilestoneArgs(
    val sagaMainContext: String,
    val milestoneContext: String,
    val genreName: String,
    val persona: String,
    val conversationalStyle: String,
    val referencePoints: String,
)

data class LoadingMessageArgs(
    val sagaMainContext: String,
    val genreName: String,
    val interludeStyle: String,
)

data class NewCharacterMilestoneArgs(
    val sagaMainContext: String,
    val newCharacterInfo: String,
    val genreName: String,
    val persona: String,
    val genreTone: String,
    val conversationalStyle: String,
)

data class IntroMilestoneArgs(
    val genreName: String,
    val sagaMainContext: String,
    val sceneContextSynthesize: String,
    val originalTextRewrite: String,
    val milestoneTypeContext: String,
    val persona: String,
    val genreTone: String,
)

object MilestonePrompts {
    const val CONGRATS_MILESTONE_BLUEPRINT = "congrats_milestone_blueprint"
    const val INTRO_MILESTONE_BLUEPRINT = "intro_milestone_blueprint"
    const val LOADING_MESSAGE_BLUEPRINT = "loading_message_blueprint"
    const val NEW_CHARACTER_MILESTONE_BLUEPRINT = "new_character_milestone_blueprint"

    suspend fun generateCongratsMessage(
        promptService: PromptService,
        milestone: SagaMilestone,
        saga: SagaContent,
        identity: String,
    ): String? {
        saga.data.genre
        if (milestone is SagaMilestone.Introduction) {
            return rewriteIntroduction(promptService, milestone, saga, identity)
        }

        if (milestone is SagaMilestone.Loading) {
            return generateLoadingMessage(promptService, saga, identity)
        }

        if (milestone is SagaMilestone.NewCharacter) {
            return generateNewCharacterMessage(promptService, milestone, saga, identity)
        }

        if (milestone is SagaMilestone.CurrentObjective) {
            return null
        }

        val args =
            CongratsMilestoneArgs(
                sagaMainContext = SagaPrompts.mainContext(saga, ommitCharacter = true),
                milestoneContext =
                    milestone.toAINormalize(
                        fieldsToExclude =
                            ChatPrompts.characterExclusions
                                .plus(ChapterPrompts.CHAPTER_EXCLUSIONS)
                                .plus(TimelinePrompts.timelineExclusions),
                    ),
                genreName = saga.data.genre.name,
                persona =
                identity,
                conversationalStyle = identity,
                referencePoints = getReferencePoints(milestone),
            )

        return promptService.buildRemotePrompt(CONGRATS_MILESTONE_BLUEPRINT, args)
    }

    suspend fun generateLoadingMessage(
        promptService: PromptService,
        saga: SagaContent,
        identity: String,
    ): String {
        val args =
            LoadingMessageArgs(
                sagaMainContext = SagaPrompts.mainContext(saga, ommitCharacter = true),
                genreName = saga.data.genre.name,
                interludeStyle = identity,
            )
        return promptService.buildRemotePrompt(LOADING_MESSAGE_BLUEPRINT, args)
    }

    suspend fun generateNewCharacterMessage(
        promptService: PromptService,
        milestone: SagaMilestone.NewCharacter,
        saga: SagaContent,
        identity: String,
    ): String {
        val args =
            NewCharacterMilestoneArgs(
                sagaMainContext = SagaPrompts.mainContext(saga, ommitCharacter = true),
                newCharacterInfo = milestone.character.toAINormalize(fieldsToExclude = ChatPrompts.characterExclusions),
                genreName = saga.data.genre.name,
                persona = identity,
                genreTone = identity,
                conversationalStyle = identity,
            )
        return promptService.buildRemotePrompt(NEW_CHARACTER_MILESTONE_BLUEPRINT, args)
    }

    private fun getReferencePoints(milestone: SagaMilestone): String =
        when (milestone) {
            is SagaMilestone.NewCharacter -> {
                ""
            }

            is SagaMilestone.NewEvent -> {
                """
                - The event title and what actually happened
                - Characters involved and their relationships
                - Plot importance and emotional impact
                """.trimIndent()
            }

            is SagaMilestone.ChapterFinished -> {
                """
                - Chapter title and the arc it covered
                - Major plot points and character developments
                """.trimIndent()
            }

            is SagaMilestone.ActFinished -> {
                """
                - Act title and its scope within the saga
                - Major themes and conflicts resolved
                """.trimIndent()
            }

            is SagaMilestone.CurrentObjective -> {
                """
                - The actual objective they need to achieve
                - Stakes and why it matters to the story
                """.trimIndent()
            }

            else -> {
                ""
            }
        }

    suspend fun rewriteIntroduction(
        promptService: PromptService,
        milestone: SagaMilestone.Introduction,
        saga: SagaContent,
        identity: String,
    ): String {
        val summary = milestone.sceneSummary
        val sceneContextSynthesize =
            if (summary != null) {
                """
                        - Current Mood: ${summary.mood}
                                    - Immediate Objective: ${summary.immediateObjective}
                        - Main Conflict: ${summary.currentConflict}
                        - Active Characters: ${summary.charactersPresent.joinToString()}
                    - Location: ${summary.currentLocation}
                - Narrative Weight: ${summary.tensionLevel}
                """.trimIndent()
            } else {
                ""
            }

        val milestoneTypeContext =
            if (summary == null && milestone.introduction.isBlank()) {
                "- Type: ${milestone.type.name.lowercase()}\n- This marks a significant transition in the story"
            } else {
                ""
            }

        val args =
            IntroMilestoneArgs(
                genreName = saga.data.genre.name,
                sagaMainContext = SagaPrompts.mainContext(saga, ommitCharacter = true),
                sceneContextSynthesize = sceneContextSynthesize,
                originalTextRewrite = milestone.introduction,
                milestoneTypeContext = milestoneTypeContext,
                persona = identity,
                genreTone = identity,
            )

        return promptService.buildRemotePrompt(INTRO_MILESTONE_BLUEPRINT, args)
    }
}
