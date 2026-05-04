package com.ilustris.sagai.features.share.domain

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.ilustris.sagai.features.act.data.model.Book
import com.ilustris.sagai.features.act.data.model.BookPage
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.bodyFontResource
import com.ilustris.sagai.ui.theme.headerFontResource
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class PDFGenerator
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun generateBookPDF(
            book: Book,
            genre: Genre,
            volume: String,
        ): File? {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size

            val headerTypeface =
                genre.headerFontResource()?.let { ResourcesCompat.getFont(context, it) }
                    ?: Typeface.DEFAULT
            val bodyTypeface =
                genre.bodyFontResource()?.let { ResourcesCompat.getFont(context, it) }
                    ?: Typeface.DEFAULT
            val genreColor = genre.color.toArgb()
            val iconColor = genre.iconColor.toArgb()

            // 1. Cover Page
            val coverPage = pdfDocument.startPage(pageInfo)
            drawCoverPage(
                coverPage.canvas,
                book,
                genre,
                headerTypeface,
                bodyTypeface,
                iconColor,
                volume,
            )
            pdfDocument.finishPage(coverPage)

            // 2. Chapters and Content Pages
            var globalPageCount = 1
            book.chapters.forEach { chapter ->
                // Chapter Start Page
                val chapterStartPage = pdfDocument.startPage(pageInfo)
                drawChapterStartPage(
                    chapterStartPage.canvas,
                    chapter.title,
                    genre,
                    headerTypeface,
                    genreColor,
                )
                pdfDocument.finishPage(chapterStartPage)

                // Chapter Pages
                chapter.pages.forEach { page ->
                    val contentPage = pdfDocument.startPage(pageInfo)
                    drawContentPage(
                        contentPage.canvas,
                        chapter.title,
                        page,
                        globalPageCount,
                        genre,
                        headerTypeface,
                        bodyTypeface,
                        genreColor,
                    )
                    pdfDocument.finishPage(contentPage)
                    globalPageCount++
                }
            }

            // 3. Author Note (End Paper)
            book.authorNote?.let { note ->
                val endPage = pdfDocument.startPage(pageInfo)
                drawEndPage(endPage.canvas, book, note, genre, headerTypeface, bodyTypeface, genreColor)
                pdfDocument.finishPage(endPage)
            }

            val sharesDir = File(context.cacheDir, "shares")
            if (!sharesDir.exists()) sharesDir.mkdirs()
            val file = File(sharesDir, "${book.actTitle.replace(" ", "_")}.pdf")

            return try {
                pdfDocument.writeTo(FileOutputStream(file))
                pdfDocument.close()
                file
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        private fun drawCoverPage(
            canvas: Canvas,
            book: Book,
            genre: Genre,
            headerTypeface: Typeface,
            bodyTypeface: Typeface,
            iconColor: Int,
            volume: String,
        ) {
            val paint = Paint()
            paint.color = genre.color.toArgb() // Genre background
            canvas.drawRect(0f, 0f, 595f, 842f, paint)

            val headerPaint =
                TextPaint().apply {
                    color = iconColor
                    typeface = headerTypeface
                    textSize = 40f
                    textAlign = Paint.Align.LEFT
                    isAntiAlias = true
                }

            val bodyPaint =
                TextPaint().apply {
                    color = iconColor
                    typeface = bodyTypeface
                    textSize = 18f
                    textAlign = Paint.Align.LEFT
                    isAntiAlias = true
                }

            val volPaint =
                TextPaint().apply {
                    color = iconColor
                    typeface = bodyTypeface
                    textSize = 12f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                    letterSpacing = 0.4f
                }

            // Using StaticLayout for titles to handle wrapping and margins
            val coverMargin = 60
            val coverContentWidth = (595 - (coverMargin * 2))

            val sagaTitleLayout =
                StaticLayout.Builder
                    .obtain(
                        book.sagaTitle.uppercase(),
                        0,
                        book.sagaTitle.length,
                        headerPaint,
                        coverContentWidth,
                    ).setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .build()

            val actTitleLayout =
                StaticLayout.Builder
                    .obtain(book.actTitle, 0, book.actTitle.length, bodyPaint, coverContentWidth)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .build()

            canvas.drawText("VOL $volume", 595f / 2, 180f, volPaint)

            canvas.save()
            canvas.translate(coverMargin.toFloat(), 220f)
            sagaTitleLayout.draw(canvas)
            canvas.restore()

            canvas.save()
            canvas.translate(coverMargin.toFloat(), 220f + sagaTitleLayout.height + 20f)
            actTitleLayout.draw(canvas)
            canvas.restore()

            // Draw icon safely (supporting vectors) using the icon color
            ResourcesCompat.getDrawable(context.resources, genre.icon, null)?.let { drawable ->
                val targetWidth = 100
                val aspect = drawable.intrinsicHeight.toFloat() / drawable.intrinsicWidth.toFloat()
                val targetHeight = (targetWidth * aspect).toInt()
                val bitmap = drawable.toBitmap(targetWidth, targetHeight)

                val iconPaint =
                    Paint().apply {
                        alpha = 255
                        colorFilter =
                            android.graphics.PorterDuffColorFilter(
                                iconColor,
                                android.graphics.PorterDuff.Mode.SRC_IN,
                            )
                    }
                val iconY = 220f + sagaTitleLayout.height + 20f + actTitleLayout.height + 60f
                canvas.drawBitmap(bitmap, (595f - targetWidth) / 2, iconY, iconPaint)
            }

            val quotePaint =
                TextPaint().apply {
                    color = iconColor
                    alpha = 180
                    typeface = bodyTypeface
                    textSize = 14f
                    textAlign = Paint.Align.LEFT
                    isAntiAlias = true
                }

            val quoteLayout =
                StaticLayout.Builder
                    .obtain(
                        "\"${book.coverQuote}\"",
                        0,
                        book.coverQuote.length + 2,
                        quotePaint,
                        400,
                    ).setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .build()

            canvas.save()
            canvas.translate((595f - 400f) / 2, 650f)
            quoteLayout.draw(canvas)
            canvas.restore()
        }

        private fun drawChapterStartPage(
            canvas: Canvas,
            title: String,
            genre: Genre,
            headerTypeface: Typeface,
            genreColor: Int,
        ) {
            val paint = Paint()
            paint.color = Color.WHITE
            canvas.drawRect(0f, 0f, 595f, 842f, paint)

            val titlePaint =
                TextPaint().apply {
                    color = genreColor
                    typeface = headerTypeface
                    textSize = 32f
                    textAlign = Paint.Align.LEFT
                    isAntiAlias = true
                }

            val titleLayout =
                StaticLayout.Builder
                    .obtain(title.uppercase(), 0, title.length, titlePaint, 450)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .build()

            canvas.save()
            canvas.translate((595f - 450f) / 2, 400f - (titleLayout.height / 2))
            titleLayout.draw(canvas)
            canvas.restore()

            // Draw icon below title
            ResourcesCompat.getDrawable(context.resources, genre.icon, null)?.let { drawable ->
                val iconSize = 48
                val aspect = drawable.intrinsicHeight.toFloat() / drawable.intrinsicWidth.toFloat()
                val targetHeight = (iconSize * aspect).toInt()
                val bitmap = drawable.toBitmap(iconSize, targetHeight)

                val iconPaint =
                    Paint().apply {
                        alpha = 255
                        colorFilter =
                            android.graphics.PorterDuffColorFilter(
                                genreColor,
                                android.graphics.PorterDuff.Mode.SRC_IN,
                            )
                    }
                canvas.drawBitmap(bitmap, (595f - iconSize) / 2, 450f + titleLayout.height, iconPaint)
            }
        }

        private fun drawContentPage(
            canvas: Canvas,
            chapterTitle: String,
            page: BookPage,
            pageNum: Int,
            genre: Genre,
            headerTypeface: Typeface,
            bodyTypeface: Typeface,
            genreColor: Int,
        ) {
            val paint = Paint()
            paint.color = Color.WHITE
            canvas.drawRect(0f, 0f, 595f, 842f, paint)

            val margin = 60f
            val contentWidth = 595f - (margin * 2)

            // Chapter title header removed for "pure story" experience

            val bodyPaint =
                TextPaint().apply {
                    color = Color.BLACK
                    typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
                    textSize = 14f
                    isAntiAlias = true
                    letterSpacing = 0.01f
                }

            val bodyLayout =
                StaticLayout.Builder
                    .obtain(page.content, 0, page.content.length, bodyPaint, contentWidth.toInt())
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0f, 1.4f) // Increased line spacing for better readability
                    .build()

            canvas.save()
            canvas.translate(margin, 80f) // Started lower for clean look
            bodyLayout.draw(canvas)
            canvas.restore()

            // Footer Icon & Page Number
            val footerColor = Color.parseColor("#BBBBBB")
            val footerPaint =
                TextPaint().apply {
                    color = footerColor
                    typeface = bodyTypeface
                    textSize = 10f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }

            // Draw small genre icon at bottom center
            ResourcesCompat.getDrawable(context.resources, genre.icon, null)?.let { drawable ->
                val iconSize = 24
                val aspect = drawable.intrinsicHeight.toFloat() / drawable.intrinsicWidth.toFloat()
                val targetHeight = (iconSize * aspect).toInt()
                val bitmap = drawable.toBitmap(iconSize, targetHeight)

                val iconPaint =
                    Paint().apply {
                        alpha = 100 // Subtle watermark effect
                        colorFilter =
                            android.graphics.PorterDuffColorFilter(
                                genreColor,
                                android.graphics.PorterDuff.Mode.SRC_IN,
                            )
                    }
                canvas.drawBitmap(bitmap, (595f - iconSize) / 2, 780f, iconPaint)
            }

            canvas.drawText(pageNum.toString(), 595f / 2, 820f, footerPaint)
        }

        private fun drawEndPage(
            canvas: Canvas,
            book: Book,
            note: String,
            genre: Genre,
            headerTypeface: Typeface,
            bodyTypeface: Typeface,
            genreColor: Int,
        ) {
            val paint = Paint()
            paint.color = Color.parseColor("#121212")
            canvas.drawRect(0f, 0f, 595f, 842f, paint)

            val bodyPaint =
                TextPaint().apply {
                    color = Color.LTGRAY
                    typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
                    textSize = 14f
                    textAlign = Paint.Align.LEFT
                    isAntiAlias = true
                }

            val noteLayout =
                StaticLayout.Builder
                    .obtain(note, 0, note.length, bodyPaint, 450)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setLineSpacing(0f, 1.2f)
                    .build()

            // Center the "letter" vertically in the page
            val noteHeight = noteLayout.height
            val startY = (842f - noteHeight) / 2

            canvas.save()
            canvas.translate((595f - 450f) / 2, startY)
            noteLayout.draw(canvas)
            canvas.restore()
        }
    }
