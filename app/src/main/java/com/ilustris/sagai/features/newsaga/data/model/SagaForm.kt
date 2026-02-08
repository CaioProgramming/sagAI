package com.ilustris.sagai.features.newsaga.data.model

import com.ilustris.sagai.features.characters.data.model.CharacterInfo

data class SagaForm(
    val saga: SagaDraft = SagaDraft(),
    val character: CharacterInfo? = null,
)

fun SagaForm?.isReady() = this != null && isSagaBlank().not() && isCharacterBlank().not()

fun SagaForm.isSagaBlank() = saga.title.isBlank() && saga.description.isBlank()

fun SagaForm.isCharacterBlank() = character?.name.isNullOrBlank() && character?.description.isNullOrBlank()
