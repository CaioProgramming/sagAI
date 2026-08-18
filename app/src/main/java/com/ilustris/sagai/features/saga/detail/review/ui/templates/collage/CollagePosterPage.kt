package com.ilustris.sagai.features.saga.detail.review.ui.templates.collage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
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
import com.ilustris.sagai.ui.theme.themeBubble
import com.ilustris.sagai.ui.theme.themeFilter
import com.ilustris.sagai.ui.theme.themeIcon
import kotlinx.coroutines.delay
import kotlin.random.Random

private const val ASSEMBLY_STEPS = 7
private const val ASSEMBLY_STEP_MS = 90L
private const val IDLE_TREMOR_STEP_MS = 240L
private const val IDLE_JITTER_FRACTION = 0.14f
private const val ENTRANCE_SLIDE_PX = 46f

/** Insert only splits a text into several chips once it would run past this many lines. */
private const val CHIP_SPLIT_MAX_LINES = 3
private val CHIP_MAX_WIDTH = 130.dp

/** Reserved clear space at the top of [CollagePosterPage.Show]'s chip layer, so inserts never start above where the icon/title sit in the depth layer behind them. */
private val CHIP_AREA_TOP_MARGIN = 190.dp

private val STROKE_RING_OFFSETS =
    listOf(
        0f to -1f,
        0.707f to -0.707f,
        1f to 0f,
        0.707f to 0.707f,
        0f to 1f,
        -0.707f to 0.707f,
        -1f to 0f,
        -0.707f to -0.707f,
    )

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
                    Box(
                        Modifier
                            .widthIn(max = CHIP_MAX_WIDTH)
                            .border(2.dp, chipColor, themeBubble())
                            .clip(themeBubble())
                            .background(MaterialTheme.colorScheme.background, themeBubble())
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = text,
                            color = chipColor.readableTextColor(),
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

        Box(modifier.fillMaxSize()) {
            DepthLayout(
                imagePath = cover.url,
                modifier = Modifier.fillMaxSize().themeFilter(),
                foregroundImageModifier =
                    Modifier
                        .imageStroke(MaterialTheme.colorScheme.background, strokeWidthPx)
                        .imageStroke(accent, strokeWidthPx * 1.3f),
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
                        fontSize = MaterialTheme.typography.displayMedium.fontSize,
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

private fun Color.readableTextColor() = if (luminance() > 0.5f) Color.Black else Color.White

/**
 * Solid-color outline hugging the content's actual alpha shape (not its bounding box) — stamps
 * [STROKE_RING_OFFSETS] copies offset by [widthPx], flattens each to a flat [color] silhouette via
 * `saveLayer` + `SrcIn`, then draws the real content on top. Same "offset ghost" technique as
 * [com.ilustris.sagai.ui.animations.comicExtrude]'s front-face outline, without the extrusion body
 * or pop animation — this is meant to sit still behind a photo, not bounce.
 */
private fun Modifier.imageStroke(
    color: Color,
    widthPx: Float,
) = drawWithContent {
    fun drawGhost(
        dx: Float,
        dy: Float,
    ) {
        drawIntoCanvas { canvas ->
            val paint = Paint()
            canvas.saveLayer(Rect(0f, 0f, size.width, size.height), paint)
            canvas.translate(dx, dy)
            drawContent()
            drawRect(color = color, blendMode = BlendMode.SrcIn)
            canvas.restore()
        }
    }
    STROKE_RING_OFFSETS.forEach { (rx, ry) -> drawGhost(rx * widthPx, ry * widthPx) }
    drawContent()
}

/**
 * One collage element that jump-cuts through [ASSEMBLY_STEPS] discrete frames ([ASSEMBLY_STEP_MS]
 * apart, decelerating) as it slides/rotates into place — a "glued into place" stop-motion feel
 * rather than a continuously eased tween. Once settled, it keeps a faint perpetual tremor (unless
 * [idleTremor] is false) — same low-fps jitter idea as
 * [com.ilustris.sagai.ui.components.RansomLetter]'s letters, so the whole page reads as one
 * consistent stop-motion identity instead of a one-shot entrance. Positioning is entirely the
 * caller's [modifier] (e.g. `Modifier.align(...).padding(...)`, scoped to whatever container
 * — [Box] or [Column] — it's actually placed in) so this stays reusable across both.
 */
@Composable
private fun AssemblingPiece(
    modifier: Modifier = Modifier,
    rotation: Float,
    delayMs: Long,
    canAnimate: Boolean,
    seed: Int,
    idleTremor: Boolean = true,
    content: @Composable () -> Unit,
) {
    var step by remember { mutableIntStateOf(if (canAnimate) 0 else ASSEMBLY_STEPS) }

    LaunchedEffect(canAnimate) {
        if (canAnimate) {
            delay(delayMs)
            repeat(ASSEMBLY_STEPS) {
                step++
                delay(ASSEMBLY_STEP_MS)
            }
        } else {
            step = ASSEMBLY_STEPS
        }
        if (idleTremor) {
            while (true) {
                delay(IDLE_TREMOR_STEP_MS)
                step++
            }
        }
    }

    val settleStep = step.coerceAtMost(ASSEMBLY_STEPS)
    val t = settleStep / ASSEMBLY_STEPS.toFloat()
    val eased = 1f - (1f - t) * (1f - t)
    val isSettled = step >= ASSEMBLY_STEPS
    val jitterDecay = if (isSettled) IDLE_JITTER_FRACTION else (1f - t)
    val jitter = Random(seed * 131 + step)
    val jitterX = (jitter.nextFloat() - 0.5f) * 40f * jitterDecay
    val jitterY = (jitter.nextFloat() - 0.5f) * 40f * jitterDecay
    val jitterRot = (jitter.nextFloat() - 0.5f) * 26f * jitterDecay
    val entranceY = ENTRANCE_SLIDE_PX * (1f - eased)
    val scale = 0.6f + 0.4f * eased
    val visible = step > 0

    Box(
        modifier.graphicsLayer {
            translationX = jitterX
            translationY = entranceY + jitterY
            rotationZ = rotation + jitterRot
            scaleX = scale
            scaleY = scale
            alpha = if (visible) 1f else 0f
        },
    ) {
        content()
    }
}
