package com.ilustris.sagai.features.saga.detail.review.ui.templates.collage

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.model.isComplete
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewCharactersPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewConclusionPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewExperience
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewExpressivenessPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewFarewellsPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewHookPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewJourneyPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPlaystylePage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewStartPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewSummaryPage
import com.ilustris.sagai.features.saga.detail.review.ui.coverImageSource
import com.ilustris.sagai.features.saga.detail.review.ui.topCharacterImageSource

/**
 * Punk Rock's zine/collage template. Only the opener ([CollagePosterPage]) is bespoke so far —
 * the rest of the stages still reuse [com.ilustris.sagai.features.saga.detail.review.ui.DefaultReviewExperience]'s
 * plain pages, same as how the Book template started before its pages were retrofitted one at a
 * time.
 */
class CollageReviewExperience(
    private val content: SagaContent,
) : ReviewExperience {
    override val pages: List<ReviewPage>
        get() {
            val review = content.data.review ?: return emptyList()

            return buildList {
                review.introduction?.let {
                    add(CollagePosterPage(content))
                    it.content?.let { intro -> add(ReviewStartPage(content, intro)) }
                }

                review.expressiveness?.let {
                    it.hook?.let { hook -> add(ReviewHookPage(content, hook, ReviewPageType.EXPRESSIVENESS)) }
                    add(ReviewExpressivenessPage(it, content))
                }

                review.playstyle?.let {
                    it.hook?.let { hook -> add(ReviewHookPage(content, hook, ReviewPageType.PLAYSTYLE)) }
                    it.content?.let { add(ReviewPlaystylePage(content, it)) }
                }

                review.topCharacters?.let {
                    it.hook?.let { hook -> add(ReviewHookPage(content, hook, ReviewPageType.CHARACTERS)) }
                    add(ReviewCharactersPage(content, it))
                }

                review.actsInsight?.let {
                    it.hook?.let { hook -> add(ReviewHookPage(content, hook, ReviewPageType.JOURNEY)) }
                    it.content?.let { add(ReviewJourneyPage(content, it)) }
                }

                review.conclusion?.let {
                    it.hook?.let { hook -> add(ReviewHookPage(content, hook, ReviewPageType.CONCLUSION)) }
                    add(ReviewConclusionPage(content))
                }

                review.farewells
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { add(ReviewFarewellsPage(content, it)) }

                if (review.isComplete()) {
                    add(ReviewSummaryPage(content))
                }
            }
        }
}
