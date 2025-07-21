package com.ilustris.sagai.features.newsaga.data.model

import ai.atick.material.MaterialColor
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.Details

data class SagaForm(
    val title: String = "",
    val description: String = "",
    val genre: Genre = Genre.entries.first(),
    val character: Character = Character(details = Details()),
)

fun Genre.colorPalette() =
    when (this) {
        Genre.FANTASY ->
            listOf(
                MaterialColor.RedA200,
                MaterialColor.Red900,
                MaterialColor.DeepOrange600,
                MaterialColor.Red500,
            )
        Genre.SCI_FI ->
            listOf(
                MaterialColor.DeepPurpleA400,
                MaterialColor.IndigoA700,
                MaterialColor.DeepPurple500,
                MaterialColor.DeepPurple400,
                MaterialColor.Indigo500,
            )
    }
