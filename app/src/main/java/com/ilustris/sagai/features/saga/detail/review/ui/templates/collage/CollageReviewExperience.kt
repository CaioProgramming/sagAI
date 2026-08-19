package com.ilustris.sagai.features.saga.detail.review.ui.templates.collage

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.model.isComplete
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewExperience
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewSummaryPage
import com.ilustris.sagai.features.saga.detail.review.ui.coverImageSource
import com.ilustris.sagai.features.saga.detail.review.ui.topCharacterImageSource

/**
 * Punk Rock's zine/collage template. Every narrative stage is now bespoke: the opener
 * ([CollagePosterPage]), the plain hook/text stages ([CollageTextPage]), expressiveness
 * ([CollageEmotionPage]), playtime ([CollagePlaytimePage]), the cast ([CollageCharactersPage]),
 * the journey ([CollageJourneyPage]), the closing cast call ([CollageConclusionPage]) and the
 * send-off ([CollageFarewellsPage]).
 *
 * Only the final summary card still comes from
 * [com.ilustris.sagai.features.saga.detail.review.ui.DefaultReviewExperience].
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
                    it.content?.let { intro -> add(CollageTextPage(content, ReviewPageType.INTRO, intro)) }
                }

                review.expressiveness?.let {
                    it.hook?.let { hook -> add(CollageTextPage(content, ReviewPageType.EXPRESSIVENESS, hook)) }
                    add(CollageEmotionPage(content, it))
                }

                review.playstyle?.let {
                    it.hook?.let { hook -> add(CollageTextPage(content, ReviewPageType.PLAYSTYLE, hook)) }
                    it.content?.let { add(CollagePlaytimePage(content, it)) }
                }

                review.topCharacters?.let {
                    it.hook?.let { hook -> add(CollageTextPage(content, ReviewPageType.CHARACTERS, hook)) }
                    add(CollageCharactersPage(content, it))
                }

                review.actsInsight?.let {
                    it.hook?.let { hook -> add(CollageTextPage(content, ReviewPageType.JOURNEY, hook)) }
                    it.content?.let { add(CollageJourneyPage(content, it)) }
                }

                review.conclusion?.let {
                    it.hook?.let { hook -> add(CollageTextPage(content, ReviewPageType.CONCLUSION, hook)) }
                    add(CollageConclusionPage(content))
                }

                review.farewells
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { add(CollageFarewellsPage(content, it)) }

                if (review.isComplete()) {
                    add(ReviewSummaryPage(content))
                }
            }
        }
}
