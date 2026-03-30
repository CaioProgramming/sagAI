package com.ilustris.sagai.features.characters.data.usecase

import android.content.Context
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.ai.GemmaClient
import timber.log.Timber
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.model.ImageType
import com.ilustris.sagai.core.ai.prompts.CharacterPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.file.GenreReferenceHelper
import com.ilustris.sagai.core.file.ImageCropHelper
import com.ilustris.sagai.core.segmentation.ImageSegmentationHelper
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.CharacterUpdateGen
import com.ilustris.sagai.features.characters.data.model.KnowledgeUpdateResult
import com.ilustris.sagai.features.characters.data.model.NickNameGen
import com.ilustris.sagai.features.characters.data.model.SmartZoom
import com.ilustris.sagai.features.characters.data.model.fullName
import com.ilustris.sagai.features.characters.events.data.model.CharacterEvent
import com.ilustris.sagai.features.characters.events.data.repository.CharacterEventRepository
import com.ilustris.sagai.features.characters.relations.data.usecase.CharacterRelationUseCase
import com.ilustris.sagai.features.characters.repository.CharacterRepository
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.findTimeline
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.ui.theme.utils.getRandomColorHex
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@OptIn(PublicPreviewAPI::class)
class CharacterUseCaseImpl
    @Inject
    constructor(
        private val repository: CharacterRepository,
        private val eventsRepository: CharacterEventRepository,
        private val characterRelationUseCase: CharacterRelationUseCase,
        private val imagenClient: ImagenClient,
        private val textGenClient: TextGenClient,
        private val gemmaClient: GemmaClient,
        private val fileHelper: FileHelper,
        private val imageCropHelper: ImageCropHelper,
        private val genreReferenceHelper: GenreReferenceHelper,
        private val billingService: BillingService,
        private val imageSegmentationHelper: ImageSegmentationHelper,
        private val analyticsService: com.ilustris.sagai.core.analytics.AnalyticsService,
        private val genreConfigService: com.ilustris.sagai.core.ai.services.GenreConfigService,
        private val promptService: com.ilustris.sagai.core.ai.services.PromptService,
        private val remoteConfigService: com.ilustris.sagai.core.services.RemoteConfigService,
        @param:ApplicationContext
        private val context: Context,
    ) : CharacterUseCase {
        override fun getAllCharacters(): Flow<List<Character>> = repository.getAllCharacters()

        override suspend fun insertCharacter(character: Character) =
            repository.insertCharacter(
                character.copy(
                    id = 0,
                    joinedAt = Calendar.getInstance().timeInMillis,
                    image = emptyString(),
                    voice = null,
                ),
            )

        override suspend fun updateCharacter(character: Character) = repository.updateCharacter(character)

        override suspend fun deleteCharacter(characterId: Int) = repository.deleteCharacter(characterId)

        override suspend fun getCharacterById(characterId: Int): Character? = repository.getCharacterById(characterId)

        override suspend fun generateCharacterImage(
            character: Character,
            saga: Saga,
        ): RequestResult<Pair<Character, String>> =
            executeRequest(true) {
                val image =
                    imagenClient
                        .generateIntegratedImage(
                            genre = saga.genre,
                            imageReference = null,
                            context =
                                buildString {
                                    appendLine(
                                        character.toAINormalize(
                                            listOf(
                                                "id",
                                                "image",
                                                "sagaId",
                                                "joinedAt",
                                                "smartZoom",
                                                "knowledge",
                                                "firstSceneId",
                                                "emojified",
                                                "hexColor",
                                            ),
                                        ),
                                    )
                                },
                            imageType = ImageType.ICON,
                            variationId = saga.variationId,
                        )

                if (image.isFailure) {
                    throw image.error.value
                }

                val file =
                    fileHelper.saveFile(
                        character.name,
                        image.getSuccess(),
                        path = "${saga.id}/characters/",
                    )!!
                val newCharacter =
                    character.copy(image = file.path)
                repository.updateCharacter(newCharacter)

                newCharacter to ""
            }

        override suspend fun createSmartZoom(character: Character): RequestResult<Unit> =
            executeRequest {
                Timber.i("createSmartZoom: creating zoom for ${character.name}")
                val smartZoom = imageSegmentationHelper.calculateSmartZoom(character.image).getSuccess()
                val newCharacter =
                    character.copy(smartZoom = smartZoom ?: SmartZoom(needsZoom = false))
                repository.updateCharacter(newCharacter)
            }

        override suspend fun generateCharacter(
            sagaContent: SagaContent,
            description: String,
        ): RequestResult<Character> =
            executeRequest {
                val bannedNames = repository.getAllCharacterNames()
                // Generate theme color first to pass to AI for appearance guidance
                val themeColor = getRandomColorHex()
                val config =
                    genreConfigService.getGenreConfig(
                        sagaContent.data.genre,
                        sagaContent.data.variationId,
                    )
                val prompt =
                    CharacterPrompts.characterGeneration(
                        promptService,
                        sagaContent,
                        config,
                        description,
                        bannedNames,
                        themeColor,
                    )
                Timber.d(
                    "generateCharacter: Starting character generation with theme color $themeColor...",
                )
                val newCharacter =
                    gemmaClient.generate<Character>(
                        prompt,
                        useCore = true,
                        filterOutputFields =
                            listOf(
                                "id",
                                "image",
                                "joinedAt",
                                "sagaId",
                                "smartZoom",
                                "voice",
                                "hexColor",
                                "firstSceneId",
                            ),
                        requirement = GemmaClient.ModelRequirement.HIGH,
                    )!!

                val character = sagaContent.findCharacter(newCharacter.name)

                if (character?.data?.fullName() == newCharacter.fullName()) {
                    error("Character already exists")
                }
                val characterTransaction =
                    insertCharacter(
                        newCharacter.copy(
                            id = 0,
                            sagaId = sagaContent.data.id,
                            firstSceneId = sagaContent.getCurrentTimeLine()?.data?.id,
                            joinedAt = System.currentTimeMillis(),
                            image = emptyString(),
                            hexColor = themeColor,
                            smartZoom = null,
                        ),
                    )
                CoroutineScope(Dispatchers.IO).launch {
                    generateCharacterImage(
                        characterTransaction,
                        sagaContent.data,
                    )
                }
                characterTransaction
            }

        override suspend fun generateCharactersUpdate(
            timeline: Timeline,
            saga: SagaContent,
        ): RequestResult<Unit> =
            executeRequest {
                val prompt =
                    CharacterPrompts.characterLoreGeneration(
                        promptService,
                        timeline,
                        saga.getCharacters(),
                    )
                val request =
                    gemmaClient.generate<CharacterUpdateGen>(
                        prompt,
                        requirement = GemmaClient.ModelRequirement.MEDIUM,
                        temperatureRandomness = .3f,
                    )!!

                val updatedCharacters =
                    request
                        .updates
                        .mapNotNull {
                            val character = saga.findCharacter(it.characterName)
                            val timelineContent = saga.findTimeline(timeline.id)
                            val characterEventOnThisTimeline =
                                timelineContent
                                    ?.characterEventDetails
                                    ?.find { it.character.id == character?.data?.id }

                            if (characterEventOnThisTimeline != null) {
                                Timber.e(
                                    "Character event already exists for this timeline(${timeline.id})",
                                )
                                return@mapNotNull null
                            }

                            if (character == null) {
                                Timber.e(
                                    "generateCharactersUpdate: Couldn't find character ${it.characterName} on saga.",
                                )
                            }

                            character?.let { character ->
                                CharacterEvent(
                                    id = 0,
                                    character.data.id,
                                    gameTimelineId = timeline.id,
                                    title = it.title,
                                    summary = it.description,
                                )
                            }
                        }
                Timber.d("Updating ${updatedCharacters.size} characters events.")
                eventsRepository.insertCharacterEvents(updatedCharacters).asSuccess()
            }

        override suspend fun generateCharacterRelations(
            timeline: Timeline,
            saga: SagaContent,
        ): RequestResult<Unit> = characterRelationUseCase.generateCharacterRelation(timeline, saga)

        override suspend fun findAndSuggestNicknames(
            saga: SagaContent,
            timelineContent: TimelineContent,
        ): RequestResult<Unit> =
            executeRequest {
                try {
                    val charactersList = saga.getCharacters()
                    val prompt =
                        CharacterPrompts.findNickNames(
                            promptService,
                            charactersList,
                            timelineContent.messages.map { it.message },
                            timelineContent.data,
                            saga.data,
                        )

                    val suggestions =
                        gemmaClient
                            .generate<NickNameGen>(
                                prompt,
                                requirement = GemmaClient.ModelRequirement.MEDIUM,
                            )!!
                            .suggestions

                    if (suggestions.isEmpty()) {
                        Timber.i("No new nicknames found.")
                        return@executeRequest
                    }

                    Timber.i("Found ${suggestions.size} nickname suggestions.")

                    suggestions.forEach { suggestion ->
                        saga.findCharacter(suggestion.characterName)?.let { characterContent ->
                            val currentNicknames =
                                (characterContent.data.nicknames ?: emptyList()).toMutableList()
                            val newNicknames =
                                suggestion.newNicknames.filter { !currentNicknames.contains(it) && it.length > 2 }
                            if (newNicknames.isNotEmpty()) {
                                val updatedCharacter =
                                    characterContent.data.copy(
                                        nicknames = (newNicknames).distinct(),
                                    )
                                updateCharacter(updatedCharacter)
                                Timber.i(
                                    "Updated character ${updatedCharacter.name} with new nicknames: $newNicknames",
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error suggesting nicknames: ${e.message}")
                    e.printStackTrace()
                }
            }

        override suspend fun generateCharacterResume(
            character: CharacterContent,
            saga: SagaContent,
        ): RequestResult<String> =
            executeRequest {
                if (character.events.isEmpty()) {
                    return@executeRequest character.data.backstory
                }
                val config =
                    genreConfigService.getGenreConfig(saga.data.genre, saga.data.variationId)!!
                val prompt =
                    CharacterPrompts.characterResume(
                        promptService,
                        promptService.getPromptDirectives(),
                        character,
                        saga,
                        config,
                    )
                gemmaClient.generate<String>(
                    prompt,
                    requirement = GemmaClient.ModelRequirement.MEDIUM,
                )!!
            }

        override suspend fun updateCharacterKnowledge(
            timeline: Timeline,
            saga: SagaContent,
        ): RequestResult<Unit> =
            executeRequest {
                val characters = saga.getCharacters()
                if (characters.isEmpty()) return@executeRequest

                val prompt =
                    CharacterPrompts.knowledgeUpdatePrompt(promptService, timeline, characters)
                val result =
                    gemmaClient.generate<KnowledgeUpdateResult>(
                        prompt,
                        requirement = GemmaClient.ModelRequirement.MEDIUM,
                    )

                if (result?.updates?.isNotEmpty() == true) {
                    Timber.i(
                        "Updating knowledge for ${result.updates.size} characters.",
                    )
                    result.updates.forEach { update ->
                        saga.findCharacter(update.characterName)?.let { charContent ->
                            val currentKnowledge =
                                (charContent.data.knowledge ?: emptyList()).toMutableList()
                            val newFacts =
                                update.learnedFacts.filter { !currentKnowledge.contains(it) }

                            if (newFacts.isNotEmpty()) {
                                currentKnowledge.addAll(newFacts)
                                if (currentKnowledge.size > 50) {
                                    currentKnowledge.subList(0, currentKnowledge.size - 50).clear()
                                }

                                val updatedChar =
                                    charContent.data.copy(knowledge = currentKnowledge)
                                updateCharacter(updatedChar)
                                Timber.d(
                                    "Added ${newFacts.size} facts to ${update.characterName}",
                                )
                            }
                        }
                    }
                } else {
                    Timber.d(
                        "No new knowledge extracted from this timeline event.",
                    )
                }
            }
    }
