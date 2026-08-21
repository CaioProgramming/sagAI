package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.model.isComplete
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewExperience
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewNavigationStyle
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.features.saga.detail.review.ui.coverImageSource
import com.ilustris.sagai.features.saga.detail.review.ui.notableChapterImageSources

/**
 * Cyberpunk and Space Opera's shared SagaReview template: monospace text typed line by line over
 * a CRT-scanline background, with a glitch VFX overlay on top. Genre identity comes from theme
 * colors/fonts and [TerminalBackground]'s per-genre CRT treatment (plain hacker-terminal scanlines
 * vs. Space Opera's own `spaceVoyage` phosphor/jitter VFX), not from separate page classes — see
 * [com.ilustris.sagai.features.saga.detail.review.ui.GenreReviewTemplateMapping].
 * Reuses the same [com.ilustris.sagai.features.saga.detail.data.model.Review]
 * stage data as [com.ilustris.sagai.features.saga.detail.review.ui.DefaultReviewExperience] —
 * only the page visuals differ, navigation is still a vertical swipe.
 */
class TerminalReviewExperience(
    private val content: SagaContent,
) : ReviewExperience {
    override val navigationStyle: ReviewNavigationStyle = ReviewNavigationStyle.TerminalSwipe

    override val pages: List<ReviewPage>
        get() {
            val review = content.data.review ?: return emptyList()

            return buildList {
                val coverImage = content.coverImageSource()
                val introHook = review.introduction?.hook

                if (coverImage != null) {
                    add(TerminalBootPage(content, coverImage, introHook, content.data))
                } else {
                    introHook?.let { add(TerminalTextPage(content, it, ReviewPageType.INTRO, "boot")) }
                }

                review.introduction?.content?.let {
                    add(TerminalTextPage(content, it, ReviewPageType.INTRO, "log"))
                }

                review.expressiveness?.let { stage ->
                    stage.hook?.let {
                        add(TerminalTextPage(content, it, ReviewPageType.EXPRESSIVENESS, "analyze"))
                    }
                    add(TerminalEmotionScanPage(content, stage.content))
                }

                review.playstyle?.let { stage ->
                    stage.hook?.let {
                        add(TerminalTextPage(content, it, ReviewPageType.PLAYSTYLE, "trace"))
                    }
                    // The content rides the counter rather than getting a screen of its own: the
                    // prose is *about* the figure, and printing it beside the number is the whole
                    // reason the number is there.
                    add(TerminalUptimePage(content, stage.content))
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

                    // The recap prints under the gathering itself. Splitting them left a page of
                    // text about pictures and a page of pictures with nothing said about them.
                    val plates = content.notableChapterImageSources(limit = 6)
                    if (plates.isNotEmpty()) {
                        add(TerminalArchivePage(content, plates, stage.content))
                    } else {
                        stage.content?.let {
                            add(TerminalTextPage(content, it, ReviewPageType.JOURNEY, "log"))
                        }
                    }
                }

                // Only the hook stands alone here — the send-off's own words belong on the last
                // screen, where the log is being closed, not on a page before it.
                review.conclusion?.hook?.let {
                    add(TerminalTextPage(content, it, ReviewPageType.CONCLUSION, "shutdown"))
                }

                review.farewells
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { add(TerminalFarewellsPage(content, it)) }

                if (review.isComplete()) {
                    add(TerminalSummaryPage(content, review.conclusion?.content))
                }
            }
        }
}
