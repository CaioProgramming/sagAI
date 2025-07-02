package com.ilustris.sagai.features.newsaga.data.model

import ai.atick.material.MaterialColor
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.ilustris.sagai.R

data class SagaForm(
    val title: String = "",
    val description: String = "",
    val genre: Genre = Genre.entries.first(),
    val characterDescription: String = "",
)

enum class Genre(
    val title: String,
    val icon: Int,
    val color: Color,
    val iconColor: Color,
    @DrawableRes
    val background: Int,
) {
    FANTASY(
        "Fantasia",
        R.drawable.fantasy_icon,
        MaterialColor.Red800,
        Color.White,
        R.drawable.fantasy,
    ),
    SCI_FI(
        "Cyberpunk",
        R.drawable.scifi_icon,
        MaterialColor.DeepPurpleA200,
        Color.White,
        R.drawable.scifi,
    ),
}


fun Genre.colorPalette() = when(this) {
    Genre.FANTASY -> listOf(
        MaterialColor.Amber400,
        MaterialColor.Red700,
        MaterialColor.RedA400,
        MaterialColor.Orange900,
        MaterialColor.Red900,
    )
    Genre.SCI_FI -> listOf(
        MaterialColor.DeepPurple300,
        MaterialColor.Blue800,
        MaterialColor.Purple800,
        MaterialColor.Indigo500,
    )
}
