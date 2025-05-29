package com.ilustris.sagai.features.newsaga.data.model

import ai.atick.material.MaterialColor
import androidx.compose.ui.graphics.Color
import com.ilustris.sagai.R

data class SagaForm(
    val title: String = "",
    val description: String = "",
    val genre: Genre = Genre.entries.first(),
)

enum class Genre(
    val title: String,
    val icon: Int,
    val color: Color,
    val iconColor: Color,
) {
    FANTASY("Fantasia", R.drawable.red_dragon, MaterialColor.Red800, Color.White),
    SCI_FI("Ficção Científica", R.drawable.cyberpunk_girl, MaterialColor.DeepPurple400, Color.White),
}
