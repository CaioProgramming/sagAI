package com.ilustris.sagai.features.act.ui

import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.utils.toRoman
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.data.model.BookPage
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.actNumber
import javax.inject.Inject

sealed class PageItem {
    data class ChapterStart(
        val title: String,
    ) : PageItem()

    data class Content(
        val chapterTitle: String,
        val page: BookPage,
        val showDropCap: Boolean = false,
    ) : PageItem()

    data class Illustration(
        val imagePath: String,
        val title: String? = null,
    ) : PageItem()

    data class CharacterGrid(
        val characters: List<CharacterContent>,
    ) : PageItem()

    data class BookCover(
        val sagaTitle: String,
        val actTitle: String,
        val volume: String,
        val quote: String,
    ) : PageItem()
}

class BookPageMapper
    @Inject
    constructor(
        private val fileHelper: FileHelper,
    ) {
        /**
         * @param saga       Full saga context (icon, title, genre).
         * @param act        The act whose book is being read.
         * @param characters All saga characters; only those present in this act are shown.
         * @param validatedImages Map of image-path → isValid (pre-validated by the ViewModel).
         */
        fun buildPages(
            saga: SagaContent,
            act: ActContent,
            characters: List<CharacterContent>,
        ): List<PageItem> =
            buildList {
                val book = act.book ?: return emptyList()
                val addedImages = mutableSetOf<String>()
                val allImages = act.chapters.filter { fileHelper.readFile(it.data.coverImage) != null }

                add(
                    PageItem.BookCover(
                        sagaTitle = book.sagaTitle,
                        actTitle = book.actTitle,
                        volume = saga.actNumber(act.data).toRoman(),
                        quote = book.coverQuote,
                    ),
                )

                val isFirst = act == saga.acts.first()
                if (isFirst) {
                    if (saga.data.icon.isNotEmpty()) {
                        add(PageItem.Illustration(saga.data.icon, saga.data.title))
                    }
                }

                // Chapter pages
                book.chapters.forEachIndexed { index, chapter ->
                    add(PageItem.ChapterStart(chapter.title))
                    allImages.randomOrNull()?.let {
                        if (!addedImages.contains(it.data.coverImage)) {
                            add(PageItem.Illustration(it.data.coverImage, chapter.title))
                            addedImages.add(it.data.coverImage)
                        }
                    }
                    // Body pages
                    chapter.pages.forEachIndexed { pageIndex, page ->
                        add(
                            PageItem.Content(
                                chapterTitle = chapter.title,
                                page = page,
                                showDropCap = pageIndex == 0,
                            ),
                        )
                    }
                }

                val presentCharacters = act.getPresentCharacters(characters)
                if (presentCharacters.isNotEmpty()) {
                    add(PageItem.CharacterGrid(presentCharacters))
                }
            }

        /** Pre-validates all images required to render the given act and returns the result map. */
        suspend fun validateImages(
            saga: SagaContent,
            act: ActContent,
        ): Map<String, Boolean> {
            val paths =
                buildList {
                    if (saga.data.icon.isNotEmpty()) add(saga.data.icon)
                    act.chapters.forEach { chapter ->
                        if (chapter.data.coverImage.isNotEmpty()) add(chapter.data.coverImage)
                    }
                }
            return paths.associateWith { fileHelper.readFile(it) != null }
        }
    }
