package com.ilustris.sagai.features.newsaga.data.model

import ai.atick.material.MaterialColor
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.ilustris.sagai.R
import com.ilustris.sagai.ui.theme.filters.SelectiveColorParams

enum class
Genre(
    @StringRes
    val title: Int,
    val color: Color,
    val iconColor: Color,
    @DrawableRes
    val background: Int,
    val ambientMusicConfigKey: String,
) {
    FANTASY(
        title = R.string.genre_fantasy,
        color = MaterialColor.Red800,
        iconColor = Color.White,
        background = R.drawable.fantasy,
        ambientMusicConfigKey = "fantasy_ambient_music_url",
    ),
    SCI_FI(
        title = R.string.genre_scifi,
        color = MaterialColor.DeepPurpleA200,
        iconColor = Color.White,
        background = R.drawable.scifi,
        ambientMusicConfigKey = "scifi_ambient_music_url",
    ),

    HORROR(
        title = R.string.genre_horror,
        color = MaterialColor.BlueGray200,
        iconColor = Color.Black,
        background = R.drawable.horror,
        ambientMusicConfigKey = "horror_ambient_music_url",
    ),

    HEROES(
        title = R.string.genre_heroes,
        color = MaterialColor.Blue900,
        iconColor = Color.White,
        background = R.drawable.hero,
        ambientMusicConfigKey = "heroes_ambient_music_url",
    ),
    CRIME(
        title = R.string.genre_crime,
        color = MaterialColor.PinkA100,
        iconColor = Color.White,
        background = R.drawable.crime,
        ambientMusicConfigKey = "crime_ambient_music_url",
    ),

    SPACE_OPERA(
        title = R.string.genre_space_opera,
        color = MaterialColor.CyanA700,
        iconColor = Color.Black,
        background = R.drawable.space_opera,
        ambientMusicConfigKey = "space_opera_ambient_music_url",
    ),
}

fun Genre.selectiveHighlight(): SelectiveColorParams =
    when (this) {
        Genre.FANTASY ->
            SelectiveColorParams(
                targetColor = color,
                hueTolerance = 1f,
                saturationThreshold = .5f,
                lightnessThreshold = .5f,
                highlightSaturationBoost = 2f,
                desaturationFactorNonTarget = .4f,
            )

        Genre.SCI_FI ->
            SelectiveColorParams(
                targetColor = color,
                hueTolerance = .11f,
                saturationThreshold = .15f,
                lightnessThreshold = .47f,
                highlightSaturationBoost = 2f,
                desaturationFactorNonTarget = .5f,
            )

        Genre.HORROR ->
            SelectiveColorParams(
                targetColor = color,
                hueTolerance = .3f,
                saturationThreshold = .3f,
                highlightSaturationBoost = 1.3f,
                desaturationFactorNonTarget = .7f,
            )

        Genre.HEROES ->
            SelectiveColorParams(
                targetColor = color,
                hueTolerance = .3f,
                saturationThreshold = .45f,
                lightnessThreshold = .25f,
                highlightSaturationBoost = 1.4f,
                desaturationFactorNonTarget = .5f,
            )

        Genre.CRIME ->
            SelectiveColorParams(
                targetColor = color,
                hueTolerance = .05f,
                saturationThreshold = .13f,
                lightnessThreshold = .10f,
                highlightSaturationBoost = 1.4f,
                desaturationFactorNonTarget = .4f,
            )
        Genre.SPACE_OPERA ->
            SelectiveColorParams(
                targetColor = color,
                hueTolerance = 1f,
                saturationThreshold = .5f,
                lightnessThreshold = .5f,
                highlightSaturationBoost = 2f,
                desaturationFactorNonTarget = .4f,
            )
    }

fun Genre.defaultHeaderImage() =
    when (this) {
        Genre.FANTASY -> R.drawable.fantasy_card
        Genre.SCI_FI -> R.drawable.scifi_card
        Genre.HORROR -> R.drawable.horror_card
        Genre.HEROES -> R.drawable.hero_card
        Genre.CRIME -> R.drawable.crime_card
        Genre.SPACE_OPERA -> R.drawable.space_opera_card
    }

fun Genre.shimmerColors() =
    listOf(
        Color.Transparent,
        color.copy(alpha = .3f),
    ).plus(colorPalette())
        .plus(color.copy(alpha = .3f))
        .plus(Color.Transparent)

fun Genre.colorPalette() =
    when (this) {
        Genre.FANTASY ->
            listOf(
                MaterialColor.Red900,
                MaterialColor.Pink200,
                MaterialColor.DeepOrange600,
                MaterialColor.Red500,
            )

        Genre.SCI_FI ->
            listOf(
                MaterialColor.Purple500,
                MaterialColor.Teal800,
                MaterialColor.DeepPurple800,
                MaterialColor.PinkA100,
            )

        Genre.HORROR ->
            listOf(
                MaterialColor.BlueGray100,
                MaterialColor.BlueGray700,
                MaterialColor.LightBlue50,
                MaterialColor.BlueGray300,
            )

        Genre.HEROES ->
            listOf(
                MaterialColor.Blue500,
                MaterialColor.Blue900,
                MaterialColor.LightBlue300,
                MaterialColor.RedA200,
            )

        Genre.CRIME ->
            listOf(
                MaterialColor.PinkA200,
                MaterialColor.PinkA100,
                MaterialColor.Amber300,
                MaterialColor.YellowA200,
            )
        Genre.SPACE_OPERA ->
            listOf(
                MaterialColor.CyanA700,
                MaterialColor.LightBlueA400,
                MaterialColor.TealA400,
                MaterialColor.LimeA200,
            )
    }
