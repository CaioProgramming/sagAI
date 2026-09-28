package com.ilustris.sagai.features.chapter.data.usecase

import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.ai.model.GeneratedContentWithLore
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.chapter.data.model.Chapter
import kotlinx.coroutines.flow.Flow

interface ChapterUseCase {
    suspend fun generateChapterIntroduction(
        sagaId: Int,
        chapterContent: Chapter,
    ): RequestResult<GeneratedContent<Chapter>>

    suspend fun saveChapter(chapter: Chapter): Chapter

    suspend fun deleteChapter(chapter: Chapter)

    suspend fun updateChapter(chapter: Chapter): Chapter

    suspend fun deleteChapterById(chapterId: Int)

    suspend fun deleteAllChapters()

    fun getChaptersInfoBySaga(sagaId: Int): Flow<List<com.ilustris.sagai.features.chapter.data.model.ChapterInfo>>

    suspend fun generateChapterCover(chapterId: Int): RequestResult<Chapter>

    suspend fun generateChapterCoverStream(chapterId: Int): Flow<StreamingState<GeneratedContent<Chapter>>>

    suspend fun generateChapter(chapterId: Int): RequestResult<Chapter>

    suspend fun generateChapterStream(chapterId: Int): Flow<StreamingState<GeneratedContent<Chapter>?>>

    suspend fun reviewChapter(chapterId: Int): RequestResult<Chapter>

    suspend fun generateChapterIntroductionStream(chapterId: Int): Flow<StreamingState<GeneratedContent<Chapter>?>>

    /**
     * Deals the chapter-closure dilemmas, before the chapter is synthesized. Returns the chapter
     * with [Chapter.playerChoiceCards] set: three pairs, or an empty list when nothing usable came
     * back (the chapter then closes without cards). A chapter that already has a hand gets it back
     * unchanged, so resuming never deals a new one. [ChoiceCardsReveal.screenTitle]/[ChoiceCardsReveal.screenSubtitle]
     * are blank on that resume path (they aren't persisted) — the screen falls back to a static label.
     */
    fun generateChoiceCardsStream(chapterId: Int): Flow<StreamingState<ChoiceCardsReveal?>>

    /**
     * Stores the player's picks (hidden tags, in card order) and rewrites the chapter's
     * playerSpectrum from them, seeded by the previous chapter's read.
     */
    suspend fun recordPlayerChoiceAnswers(
        chapterId: Int,
        answers: List<String>,
    ): RequestResult<Chapter>

    fun synthesizeChapterEvolutionStream(chapterId: Int): Flow<StreamingState<GeneratedContentWithLore<Chapter>?>>
}

/**
 * What a freshly dealt hand of choice cards carries besides the cards themselves — the in-fiction
 * framing the Milestone screen shows once, above whichever card is currently up. Not persisted:
 * a resumed hand (the app closed with cards pending) falls back to a static label instead.
 */
data class ChoiceCardsReveal(
    val chapter: Chapter,
    val screenTitle: String,
    val screenSubtitle: String,
)
