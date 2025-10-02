package com.ilustris.sagai.features.characters.data.usecase

import android.content.Context
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.models.ImageReference
import com.ilustris.sagai.core.ai.prompts.CharacterPrompts
import com.ilustris.sagai.core.ai.prompts.GenrePrompts
import com.ilustris.sagai.core.ai.prompts.ImageGuidelines
import com.ilustris.sagai.core.ai.prompts.ImagePrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.core.services.BillingState
import com.ilustris.sagai.core.utils.FileHelper
import com.ilustris.sagai.core.utils.GenreReferenceHelper
import com.ilustris.sagai.core.utils.ImageCropHelper
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterUpdate
import com.ilustris.sagai.features.characters.events.data.model.CharacterEvent
import com.ilustris.sagai.features.characters.events.data.repository.CharacterEventRepository
import com.ilustris.sagai.features.characters.relations.data.usecase.CharacterRelationUseCase
import com.ilustris.sagai.features.characters.repository.CharacterRepository
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
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
            executeRequest {
                val isPremium = billingService.isPremium()

                val styleReferenceBitmap =
                    ImageReference(
                        genreReferenceHelper.getGenreStyleReference(saga.genre).getSuccess()!!,
                        ImageGuidelines.styleReferenceGuidance,
                    )
                val portraitReference =
                    genreReferenceHelper.getPortraitReference().getSuccess()?.let {
                        ImageReference(it, ImageGuidelines.compositionReferenceGuidance)
                    }
                val references =
                    if (isPremium) {
                        listOfNotNull(
                            portraitReference,
                            styleReferenceBitmap,
                        )
                    } else {
                        emptyList()
                    }

                val descriptionPrompt =
                    if (isPremium) {
                        CharacterPrompts.descriptionTranslationPrompt(
                            character,
                            saga.genre,
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
                        .generateImage(translatedDescription)!!
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
                val newCharacter =
                    textGenClient.generate<Character>(
                        CharacterPrompts.characterGeneration(
                            sagaContent,
                            description,
                        ),
                    )!!

                val character = sagaContent.getCharacters().find { it.name.equals(newCharacter.name, true) }
                if (character != null) {
                    error("Character already exists")
                }
                val characterTransaction =
                    insertCharacter(
                        newCharacter.copy(
                            sagaId = sagaContent.data.id,
                            firstSceneId = sagaContent.getCurrentTimeLine()?.data?.id,
                            joinedAt = System.currentTimeMillis(),
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
                val request = gemmaClient.generate<List<CharacterUpdate>>(prompt)!!

                val updatedCharacters =
                    request.mapNotNull {
                        val character = saga.getCharacters().find { c -> c.name.equals(it.characterName, true) }
                        character?.let { character ->
                            CharacterEvent(
                                id = 0,
                                character.id,
                                gameTimelineId = timeline.id,
                                title = it.title,
                                summary = it.description,
                            )
                        }
                    }

                eventsRepository.insertCharacterEvents(updatedCharacters).asSuccess()
            }

        override suspend fun generateCharacterRelations(
            timeline: Timeline,
            saga: SagaContent,
        ): RequestResult<Unit> = characterRelationUseCase.generateCharacterRelation(timeline, saga)
    }
