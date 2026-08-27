package com.ilustris.sagai.features.saga.detail.review.ui

import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.CharacterProfile
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private fun character(
    id: Int,
    name: String,
    image: String,
) = CharacterContent(
    data =
        Character(
            id = id,
            name = name,
            image = image,
            details = Details(),
            profile = CharacterProfile(),
        ),
)

private fun chapter(
    id: Int,
    title: String,
    coverImage: String,
) = ChapterContent(data = Chapter(id = id, actId = 1, title = title, coverImage = coverImage))

class ReviewImageSourcesTest {
    @Test
    fun `coverImageSource is null when the saga has no icon`() {
        val content = SagaContent(data = Saga(icon = ""))
        assertNull(content.coverImageSource())
    }

    @Test
    fun `coverImageSource uses the saga icon and title`() {
        val content = SagaContent(data = Saga(icon = "https://img/icon.png", title = "My Saga"))
        assertEquals(ReviewImageSource("https://img/icon.png", "My Saga"), content.coverImageSource())
    }

    @Test
    fun `topCharacterImageSource is null when no character has an image`() {
        val content =
            SagaContent(
                data = Saga(),
                characters = listOf(character(1, "Vex", "")),
            )
        assertNull(content.topCharacterImageSource())
    }

    @Test
    fun `topCharacterImageSource resolves the top-ranked character's portrait`() {
        val content =
            SagaContent(
                data = Saga(),
                characters = listOf(character(1, "Vex", "https://img/vex.png")),
            )
        assertEquals(ReviewImageSource("https://img/vex.png", "Vex"), content.topCharacterImageSource())
    }

    @Test
    fun `notableChapterImageSource is null when no chapter has a cover image`() {
        val content =
            SagaContent(
                data = Saga(),
                acts = listOf(ActContent(data = Act(id = 1), chapters = listOf(chapter(1, "Chapter One", "")))),
            )
        assertNull(content.notableChapterImageSource())
    }

    @Test
    fun `notableChapterImageSource skips chapters without a cover and returns the first that has one`() {
        val content =
            SagaContent(
                data = Saga(),
                acts =
                    listOf(
                        ActContent(
                            data = Act(id = 1),
                            chapters =
                                listOf(
                                    chapter(1, "Chapter One", ""),
                                    chapter(2, "Chapter Two", "https://img/chapter2.png"),
                                ),
                        ),
                    ),
            )
        assertEquals(
            ReviewImageSource("https://img/chapter2.png", "Chapter Two"),
            content.notableChapterImageSource(),
        )
    }
}
