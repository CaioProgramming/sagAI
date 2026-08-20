package com.ilustris.sagai.features.saga.detail.review.ui.templates.comic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.saga.chat.domain.model.rankTopCharacters
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType

internal const val CAST_GROUP = "cast"

/**
 * The lead of the cast, in a frame of its own.
 *
 * Only this one carries the stage's writing. The rest of the cast answer with a tally alone —
 * the ranking is told by how much room a face gets, so repeating the prose beside every portrait
 * would just be noise under pictures that already say it.
 */
class ComicCastLeadPanel(
    override val content: SagaContent,
    private val character: Character,
    private val messageCount: Int,
    private val text: ReviewText,
) : ReviewPage,
    ComicPanelPage {
    override val pageType: ReviewPageType = ReviewPageType.CHARACTERS

    override val panelSpan = PanelSpan.FULL

    override val estimatedRevealDurationMs: Long = 5200L

    override val balloons: List<ComicBalloonSpec>
        get() =
            buildList {
                text.title?.let { title ->
                    add(
                        ComicBalloonSpec(
                            alignment = Alignment.TopStart,
                            widthFraction = 0.7f,
                            offset = DpOffset((-8).dp, 8.dp),
                        ) { ComicFadeIn { ComicTag(text = title) } },
                    )
                }

                add(
                    ComicBalloonSpec(
                        alignment = Alignment.BottomStart,
                        widthFraction = 0.4f,
                        offset = DpOffset(8.dp, (-8).dp),
                    ) {
                        ComicFadeIn(delayMillis = 250) {
                            ComicTag(
                                text =
                                    stringResource(
                                        R.string.messages_count_label,
                                        "$messageCount",
                                    ),
                            )
                        }
                    },
                )

                text.subtitle?.let { subtitle ->
                    add(
                        ComicBalloonSpec(
                            alignment = Alignment.BottomEnd,
                            widthFraction = 0.56f,
                            offset = DpOffset(8.dp, (-8).dp),
                        ) {
                            ComicFadeIn(delayMillis = 500) { ComicCaptionBox(text = subtitle) }
                        },
                    )
                }
            }

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) = CastPortrait(character, modifier)
}

/** A supporting player: their face, and how much of the saga they spoke for. */
class ComicCastMemberPanel(
    override val content: SagaContent,
    private val character: Character,
    private val messageCount: Int,
) : ReviewPage,
    ComicPanelPage {
    override val pageType: ReviewPageType = ReviewPageType.CHARACTERS

    override val panelSpan = PanelSpan.GRID

    override val groupKey = CAST_GROUP

    override val estimatedRevealDurationMs: Long = 2600L

    override val balloons: List<ComicBalloonSpec>
        get() =
            listOf(
                ComicBalloonSpec(
                    alignment = Alignment.BottomStart,
                    widthFraction = 0.94f,
                    offset = DpOffset(6.dp, (-6).dp),
                ) {
                    ComicFadeIn {
                        ComicTag(
                            text = stringResource(R.string.messages_count_label, "$messageCount"),
                        )
                    }
                },
            )

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) = CastPortrait(character, modifier)
}

@Composable
private fun CastPortrait(
    character: Character,
    modifier: Modifier,
) {
    Box(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        character.image.takeIf { it.isNotBlank() }?.let {
            AsyncImage(
                model = it,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/**
 * The cast by how much they spoke, protagonist excluded — the player's own character heads every
 * count by construction, so leaving them in would spend the lead frame on the reader themselves.
 *
 * Everyone who spoke at all is here. Having taken a top-N as well, the review quietly dropped
 * characters who were genuinely part of the story just for being quiet in it — and having a line
 * at all is already the test of whether someone took part, so ranking them and then truncating
 * that ranking was applying the same judgement twice, the second time badly.
 */
internal fun SagaContent.rankedSupportingCast(): List<Pair<Character, Int>> {
    val messages = flatMessages()
    val supporting = characters.map { it.data }.filter { it.id != mainCharacter?.data?.id }
    return messages
        .rankTopCharacters(supporting)
        .filter { it.second > 0 }
}
