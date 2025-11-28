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
) {
    FANTASY(
        title = R.string.genre_fantasy,
        color = MaterialColor.RedA400,
        iconColor = Color.White,
        background = R.drawable.fantasy,
    ),
    CYBERPUNK(
        title = R.string.genre_scifi,
        color = MaterialColor.DeepPurpleA200,
        iconColor = Color.White,
        background = R.drawable.scifi,
    ),

    HORROR(
        title = R.string.genre_horror,
        color = MaterialColor.BlueGray200,
        iconColor = Color.Black,
        background = R.drawable.horror,
    ),

    HEROES(
        title = R.string.genre_heroes,
        color = MaterialColor.Blue900,
        iconColor = Color.White,
        background = R.drawable.hero,
    ),
    CRIME(
        title = R.string.genre_crime,
        color = MaterialColor.PinkA100,
        iconColor = Color.White,
        background = R.drawable.crime,
    ),

    SHINOBI(
        title = R.string.genre_shinobi,
        color = Color(0xff880101),
        iconColor = Color.White,
        background = R.drawable.shinobi_background,
    ),

    SPACE_OPERA(
        title = R.string.genre_space_opera,
        color = MaterialColor.CyanA700,
        iconColor = Color.Black,
        background = R.drawable.space_opera,
    ),

    COWBOYS(
        title = R.string.genre_cowboys,
        color = Color(0xFF8D6E63),
        iconColor = Color.White,
        background = R.drawable.cowboys,
    ),
    ;

    val ambientMusicConfigKey: String = "${this.name}_ambient_music_url".lowercase()
}

fun Genre.selectiveHighlight(): SelectiveColorParams =
    when (this) {
        Genre.FANTASY ->
            SelectiveColorParams(
                targetColor = color,
                hueTolerance = .85f,
                saturationThreshold = .6f,
                lightnessThreshold = .25f,
                highlightSaturationBoost = 2f,
                desaturationFactorNonTarget = .5f,
            )

        Genre.CYBERPUNK ->
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
        Genre.SHINOBI ->
            SelectiveColorParams(
                targetColor = color,
                // tight hue tolerance to preserve that specific wine red accent
                hueTolerance = .02f,
                saturationThreshold = .18f,
                lightnessThreshold = .12f,
                highlightSaturationBoost = 1.8f,
                desaturationFactorNonTarget = .45f,
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

        Genre.COWBOYS ->
            SelectiveColorParams(
                targetColor = color,
                hueTolerance = .1f,
                saturationThreshold = .2f,
                lightnessThreshold = .2f,
                highlightSaturationBoost = 1.5f,
                desaturationFactorNonTarget = .5f,
            )
    }

fun Genre.defaultHeaderImage() =
    when (this) {
        Genre.FANTASY -> R.drawable.fantasy_card
        Genre.CYBERPUNK -> R.drawable.scifi_card
        Genre.HORROR -> R.drawable.horror_card
        Genre.HEROES -> R.drawable.hero_card
        Genre.CRIME -> R.drawable.crime_card
        Genre.SHINOBI -> R.drawable.shinobi_card
        Genre.SPACE_OPERA -> R.drawable.space_opera_card
        Genre.COWBOYS -> R.drawable.cowboys_card
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
                color,
                MaterialColor.OrangeA200,
                MaterialColor.DeepOrange600,
                MaterialColor.RedA200,
            )

        Genre.CYBERPUNK ->
            listOf(
                color,
                MaterialColor.Teal800,
                MaterialColor.DeepPurple800,
                MaterialColor.PinkA100,
            )

        Genre.HORROR ->
            listOf(
                color,
                MaterialColor.BlueGray700,
                MaterialColor.LightBlue50,
                MaterialColor.BlueGray300,
            )

        Genre.HEROES ->
            listOf(
                color,
                MaterialColor.Blue900,
                MaterialColor.LightBlue300,
                MaterialColor.RedA200,
            )

        Genre.CRIME ->
            listOf(
                color,
                MaterialColor.PinkA100,
                MaterialColor.Amber300,
                MaterialColor.YellowA200,
            )
        Genre.SHINOBI ->
            listOf(
                color,
                MaterialColor.Brown100,
                MaterialColor.Pink100,
                MaterialColor.Indigo600,
            )
        Genre.SPACE_OPERA ->
            listOf(
                color,
                MaterialColor.LightBlueA400,
                MaterialColor.TealA400,
                MaterialColor.LimeA200,
            )

        Genre.COWBOYS ->
            listOf(
                color,
                MaterialColor.Orange300,
                MaterialColor.Brown300,
                MaterialColor.DeepOrange200,
            )
    }
