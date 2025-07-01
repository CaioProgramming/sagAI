package com.ilustris.sagai.features.characters.domain

import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.ai.CharacterPrompts
import com.ilustris.sagai.core.ai.GenrePrompts
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.network.body.FreepikRequest
import com.ilustris.sagai.core.utils.FileHelper
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.repository.CharacterRepository
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
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
            saga: SagaData,
        ): RequestResult<Exception, Character> =
            try {
                val prompt = CharacterPrompts.generateImage(character, saga)
                val request =
                    FreepikRequest(
                        prompt,
                        GenrePrompts.negativePrompt(saga.genre),
                        GenrePrompts.characterStyling(saga.genre),
                    )
                val image = imagenClient.generateImage(prompt)!!.data
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

                val characterQuery = repository.getCharacterByName(newCharacter.name)
                if (characterQuery != null) {
                    throw Exception("Character already exists")
                }
                val characterTransaction =
                    insertCharacter(newCharacter.copy(sagaId = sagaContent.data.id))
                val iconGen =
                    generateCharacterImage(
                        character = characterTransaction,
                        saga = sagaContent.data,
                    )
                if (iconGen is RequestResult.Success) {
                    RequestResult.Success(iconGen.success.value)
                } else {
                    RequestResult.Success(characterTransaction)
                }
            } catch (e: Exception) {
                e.asError()
            }
    }
