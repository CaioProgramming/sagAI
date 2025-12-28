package com.ilustris.sagai.features.characters.data.usecase

import android.content.Context
import android.util.Log
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.model.ImageReference
import com.ilustris.sagai.core.ai.prompts.CharacterPrompts
import com.ilustris.sagai.core.ai.prompts.ChatPrompts
import com.ilustris.sagai.core.ai.prompts.ImagePrompts
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.analytics.AnalyticsConstants
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
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterUpdate
import com.ilustris.sagai.features.characters.data.model.NicknameSuggestion
import com.ilustris.sagai.features.characters.data.model.SmartZoom
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
import kotlinx.coroutines.withContext
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
                val isPremium = billingService.isPremium()

                val portraitReference =
                    genreReferenceHelper.getPortraitReference().getSuccess()?.let {
                        ImageReference(it, ImagePrompts.extractComposition())
                    }
                val references =
                    if (isPremium) {
                        listOfNotNull(
                            portraitReference,
                        )
                    } else {
                        emptyList()
                    }

                val visualComposition =
                    imagenClient
                        .extractComposition(
                            listOfNotNull(portraitReference),
                        ).getSuccess()

                val descriptionPrompt =
                    SagaPrompts.iconDescription(
                        saga.genre,
                        mapOf(
                            "saga" to saga.toAINormalize(ChatPrompts.sagaExclusions),
                            "character" to
                                character.toAINormalize(
                                    listOf(
                                        "id",
                                        "image",
                                        "sagaId",
                                        "joinedAt",
                                        "emojified",
                                        "abilities",
                                    ),
                                ),
                        ).toJsonFormat(),
                        visualComposition,
                        characterHexColor = character.hexColor,
                    )

                val translatedDescription =
                    gemmaClient.generate<String>(
                        descriptionPrompt,
                        references = references,
                        requireTranslation = false,
                        requirement = GemmaClient.ModelRequirement.HIGH,
                    )!!

                // NEW: Review the generated description before image generation
                val reviewedPrompt =
                    imagenClient
                        .reviewAndCorrectPrompt(
                            imageType = AnalyticsConstants.ImageType.AVATAR,
                            visualDirection = visualComposition,
                            genre = saga.genre,
                            finalPrompt = translatedDescription,
                        ).getSuccess()

                // Use the reviewed prompt, or fallback to original if review failed
                val finalPromptForGeneration =
                    reviewedPrompt?.correctedPrompt ?: run {
                        Log.w(
                            "CharacterUseCase",
                            "Review failed or returned null, using original description",
                        )
                        translatedDescription
                    }

                val image =
                    imagenClient
                        .generateImage(finalPromptForGeneration, canByPass = false)!!

                val file =
                    fileHelper.saveFile(character.name, image, path = "${saga.id}/characters/")!!
                val newCharacter =
                    character.copy(image = file.path)
                repository.updateCharacter(newCharacter)
                image.recycle()

                withContext(Dispatchers.IO) {
                    createSmartZoom(newCharacter)
                }
                newCharacter to finalPromptForGeneration
            }

        override suspend fun createSmartZoom(character: Character): RequestResult<Unit> =
            executeRequest {
                Log.i(javaClass.simpleName, "createSmartZoom: creating zoom for ${character.name}")
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
                val prompt =
                    CharacterPrompts.characterGeneration(
                        sagaContent,
                        description,
                    )
                Log.d(javaClass.simpleName, "generateCharacter: Starting character generation...")
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
                            ),
                        requirement = GemmaClient.ModelRequirement.HIGH,
                    )!!

                val character =
                    sagaContent.getCharacters().find { it.name.equals(newCharacter.name, true) }
                if (character != null) {
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
                            hexColor = getRandomColorHex(),
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
                val prompt = CharacterPrompts.characterLoreGeneration(timeline, saga.getCharacters())
                val request =
                    gemmaClient.generate<List<CharacterUpdate>>(
                        prompt,
                        describeOutput = false,
                        requirement = GemmaClient.ModelRequirement.HIGH,
                    )!!

                val updatedCharacters =
                    request
                        .mapNotNull {
                            val character = saga.findCharacter(it.characterName)
                            val timelineContent = saga.findTimeline(timeline.id)
                            val characterEventOnThisTimeline =
                                timelineContent
                                    ?.characterEventDetails
                                    ?.find { it.character.id == character?.data?.id }

                            if (characterEventOnThisTimeline != null) {
                                Log.e(
                                    javaClass.simpleName,
                                    "Character event already exists for this timeline(${timeline.id})",
                                )
                                return@mapNotNull null
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
                Log.d(javaClass.simpleName, "Updating ${updatedCharacters.size} characters events.")
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
                            charactersList,
                            timelineContent.messages.map { it.message },
                            timelineContent.data,
                            saga.data,
                        )

                    val suggestions =
                        gemmaClient.generate<List<NicknameSuggestion>>(
                            prompt,
                            requirement = GemmaClient.ModelRequirement.MEDIUM,
                        )!!

                    if (suggestions.isEmpty()) {
                        Log.i(javaClass.simpleName, "No new nicknames found.")
                        return@executeRequest
                    }

                    Log.i(javaClass.simpleName, "Found ${suggestions.size} nickname suggestions.")

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
                                Log.i(
                                    javaClass.simpleName,
                                    "Updated character ${updatedCharacter.name} with new nicknames: $newNicknames",
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(javaClass.simpleName, "Error suggesting nicknames: ${e.message}")
                    e.printStackTrace()
                }
            }
    }
