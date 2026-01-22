package com.ilustris.sagai.features.saga.detail.review.ui

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages

class DefaultReviewExperience(
    private val content: SagaContent,
    private val onNavigate: (Int) -> Unit,
) : ReviewExperience {
    override val pages: List<ReviewPage>
        get() {
            val review = content.data.review ?: return emptyList()
            val genre = content.data.genre

            return buildList {
                // Intro
                review.introduction?.let {
                    it.hook?.let { hook -> add(ReviewIntroAnimationPage(hook, content)) }
                    add(ReviewIntroPage(content))
                }

                // Expressiveness (Activity)
                review.expressiveness?.let {
                    val mainCharId = content.data.mainCharacterId
                    val playerMessages =
                        content.flatMessages().filter { it.message.characterId == mainCharId }
                    val totalActivity =
                        playerMessages.sumOf { msg ->
                            listOf(
                                "<action>",
                                "<think>",
                                "<narrator>",
                            ).count { tag -> msg.message.text.contains(tag) }
                        }

                    it.hook?.let { hook -> add(ReviewHookPage(hook, genre)) }
                    add(
                        ReviewExpressivenessPage(
                            it,
                            genre,
                            totalActivity,
                        ),
                    )
                }

                // Playstyle
                review.playstyle?.let {
                    it.hook?.let { hook -> add(ReviewHookPage(hook, genre)) }
                    add(ReviewPlaystylePage(content))
                }

                // Characters
                review.topCharacters?.let {
                    it.hook?.let { hook -> add(ReviewHookPage(hook, genre)) }
                    add(ReviewCharactersPage(content))
                }

                // Journey
                review.actsInsight?.let {
                    it.hook?.let { hook -> add(ReviewHookPage(hook, genre)) }
                    add(ReviewJourneyPage(content))
                }

                // Conclusion
                review.conclusion?.let {
                    it.hook?.let { hook -> add(ReviewHookPage(hook, genre)) }
                    add(ReviewConclusionPage(content))
                }

                // Summary
                add(ReviewSummaryPage(content, onNavigate))
            }
        }
}
