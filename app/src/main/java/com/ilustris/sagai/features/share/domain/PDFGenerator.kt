package com.ilustris.sagai.features.share.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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

            // 1. Cover Page
            val coverPage = pdfDocument.startPage(pageInfo)
            drawCoverPage(coverPage.canvas, book, genre, headerTypeface, bodyTypeface, genreColor)
            pdfDocument.finishPage(coverPage)

            // 2. Content Pages
            book.pages.forEachIndexed { index, page ->
                val contentPage = pdfDocument.startPage(pageInfo)
                drawContentPage(
                    contentPage.canvas,
                    page,
                    index + 1,
                    genre,
                    headerTypeface,
                    bodyTypeface,
                    genreColor,
                )
                pdfDocument.finishPage(contentPage)
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
            genreColor: Int,
        ) {
            val paint = Paint()
            paint.color = Color.parseColor("#121212") // Dark background for premium feel
            canvas.drawRect(0f, 0f, 595f, 842f, paint)

            val headerPaint =
                TextPaint().apply {
                    color = genreColor
                    typeface = headerTypeface
                    textSize = 40f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }

            val bodyPaint =
                TextPaint().apply {
                    color = Color.WHITE
                    typeface = bodyTypeface
                    textSize = 18f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }

            canvas.drawText(book.sagaTitle.uppercase(), 595f / 2, 250f, headerPaint)
            canvas.drawText(book.actTitle, 595f / 2, 300f, bodyPaint)

            // Draw icon with some transparency
            val icon = BitmapFactory.decodeResource(context.resources, genre.icon)
            val aspect = icon.height.toFloat() / icon.width.toFloat()
            val targetWidth = 150f
            val targetHeight = targetWidth * aspect
            val scaledIcon =
                Bitmap.createScaledBitmap(icon, targetWidth.toInt(), targetHeight.toInt(), true)

            val iconPaint = Paint().apply { alpha = 200 }
            canvas.drawBitmap(scaledIcon, (595f - targetWidth) / 2, 400f, iconPaint)

            val quotePaint =
                TextPaint().apply {
                    color = Color.GRAY
                    typeface = bodyTypeface
                    textSize = 14f
                    textAlign = Paint.Align.CENTER
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
            canvas.translate(595f / 2, 650f)
            quoteLayout.draw(canvas)
            canvas.restore()

            canvas.drawText(
                "Sagas AI",
                595f / 2,
                780f,
                quotePaint.apply {
                    textSize = 10f
                    letterSpacing = 0.5f
                },
            )
        }

        private fun drawContentPage(
            canvas: Canvas,
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

            val titlePaint =
                TextPaint().apply {
                    color = genreColor
                    typeface = headerTypeface
                    textSize = 24f
                    isAntiAlias = true
                }

            canvas.drawText(page.chapterTitle, 50f, 80f, titlePaint)

            val bodyPaint =
                TextPaint().apply {
                    color = Color.BLACK
                    typeface = bodyTypeface
                    textSize = 14f
                    isAntiAlias = true
                    letterSpacing = 0.02f
                }

            val bodyLayout =
                StaticLayout.Builder
                    .obtain(page.content, 0, page.content.length, bodyPaint, 495)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0f, 1.2f)
                    .build()

            canvas.save()
            canvas.translate(50f, 130f)
            bodyLayout.draw(canvas)
            canvas.restore()

            val footerPaint =
                TextPaint().apply {
                    color = Color.LTGRAY
                    typeface = bodyTypeface
                    textSize = 10f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }

            canvas.drawText(pageNum.toString(), 595f / 2, 800f, footerPaint)
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

            val titlePaint =
                TextPaint().apply {
                    color = genreColor
                    typeface = headerTypeface
                    textSize = 28f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }

            canvas.drawText("AUTHOR'S NOTE", 595f / 2, 200f, titlePaint)

            val bodyPaint =
                TextPaint().apply {
                    color = Color.LTGRAY
                    typeface = bodyTypeface
                    textSize = 14f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }

            val noteLayout =
                StaticLayout.Builder
                    .obtain(note, 0, note.length, bodyPaint, 450)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setLineSpacing(0f, 1.2f)
                    .build()

            canvas.save()
            canvas.translate(595f / 2, 300f)
            noteLayout.draw(canvas)
            canvas.restore()

            canvas.drawText("End of Volume", 595f / 2, 700f, titlePaint.apply { textSize = 16f })
            canvas.drawText(book.sagaTitle, 595f / 2, 730f, bodyPaint.apply { textSize = 12f })
        }
    }
