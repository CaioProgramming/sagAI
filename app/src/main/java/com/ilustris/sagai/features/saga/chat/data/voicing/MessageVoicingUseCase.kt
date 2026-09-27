package com.ilustris.sagai.features.saga.chat.data.voicing

import com.ilustris.sagai.core.ai.AudioGenClient
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.ai.model.Voice
import com.ilustris.sagai.core.ai.model.createAudioGenerationRequest
import com.ilustris.sagai.core.ai.model.createMultiSpeakerAudioRequest
import com.ilustris.sagai.core.ai.prompts.AudioPerformanceArgs
import com.ilustris.sagai.core.ai.prompts.VoicePrompts
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.repository.MessageRepository
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A message voiced and saved: [message] now points at [audioPath]. [script] and [blocks] let the
 * live captions follow which part of the message is playing.
 */
data class VoicedMessage(
    val message: Message,
    val audioPath: String,
    val script: PerformanceScript,
    val blocks: List<MessageBlock>,
)

/**
 * Turns a saved message into its audio performance: cast voices (usually already done) → a
 * performance script from `audio_performance_blueprint` (Gemma, falls back to a deterministic
 * script) → one TTS call with up to two speakers (the character + the narrator) → the WAV saved
 * on the message.
 *
 * Replaces the old per-message `audio_config_blueprint` call and the tag-stripping that dropped
 * narration from character messages. Errors from the TTS (including
 * [com.ilustris.sagai.core.ai.QuotaExhaustedException]) propagate so the caller can decide how to
 * degrade.
 */
@Singleton
class MessageVoicingUseCase
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
        private val promptService: PromptService,
        private val audioGenClient: AudioGenClient,
        private val voiceCasting: VoiceCastingUseCase,
        private val fileHelper: FileHelper,
        private val messageRepository: MessageRepository,
    ) {
        /** Null when the message has nothing a voice can perform (e.g. only a thought). */
        suspend fun voice(
            saga: SagaMetadata,
            message: Message,
            sceneBrief: String? = null,
        ): VoicedMessage? {
            val blocks = MessageBlocks.split(message.text)
            if (blocks.isEmpty()) return null

            val character =
                message.characterId
                    ?.takeIf { message.senderType != SenderType.NARRATOR }
                    ?.let { id -> saga.characters.find { it.id == id } }
            val cast = character?.let { voiceCasting.voiceFor(saga, it) }
            val narratorVoice = voiceCasting.narratorVoice(saga)

            val script = script(message, character, cast, blocks, sceneBrief)
            if (script.lines.isEmpty()) return null

            val request = buildRequest(script, character, cast, narratorVoice)
            val wav = audioGenClient.generate(request)
            val file =
                fileHelper.saveBinaryFile(
                    wav,
                    path = "sagas/${saga.data.id}/audios",
                    fileName = "message_${message.id}_audio",
                    extension = "wav",
                ) ?: error("Could not save the audio for message ${message.id}")

            val updated = messageRepository.updateMessage(message.copy(audioPath = file.absolutePath, audible = true))
            return VoicedMessage(updated, file.absolutePath, script, blocks)
        }

        private suspend fun script(
            message: Message,
            character: Character?,
            cast: CastVoice?,
            blocks: List<MessageBlock>,
            sceneBrief: String?,
        ): PerformanceScript {
            val speaker = character?.name
            val generated =
                withTimeoutOrNull(SCRIPT_TIMEOUT_MS) {
                    runCatching {
                        gemmaClient.generate<PerformanceScript>(
                            promptSplit =
                                VoicePrompts.performancePrompt(
                                    promptService,
                                    AudioPerformanceArgs(
                                        speaker = speaker ?: NARRATOR_SPEAKER,
                                        speakerVoiceDirection = cast?.direction.orEmpty(),
                                        emotionalTone = message.emotionalTone?.name.orEmpty(),
                                        sceneBrief = sceneBrief.orEmpty(),
                                        blocks = MessageBlocks.render(blocks),
                                    ),
                                ),
                            requireTranslation = false,
                            requirement = ModelRequirement.LOW,
                        )
                    }.onFailure { Timber.w(it, "Performance script failed, using the deterministic one") }
                        .getOrNull()
                }
            return generated
                ?.let { PerformanceScripts.sanitize(it, speaker, blocks.size) }
                ?.takeIf { it.lines.isNotEmpty() }
                ?: PerformanceScripts.deterministic(blocks, speaker)
        }

        private fun buildRequest(
            script: PerformanceScript,
            character: Character?,
            cast: CastVoice?,
            narratorVoice: Voice,
        ) = run {
            val characterVoice = cast?.voice ?: narratorVoice
            val speakers = script.lines.map { it.isNarrator }.distinct()
            val directions =
                listOfNotNull(
                    script.style.takeIf { it.isNotBlank() },
                    cast?.direction?.takeIf { it.isNotBlank() }?.let { "${character?.name}: $it" },
                ).joinToString("\n").ifBlank { null }

            if (speakers.size == 1) {
                createAudioGenerationRequest(
                    text = script.lines.joinToString(" ") { it.text },
                    voice = if (speakers.first()) narratorVoice else characterVoice,
                    instruction = directions,
                )
            } else {
                val characterLabel = ttsLabel(character?.name)
                createMultiSpeakerAudioRequest(
                    script =
                        script.lines.joinToString("\n") { line ->
                            "${if (line.isNarrator) NARRATOR_LABEL else characterLabel}: ${line.text}"
                        },
                    speakers = mapOf(NARRATOR_LABEL to narratorVoice, characterLabel to characterVoice),
                    instruction = directions,
                )
            }
        }

        /** Speaker names must match between the config and the "Name:" prefixes; keep them plain. */
        private fun ttsLabel(name: String?): String =
            name
                ?.filter { it.isLetterOrDigit() }
                ?.takeIf { it.isNotBlank() && !it.equals(NARRATOR_LABEL, ignoreCase = true) }
                ?: "Speaker"

        companion object {
            private const val SCRIPT_TIMEOUT_MS = 3_000L
            private const val NARRATOR_LABEL = "Narrator"
        }
    }
