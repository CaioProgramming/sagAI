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
 * Crime's SagaReview presented as a simulated text thread — see
 * [com.ilustris.sagai.features.saga.detail.review.ui.ReviewNavigationStyle.ChatScroll]. Opens
 * with [CrimeTitleCardPage] (the saga's name, handwritten, like a chapter title) before any
 * bubble appears. Every stage's hook and content lines become their own bubble, alternating
 * sides globally across the whole thread (not reset per stage) so it reads as one continuous
 * back-and-forth rather than N separate exchanges. No avatars on the alternating bubbles —
 * deliberately a 1:1 thread, not a group chat — except Farewells, where each message really is
 * attributed to a specific character. The data-driven visuals Default shows for a stage
 * (emotional-tone shape, playtime counter, cast roster, chapter stills) are *not* dropped just
 * because the presentation changed — they become "attachments": a rich card emitted immediately
 * after that stage's content bubble, on the same side/turn as that message, like sending a photo
 * or a shared card right after a text. Reuses the same
 * [com.ilustris.sagai.features.saga.detail.data.model.Review] stage data as
 * [com.ilustris.sagai.features.saga.detail.review.ui.DefaultReviewExperience] — only the
 * presentation differs.
 */
class CrimeReviewExperience(
    private val content: SagaContent,
) : ReviewExperience {
    override val navigationStyle: ReviewNavigationStyle = ReviewNavigationStyle.ChatScroll

    override val pages: List<ReviewPage>
        get() {
            val review = content.data.review ?: return emptyList()

            var isMeTurn = false
            fun nextIsMe(): Boolean {
                isMeTurn = !isMeTurn
                return isMeTurn
            }

            return buildList {
                add(CrimeTitleCardPage(content))

                /** Adds one bubble for [text], returns the side it landed on, or null if there was nothing to say. */
                fun addText(
                    pageType: ReviewPageType,
                    text: ReviewText?,
                ): Boolean? {
                    text ?: return null
                    val rawTitle = text.title?.takeIf { it.isNotBlank() }
                    val body = text.subtitle?.takeIf { it.isNotBlank() } ?: rawTitle
                    if (body.isNullOrBlank()) return null
                    val headerTitle = rawTitle?.takeIf { it != body }
                    val isMe = nextIsMe()
                    add(CrimeTextMessagePage(content, pageType, body, isMe, title = headerTitle))
                    return isMe
                }

                fun addStage(
                    stage: ReviewStage?,
                    pageType: ReviewPageType,
                    attachment: ((isMe: Boolean) -> ReviewPage?)? = null,
                ) {
                    stage ?: return
                    addText(pageType, stage.hook)
                    val contentIsMe = addText(pageType, stage.content)
                    if (contentIsMe != null && attachment != null) {
                        attachment(contentIsMe)?.let { add(it) }
                    }
                }

                addStage(review.introduction, ReviewPageType.INTRO)

                addStage(review.expressiveness, ReviewPageType.EXPRESSIVENESS) { isMe ->
                    val tone =
                        content
                            .flatEvents()
                            .map { it.emotionalRanking() }
                            .firstOrNull()
                            ?.firstOrNull()
                            ?.first
                    tone?.let { CrimeVibeStatPage(content, it, isMe) }
                }

                addStage(review.playstyle, ReviewPageType.PLAYSTYLE) { isMe ->
                    CrimePlaystyleStatPage(content, isMe)
                }

                addStage(review.topCharacters, ReviewPageType.CHARACTERS) { isMe ->
                    val topCharacters =
                        content
                            .flatMessages()
                            .rankTopCharacters(content.getCharacters(true))
                            .take(5)
                    topCharacters.takeIf { it.isNotEmpty() }?.let { CrimeContactCardMessagePage(content, it, isMe) }
                }

                addStage(review.actsInsight, ReviewPageType.JOURNEY) { isMe ->
                    val images = content.notableChapterImageSources(limit = 6)
                    images.takeIf { it.isNotEmpty() }?.let { CrimeAlbumMessagePage(content, it, isMe) }
                }

                addStage(review.conclusion, ReviewPageType.CONCLUSION)

                review.farewells
                    ?.takeIf { it.isNotEmpty() }
                    ?.forEach { farewell ->
                        val speaker =
                            content.characters
                                .find { it.data.id == farewell.characterId }
                                ?.data
                        if (speaker != null) {
                            add(
                                CrimeTextMessagePage(
                                    content,
                                    ReviewPageType.FAREWELLS,
                                    farewell.cleanMessage(speaker.name),
                                    isMe = speaker.id == content.mainCharacter?.data?.id,
                                    sender = speaker,
                                ),
                            )
                        }
                    }

                if (review.isComplete()) {
                    add(CrimeSummaryMessagePage(content))
                }
            }
        }
}
