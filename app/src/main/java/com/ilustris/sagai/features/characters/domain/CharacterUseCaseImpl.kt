package com.ilustris.sagai.features.characters.domain

import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.Schema
import com.ilustris.sagai.core.ai.CharacterFraming
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.prompts.CharacterPrompts
import com.ilustris.sagai.core.ai.prompts.GenrePrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.network.body.FreepikRequest
import com.ilustris.sagai.core.utils.FileHelper
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.repository.CharacterRepository
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@OptIn(PublicPreviewAPI::class)
class CharacterUseCaseImpl
    @Inject
    constructor(
        private val repository: CharacterRepository,
        private val imagenClient: ImagenClient,
        private val textGenClient: TextGenClient,
        private val fileHelper: FileHelper,
    ) : CharacterUseCase {
        override fun getAllCharacters(): Flow<List<Character>> = repository.getAllCharacters()

        override suspend fun insertCharacter(character: Character) = repository.insertCharacter(character)

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
                            CharacterFraming.PORTRAIT,
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
            description: String,
            saga: SagaData,
        ): RequestResult<Exception, Character> =
            try {
                val translatedDescription =
                    textGenClient.generate<String>(
                        CharacterPrompts.descriptionTranslationPrompt(
                            character,
                            CharacterFraming.PORTRAIT,
                            saga.genre,
                        ),
                        customSchema = Schema.string(),
                        requireTranslation = false,
                    )
                val prompt = CharacterPrompts.generateImage(character, saga, translatedDescription!!)

                val request =
                    FreepikRequest(
                        prompt,
                        GenrePrompts.negativePrompt(saga.genre),
                        GenrePrompts.characterStyling(saga.genre),
                    )

                val image = imagenClient.generateImage(prompt)!!
                val file = fileHelper.saveFile(character.name, image, path = "${saga.id}/characters/")
                // val file = fileHelper.saveFile(character.name, image!!.data, path = "${saga.id}/characters/")
                val newCharacter = character.copy(image = file!!.path)
                repository.updateCharacter(newCharacter)
                RequestResult.Success(newCharacter)
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

                val characterQuery =
                    repository.getCharacterByName(newCharacter.name, sagaContent.data.id)
                if (characterQuery != null) {
                    throw Exception("Character already exists")
                }
                val characterTransaction =
                    insertCharacter(newCharacter.copy(sagaId = sagaContent.data.id))
                characterTransaction.asSuccess()
            } catch (e: Exception) {
                e.asError()
            }
    }
