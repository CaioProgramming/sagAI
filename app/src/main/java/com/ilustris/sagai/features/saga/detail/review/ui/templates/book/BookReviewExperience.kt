package com.ilustris.sagai.features.saga.detail.review.ui.templates.book

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.model.isComplete
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewExperience
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewNavigationStyle
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.features.saga.detail.review.ui.coverImageSource
import com.ilustris.sagai.features.saga.detail.review.ui.notableChapterImageSources
import com.ilustris.sagai.features.saga.detail.review.ui.topCharacterImageSource

/**
 * Fantasy and Shinobi's shared SagaReview template: a storybook page scrolled hands-free like
 * unrolling a scroll instead of swiped like stories — see
 * [com.ilustris.sagai.features.saga.detail.review.ui.ReviewNavigationStyle.ContinuousScroll].
 * Genre identity comes from theme colors/fonts and [BookBackground]'s per-genre texture (parchment
 * grain vs. rice-paper fiber), not from separate page classes — see
 * [com.ilustris.sagai.features.saga.detail.review.ui.GenreReviewTemplateMapping].
 * Reuses the same [com.ilustris.sagai.features.saga.detail.data.model.Review] stage data as
 * [com.ilustris.sagai.features.saga.detail.review.ui.DefaultReviewExperience] —
 * only the page visuals and navigation model differ.
 */
class BookReviewExperience(
    private val content: SagaContent,
) : ReviewExperience {
    override val navigationStyle: ReviewNavigationStyle = ReviewNavigationStyle.ContinuousScroll

    override val pages: List<ReviewPage>
        get() {
            val review = content.data.review ?: return emptyList()

            return buildList {
                content.coverImageSource()?.let { add(BookCoverPage(content, it)) }

                review.introduction?.let { stage ->
                    stage.hook?.let { add(BookTextPage(content, it, ReviewPageType.INTRO, isEpigraph = true)) }
                    stage.content?.let { add(BookTextPage(content, it, ReviewPageType.INTRO)) }
                }

                review.expressiveness?.let { stage ->
                    stage.hook?.let {
                        add(BookTextPage(content, it, ReviewPageType.EXPRESSIVENESS, isEpigraph = true))
                    }
                    if (stage.content != null) {
                        add(BookExpressivenessPage(content, stage))
                    }
                }

                review.playstyle?.let { stage ->
                    stage.hook?.let {
                        add(BookTextPage(content, it, ReviewPageType.PLAYSTYLE, isEpigraph = true))
                    }
                    stage.content?.let {
                        add(BookPlaystylePage(content, it))
                    }
                }

                review.topCharacters?.let { stage ->
                    stage.hook?.let {
                        add(BookTextPage(content, it, ReviewPageType.CHARACTERS, isEpigraph = true))
                    }
                    content.topCharacterImageSource()?.let {
                        add(BookIllustrationPage(content, it, ReviewPageType.CHARACTERS))
                    }
                    add(BookCharactersPage(content, stage))
                }

                review.actsInsight?.let { stage ->
                    stage.hook?.let {
                        add(BookTextPage(content, it, ReviewPageType.JOURNEY, isEpigraph = true))
                    }
                    stage.content?.let {
                        add(BookTextPage(content, it, ReviewPageType.JOURNEY))
                    }
                }

                content.notableChapterImageSources().takeIf { it.isNotEmpty() }?.let {
                    add(BookJourneyPlatePage(content, it))
                }

                review.conclusion?.let { stage ->
                    stage.hook?.let {
                        add(BookTextPage(content, it, ReviewPageType.CONCLUSION, isEpigraph = true))
                    }
                    if (stage.content != null && content.mainCharacter != null) {
                        add(BookConclusionPage(content))
                    }
                }

                review.farewells
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { add(BookFarewellsPage(content, it)) }

                if (review.isComplete()) {
                    add(BookSummaryPage(content))
                }
            }
        }
}
