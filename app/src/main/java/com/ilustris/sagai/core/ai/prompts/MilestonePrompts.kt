package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone

data class MilestoneGenerationArgs(
    val sagaMainContext: String,
    val milestoneDetails: String,
    val genreName: String,
    val narrativeVoice: String,
)

data class IntroMilestoneArgs(
    val genreName: String,
    val sagaMainContext: String,
    val sceneContext: String,
    val introductionText: String,
    val milestoneType: String,
    val narrativeVoice: String,
)

object MilestonePrompts {
    const val MILESTONE_GENERATION_BLUEPRINT = "milestone_generation_blueprint"
    const val INTRO_MILESTONE_BLUEPRINT = "intro_milestone_blueprint"

    suspend fun generateCongratsMessage(
        promptService: PromptService,
        milestone: SagaMilestone,
        saga: SagaContent,
        identity: String,
    ): SplitPrompt? {
        if (milestone is SagaMilestone.Introduction) {
            return rewriteIntroduction(promptService, milestone, saga, identity)
        }

        if (milestone is SagaMilestone.Loading) {
            return null
        }

        val args =
            MilestoneGenerationArgs(
                sagaMainContext = SagaPrompts.mainContext(saga, ommitCharacter = true),
                milestoneDetails = getMilestoneDetails(milestone),
                genreName = saga.data.genre.name,
                narrativeVoice = identity,
            )

        return promptService.buildSplitBlueprint(MILESTONE_GENERATION_BLUEPRINT, args)
    }

    private fun getMilestoneDetails(milestone: SagaMilestone): String =
        when (milestone) {
            is SagaMilestone.NewCharacter -> milestone.character.toAINormalize()
            is SagaMilestone.NewEvent -> milestone.timeline.toAINormalize()
            is SagaMilestone.ChapterFinished -> milestone.chapter.toAINormalize()
            is SagaMilestone.ActFinished -> milestone.act.toAINormalize()
            else -> milestone.javaClass.simpleName
        }

    suspend fun rewriteIntroduction(
        promptService: PromptService,
        milestone: SagaMilestone.Introduction,
        saga: SagaContent,
        identity: String,
    ): SplitPrompt {
        val args =
            IntroMilestoneArgs(
                genreName = saga.data.genre.name,
                sagaMainContext = SagaPrompts.mainContext(saga, ommitCharacter = true),
                sceneContext = milestone.sceneSummary?.toAINormalize() ?: "",
                introductionText = milestone.introduction,
                milestoneType = milestone.type.name,
                narrativeVoice = identity,
            )

        return promptService.buildSplitBlueprint(INTRO_MILESTONE_BLUEPRINT, args)
    }
}
