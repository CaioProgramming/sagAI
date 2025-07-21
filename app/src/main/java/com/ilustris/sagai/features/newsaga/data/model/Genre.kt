package com.ilustris.sagai.features.newsaga.data.model

import ai.atick.material.MaterialColor
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.ilustris.sagai.R
import com.ilustris.sagai.ui.theme.filters.SelectiveColorParams

enum class
Genre(
    val title: String,
    val icon: Int,
    val color: Color,
    val iconColor: Color,
    @DrawableRes
    val background: Int,
    val ambientMusicConfigKey: String // New field for Remote Config key
) {
    FANTASY(
        "Fantasia",
        R.drawable.fantasy_icon,
        MaterialColor.Red800,
        Color.Companion.White,
        R.drawable.fantasy,
        "fantasy_ambient_music_url" // Example key
    ),
    SCI_FI(
        "Cyberpunk",
        R.drawable.scifi_icon,
        MaterialColor.DeepPurpleA200,
        Color.Companion.White,
        R.drawable.scifi,
        "scifi_ambient_music_url" // Example key
    ),
}

@StringRes
fun Genre.getNamePlaceholderResId(): Int =
    when (this) {
        Genre.FANTASY -> R.string.character_form_placeholder_name_fantasy
        Genre.SCI_FI -> R.string.character_form_placeholder_name_scifi
        else -> R.string.character_form_placeholder_name
    }

fun Genre.selectiveHighlight(): SelectiveColorParams =
    when (this) {
        Genre.FANTASY ->
            SelectiveColorParams(
                targetColor = color,
                hueTolerance = .05f,
                saturationThreshold = .1f,
                highlightSaturationBoost = 1.2f,
            )

        Genre.SCI_FI ->
            SelectiveColorParams(
                targetColor = color,
                hueTolerance = .2f,
                saturationThreshold = .1f,
                lightnessThreshold = .2f,
                highlightSaturationBoost = 2f,
                highlightLightnessBoost = .1f,
                desaturationFactorNonTarget = .3f,
            )
    }
