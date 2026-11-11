package com.ilustris.sagai.features.share.domain

import android.content.Context
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.core.theme.GenreFontService
import com.ilustris.sagai.features.act.data.model.Book
import com.ilustris.sagai.features.act.data.model.BookPage
import com.ilustris.sagai.features.act.ui.PageItem
import com.ilustris.sagai.features.newsaga.data.model.Genre
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory

class EpubGeneratorTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private val book =
        Book(
            id = 7,
            actId = 3,
            actTitle = "Act <One> & \"Co\"",
            sagaTitle = "Saga",
            coverQuote = "A quote",
            authorNote = "Thanks for reading.\nSee you.",
        )

    private val pages =
        listOf(
            PageItem.BookCover("Saga", "Act <One> & \"Co\"", "I", "A quote"),
            PageItem.ChapterStart("The Beginning"),
            PageItem.Content("The Beginning", BookPage("Once upon a time & more.\nSecond <paragraph>."), true, "chapter:1", 0),
            PageItem.Content("The Beginning", BookPage("Page two."), false, "chapter:1", 1),
            PageItem.ChapterStart("The End"),
            PageItem.Content("The End", BookPage("Done."), true, "chapter:2", 0),
        )

    private fun generate(withFonts: Boolean = false): File {
        val context = mockk<Context>()
        every { context.cacheDir } returns tmp.newFolder("cache")
        val fonts = mockk<GenreFontService>()
        val font = tmp.newFile("body.ttf").apply { writeBytes(byteArrayOf(0, 1, 0, 0)) }
        coEvery { fonts.getFontFiles(any(), any()) } returns if (withFonts) (null to font) else (null to null)
        val configs = mockk<GenreVisualConfigService>()
        coEvery { configs.getVisualConfig(any()) } returns null
        return runBlocking { EpubGenerator(context, fonts, configs).generateBookEpub(book, Genre.FANTASY, "I", pages) }
    }

    private fun ZipFile.text(path: String) = getInputStream(getEntry(path)).bufferedReader().use { it.readText() }

    private fun ZipFile.xml(path: String): Document =
        DocumentBuilderFactory
            .newInstance()
            .apply { isNamespaceAware = true }
            .newDocumentBuilder()
            .parse(getInputStream(getEntry(path)))

    @Test
    fun `mimetype is the first entry and stored uncompressed`() {
        ZipFile(generate()).use { zip ->
            val first = zip.entries().nextElement()
            assertEquals("mimetype", first.name)
            assertEquals(ZipEntry.STORED, first.method)
            assertEquals("application/epub+zip", zip.text("mimetype"))
        }
    }

    @Test
    fun `every xml part is well formed and special characters are escaped`() {
        ZipFile(generate()).use { zip ->
            zip
                .entries()
                .asSequence()
                .map { it.name }
                .filter { it.endsWith(".xhtml") || it.endsWith(".opf") || it.endsWith(".xml") }
                .forEach { assertNotNull(it, zip.xml(it)) }
            assertTrue(zip.text("OEBPS/s2.xhtml").contains("nce upon a time &amp; more."))
            assertTrue(zip.text("OEBPS/s2.xhtml").contains("Second &lt;paragraph&gt;."))
        }
    }

    @Test
    fun `manifest, spine and navigation agree with the zip contents`() {
        ZipFile(generate(withFonts = true)).use { zip ->
            val opf = zip.xml("OEBPS/content.opf")
            val items = opf.getElementsByTagName("item")
            val ids = (0 until items.length).map { (items.item(it) as Element).getAttribute("id") }.toSet()
            (0 until items.length).forEach {
                val href = (items.item(it) as Element).getAttribute("href")
                assertNotNull("missing $href", zip.getEntry("OEBPS/$href"))
            }
            val spine = opf.getElementsByTagName("itemref")
            (0 until spine.length).forEach { assertTrue(ids.contains((spine.item(it) as Element).getAttribute("idref"))) }

            val nav = zip.xml("OEBPS/nav.xhtml").getElementsByTagName("a")
            assertEquals(
                listOf("s1.xhtml", "s2.xhtml", "s3.xhtml"),
                (0 until nav.length).map { (nav.item(it) as Element).getAttribute("href") },
            )

            assertNotNull(zip.getEntry("OEBPS/fonts/body-font.ttf"))
            assertTrue(zip.text("OEBPS/style.css").contains("@font-face"))
        }
    }

    @Test
    fun `a chapter's pages flow into one document with a single drop cap`() {
        ZipFile(generate()).use { zip ->
            val chapter = zip.text("OEBPS/s2.xhtml")
            assertEquals(1, Regex("class=\"dropcap\"").findAll(chapter).count())
            assertTrue(chapter.contains("<p>Page two.</p>"))
            assertTrue(zip.text("OEBPS/s4.xhtml").contains("Thanks for reading."))
        }
    }
}
