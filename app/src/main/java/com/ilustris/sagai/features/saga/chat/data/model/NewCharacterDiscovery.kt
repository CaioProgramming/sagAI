package com.ilustris.sagai.features.saga.chat.data.model

/**
 * Optional signal from the reply AI when a genuinely new character enters the scene.
 * Used as the discovery seed for [com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase.generateCharacter].
 */
data class NewCharacterDiscovery(
    val name: String,
    val discoveryContext: String,
    val role: String? = null,
)
