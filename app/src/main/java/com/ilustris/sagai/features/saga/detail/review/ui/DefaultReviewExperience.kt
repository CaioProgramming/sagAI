package com.ilustris.sagai.features.saga.detail.review.ui

import com.ilustris.sagai.features.home.data.model.SagaContent

class DefaultReviewExperience(
    private val content: SagaContent,
    private val onNavigate: (Int) -> Unit,
) : ReviewExperience {
    override val pages: List<ReviewPage>
        get() {
            val review = content.data.review ?: return emptyList()
            content.data.genre

            return buildList {
                // Intro
                review.introduction?.let {
                    it.hook?.let { hook -> add(ReviewIntroAnimationPage(hook, content)) }
                    it.content?.let { add(ReviewStartPage(content, it)) }
                }

                review.expressiveness?.let {

                    it.hook?.let { hook -> add(ReviewHookPage(content, hook)) }
                    add(
                        ReviewExpressivenessPage(
                            it,
                            content,
                        ),
                    )
                }

                // Playstyle
                review.playstyle?.let {
                    it.hook?.let { hook -> add(ReviewHookPage(content, hook)) }
                    it.content?.let {
                        add(ReviewPlaystylePage(content, it))
                    }
                }

                // Characters
                review.topCharacters?.let {
                    it.hook?.let { hook -> add(ReviewHookPage(content, hook)) }
                    add(ReviewCharactersPage(content))
                }

                // Journey
                review.actsInsight?.let {
                    it.hook?.let { hook -> add(ReviewHookPage(content, hook)) }
                    add(ReviewJourneyPage(content))
                }

                // Conclusion
                review.conclusion?.let {
                    it.hook?.let { hook -> add(ReviewHookPage(content, hook)) }
                    add(ReviewConclusionPage(content))
                }

                // Summary
                add(ReviewSummaryPage(content, onNavigate))
            }
        }
}
