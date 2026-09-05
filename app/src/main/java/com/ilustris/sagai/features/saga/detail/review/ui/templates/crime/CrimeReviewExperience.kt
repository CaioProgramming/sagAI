package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.saga.chat.domain.model.rankTopCharacters
import com.ilustris.sagai.features.saga.detail.data.model.ReviewStage
import com.ilustris.sagai.features.saga.detail.data.model.cleanMessage
import com.ilustris.sagai.features.saga.detail.data.model.isComplete
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewExperience
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewNavigationStyle
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.features.saga.detail.review.ui.notableChapterImageSources
import com.ilustris.sagai.ui.genre.comic.splitIntoBeats

/**
 * The table caps nothing. Every chapter that has art gets a photo, and every character who spoke
 * gets a portrait — `notableChapterImageSources` already drops the chapters with no cover, which is
 * the only filtering either set needs.
 *
 * The templates that do cap (the default review's top five, Collage's sticker slots) cap because
 * they draw onto one fixed page and genuinely run out of room. A table that scrolls has no such
 * limit, so carrying their numbers over here only ever hid material — and the reader is here to
 * revisit their own story, where there is no way to know from this side which chapter or which
 * character was the one that mattered to them.
 */
private const val ALL_WITH_ART = Int.MAX_VALUE

/**
 * Crime's SagaReview presented as photos spread on a table — see
 * [com.ilustris.sagai.features.saga.detail.review.ui.ReviewNavigationStyle.Corkboard]. One card per
 * subject instead of alternating chat bubbles: the saga icon opens the table, every top character
 * and notable chapter gets its own photo, and the text-only stages become notes.
 *
 * Every stage the review writes reaches the table, which is the thing this template got wrong at
 * first: it read `topCharacters` and `actsInsight` only to decide *whether* to lay out portraits
 * and stills, and never showed a word either stage had written — prose both
 * [com.ilustris.sagai.features.saga.detail.review.ui.DefaultReviewExperience] and the comic page do
 * surface. Now that writing rides on the **backs** of the very photos it describes (tap to turn one
 * over), and falls back to a note of its own when a saga has no art to carry it.
 */
class CrimeReviewExperience(
    private val content: SagaContent,
) : ReviewExperience {
    override val navigationStyle: ReviewNavigationStyle = ReviewNavigationStyle.Corkboard

    override val pages: List<ReviewPage>
        get() {
            val review = content.data.review ?: return emptyList()

            return buildList {
                add(
                    CorkboardCoverPinPage(
                        content = content,
                        caption = review.introduction.shortCaption(),
                        fullIntroduction = review.introduction.fullProse(),
                    ),
                )

                review.expressiveness?.let { stage ->
                    val tone =
                        content
                            .flatEvents()
                            .map { it.emotionalRanking() }
                            .firstOrNull()
                            ?.firstOrNull()
                            ?.first
                    tone?.let { add(CorkboardVibePinPage(content, it, stage.shortCaption())) }
                }

                review.playstyle?.let { stage ->
                    add(CorkboardPlaystylePinPage(content, stage.shortCaption()))
                }

                review.topCharacters?.let { stage ->
                    val cast =
                        content
                            .flatMessages()
                            .rankTopCharacters(content.getCharacters(true))
                            // rankTopCharacters ranks but never filters, so without this a
                            // character who never said a word still gets a photo on the board.
                            .filter { (_, messageCount) -> messageCount > 0 }

                    if (cast.isEmpty()) {
                        // No portraits to write on, so the cast write-up gets a card of its own
                        // rather than being dropped.
                        stage.fullProse()?.let {
                            add(CorkboardNotePinPage(content, ReviewPageType.CHARACTERS, it, title = stage.headline()))
                        }
                    } else {
                        cast.forEachIndexed { index, (character, messageCount) ->
                            add(
                                CorkboardCharacterPinPage(
                                    content = content,
                                    character = character,
                                    messageCount = messageCount,
                                    // The whole group shares one write-up, so it goes on the back of
                                    // the lead portrait — the comic page's lead panel does the same.
                                    castNote = stage.fullProse().takeIf { index == 0 },
                                ),
                            )
                        }
                    }
                }

                review.actsInsight?.let { stage ->
                    val images = content.notableChapterImageSources(limit = ALL_WITH_ART)
                    val prose = stage.fullProse()

                    if (images.isEmpty()) {
                        prose?.let {
                            add(CorkboardNotePinPage(content, ReviewPageType.JOURNEY, it, title = stage.headline()))
                        }
                    } else {
                        // The recap is dealt across the photos it describes, a beat per still, the
                        // way the comic's plates carry theirs.
                        val beats =
                            prose?.let { splitIntoBeats(it, maxBeats = images.size) }.orEmpty()
                        images.forEachIndexed { index, image ->
                            add(CorkboardChapterPinPage(content, image, beats.getOrNull(index)))
                        }
                    }
                }

                review.conclusion?.let { stage ->
                    stage.fullProse()?.let {
                        add(CorkboardNotePinPage(content, ReviewPageType.CONCLUSION, it, title = stage.headline()))
                    }
                }

                review.farewells
                    ?.takeIf { it.isNotEmpty() }
                    ?.forEach { farewell ->
                        val speaker =
                            content.characters
                                .find { it.data.id == farewell.characterId }
                                ?.data
                        if (speaker != null) {
                            add(
                                CorkboardNotePinPage(
                                    content,
                                    ReviewPageType.FAREWELLS,
                                    farewell.cleanMessage(speaker.name),
                                    sender = speaker,
                                ),
                            )
                        }
                    }

                if (review.isComplete()) {
                    add(CorkboardSummaryPinPage(content))
                }
            }
        }
}

/**
 * The line or two the front of a card has room for under its photo. Prefers each text's subtitle —
 * the title is usually a label ("Sua jornada") that the card's own layout already says.
 */
private fun ReviewStage?.shortCaption(): String? {
    this ?: return null
    val hookLine = hook?.subtitle?.takeIf { it.isNotBlank() } ?: hook?.title?.takeIf { it.isNotBlank() }
    val contentLine = content?.subtitle?.takeIf { it.isNotBlank() } ?: content?.title?.takeIf { it.isNotBlank() }
    return listOfNotNull(hookLine, contentLine).joinToString(separator = " ").takeIf { it.isNotBlank() }
}

/**
 * Everything a stage wrote, in order and de-duplicated — what the back of a card carries, and the
 * reason no stage's prose goes missing any more.
 */
private fun ReviewStage?.fullProse(): String? {
    this ?: return null
    return listOfNotNull(hook?.subtitle, content?.title, content?.subtitle)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinct()
        .joinToString(separator = "\n\n")
        .takeIf { it.isNotBlank() }
}

/** A stage's own heading, when it has one worth putting at the top of a note. */
private fun ReviewStage.headline(): String? =
    (hook?.title ?: content?.title)?.takeIf { it.isNotBlank() }
