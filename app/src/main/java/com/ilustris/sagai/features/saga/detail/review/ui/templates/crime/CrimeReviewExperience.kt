package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.saga.chat.domain.model.rankTopCharacters
import com.ilustris.sagai.features.saga.detail.data.model.ReviewStage
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.features.saga.detail.data.model.cleanMessage
import com.ilustris.sagai.features.saga.detail.data.model.isComplete
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewExperience
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewNavigationStyle
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.features.saga.detail.review.ui.notableChapterImageSources

/**
 * Crime's SagaReview presented as a detective's corkboard — see
 * [com.ilustris.sagai.features.saga.detail.review.ui.ReviewNavigationStyle.Corkboard]. One pin per
 * subject instead of alternating chat bubbles: the saga icon opens the board, every top character
 * and notable chapter still gets its own photo (rather than one collapsed attachment for the
 * whole set), and the text-only stages become pinned notes. Reuses the same
 * [com.ilustris.sagai.features.saga.detail.data.model.Review] stage data as
 * [com.ilustris.sagai.features.saga.detail.review.ui.DefaultReviewExperience] — only the
 * presentation differs.
 */
class CrimeReviewExperience(
    private val content: SagaContent,
) : ReviewExperience {
    override val navigationStyle: ReviewNavigationStyle = ReviewNavigationStyle.Corkboard

    override val pages: List<ReviewPage>
        get() {
            val review = content.data.review ?: return emptyList()

            return buildList {
                add(CorkboardCoverPinPage(content, review.introduction.mergedCaption()))

                review.expressiveness?.let { stage ->
                    val tone =
                        content
                            .flatEvents()
                            .map { it.emotionalRanking() }
                            .firstOrNull()
                            ?.firstOrNull()
                            ?.first
                    tone?.let { add(CorkboardVibePinPage(content, it, stage.mergedCaption())) }
                }

                review.playstyle?.let { stage ->
                    add(CorkboardPlaystylePinPage(content, stage.mergedCaption()))
                }

                if (review.topCharacters != null) {
                    content
                        .flatMessages()
                        .rankTopCharacters(content.getCharacters(true))
                        .take(5)
                        .forEach { (character, messageCount) ->
                            add(CorkboardCharacterPinPage(content, character, messageCount))
                        }
                }

                if (review.actsInsight != null) {
                    content.notableChapterImageSources(limit = 6).forEach { image ->
                        add(CorkboardChapterPinPage(content, image))
                    }
                }

                review.conclusion.mergedCaption()?.let { caption ->
                    add(CorkboardNotePinPage(content, ReviewPageType.CONCLUSION, caption))
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
 * Merges a stage's hook and content into one short caption — a board pin has room for a line or
 * two, not the two separate chat bubbles the old thread gave each stage.
 */
private fun ReviewStage?.mergedCaption(): String? {
    this ?: return null
    fun ReviewText?.body(): String? =
        this?.subtitle?.takeIf { it.isNotBlank() } ?: this?.title?.takeIf { it.isNotBlank() }
    return listOfNotNull(hook.body(), content.body()).joinToString(separator = " ").takeIf { it.isNotBlank() }
}
