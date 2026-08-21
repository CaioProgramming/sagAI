package com.ilustris.sagai.features.saga.detail.review.ui.templates.collage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.features.saga.chat.domain.model.rankTopCharacters
import com.ilustris.sagai.features.saga.detail.data.model.ReviewStage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.features.saga.detail.review.ui.reviewCastTitle
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.theme.hexToColor
import kotlinx.coroutines.delay

private const val STICKER_MOUNT_STAGGER_MS = 900L

private data class StickerSlot(
    val anchor: Alignment,
    val paddingStart: Dp = 0.dp,
    val paddingTop: Dp = 0.dp,
    val paddingEnd: Dp = 0.dp,
    val paddingBottom: Dp = 0.dp,
    val rotation: Float,
)

private val STICKER_SLOTS =
    listOf(
        StickerSlot(Alignment.TopStart, paddingStart = 12.dp, paddingTop = 20.dp, rotation = -7f),
        StickerSlot(Alignment.TopEnd, paddingEnd = 16.dp, paddingTop = 110.dp, rotation = 9f),
        StickerSlot(Alignment.BottomStart, paddingStart = 20.dp, paddingBottom = 96.dp, rotation = 7f),
        StickerSlot(Alignment.BottomEnd, paddingEnd = 20.dp, paddingBottom = 28.dp, rotation = -6f),
        StickerSlot(Alignment.CenterEnd, paddingEnd = 28.dp, paddingTop = 70.dp, rotation = -11f),
    )

/**
 * Punk Rock's cast page: the top characters (main character already filtered out by
 * [getCharacters]'s `filterMainCharacter`) as background-free stickers scattered around a centered
 * cast title, instead of the default review's avatar row/list. Mirrors what the default page
 * highlights — the most-present character stands out (bigger sticker) and every character shows
 * their own message count — but as stickers instead of a ranked list.
 *
 * Each sticker's outline uses the character's own [Character.hexColor] instead of one shared genre
 * accent, so the cast reads as visually distinct people rather than a uniform set.
 *
 * [DepthLayout] mounts are staggered [STICKER_MOUNT_STAGGER_MS] apart instead of all appearing at
 * once: [DepthLayoutViewModel] holds a single bitmap pair with no per-image cache, so simultaneous
 * instances sharing a `hiltViewModel()` default key would clobber each other — each sticker below
 * gets an explicit distinct key to avoid that, and mounting them one at a time avoids firing
 * several concurrent MLKit segmentations at once too.
 */
class CollageCharactersPage(
    override val content: SagaContent,
    private val stage: ReviewStage,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.CHARACTERS

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val genre = content.data.genre
        val accent = genre.compiledColorPalette().firstOrNull() ?: MaterialTheme.colorScheme.primary

        val topCharacters =
            remember {
                content
                    .flatMessages()
                    .rankTopCharacters(content.getCharacters(true))
                    .filter { it.first.image.isNotBlank() }
                    .take(STICKER_SLOTS.size)
            }

        var visibleCount by remember { mutableIntStateOf(if (canAnimate) 0 else topCharacters.size) }
        LaunchedEffect(canAnimate, topCharacters.size) {
            if (canAnimate) {
                repeat(topCharacters.size) { index ->
                    visibleCount = index + 1
                    delay(STICKER_MOUNT_STAGGER_MS)
                }
            }
        }

        Box(modifier.fillMaxSize()) {
            // Stickers first, center text last — the text has to paint on top of anything a
            // sticker's scattered position might land under it, not the other way around.
            topCharacters.forEachIndexed { index, (character, messageCount) ->
                if (index >= visibleCount) return@forEachIndexed
                val slot = STICKER_SLOTS[index]
                val characterColor = character.hexColor.hexToColor() ?: accent
                AssemblingPiece(
                    modifier =
                        Modifier
                            .align(slot.anchor)
                            .padding(
                                start = slot.paddingStart,
                                top = slot.paddingTop,
                                end = slot.paddingEnd,
                                bottom = slot.paddingBottom,
                            ),
                    rotation = slot.rotation,
                    delayMs = 0,
                    canAnimate = canAnimate,
                    seed = index + 30,
                ) {
                    CharacterSticker(
                        character = character,
                        accentColor = characterColor,
                        caption = stringResource(R.string.messages_count_label, messageCount),
                        featured = index == 0,
                    )
                }
            }

            Column(
                Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                genre.stylisedText(
                    text = genre.reviewCastTitle(),
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                )

                // The AI's own line about the cast's most-present character — same field the
                // default review shows alongside its top-character spotlight. Flat (unblurred)
                // shadow so it stays legible even where a sticker happens to sit behind it.
                stage.content?.subtitle?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                shadow =
                                    Shadow(
                                        color = MaterialTheme.colorScheme.background,
                                        offset = Offset(2f, 2f),
                                        blurRadius = 0f,
                                    ),
                            ),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
    }
}
