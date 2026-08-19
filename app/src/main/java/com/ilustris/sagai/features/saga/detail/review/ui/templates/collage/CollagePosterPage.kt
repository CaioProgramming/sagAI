package com.ilustris.sagai.features.saga.detail.review.ui.templates.collage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.features.saga.chat.ui.components.rememberMessageBlocks
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.features.saga.detail.review.ui.coverImageSource
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.components.views.DepthLayout
import com.ilustris.sagai.ui.theme.themeFilter
import com.ilustris.sagai.ui.theme.themeIcon

/** Insert only splits a text into several chips once it would run past this many lines. */
private const val CHIP_SPLIT_MAX_LINES = 3
private val CHIP_MAX_WIDTH = 130.dp

/** Reserved clear space at the top of [CollagePosterPage.Show]'s chip layer, so inserts never start above where the icon/title sit in the depth layer behind them. */
private val CHIP_AREA_TOP_MARGIN = 190.dp

/** One scattered magazine-insert slot, small paddings since [CHIP_AREA_TOP_MARGIN] already clears the title above. */
private data class ChipSlot(
    val anchor: Alignment,
    val paddingStart: Dp = 0.dp,
    val paddingTop: Dp = 0.dp,
    val paddingEnd: Dp = 0.dp,
    val paddingBottom: Dp = 0.dp,
    val rotation: Float,
)

private val CHIP_SLOTS =
    listOf(
        ChipSlot(Alignment.TopStart, paddingStart = 8.dp, rotation = -6f),
        ChipSlot(Alignment.TopEnd, paddingEnd = 8.dp, paddingTop = 70.dp, rotation = 8f),
        ChipSlot(Alignment.CenterStart, paddingStart = 8.dp, paddingBottom = 60.dp, rotation = 5f),
        ChipSlot(Alignment.CenterEnd, paddingEnd = 8.dp, paddingTop = 10.dp, rotation = -7f),
    )

/**
 * Punk Rock's opener, styled as an actual magazine cover:
 * - [Background] keeps the icon + title tucked *behind* the segmented character (in
 *   [DepthLayout]'s middle `content()` slot) for the depth illusion — purely decorative, so it's
 *   fine if the subject partially covers it. The scribble doodles render as a sibling *after* the
 *   whole [DepthLayout] instead, so they paint on top of everything including the character, like
 *   someone drew on the finished poster.
 * - [Show] renders the actual readable text inserts *in front* of everything. They used to live in
 *   the same behind-the-character layer as the title, but the character's silhouette varies a lot
 *   per generated cover and reliably ended up cutting words off — legibility wins over the depth
 *   effect for anything meant to be read.
 */
class CollagePosterPage(
    override val content: SagaContent,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.INTRO

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val genre = content.data.genre
        val palette = genre.compiledColorPalette()
        val accent = palette.firstOrNull() ?: MaterialTheme.colorScheme.primary
        val secondary = palette.getOrNull(1) ?: accent

        val hook =
            content.data.review
                ?.introduction
                ?.hook
        val introContent =
            content.data.review
                ?.introduction
                ?.content

        val density = LocalDensity.current
        val chipMaxWidthPx = with(density) { CHIP_MAX_WIDTH.toPx().toInt() }
        val chipStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)

        val hookChips =
            rememberMessageBlocks(
                text = hook?.subtitle.orEmpty(),
                style = chipStyle,
                maxWidthPx = chipMaxWidthPx,
                maxLines = CHIP_SPLIT_MAX_LINES,
            ).filter { it.isNotBlank() }

        val contentChips =
            rememberMessageBlocks(
                text = introContent?.subtitle.orEmpty(),
                style = chipStyle,
                maxWidthPx = chipMaxWidthPx,
                maxLines = CHIP_SPLIT_MAX_LINES,
            ).filter { it.isNotBlank() }

        val chips =
            remember(hook, introContent, hookChips, contentChips) {
                (listOfNotNull(hook?.title, introContent?.title) + hookChips + contentChips)
                    .filter { it.isNotBlank() }
                    .distinct()
                    .take(CHIP_SLOTS.size)
            }

        Box(
            modifier
                .fillMaxSize()
                .padding(top = CHIP_AREA_TOP_MARGIN),
        ) {
            chips.forEachIndexed { index, text ->
                val slot = CHIP_SLOTS[index]
                val chipColor = if (index % 2 == 0) accent else secondary
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
                    delayMs = index * 140L,
                    canAnimate = canAnimate,
                    seed = index + 10,
                ) {
                    // Every other insert stays paper-white so the coloured ones read as
                    // highlights rather than the page turning into a row of uniform labels.
                    val paper = if (index % 2 == 0) chipColor else PAPER_WHITE
                    TornPaperScrap(
                        seed = index + 300,
                        paperColor = paper,
                        modifier = Modifier.widthIn(max = CHIP_MAX_WIDTH),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Text(
                            text = text,
                            color = paper.readableTextColor(),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            style = chipStyle,
                        )
                    }
                }
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        val genre = content.data.genre
        val accent = genre.compiledColorPalette().firstOrNull() ?: MaterialTheme.colorScheme.primary
        val cover = content.coverImageSource()

        if (cover == null) {
            super.Background(modifier)
            return
        }

        val strokeWidthPx = with(LocalDensity.current) { 4.dp.toPx() }
        // The cutout's outline redraws on the same stop-motion cadence as the collage pieces, so
        // the character reads as hand-cut on every frame rather than pinned under a static border.
        val strokeFrame = rememberStopMotionFrame()
        val strokeJitterPx = with(LocalDensity.current) { 1.6.dp.toPx() }

        Box(modifier.fillMaxSize()) {
            DepthLayout(
                imagePath = cover.url,
                modifier = Modifier.fillMaxSize().themeFilter(),
                foregroundImageModifier =
                    Modifier
                        .imageStroke(
                            color = MaterialTheme.colorScheme.background,
                            widthPx = strokeWidthPx,
                            jitterFrame = strokeFrame,
                            jitterAmountPx = strokeJitterPx,
                        ).imageStroke(
                            color = accent,
                            widthPx = strokeWidthPx * 1.3f,
                            jitterFrame = strokeFrame,
                            jitterAmountPx = strokeJitterPx,
                        ),
            ) {
                // Sits behind the segmented character, in front of the raw photo — the sharp
                // foreground cutout redraws over this, so the title reads as tucked behind the
                // subject rather than floating above the whole scene.
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AssemblingPiece(
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 20.dp),
                        rotation = 0f,
                        delayMs = 500,
                        canAnimate = true,
                        seed = 20,
                        idleTremor = false,
                    ) {
                        Image(
                            themeIcon(),
                            null,
                            Modifier.size(40.dp),
                            colorFilter = ColorFilter.tint(accent),
                        )
                    }

                    genre.stylisedText(
                        text = content.data.title,
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        fontSize = MaterialTheme.typography.displayLarge.fontSize,
                    )
                }
            }

            // Drawn as a sibling *after* the whole DepthLayout, so it paints on top of the
            // background, the title and the character alike — a doodle on the finished poster,
            // not tucked behind it like the title.
            PunkScribbleOverlay(modifier = Modifier.fillMaxSize())
        }
    }
}
