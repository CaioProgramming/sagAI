package com.ilustris.sagai.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre

// Set of Material typography styles to start with
val Typography =
    Typography(
        bodyLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
            ),
    )

fun Genre.headerFont(): FontFamily {
    val fontResource =
        when (this) {
            Genre.FANTASY -> R.font.dragon_force
            Genre.CYBERPUNK -> R.font.ninja_cyber
            Genre.HORROR -> R.font.pixelwarden
            Genre.HEROES -> R.font.super_energy
            Genre.CRIME -> R.font.broadway
            Genre.SPACE_OPERA -> R.font.space_runner
            Genre.SHINOBI -> R.font.genjiro
            else -> null
        }
    return if (fontResource != null) {
        FontFamily(Font(fontResource, FontWeight.Normal))
    } else {
        FontFamily.Default
    }
}

fun Genre.bodyFont(): FontFamily {
    val fontResource =
        when (this) {
            Genre.FANTASY -> R.font.fondamento_regular
            Genre.CYBERPUNK -> R.font.tektur
            Genre.HORROR -> R.font.jersey
            Genre.HEROES -> R.font.comic_book
            Genre.CRIME -> R.font.retro_neon
            Genre.SPACE_OPERA -> R.font.eightgon
            Genre.SHINOBI -> R.font.hina
            else -> null
        }
    return if (fontResource != null) {
        FontFamily(Font(fontResource, FontWeight.Normal))
    } else {
        FontFamily.Default
    }
}
