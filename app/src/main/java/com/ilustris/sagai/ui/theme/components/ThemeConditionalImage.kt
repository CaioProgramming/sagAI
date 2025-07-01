package com.ilustris.sagai.ui.theme.components

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.invertedColors

@Composable
fun ConditionalImage(
    @DrawableRes resource: Int,
    brush: Brush,
    customBlendMode: BlendMode? = null,
    modifier: Modifier,
) {
    val blendMode = customBlendMode ?: if (isSystemInDarkTheme()) BlendMode.Darken else BlendMode.Lighten
    val resource = painterResource(resource)

    Box(
        modifier.gradientFill(
            brush = brush,
            blendMode = blendMode,
        ),
    ) {
        Image(
            resource,
            null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .fillMaxSize()
                    .then(
                        if (isSystemInDarkTheme().not()) {
                            Modifier.invertedColors()
                        } else {
                            Modifier
                        },
                    ),
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL, showBackground = true)
@Composable
fun ConditionalImagePreview() {
    LazyVerticalGrid(columns = GridCells.Fixed(2)) {
        val blendOptions =
            listOf(
                "ColorBurn" to BlendMode.ColorBurn,
                "ColorDodge" to BlendMode.ColorDodge,
                "Darken" to BlendMode.Darken,
                "Dst" to BlendMode.Dst,
                "DstAtop" to BlendMode.DstAtop,
                "DstOver" to BlendMode.DstOver,
                "Lighten" to BlendMode.Lighten,
                "Modulate" to BlendMode.Modulate,
                "Screen" to BlendMode.Screen,
                "Src" to BlendMode.Src,
                "SrcAtop" to BlendMode.SrcAtop,
                "SrcIn" to BlendMode.SrcIn,
                "SrcOut" to BlendMode.SrcOut,
                "SrcOver" to BlendMode.SrcOver,
                "Xor" to BlendMode.Xor,
                "Plus" to BlendMode.Plus,
                "Overlay" to BlendMode.Overlay,
            )
        val testGenre = Genre.SCI_FI
        items(blendOptions) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    it.first,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(4.dp),
                )
                ConditionalImage(
                    testGenre.background,
                    testGenre.gradient(),
                    customBlendMode = it.second,
                    Modifier
                        .size(200.dp)
                        .aspectRatio(1f),
                )
            }
        }
    }
}
