package com.ilustris.sagai.features.characters.data.usecase

import android.content.Context
import android.util.Log
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.models.ImageReference
import com.ilustris.sagai.core.ai.prompts.CharacterPrompts
import com.ilustris.sagai.core.ai.prompts.ChatPrompts
import com.ilustris.sagai.core.ai.prompts.ImageGuidelines
import com.ilustris.sagai.core.ai.prompts.ImagePrompts
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.file.GenreReferenceHelper
import com.ilustris.sagai.core.file.ImageCropHelper
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterUpdate
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
import com.ilustris.sagai.ui.theme.toHex
import com.slowmac.autobackgroundremover.removeBackground
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
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
        @ApplicationContext
        private val context: Context,
    ) : CharacterUseCase {
        override fun getAllCharacters(): Flow<List<Character>> = repository.getAllCharacters()

        override suspend fun insertCharacter(character: Character) =
            repository.insertCharacter(
                character.copy(
                    id = 0,
                    joinedAt = Calendar.getInstance().timeInMillis,
                    image = emptyString(),
                ),
            )

        override suspend fun updateCharacter(character: Character) = repository.updateCharacter(character)

        override suspend fun deleteCharacter(characterId: Int) = repository.deleteCharacter(characterId)

        override suspend fun getCharacterById(characterId: Int): Character? = repository.getCharacterById(characterId)

        override suspend fun generateCharacterImage(
            character: Character,
            saga: Saga,
        ): RequestResult<Pair<Character, String>> =
            executeRequest(false) {
                // TODO REMOVE FORCED PREMIUM
                val isPremium = true // billingService.isPremium()

                val portraitReference =
                    genreReferenceHelper.getPortraitReference().getSuccess()?.let {
                        ImageReference(it, ImageGuidelines.compositionReferenceGuidance)
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
                    if (isPremium) {
                        SagaPrompts.iconDescription(
                            saga.genre,
                            mapOf(
                                "saga" to saga.toJsonFormatExcludingFields(ChatPrompts.sagaExclusions),
                                "character" to
                                    character.toJsonFormatExcludingFields(
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
                        )
                    } else {
                        ImagePrompts.simpleEmojiRendering(
                            saga.genre.color.toHex(),
                            character,
                        )
                    }

                val translatedDescription =
                    gemmaClient.generate<String>(
                        descriptionPrompt,
                        references = references,
                        requireTranslation = false,
                    )!!

                val image =
                    imagenClient
                        .generateImage(translatedDescription.plus(ImagePrompts.criticalGenerationRule()), canByPass = false)!!
                        .apply {
                            if (isPremium.not()) {
                                this.removeBackground(context, true)
                            } else {
                                imageCropHelper.cropToPortraitBitmap(this)
                            }
                        }

                val file =
                    fileHelper.saveFile(character.name, image, path = "${saga.id}/characters/")
                val newCharacter = character.copy(image = file!!.path, emojified = isPremium.not())
                repository.updateCharacter(newCharacter)
                image.recycle()
                newCharacter to translatedDescription
            }

        override suspend fun generateCharacter(
            sagaContent: SagaContent,
            description: String,
        ): RequestResult<Character> =
            executeRequest {
                var prompt =
                    CharacterPrompts.characterGeneration(
                        sagaContent,
                        description,
                    )
                Log.d(javaClass.simpleName, "generateCharacter: Starting character generation...")
                val newCharacter =
                    gemmaClient.generate<Character>(
                        prompt,
                        filterOutputFields =
                            listOf(
                                "id",
                                "image",
                                "joinedAt",
                                "sagaId",
                            ),
                    )!!

                val character =
                    sagaContent.getCharacters().find { it.name.equals(newCharacter.name, true) }
                if (character != null) {
                    error("Character already exists")
                }
                val characterTransaction =
                    insertCharacter(
                        newCharacter.copy(
                            sagaId = sagaContent.data.id,
                            firstSceneId = sagaContent.getCurrentTimeLine()?.data?.id,
                            joinedAt = System.currentTimeMillis(),
                            image = emptyString(),
                        ),
                    )
                characterTransaction
            }

        override suspend fun generateCharactersUpdate(
            timeline: Timeline,
            saga: SagaContent,
        ): RequestResult<Unit> =
            executeRequest {
                val prompt = CharacterPrompts.characterLoreGeneration(timeline, saga.getCharacters())
                val request =
                    gemmaClient.generate<List<CharacterUpdate>>(prompt, describeOutput = false)!!

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
            lastMessages: List<String>,
        ): RequestResult<Unit> =
            executeRequest {
                try {
                    Log.i(
                        javaClass.simpleName,
                        "Analyzing last ${lastMessages.size} messages for nicknames...",
                    )
                    val charactersList = saga.getCharacters()
                    val prompt =
                        buildString {
                            appendLine("Analyze the following messages to find new informal names or nicknames for the characters listed.")
                            appendLine("\nCharacters: $charactersList")
                            appendLine("\nRecent Messages: $lastMessages")
                            appendLine(
                                "\nRespond ONLY with a JSON array in the format: '[{\"characterName\": \"Character Full Name\", \"newNicknames\": [\"nickname1\", \"nickname2\"]}]'",
                            )
                            appendLine(
                                "\nOnly include characters for whom you found new nicknames. If no new nicknames are found, return an empty array.",
                            )
                        }

                    val suggestions =
                        gemmaClient.generate<List<com.ilustris.sagai.features.characters.data.model.NicknameSuggestion>>(
                            prompt,
                        )

                    if (suggestions.isNullOrEmpty()) {
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
                                        nicknames = (currentNicknames + newNicknames).distinct(),
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
