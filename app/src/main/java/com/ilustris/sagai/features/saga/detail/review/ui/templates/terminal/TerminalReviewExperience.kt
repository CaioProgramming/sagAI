package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.model.isComplete
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewExperience
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewNavigationStyle
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType

/**
 * Cyberpunk's SagaReview presented as a computer terminal: monospace text typed
 * line by line over a CRT-scanline background, advanced by tapping instead of
 * swiping. Reuses the same [com.ilustris.sagai.features.saga.detail.data.model.Review]
 * stage data as [com.ilustris.sagai.features.saga.detail.review.ui.DefaultReviewExperience] —
 * only the page visuals and navigation model differ.
 */
class TerminalReviewExperience(
    private val content: SagaContent,
) : ReviewExperience {
    override val navigationStyle: ReviewNavigationStyle = ReviewNavigationStyle.TapToAdvance

    override val pages: List<ReviewPage>
        get() {
            val review = content.data.review ?: return emptyList()

            return buildList {
                review.introduction?.let { stage ->
                    stage.hook?.let { add(TerminalTextPage(content, it, ReviewPageType.INTRO, "boot")) }
                    stage.content?.let { add(TerminalTextPage(content, it, ReviewPageType.INTRO, "log")) }
                }

                review.expressiveness?.let { stage ->
                    stage.hook?.let {
                        add(TerminalTextPage(content, it, ReviewPageType.EXPRESSIVENESS, "analyze"))
                    }
                    stage.content?.let {
                        add(TerminalTextPage(content, it, ReviewPageType.EXPRESSIVENESS, "report"))
                    }
                }

                review.playstyle?.let { stage ->
                    stage.hook?.let {
                        add(TerminalTextPage(content, it, ReviewPageType.PLAYSTYLE, "trace"))
                    }
                    stage.content?.let {
                        add(TerminalTextPage(content, it, ReviewPageType.PLAYSTYLE, "report"))
                    }
                }

                review.topCharacters?.let { stage ->
                    stage.hook?.let {
                        add(TerminalTextPage(content, it, ReviewPageType.CHARACTERS, "query"))
                    }
                    add(TerminalCharactersPage(content, stage))
                }

                review.actsInsight?.let { stage ->
                    stage.hook?.let {
                        add(TerminalTextPage(content, it, ReviewPageType.JOURNEY, "history"))
                    }
                    stage.content?.let {
                        add(TerminalTextPage(content, it, ReviewPageType.JOURNEY, "log"))
                    }
                }

                review.conclusion?.let { stage ->
                    stage.hook?.let {
                        add(TerminalTextPage(content, it, ReviewPageType.CONCLUSION, "shutdown"))
                    }
                    stage.content?.let {
                        add(TerminalTextPage(content, it, ReviewPageType.CONCLUSION, "log"))
                    }
                }

                review.farewells
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { add(TerminalFarewellsPage(content, it)) }

                if (review.isComplete()) {
                    add(TerminalSummaryPage(content))
                }
            }
        }
}
