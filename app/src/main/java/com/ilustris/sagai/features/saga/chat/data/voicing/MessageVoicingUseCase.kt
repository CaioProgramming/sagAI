package com.ilustris.sagai.features.saga.chat.data.voicing

import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
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
    val clip: VoicedClip,
)

/** A performed WAV, with the script and blocks the live captions follow. */
data class VoicedClip(
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
            val character =
                message.characterId
                    ?.takeIf { message.senderType != SenderType.NARRATOR }
                    ?.let { id -> saga.characters.find { it.id == id } }
            val clip =
                voiceText(
                    saga = saga,
                    character = character,
                    text = message.text,
                    emotionalTone = message.emotionalTone,
                    directory = "sagas/${saga.data.id}/audios",
                    fileName = "message_${message.id}_audio",
                    sceneBrief = sceneBrief,
                ) ?: return null
            val updated = messageRepository.updateMessage(message.copy(audioPath = clip.audioPath, audible = true))
            return VoicedMessage(updated, clip)
        }

        /**
         * Performs [text] as [character] (the narrator when null) and saves the WAV under
         * [directory] (relative to the app's files dir), touching no table — for lines that don't
         * live in the saga's messages, like the epilogue's. Null when there is nothing to perform.
         */
        suspend fun voiceText(
            saga: SagaMetadata,
            character: Character?,
            text: String,
            emotionalTone: EmotionalTone?,
            directory: String,
            fileName: String,
            sceneBrief: String? = null,
        ): VoicedClip? {
            val blocks = MessageBlocks.split(text)
            if (blocks.isEmpty()) return null

            val cast = character?.let { voiceCasting.voiceFor(saga, it) }
            val narratorVoice = voiceCasting.narratorVoice(saga)

            val script = script(emotionalTone, character, cast, blocks, sceneBrief)
            if (script.lines.isEmpty()) return null

            val request = buildRequest(script, character, cast, narratorVoice)
            val wav = audioGenClient.generate(request)
            val file =
                fileHelper.saveBinaryFile(
                    wav,
                    path = directory,
                    fileName = fileName,
                    extension = "wav",
                ) ?: error("Could not save the audio $directory/$fileName")
            return VoicedClip(file.absolutePath, script, blocks)
        }

        private suspend fun script(
            emotionalTone: EmotionalTone?,
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
                                        emotionalTone = emotionalTone?.name.orEmpty(),
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
            // Gemma answers the script in ~5-6s in practice (measured 2026-09-28), not the ~1s the
            // plan guessed: at 3s it always timed out and every clip lost its sounds. TTS itself
            // takes ~10s, so a few more seconds here is a small share of the wait.
            private const val SCRIPT_TIMEOUT_MS = 8_000L
            private const val NARRATOR_LABEL = "Narrator"
        }
    }
