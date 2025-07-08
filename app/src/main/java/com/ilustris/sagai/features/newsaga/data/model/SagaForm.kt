package com.ilustris.sagai.features.newsaga.data.model

import ai.atick.material.MaterialColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import com.ilustris.sagai.R
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
                MaterialColor.Amber400,
                MaterialColor.Red700,
                MaterialColor.RedA400,
                MaterialColor.Orange900,
                MaterialColor.Red900,
            )
        Genre.SCI_FI ->
            listOf(
                MaterialColor.Teal400,
                MaterialColor.Blue500,
                MaterialColor.DeepPurple300,
                MaterialColor.Indigo500,
            )
    }

@Composable
fun Genre.genderIcon(gender: String): Int {
    val gendersArray = stringArrayResource(R.array.character_form_gender_options)
    return when (this) {
        Genre.FANTASY ->
            when (gender) {
                gendersArray.first() -> R.drawable.knight
                gendersArray.last() -> R.drawable.princess
                else -> R.drawable.ic_spark
            }

        Genre.SCI_FI ->
            when (gender) {
                gendersArray.first() -> R.drawable.cyberpunk_cyborg
                gendersArray.last() -> R.drawable.cyberpunk_girl
                else -> R.drawable.ic_spark
            }
    }
}
