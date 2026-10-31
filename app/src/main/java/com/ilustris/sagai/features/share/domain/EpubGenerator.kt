package com.ilustris.sagai.features.share.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.core.theme.GenreFontService
import com.ilustris.sagai.features.act.data.model.Book
import com.ilustris.sagai.features.act.ui.PageItem
import com.ilustris.sagai.features.act.ui.components.ChapterDropCap
import com.ilustris.sagai.features.newsaga.data.model.Genre
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

/**
 * Writes a volume as a reflowable EPUB 3, from the same [PageItem]s the reader and the PDF render.
 *
 * The PDF fixes every page at A4; here the page splits are dropped and each chapter flows as one
 * document, so the text fits whatever screen or e-reader opens it. An EPUB is a zip of XHTML with
 * `mimetype` as its first, uncompressed entry, so the `java.util.zip` stream is all it takes.
 */
class EpubGenerator
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val genreFontService: GenreFontService,
        private val genreVisualConfigService: GenreVisualConfigService,
    ) {
        private class Section(
            val id: String,
            val title: String?,
            val cssClass: String,
            val body: StringBuilder = StringBuilder(),
        ) {
            val fileName get() = "$id.xhtml"
        }

        private class Resource(
            val id: String,
            val path: String,
            val mediaType: String,
            val bytes: ByteArray,
        )

        suspend fun generateBookEpub(
            book: Book,
            genre: Genre,
            volume: String,
            pages: List<PageItem>,
        ): File =
            withContext(Dispatchers.IO) {
                val config = genreVisualConfigService.getVisualConfig(genre)
                val (headerFont, bodyFont) = genreFontService.getFontFiles(genre, config)

                val resources = mutableListOf<Resource>()
                val fonts = embedFonts(headerFont, bodyFont, resources)
                val sections = buildSections(book, genre, pages, resources)

                val sharesDir = File(context.cacheDir, "shares").apply { mkdirs() }
                val file = File(sharesDir, "${book.actTitle.replace(FileNameForbidden, "").trim().ifBlank { "book" }}.epub")
                try {
                    writeEpub(file, book, genre, sections, resources, fonts)
                } catch (e: Exception) {
                    file.delete()
                    throw e
                }
                file
            }

        /** CSS font-family names for the header and body fonts, null for a role with no embeddable file. */
        private class EmbeddedFonts(
            val header: String?,
            val body: String?,
        )

        private fun embedFonts(
            headerFont: File?,
            bodyFont: File?,
            resources: MutableList<Resource>,
        ): EmbeddedFonts {
            fun embed(
                file: File?,
                id: String,
            ): String? {
                file ?: return null
                val ext = file.extension.lowercase().takeIf { it == "otf" || it == "ttf" } ?: return null
                resources += Resource(id, "fonts/$id.$ext", if (ext == "otf") "font/otf" else "font/ttf", file.readBytes())
                return id
            }
            // Same fallback as the reader: a genre with a single font uses it for both roles.
            val header = embed(headerFont, "header-font")
            val body = embed(bodyFont, "body-font")
            return EmbeddedFonts(header = header ?: body, body = body ?: header)
        }

        private fun buildSections(
            book: Book,
            genre: Genre,
            pages: List<PageItem>,
            resources: MutableList<Resource>,
        ): List<Section> {
            val sections = mutableListOf<Section>()
            var imageCount = 0

            fun newSection(
                title: String?,
                cssClass: String = "chapter",
            ) = Section("s${sections.size + 1}", title, cssClass).also { sections += it }

            fun addImage(
                path: String,
                caption: String?,
            ): String? {
                val bytes = encodeJpeg(path) ?: return null
                imageCount++
                val id = "img$imageCount"
                resources += Resource(id, "images/$id.jpg", "image/jpeg", bytes)
                val figcaption = caption?.let { "<figcaption>${it.xml()}</figcaption>" }.orEmpty()
                return "<figure><img src=\"images/$id.jpg\" alt=\"${caption.orEmpty().xml()}\"/>$figcaption</figure>\n"
            }

            // Pages can arrive before any chapter opens (the saga frontispiece), so the open section is lazy.
            fun currentSection() = sections.lastOrNull()?.takeIf { it.cssClass == "chapter" } ?: newSection(null)

            pages.forEach { item ->
                when (item) {
                    is PageItem.BookCover -> {
                        newSection(item.sagaTitle, cssClass = "cover").body.append(
                            """
                            <h1 class="saga">${item.sagaTitle.xml()}</h1>
                            <h2 class="act">${item.actTitle.xml()}</h2>
                            <p class="volume">${item.volume.xml()}</p>
                            <blockquote class="quote">${item.quote.xml()}</blockquote>
                            """.trimIndent(),
                        )
                    }

                    is PageItem.ChapterStart -> {
                        newSection(item.title).body.append("<h1>${item.title.xml()}</h1>\n")
                    }

                    is PageItem.Illustration -> {
                        addImage(item.imagePath, item.title)?.let { currentSection().body.append(it) }
                    }

                    is PageItem.Content -> {
                        val section = currentSection()
                        val paragraphs = item.page.content.lines().map { it.trim() }.filter { it.isNotEmpty() }
                        paragraphs.forEachIndexed { index, paragraph ->
                            val split = if (item.showDropCap && index == 0) ChapterDropCap.split(paragraph) else null
                            section.body.append(
                                if (split != null) {
                                    "<p><span class=\"dropcap\">${split.letter.toString().xml()}</span>${split.remainder.xml()}</p>\n"
                                } else {
                                    "<p>${paragraph.xml()}</p>\n"
                                },
                            )
                        }
                    }

                    is PageItem.CharacterGrid -> {
                        val section = newSection(context.getString(R.string.book_characters_title))
                        section.body.append("<h1>${section.title.orEmpty().xml()}</h1>\n<div class=\"characters\">\n")
                        item.characters.forEach { character ->
                            val portrait = character.data.image.takeIf { it.isNotBlank() }?.let { addImage(it, character.data.name) }
                            section.body.append(
                                portrait ?: "<figure><figcaption>${character.data.name.xml()}</figcaption></figure>\n",
                            )
                        }
                        section.body.append("</div>\n")
                    }
                }
            }

            book.authorNote?.takeIf { it.isNotBlank() }?.let { note ->
                val section = newSection(null, cssClass = "note")
                note.lines().map { it.trim() }.filter { it.isNotEmpty() }.forEach { section.body.append("<p>${it.xml()}</p>\n") }
            }
            return sections
        }

        private fun writeEpub(
            file: File,
            book: Book,
            genre: Genre,
            sections: List<Section>,
            resources: List<Resource>,
            fonts: EmbeddedFonts,
        ) {
            val css = stylesheet(genre, fonts, resources)
            val title = "${book.sagaTitle}: ${book.actTitle}"
            val tocSections = sections.filter { it.title != null }

            ZipOutputStream(FileOutputStream(file).buffered()).use { zip ->
                // The container must be recognisable from its first bytes: stored, never deflated.
                val mimetype = "application/epub+zip".toByteArray()
                zip.putNextEntry(
                    ZipEntry("mimetype").apply {
                        method = ZipEntry.STORED
                        size = mimetype.size.toLong()
                        compressedSize = mimetype.size.toLong()
                        crc = CRC32().apply { update(mimetype) }.value
                    },
                )
                zip.write(mimetype)
                zip.closeEntry()

                fun entry(
                    path: String,
                    content: ByteArray,
                ) {
                    zip.putNextEntry(ZipEntry(path))
                    zip.write(content)
                    zip.closeEntry()
                }

                entry("META-INF/container.xml", CONTAINER_XML.toByteArray())
                entry("OEBPS/style.css", css.toByteArray())
                resources.forEach { entry("OEBPS/${it.path}", it.bytes) }
                sections.forEach { section ->
                    val bodyClass = if (section.cssClass == "cover") " class=\"cover\"" else ""
                    entry(
                        "OEBPS/${section.fileName}",
                        xhtml(section.title ?: book.actTitle, "<body$bodyClass>\n${section.body}\n</body>").toByteArray(),
                    )
                }
                entry("OEBPS/nav.xhtml", navDocument(book, tocSections).toByteArray())
                entry("OEBPS/content.opf", packageDocument(book, title, sections, resources).toByteArray())
            }
        }

        private fun stylesheet(
            genre: Genre,
            fonts: EmbeddedFonts,
            resources: List<Resource>,
        ): String {
            val accent = genre.color.toArgb().hex()
            val onAccent = genre.iconColor.toArgb().hex()
            fun face(
                family: String?,
                name: String,
            ) = family
                ?.let { id -> resources.first { it.id == id } }
                ?.let { "@font-face { font-family: \"$name\"; src: url(\"${it.path}\"); }\n" }
                .orEmpty()

            val headerFamily = if (fonts.header != null) "\"SagasHeader\", serif" else "serif"
            val bodyFamily = if (fonts.body != null) "\"SagasBody\", serif" else "serif"
            return face(fonts.header, "SagasHeader") + face(fonts.body, "SagasBody") +
                """
                body { font-family: $bodyFamily; line-height: 1.5; margin: 5%; }
                h1, h2, .dropcap { font-family: $headerFamily; color: $accent; }
                h1 { text-align: center; margin: 1.5em 0 1em; }
                p { margin: 0 0 .8em; text-align: justify; }
                .dropcap { float: left; font-size: 3.2em; line-height: .85; padding: .04em .1em 0 0; }
                figure { margin: 1.5em 0; text-align: center; page-break-inside: avoid; }
                figure img { max-width: 100%; height: auto; }
                figcaption { font-size: .85em; opacity: .8; margin-top: .4em; }
                .characters figure { display: inline-block; width: 45%; margin: .8em 1%; vertical-align: top; }
                .note { font-style: italic; text-align: center; }
                body.cover { background-color: $accent; color: $onAccent; text-align: center; margin: 0; padding: 20% 8%; }
                body.cover h1, body.cover h2 { color: $onAccent; }
                body.cover h1 { font-size: 2.2em; letter-spacing: .04em; text-transform: uppercase; }
                body.cover .volume { letter-spacing: .3em; margin: 2em 0; }
                body.cover .quote { font-style: italic; margin: 3em 0 0; }
                """.trimIndent() + "\n"
        }

        private fun navDocument(
            book: Book,
            tocSections: List<Section>,
        ): String =
            """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops" xml:lang="${language()}">
<head><meta charset="utf-8"/><title>${book.actTitle.xml()}</title></head>
<body>
<nav epub:type="toc" id="toc">
<h1>${book.actTitle.xml()}</h1>
<ol>
${tocSections.joinToString("\n") { "<li><a href=\"${it.fileName}\">${it.title.orEmpty().xml()}</a></li>" }}
</ol>
</nav>
</body>
</html>
"""

        private fun packageDocument(
            book: Book,
            title: String,
            sections: List<Section>,
            resources: List<Resource>,
        ): String {
            val modified =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                    .apply { timeZone = TimeZone.getTimeZone("UTC") }
                    .format(System.currentTimeMillis())
            val manifest =
                buildString {
                    appendLine("<item id=\"nav\" href=\"nav.xhtml\" media-type=\"application/xhtml+xml\" properties=\"nav\"/>")
                    appendLine("<item id=\"css\" href=\"style.css\" media-type=\"text/css\"/>")
                    sections.forEach { appendLine("<item id=\"${it.id}\" href=\"${it.fileName}\" media-type=\"application/xhtml+xml\"/>") }
                    resources.forEach { appendLine("<item id=\"${it.id}\" href=\"${it.path}\" media-type=\"${it.mediaType}\"/>") }
                }
            val spine = sections.joinToString("\n") { "<itemref idref=\"${it.id}\"/>" }
            return """<?xml version="1.0" encoding="UTF-8"?>
<package xmlns="http://www.idpf.org/2007/opf" version="3.0" unique-identifier="book-id" xml:lang="${language()}">
<metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
<dc:identifier id="book-id">urn:sagas:book:${book.id}:${book.actId}</dc:identifier>
<dc:title>${title.xml()}</dc:title>
<dc:language>${language()}</dc:language>
<dc:creator>Sagas</dc:creator>
<meta property="dcterms:modified">$modified</meta>
</metadata>
<manifest>
$manifest</manifest>
<spine>
$spine
</spine>
</package>
"""
        }

        private fun xhtml(
            title: String,
            body: String,
        ): String =
            """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops" xml:lang="${language()}">
<head><meta charset="utf-8"/><title>${title.xml()}</title><link rel="stylesheet" type="text/css" href="style.css"/></head>
$body
</html>
"""

        /**
         * Flattened onto white and re-encoded as JPEG: the generated art is large, often PNG with
         * alpha, and JPEG is the one raster type every reading system is guaranteed to open.
         */
        private fun encodeJpeg(path: String): ByteArray? {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MaxImageEdge) sample *= 2
            val decoded =
                BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample }) ?: return null
            val flat = Bitmap.createBitmap(decoded.width, decoded.height, Bitmap.Config.ARGB_8888)
            Canvas(flat).apply {
                drawColor(Color.WHITE)
                drawBitmap(decoded, 0f, 0f, null)
            }
            return ByteArrayOutputStream().use { out ->
                flat.compress(Bitmap.CompressFormat.JPEG, JpegQuality, out)
                decoded.recycle()
                flat.recycle()
                out.toByteArray()
            }
        }

        private fun language() = Locale.getDefault().toLanguageTag().takeIf { it != "und" } ?: "en"

        private fun Int.hex() = String.format(Locale.US, "#%06X", this and 0xFFFFFF)

        private fun String.xml(): String =
            replace(XmlControlChars, "")
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")

        private companion object {
            const val MaxImageEdge = 1600
            const val JpegQuality = 85
            val FileNameForbidden = Regex("""[/\\:*?"<>|]""")
            val XmlControlChars = Regex("[\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F]")
            const val CONTAINER_XML = """<?xml version="1.0" encoding="UTF-8"?>
<container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
<rootfiles><rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/></rootfiles>
</container>
"""
        }
    }
