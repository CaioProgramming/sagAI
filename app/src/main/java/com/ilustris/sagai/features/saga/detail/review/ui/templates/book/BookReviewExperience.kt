package com.ilustris.sagai.features.saga.detail.review.ui.templates.book

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.model.isComplete
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewExperience
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewNavigationStyle
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType

/**
 * Fantasy's SagaReview presented as a storybook: serif type on parchment,
 * turned like pages instead of swiped like stories. Reuses the same
 * [com.ilustris.sagai.features.saga.detail.data.model.Review] stage data as
 * [com.ilustris.sagai.features.saga.detail.review.ui.DefaultReviewExperience] —
 * only the page visuals and navigation model differ.
 */
class BookReviewExperience(
    private val content: SagaContent,
) : ReviewExperience {
    override val navigationStyle: ReviewNavigationStyle = ReviewNavigationStyle.HorizontalPageFlip

    override val pages: List<ReviewPage>
        get() {
            val review = content.data.review ?: return emptyList()

            return buildList {
                review.introduction?.let { stage ->
                    stage.hook?.let { add(BookTextPage(content, it, ReviewPageType.INTRO, isEpigraph = true)) }
                    stage.content?.let { add(BookTextPage(content, it, ReviewPageType.INTRO)) }
                }

                review.expressiveness?.let { stage ->
                    stage.hook?.let {
                        add(BookTextPage(content, it, ReviewPageType.EXPRESSIVENESS, isEpigraph = true))
                    }
                    stage.content?.let {
                        add(BookTextPage(content, it, ReviewPageType.EXPRESSIVENESS))
                    }
                }

                review.playstyle?.let { stage ->
                    stage.hook?.let {
                        add(BookTextPage(content, it, ReviewPageType.PLAYSTYLE, isEpigraph = true))
                    }
                    stage.content?.let {
                        add(BookTextPage(content, it, ReviewPageType.PLAYSTYLE))
                    }
                }

                review.topCharacters?.let { stage ->
                    stage.hook?.let {
                        add(BookTextPage(content, it, ReviewPageType.CHARACTERS, isEpigraph = true))
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

                review.conclusion?.let { stage ->
                    stage.hook?.let {
                        add(BookTextPage(content, it, ReviewPageType.CONCLUSION, isEpigraph = true))
                    }
                    stage.content?.let {
                        add(BookTextPage(content, it, ReviewPageType.CONCLUSION))
                    }
                }

                if (review.isComplete()) {
                    add(BookSummaryPage(content))
                }
            }
        }
}
