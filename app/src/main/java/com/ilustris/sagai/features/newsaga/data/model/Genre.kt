package com.ilustris.sagai.features.newsaga.data.model

import ai.atick.material.MaterialColor
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.ilustris.sagai.R
import com.ilustris.sagai.ui.theme.filters.SelectiveColorParams

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
        Color.Companion.White,
        R.drawable.fantasy,
    ),
    SCI_FI(
        "Cyberpunk",
        R.drawable.scifi_icon,
        MaterialColor.DeepPurpleA200,
        Color.Companion.White,
        R.drawable.scifi,
    ),
}

@StringRes
fun Genre.getNamePlaceholderResId(): Int =
    when (this) {
        Genre.FANTASY -> R.string.character_form_placeholder_name_fantasy
        Genre.SCI_FI -> R.string.character_form_placeholder_name_scifi
        else -> R.string.character_form_placeholder_name // Default placeholder
    }

fun Genre.selectiveHighlight(): SelectiveColorParams =
    when (this) {
        Genre.FANTASY ->
            SelectiveColorParams(
                targetColor = color,
                hueTolerance = .02f,
                saturationThreshold = .05f,
            )

        Genre.SCI_FI ->
            SelectiveColorParams(
                targetColor = color,
                hueTolerance = .15f,
                saturationThreshold = .10f,
                lightnessThreshold = .05f,
                highlightSaturationBoost = 1.5f,
                highlightLightnessBoost = .05f,
                desaturationFactorNonTarget = .15f,
            )
    }
