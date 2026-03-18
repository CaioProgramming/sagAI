package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.Voice
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.SenderType

data class AudioConfigArgs(
    val sagaMainContext: String,
    val messageContext: String,
    val senderTypeInfo: String,
    val characterProfile: String,
    val characterGender: String,
    val voiceSelectionGuide: String,
)

object AudioPrompts {
    fun transcribeInstruction() = "Generate a short, playful message about listening to the user. Example: 'I'm all ears!'"

    suspend fun audioConfigPrompt(
        promptService: PromptService,
        sagaContent: SagaContent,
        message: Message,
        character: CharacterContent?,
    ): String {
        val senderTypeInfo =
            if (message.senderType == SenderType.NARRATOR) {
                """
                ## NARRATOR CONTEXT
                This is NARRATOR text - story exposition, scene descriptions, or system messages.
                Use a clear, professional, neutral narrator voice.
                The audio should have a neutral tone suitable for narration.
                Select the best narrator voice (usually MALE for authoritative or professional tones, but check saga context).
                """.trimIndent()
            } else {
                ""
            }

        val characterProfile =
            character?.data?.toAINormalize(
                listOf(
                    "id",
                    "image",
                    "sagaId",
                    "joinedAt",
                    "details",
                    "emojified",
                    "hexColor",
                    "firstSceneId",
                    "smartZoom",
                    "voice",
                ),
            )
                ?: ""

        val characterGender =
            character
                ?.data
                ?.details
                ?.physicalTraits
                ?.gender
                ?: "Search description/context for gender (e.g. 'woman', 'man', 'he', 'she')"

        val args =
            AudioConfigArgs(
                sagaMainContext = SagaPrompts.mainContext(sagaContent, character),
                messageContext = message.toAINormalize(ChatPrompts.messageExclusions),
                senderTypeInfo = senderTypeInfo,
                characterProfile = characterProfile,
                characterGender = characterGender,
                voiceSelectionGuide = Voice.getVoiceSelectionGuide(),
            )

        return promptService.buildRemotePrompt("audio_config_blueprint", args)
    }
}
