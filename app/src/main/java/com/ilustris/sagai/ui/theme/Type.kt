package com.ilustris.sagai.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre

private val interFontFamily =
    FontFamily(
        Font(R.font.inter, FontWeight.Normal),
    )

val Typography =
    Typography(
        // Display styles - largest text
        displayLarge =
            TextStyle(
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 57.sp,
                lineHeight = 64.sp,
                letterSpacing = (-0.25).sp,
            ),
        displayMedium =
            TextStyle(
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 45.sp,
                lineHeight = 52.sp,
                letterSpacing = 0.sp,
            ),
        displaySmall =
            TextStyle(
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 36.sp,
                lineHeight = 44.sp,
                letterSpacing = 0.sp,
            ),
        // Headline styles
        headlineLarge =
            TextStyle(
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                letterSpacing = 0.sp,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                letterSpacing = 0.sp,
            ),
        headlineSmall =
            TextStyle(
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                letterSpacing = 0.sp,
            ),
        // Title styles
        titleLarge =
            TextStyle(
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp,
            ),
        titleMedium =
            TextStyle(
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.15.sp,
            ),
        titleSmall =
            TextStyle(
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp,
            ),
        // Body styles
        bodyLarge =
            TextStyle(
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp,
            ),
        bodySmall =
            TextStyle(
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.4.sp,
            ),
        // Label styles
        labelLarge =
            TextStyle(
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp,
            ),
        labelMedium =
            TextStyle(
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp,
            ),
        labelSmall =
            TextStyle(
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 16.sp,
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
            Genre.COWBOY -> R.font.rye
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
            Genre.COWBOY -> R.font.special_elite
            else -> null
        }
    return if (fontResource != null) {
        FontFamily(Font(fontResource, FontWeight.Normal))
    } else {
        FontFamily.Default
    }
}
