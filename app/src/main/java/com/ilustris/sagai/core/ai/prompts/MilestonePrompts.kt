package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.GenreConfig.CompanionConfig
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone

data class CongratsMilestoneArgs(
    val sagaMainContext: String,
    val milestoneContext: String,
    val genreName: String,
    val persona: String,
    val genreTone: String,
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
    suspend fun generateCongratsMessage(
        promptService: PromptService,
        milestone: SagaMilestone,
        saga: SagaContent,
        companion: CompanionConfig?,
    ): String? {
        if (milestone is SagaMilestone.Introduction) {
            return rewriteIntroduction(promptService, milestone, saga, companion)
        }

        if (milestone is SagaMilestone.Loading) {
            return generateLoadingMessage(promptService, saga, companion)
        }

        if (milestone is SagaMilestone.NewCharacter) {
            return generateNewCharacterMessage(promptService, milestone, saga, companion)
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
                    companion?.persona
                        ?: "Enjoys commenting playfully on story twists and turns.",
                genreTone = companion?.tone ?: "",
                conversationalStyle = companion?.conversationalStyle ?: "Be creatively conversational.",
                referencePoints = getReferencePoints(milestone),
            )

        return promptService.buildRemotePrompt("congrats_milestone_blueprint", args)
    }

    suspend fun generateLoadingMessage(
        promptService: PromptService,
        saga: SagaContent,
        companion: CompanionConfig?,
    ): String {
        val args =
            LoadingMessageArgs(
                sagaMainContext = SagaPrompts.mainContext(saga, ommitCharacter = true),
                genreName = saga.data.genre.name,
                interludeStyle = companion?.interludeStyle ?: "A funny short loading text string",
            )
        return promptService.buildRemotePrompt("loading_message_blueprint", args)
    }

    suspend fun generateNewCharacterMessage(
        promptService: PromptService,
        milestone: SagaMilestone.NewCharacter,
        saga: SagaContent,
        companion: CompanionConfig?,
    ): String {
        val args =
            NewCharacterMilestoneArgs(
                sagaMainContext = SagaPrompts.mainContext(saga, ommitCharacter = true),
                newCharacterInfo = milestone.character.toAINormalize(fieldsToExclude = ChatPrompts.characterExclusions),
                genreName = saga.data.genre.name,
                persona = companion?.persona ?: "Observes new allies with skepticism and amusement.",
                genreTone = companion?.tone ?: "",
            conversationalStyle = companion?.conversationalStyle ?: "Greet them creatively.",
            )
        return promptService.buildRemotePrompt("new_character_milestone_blueprint", args)
    }

    private fun getReferencePoints(milestone: SagaMilestone): String =
        when (milestone) {
            is SagaMilestone.NewCharacter -> {
                ""
            }

            is SagaMilestone.NewEvent ->
                """
                - The event title and what actually happened
                - Characters involved and their relationships
                - Plot importance and emotional impact
                """.trimIndent()

            is SagaMilestone.ChapterFinished -> """
                - Chapter title and the arc it covered
                - Major plot points and character developments
            """.trimIndent()

            is SagaMilestone.ActFinished -> """
                - Act title and its scope within the saga
                - Major themes and conflicts resolved
                """.trimIndent()

            is SagaMilestone.CurrentObjective -> """
                - The actual objective they need to achieve
                - Stakes and why it matters to the story
                """.trimIndent()

            else -> {
                ""
            }
        }

    suspend fun rewriteIntroduction(
        promptService: PromptService,
        milestone: SagaMilestone.Introduction,
        saga: SagaContent,
        companion: CompanionConfig?,
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
            persona = companion?.persona ?: "Enjoys observing heroes face their fate.",
            genreTone = companion?.tone ?: ""
        )

        return promptService.buildRemotePrompt("intro_milestone_blueprint", args)
    }
}
