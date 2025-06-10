package com.ilustris.sagai.core.ai

import com.ilustris.sagai.features.chapter.data.model.Chapter

object ChapterPrompts {
    fun chapterOverview(chapter: Chapter) =
        """
        Current chapter overview:
        
        Title: ${chapter.title}
        Overview: ${chapter.overview}

        """
}
