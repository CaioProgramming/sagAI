package com.ilustris.sagai.features.newsaga.data.model

import ai.atick.material.MaterialColor
import android.annotation.SuppressLint
import android.graphics.RectF
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.EaseOutCubic // Added
import androidx.compose.animation.core.animateDpAsState // Added
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.ui.theme.filters.SelectiveColorParams
import com.ilustris.sagai.ui.theme.gradient
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class
Genre(
    val title: String,
    val icon: Int,
    val color: Color,
    val iconColor: Color,
    @DrawableRes
    val background: Int,
    val ambientMusicConfigKey: String,
) {
    FANTASY(
        title = "Fantasia",
        icon = R.drawable.fantasy_icon,
        color = MaterialColor.Red800,
        iconColor = Color.White,
        background = R.drawable.fantasy,
        ambientMusicConfigKey = "fantasy_ambient_music_url",
    ),
    SCI_FI(
        title = "Cyberpunk",
        icon = R.drawable.scifi_icon,
        color = MaterialColor.DeepPurpleA200,
        iconColor = Color.White,
        background = R.drawable.scifi,
        ambientMusicConfigKey = "scifi_ambient_music_url",
    ),

    HORROR(
        title = "Terror",
        icon = R.drawable.ic_spark,
        color = MaterialColor.BlueGray200,
        iconColor = Color.Black,
        background = R.drawable.horror,
        ambientMusicConfigKey = "horror_ambient_music_url",
    ),

    HEROES(
        title = "Heróis",
        icon = R.drawable.ic_spark,
        color = MaterialColor.Blue900,
        iconColor = Color.White,
        background = R.drawable.hero,
        ambientMusicConfigKey = "heroes_ambient_music_url",
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
                hueTolerance = 1f,
                saturationThreshold = .5f,
                lightnessThreshold = .4f,
                highlightSaturationBoost = 2f,
                desaturationFactorNonTarget = .5f,
            )

        Genre.SCI_FI ->
            SelectiveColorParams(
                targetColor = color,
                hueTolerance = 1f,
                saturationThreshold = .3f,
                lightnessThreshold = .15f,
                highlightSaturationBoost = 1.5f,
                highlightLightnessBoost = .0f,
                desaturationFactorNonTarget = .2f,
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
                highlightSaturationBoost = 1.2f,
                desaturationFactorNonTarget = .5f,
            )
    }

fun Genre.defaultHeaderImage() =
    when (this) {
        Genre.FANTASY -> R.drawable.fantasy_card
        Genre.SCI_FI -> R.drawable.scifi_card
        Genre.HORROR -> R.drawable.horror_card
        Genre.HEROES -> R.drawable.hero_card
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
                MaterialColor.LightBlue600,
                MaterialColor.LightBlueA700,
            )
    }
