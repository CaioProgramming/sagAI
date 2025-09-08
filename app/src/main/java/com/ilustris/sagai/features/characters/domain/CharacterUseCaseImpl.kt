package com.ilustris.sagai.features.characters.domain

import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.ai.CharacterPrompts
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.utils.FileHelper
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.repository.CharacterRepository
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

        override suspend fun generateCharacterImage(
            character: Character,
            genre: Genre,
        ): RequestResult<Exception, Character> =
            try {
                val image =
                    imagenClient.generateImage(
                        CharacterPrompts.generateImage(character, genre),
                    )
                val file = fileHelper.saveToCache(character.name, image!!.data)
                val newCharacter = character.copy(image = file!!.path)
                repository.updateCharacter(newCharacter)
                RequestResult.Success(newCharacter)
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun generateCharacter(
            sagaData: SagaData?,
            description: String,
        ): RequestResult<Exception, Character> =
            try {
                val newCharacter =
                    textGenClient.generate<Character>(
                        CharacterPrompts.characterGeneration(
                            sagaData!!,
                            description,
                        ),
                    )

                val characterTransaction = insertCharacter(newCharacter!!.copy(sagaId = sagaData.id))
                generateCharacterImage(
                    character = characterTransaction,
                    genre = sagaData.genre,
                )
            } catch (e: Exception) {
                e.asError()
            }
    }
