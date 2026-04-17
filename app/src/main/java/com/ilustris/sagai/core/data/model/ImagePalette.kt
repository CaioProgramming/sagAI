package com.ilustris.sagai.core.data.model

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.palette.graphics.Palette

data class ImagePalette(
    val dominant: Color? = null,
    val vibrant: Color? = null,
    val muted: Color? = null,
    val darkVibrant: Color? = null,
    val darkMuted: Color? = null,
    val onDominant: Color? = null,
    val onVibrant: Color? = null,
) {
    companion object {
        fun fromBitmap(bitmap: Bitmap): ImagePalette {
            val palette = Palette.from(bitmap).generate()
            val dominantSwatch = palette.dominantSwatch
            val vibrantSwatch = palette.vibrantSwatch

            return ImagePalette(
                dominant = dominantSwatch?.rgb?.let { Color(it) },
                vibrant = vibrantSwatch?.rgb?.let { Color(it) },
                muted = palette.mutedSwatch?.rgb?.let { Color(it) },
                darkVibrant = palette.darkVibrantSwatch?.rgb?.let { Color(it) },
                darkMuted = palette.darkMutedSwatch?.rgb?.let { Color(it) },
                onDominant =
                    dominantSwatch?.rgb?.let {
                        val color = Color(it)
                        if (color.luminance() > 0.5f) Color.Black else Color.White
                    },
                onVibrant =
                    vibrantSwatch?.rgb?.let {
                        val color = Color(it)
                        if (color.luminance() > 0.5f) Color.Black else Color.White
                    },
            )
        }
    }
}
