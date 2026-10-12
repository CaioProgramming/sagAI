package com.ilustris.sagai.features.share.ui

import android.graphics.Bitmap
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.features.share.presentation.SharePlayViewModel
import com.ilustris.sagai.features.share.ui.card.BakedGenreImage
import com.ilustris.sagai.features.share.ui.card.CollectorCard
import com.ilustris.sagai.features.share.ui.card.CollectorShareFrame
import com.ilustris.sagai.features.share.ui.card.drift

/** The character share: a tilting, holographic collector card of the character. */
@Composable
fun CharacterShareView(
    content: Saga,
    character: CharacterContent,
    viewModel: SharePlayViewModel = hiltViewModel(),
) {
    val genre = remember(content) { content.genre }
    val originalImage by viewModel.originalBitmap.collectAsStateWithLifecycle()
    val segmentedImage by viewModel.segmentedBitmap.collectAsStateWithLifecycle()

    LaunchedEffect(character.data.image) {
        viewModel.segmentImage(character.data.image)
    }

    CollectorShareFrame(genre, ShareType.CHARACTER.name, viewModel) { tilt, captureLayer, shareText ->
        CollectorCard(
            genre = genre,
            title = "${character.data.name} ${character.data.lastName.orEmpty()}".trim(),
            subtitle = character.data.profile.occupation,
            number = "#${character.data.id}",
            body = shareText?.text,
            tilt = tilt,
            captureLayer = captureLayer,
            modifier = Modifier.fillMaxWidth(),
        ) { cardTilt, scale ->
            CharacterArt(
                imageUrl = character.data.image,
                original = originalImage,
                segmented = segmentedImage,
                genre = genre,
                tilt = cardTilt,
                scale = scale,
            )
        }
    }
}

/**
 * The art behind the card: the character cut out over their own backdrop when segmentation is done,
 * the plain portrait until then. The two layers drift in opposite directions as the card tilts. The
 * genre filter is baked once, not run per frame.
 */
@Composable
private fun BoxScope.CharacterArt(
    imageUrl: String,
    original: Bitmap?,
    segmented: Bitmap?,
    genre: Genre,
    tilt: () -> Offset,
    scale: Float,
) {
    if (original != null && segmented != null) {
        BakedGenreImage(
            bitmap = original,
            genre = genre,
            modifier = Modifier.fillMaxSize().drift(tilt, -6f, 1.1f, scale),
            // The backdrop still contains the character. Blurred, the offset copy reads as depth of
            // field behind the sharp cut-out instead of as a second, doubled figure.
            blur = 10.dp,
        )
        BakedGenreImage(
            bitmap = segmented,
            genre = genre,
            modifier = Modifier.fillMaxSize().drift(tilt, 4f, 1.05f, scale),
        )
    } else {
        // Plain until the cut-out is ready: the genre filter is applied once, to the finished pair.
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().drift(tilt, -5f, 1.1f, scale),
        )
    }
}
