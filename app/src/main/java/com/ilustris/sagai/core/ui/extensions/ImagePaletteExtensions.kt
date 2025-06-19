package com.ilustris.sagai.core.ui.extensions

import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette

fun Drawable.generatePaletteAsync(
    onPaletteGenerated: (Palette?) -> Unit
) {
    val bitmap = this.toBitmap(
        width = this.intrinsicWidth.coerceAtLeast(1),
        height = this.intrinsicHeight.coerceAtLeast(1)
    )
    Palette.from(bitmap).generate { palette ->
        onPaletteGenerated(palette)
    }
}
