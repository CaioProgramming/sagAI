package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaMetadata

data class VoiceCastingArgs(
    val sagaContext: String,
    val character: String,
    val characterGender: String,
    val characterAge: String,
    val voiceGuide: String,
    val voicesInUse: String,
)

data class AudioPerformanceArgs(
    val speaker: String,
    val speakerVoiceDirection: String,
    val emotionalTone: String,
    val sceneBrief: String,
    /** The message's typed blocks, one per line: `index | TYPE | text`. */
    val blocks: String,
)

/** Model output of `voice_casting_blueprint`. */
data class VoiceCastingGen(
    val voice: String = "",
    val voiceDirection: String = "",
)

object VoicePrompts {
    const val VOICE_CASTING_BLUEPRINT = "voice_casting_blueprint"
    const val AUDIO_PERFORMANCE_BLUEPRINT = "audio_performance_blueprint"

    private val characterExclusions =
        listOf(
            "id",
            "image",
            "sagaId",
            "joinedAt",
            "firstSceneId",
            "emojified",
            "hexColor",
            "voice",
            "voiceDirection",
            "artwork",
            "clothing",
            "abilities",
        )

    suspend fun castingPrompt(
        promptService: PromptService,
        saga: SagaMetadata,
        character: Character,
        voiceGuide: String,
        voicesInUse: List<String>,
    ): SplitPrompt =
        promptService.buildSplitBlueprint(
            VOICE_CASTING_BLUEPRINT,
            VoiceCastingArgs(
                sagaContext = "${saga.data.title} (${saga.data.genre.name}): ${saga.data.description}",
                character = character.toAINormalize(characterExclusions),
                characterGender = character.details.physicalTraits.gender,
                characterAge =
                    character.details.physicalTraits.age
                        .takeIf { it > 0 }
                        ?.toString()
                        .orEmpty(),
                voiceGuide = voiceGuide,
                voicesInUse = voicesInUse.joinToString().ifBlank { "none" },
            ),
        )

    suspend fun performancePrompt(
        promptService: PromptService,
        args: AudioPerformanceArgs,
    ): SplitPrompt = promptService.buildSplitBlueprint(AUDIO_PERFORMANCE_BLUEPRINT, args)
}
