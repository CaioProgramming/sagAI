package com.ilustris.sagai.features.share.ui.card

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.withFrameNanos
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import timber.log.Timber

/**
 * [bitmap] graded with the genre's filter, applied once.
 *
 * `effectForGenre` is a full-size AGSL shader. On a card that tilts, with the art drifting in
 * parallax, it would run again over the whole image on every frame, which is what made the first
 * version lag. Here the filtered result is captured to a bitmap after it has drawn once, and from
 * then on the card only moves a finished picture around.
 *
 * [blur] is baked in too (API 31+): a soft backdrop costs nothing per frame once captured.
 *
 * Until the capture is ready the filtered image shows live, so the card is never without art. If the
 * capture fails, it stays live.
 */
@Composable
fun BakedGenreImage(
    bitmap: Bitmap,
    genre: Genre,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    blur: Dp = 0.dp,
) {
    val layer = rememberGraphicsLayer()
    var baked by remember(bitmap, genre) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(bitmap, genre) {
        // Two frames: one to draw the filtered image, one so the shader has certainly run.
        withFrameNanos { }
        withFrameNanos { }
        baked =
            runCatching { layer.toImageBitmap() }
                .onFailure { Timber.w(it, "Could not bake the genre filter; art stays live") }
                .getOrNull()
    }

    val ready = baked
    if (ready != null) {
        Image(ready, contentDescription = null, modifier = modifier, contentScale = contentScale)
    } else {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier =
                modifier
                    .drawWithContent {
                        layer.record { this@drawWithContent.drawContent() }
                        drawLayer(layer)
                    }.then(if (blur > 0.dp) Modifier.blur(blur) else Modifier)
                    .effectForGenre(genre),
            contentScale = contentScale,
        )
    }
}
