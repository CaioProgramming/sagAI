package com.ilustris.sagai.features.saga.chat.data.voicing

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.ai.VoiceCatalog
import com.ilustris.sagai.core.ai.model.Voice
import com.ilustris.sagai.core.ai.prompts.VoiceCastingGen
import com.ilustris.sagai.core.ai.prompts.VoicePrompts
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.repository.CharacterRepository
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.absoluteValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

data class CastVoice(
    val voice: Voice,
    val direction: String?,
)

/**
 * Picks a voice once per character (and once per saga for the narrator) and persists it, with a
 * `voiceDirection` describing how that character delivers lines. Runs off the critical path
 * wherever possible — after character creation, when live mode opens — so a turn almost never
 * waits on it.
 *
 * Shared voices are expected (there are only a few dozen): the direction is what tells two
 * characters with the same voice apart.
 */
@Singleton
class VoiceCastingUseCase
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
        private val promptService: PromptService,
        private val voiceCatalog: VoiceCatalog,
        private val characterRepository: CharacterRepository,
        private val sagaRepository: SagaRepository,
    ) {
        private val mutex = Mutex()
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        /** Fire-and-forget casting for a character that was just created. */
        fun castInBackground(character: Character) {
            scope.launch {
                try {
                    val saga = sagaRepository.getSagaMetadata(character.sagaId).first() ?: return@launch
                    voiceFor(saga, character)
                } catch (e: Exception) {
                    Timber.w(e, "Background casting failed for ${character.name}")
                }
            }
        }

        suspend fun voiceFor(
            saga: SagaMetadata,
            character: Character,
        ): CastVoice =
            mutex.withLock {
                val current = characterRepository.getCharacterById(character.id) ?: character
                val existing = voiceCatalog.find(current.voice)
                if (existing != null && !current.voiceDirection.isNullOrBlank()) {
                    return@withLock CastVoice(existing, current.voiceDirection)
                }

                val inUse =
                    (saga.characters.filter { it.id != current.id }.mapNotNull { it.voice } + listOfNotNull(saga.data.narratorVoice))
                        .filter { it.isNotBlank() }
                        .distinct()
                val cast =
                    runCatching {
                        gemmaClient.generate<VoiceCastingGen>(
                            promptSplit =
                                VoicePrompts.castingPrompt(
                                    promptService = promptService,
                                    saga = saga,
                                    character = current,
                                    voiceGuide = voiceCatalog.selectionGuide(),
                                    voicesInUse = inUse,
                                ),
                            requireTranslation = false,
                            requirement = ModelRequirement.MEDIUM,
                        )
                    }.onFailure { Timber.w(it, "Voice casting failed for ${current.name}, using fallback") }
                        .getOrNull()

                // A voice the character already had is kept: changing it mid-saga would make them
                // sound like someone else. Casting then only adds the missing direction.
                val voice = existing ?: voiceCatalog.find(cast?.voice) ?: fallbackVoice(current, inUse)
                val direction = cast?.voiceDirection?.trim()?.takeIf { it.isNotBlank() } ?: current.voiceDirection
                if (voice.id != current.voice || direction != current.voiceDirection) {
                    characterRepository.updateCharacter(current.copy(voice = voice.id, voiceDirection = direction))
                }
                CastVoice(voice, direction)
            }

        /** One narrator voice per saga, persisted on first use (the audiobook reuses the same field). */
        suspend fun narratorVoice(saga: SagaMetadata): Voice =
            mutex.withLock {
                val fresh = sagaRepository.getSagaMetadata(saga.data.id).first() ?: saga
                voiceCatalog.find(fresh.data.narratorVoice)?.let { return@withLock it }
                val inUse = fresh.characters.mapNotNull { it.voice }.toSet()
                val voices = voiceCatalog.voices()
                val pool = voices.filterNot { it.id in inUse }.ifEmpty { voices }
                val voice = pool[fresh.data.id.absoluteValue % pool.size]
                sagaRepository.updateSaga(fresh.data.copy(narratorVoice = voice.id))
                voice
            }

        /** Casts everyone in [characters] that still has no voice or direction. Failures are logged, never thrown. */
        suspend fun castAll(
            saga: SagaMetadata,
            characters: List<Character>,
        ) {
            characters
                .filter { voiceCatalog.find(it.voice) == null || it.voiceDirection.isNullOrBlank() }
                .forEach { character ->
                    runCatching { voiceFor(saga, character) }
                        .onFailure { Timber.w(it, "Background casting failed for ${character.name}") }
                }
        }

        /** Same gender when we can tell, a voice nobody in the saga uses yet when possible, stable per character. */
        private suspend fun fallbackVoice(
            character: Character,
            inUse: List<String>,
        ): Voice {
            val voices = voiceCatalog.voices()
            val gender = normalizeGender(character.details.physicalTraits.gender)
            val sameGender = voices.filter { gender == null || it.gender.equals(gender, ignoreCase = true) }.ifEmpty { voices }
            val pool = sameGender.filterNot { it.id in inUse }.ifEmpty { sameGender }
            return pool[character.id.absoluteValue % pool.size]
        }

        private fun normalizeGender(raw: String): String? {
            val value = raw.lowercase()
            return when {
                listOf("fem", "mulher", "woman", "girl", "menina", "mujer").any { it in value } -> "FEMALE"
                listOf("male", "masc", "homem", "man", "boy", "menino", "hombre").any { it in value } -> "MALE"
                else -> null
            }
        }
    }
