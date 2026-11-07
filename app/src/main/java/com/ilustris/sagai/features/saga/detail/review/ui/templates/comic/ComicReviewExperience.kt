package com.ilustris.sagai.features.saga.detail.review.ui.templates.comic

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.ui.genre.comic.splitIntoBeats
import com.ilustris.sagai.features.saga.detail.data.model.cleanMessage
import com.ilustris.sagai.features.saga.detail.data.model.isComplete
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewExperience
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewNavigationStyle
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType

/** The picture pool is split between the two plate runs so no image is shown twice on the page. */
private const val OPENING_PLATE_COUNT = 3

/**
 * Heroes' motion-comic template — the review as the pages of a printed comic rather than a
 * sequence of screens (see [ReviewNavigationStyle.ComicPages]).
 *
 * Each stage's *hook* — the line the other templates give a whole screen — is a caption strip
 * ([ComicHookPanel]) that hangs over the stage's first page of art, so it costs no page of its own.
 *
 * The organising rule is that a frame holds one thing. Where the default review shows a set — a
 * cast, a run of send-offs, a plate of chapter art — this template spends a frame per member and
 * lets the grid do the collecting, because a set boxed inside a single panel reads as one picture
 * cut up rather than as a page of moments.
 */
class ComicReviewExperience(
    private val content: SagaContent,
) : ReviewExperience {
    override val navigationStyle: ReviewNavigationStyle = ReviewNavigationStyle.ComicPages

    override val pages: List<ReviewPage>
        get() {
            val review = content.data.review ?: return emptyList()
            val images = content.comicImagePool()
            val cast = content.rankedSupportingCast()

            return buildList {
                // Cover first; the introduction's own hook is its strapline (see ComicCoverPanel).
                add(ComicCoverPanel(content))

                review.expressiveness?.let { stage ->
                    stage.hook?.let { add(ComicHookPanel(content, it, ReviewPageType.EXPRESSIVENESS)) }
                    add(ComicEmotionPanel(content, stage))
                }

                review.introduction?.content?.let {
                    add(ComicNarrationPanel(content, it, ReviewPageType.INTRO))
                }

                review.playstyle?.let { stage ->
                    stage.hook?.let { add(ComicHookPanel(content, it, ReviewPageType.PLAYSTYLE)) }
                    stage.content?.let { add(ComicPlaytimePanel(content, it)) }
                }

                // The playstyle's prose rides across the plate that follows it rather than sitting
                // in a box of its own. Captions travelling over a run of pictures is what ties the
                // images into a narrative — loose art with no writing on it says nothing.
                val opening = images.take(OPENING_PLATE_COUNT)
                // Beat 0 stays on the counter panel itself (see ComicPlaytimePanel); the plates
                // pick the prose up from there, so the writing carries on across the pictures
                // instead of restarting on them.
                val openingBeats =
                    splitIntoBeats(
                        review.playstyle?.content?.subtitle.orEmpty(),
                        maxBeats = PLAYSTYLE_BEATS,
                    ).drop(1)
                opening.forEachIndexed { index, image ->
                    add(
                        ComicPlatePanel(
                            content = content,
                            image = image,
                            groupKey = "plate-opening",
                            index = index,
                            caption = openingBeats.getOrNull(index),
                        ),
                    )
                }

                review.topCharacters?.hook?.let {
                    add(ComicHookPanel(content, it, ReviewPageType.CHARACTERS))
                }
                review.topCharacters?.content?.let { text ->
                    cast.firstOrNull()?.let { (character, count) ->
                        add(ComicCastLeadPanel(content, character, count, text))
                    }
                    cast.drop(1).forEach { (character, count) ->
                        add(ComicCastMemberPanel(content, character, count))
                    }
                }

                review.actsInsight?.hook?.let {
                    add(ComicHookPanel(content, it, ReviewPageType.JOURNEY))
                }
                review.actsInsight?.content?.let { journey ->
                    // Same again for the recap: the pictures carry it, with the writing stepping
                    // across them frame by frame instead of taking a panel of its own.
                    val closing = images.drop(OPENING_PLATE_COUNT)
                    // A saga with too few pictures to spare has no plates to carry the recap, and
                    // without them the writing would be lost: it takes a caption strip instead.
                    if (closing.isEmpty()) add(ComicNarrationPanel(content, journey, ReviewPageType.JOURNEY))
                    val journeyBeats =
                        splitIntoBeats(
                            journey.subtitle.orEmpty(),
                            maxBeats = closing.size.coerceAtLeast(1),
                        )
                    closing.forEachIndexed { index, image ->
                        add(
                            ComicPlatePanel(
                                content = content,
                                image = image,
                                groupKey = "plate-journey",
                                index = index,
                                caption = journeyBeats.getOrNull(index),
                            ),
                        )
                    }
                }

                review.conclusion?.hook?.let {
                    add(ComicHookPanel(content, it, ReviewPageType.CONCLUSION))
                }
                review.conclusion?.content?.let { add(ComicConclusionPanel(content, it)) }

                // One frame per farewell, gridded evenly — no send-off outranks another.
                review.farewells.orEmpty().forEach { farewell ->
                    val character =
                        content.characters.find { it.data.id == farewell.characterId } ?: return@forEach
                    add(
                        ComicFarewellPanel(
                            content = content,
                            character = character.data,
                            message = farewell.cleanMessage(character.data.name),
                        ),
                    )
                }

                // The back cover marks that nothing more is coming, so it waits for every stage.
                if (review.isComplete()) {
                    add(ComicBackCoverPanel(content))
                }
            }
        }
}
