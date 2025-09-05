package com.ilustris.sagai.features.characters.domain

import android.content.Context
import android.graphics.BitmapFactory
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.Schema
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ImageReference
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.prompts.CharacterPrompts
import com.ilustris.sagai.core.ai.prompts.GenrePrompts
import com.ilustris.sagai.core.ai.prompts.ImagePrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.network.body.FreepikRequest
import com.ilustris.sagai.core.utils.FileHelper
import com.ilustris.sagai.core.utils.GenreReferenceHelper
import com.ilustris.sagai.core.utils.ImageCropHelper
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterUpdate
import com.ilustris.sagai.features.characters.events.data.model.CharacterEvent
import com.ilustris.sagai.features.characters.events.data.repository.CharacterEventRepository
import com.ilustris.sagai.features.characters.relations.domain.usecase.CharacterRelationUseCase
import com.ilustris.sagai.features.characters.repository.CharacterRepository
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.defaultHeaderImage
import com.ilustris.sagai.features.timeline.data.model.Timeline
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject

@OptIn(PublicPreviewAPI::class)
class CharacterUseCaseImpl
    @Inject
    constructor(
        @ApplicationContext
        private val context: Context,
        private val repository: CharacterRepository,
        private val eventsRepository: CharacterEventRepository,
        private val characterRelationUseCase: CharacterRelationUseCase,
        private val imagenClient: ImagenClient,
        private val textGenClient: TextGenClient,
        private val gemmaClient: GemmaClient,
        private val fileHelper: FileHelper,
        private val imageCropHelper: ImageCropHelper,
        private val genreReferenceHelper: GenreReferenceHelper,
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

        override suspend fun generateCharacterPrompt(
            character: Character,
            guidelines: String,
            genre: Genre,
        ): RequestResult<Exception, String> =
            try {
                textGenClient
                    .generate<String>(
                        CharacterPrompts.descriptionTranslationPrompt(
                            character,
                            genre,
                        ),
                        customSchema = Schema.string(),
                        requireTranslation = false,
                    )!!
                    .asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun generateCharacterImage(
            character: Character,
            saga: Saga,
        ): RequestResult<Exception, Pair<Character, String>> =
            try {
                val styleReferenceBitmap =
                    BitmapFactory.decodeResource(
                        context.resources,
                        saga.genre.defaultHeaderImage(),
                    )
                val portraitReference =
                    genreReferenceHelper.getPortraitReference().getSuccess()?.let {
                        ImageReference(it, "Portrait photography composition reference")
                    }
                val references =
                    listOfNotNull(
                        ImageReference(styleReferenceBitmap, "Artistic style reference"),
                        portraitReference,
                    )
                val translatedDescription =
                    gemmaClient.generate<String>(
                        CharacterPrompts.descriptionTranslationPrompt(
                            character,
                            saga.genre,
                        ),
                        references = references,
                        requireTranslation = false,
                    )
                val prompt = ImagePrompts.generateImage(translatedDescription!!)

                val image = imagenClient.generateImage(prompt, references)!!
                val croppedImage = imageCropHelper.cropToPortraitBitmap(image)
                val file = fileHelper.saveFile(character.name, croppedImage, path = "${saga.id}/characters/")
                // val file = fileHelper.saveFile(character.name, image!!.data, path = "${saga.id}/characters/")
                val newCharacter = character.copy(image = file!!.path)
                repository.updateCharacter(newCharacter)
                RequestResult.Success(newCharacter to prompt)
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun generateCharacter(
            sagaContent: SagaContent,
            description: String,
        ): RequestResult<Exception, Character> =
            try {
                val newCharacter =
                    textGenClient.generate<Character>(
                        CharacterPrompts.characterGeneration(
                            sagaContent,
                            description,
                        ),
                    )!!

                val character = sagaContent.getCharacters().find { it.name.equals(newCharacter.name, true) }
                if (character != null) {
                    throw Exception("Character already exists")
                }
                val characterTransaction =
                    insertCharacter(
                        newCharacter.copy(
                            sagaId = sagaContent.data.id,
                            joinedAt = System.currentTimeMillis(),
                        ),
                    )
                characterTransaction.asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun generateCharactersUpdate(
            timeline: Timeline,
            saga: SagaContent,
        ): RequestResult<Exception, Unit> =
            try {
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
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun generateCharacterRelations(
            timeline: Timeline,
            saga: SagaContent,
        ): RequestResult<Exception, Unit> = characterRelationUseCase.generateCharacterRelation(timeline, saga)
    }
