package com.ilustris.sagai.features.share.domain

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
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
import com.ilustris.sagai.features.act.ui.PageItem
import com.ilustris.sagai.features.characters.data.model.CharacterContent
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
            pages: List<PageItem>,
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

            // 1. Pages (Mapped from PageItem)
            var globalPageCount = 1
            pages.forEach { item ->
                when (item) {
                    is PageItem.BookCover -> {
                        val coverPage = pdfDocument.startPage(pageInfo)
                        drawCoverPage(
                            coverPage.canvas,
                            item,
                            genre,
                            headerTypeface,
                            bodyTypeface,
                            iconColor,
                            item.volume,
                        )
                        pdfDocument.finishPage(coverPage)
                    }

                    is PageItem.ChapterStart -> {
                        val chapterStartPage = pdfDocument.startPage(pageInfo)
                        drawChapterStartPage(
                            chapterStartPage.canvas,
                            item.title,
                            book,
                            genre,
                            headerTypeface,
                            genreColor,
                        )
                        pdfDocument.finishPage(chapterStartPage)
                    }

                    is PageItem.Illustration -> {
                        val illustrationPage = pdfDocument.startPage(pageInfo)
                        drawIllustrationPage(illustrationPage.canvas, item.imagePath)
                        pdfDocument.finishPage(illustrationPage)
                    }

                    is PageItem.Content -> {
                        val contentPage = pdfDocument.startPage(pageInfo)
                        drawContentPage(
                            contentPage.canvas,
                            item.chapterTitle,
                            item.page,
                            globalPageCount,
                            book,
                            genre,
                            headerTypeface,
                            bodyTypeface,
                            genreColor,
                        )
                        pdfDocument.finishPage(contentPage)
                        globalPageCount++
                    }

                    is PageItem.CharacterGrid -> {
                        val characterGridPage = pdfDocument.startPage(pageInfo)
                        drawCharacterGridPage(
                            characterGridPage.canvas,
                            item.characters,
                            book,
                            genre,
                            headerTypeface,
                            bodyTypeface,
                            genreColor,
                        )
                        pdfDocument.finishPage(characterGridPage)
                    }
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
            pageItem: PageItem.BookCover,
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
                        pageItem.sagaTitle.uppercase(),
                        0,
                        pageItem.sagaTitle.length,
                        headerPaint,
                        coverContentWidth,
                    ).setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .build()

            val actTitleLayout =
                StaticLayout.Builder
                    .obtain(
                        pageItem.actTitle,
                        0,
                        pageItem.actTitle.length,
                        bodyPaint,
                        coverContentWidth,
                    ).setAlignment(Layout.Alignment.ALIGN_CENTER)
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
                        "\"${pageItem.quote}\"",
                        0,
                        pageItem.quote.length + 2,
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
            book: Book,
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

            // Signature footer
            val signaturePaint =
                TextPaint().apply {
                    color = genreColor
                    typeface = headerTypeface
                    textSize = 10f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                    letterSpacing = 0.2f
                }
            canvas.drawText(book.sagaTitle, 595f / 2, 820f, signaturePaint)

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
            book: Book,
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

            canvas.drawText(pageNum.toString(), 595f / 2, 810f, footerPaint)

            // Signature footer
            val signaturePaint =
                TextPaint().apply {
                    color = genreColor
                    typeface = headerTypeface
                    textSize = 10f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                    letterSpacing = 0.2f
                }
            canvas.drawText(book.sagaTitle, 595f / 2, 830f, signaturePaint)
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

    private fun drawCharacterGridPage(
            canvas: Canvas,
            characters: List<CharacterContent>,
            book: Book,
            genre: Genre,
            headerTypeface: Typeface,
            bodyTypeface: Typeface,
            genreColor: Int,
        ) {
            val paint = Paint()
            paint.color = Color.WHITE
            canvas.drawRect(0f, 0f, 595f, 842f, paint)

            val gridTitle =
                when (genre) {
                    Genre.FANTASY -> "Our Legends"
                    Genre.CYBERPUNK -> "The Edge-Runners"
                    Genre.HORROR -> "The Lost Souls"
                    Genre.HEROES -> "The Vanguard"
                    Genre.CRIME -> "The Suspects"
                    Genre.SHINOBI -> "The Shadow-Walkers"
                    Genre.SPACE_OPERA -> "The Star-Farers"
                    Genre.COWBOY -> "The Outlaws"
                    Genre.PUNK_ROCK -> "The Anarchists"
                }

            val titlePaint =
                TextPaint().apply {
                    color = genreColor
                    typeface = headerTypeface
                    textSize = 28f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }

            canvas.drawText(gridTitle.uppercase(), 595f / 2, 80f, titlePaint)

            // Signature footer
            val signaturePaint =
                TextPaint().apply {
                    color = genreColor
                    typeface = headerTypeface
                    textSize = 10f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                    letterSpacing = 0.2f
                }
            canvas.drawText(book.sagaTitle, 595f / 2, 830f, signaturePaint)

            val charNamePaint =
                TextPaint().apply {
                    color = Color.BLACK
                    typeface = bodyTypeface
                    textSize = 10f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }

            val margin = 60f
            val spacing = 20f
            val itemSize = (595f - (margin * 2) - (spacing * 2)) / 3
            val startX = margin
            var startY = 120f

            characters.forEachIndexed { index, character ->
                val column = index % 3
                val row = index / 3
                val x = startX + (column * (itemSize + spacing))
                val y = startY + (row * (itemSize + spacing + 30f))

                // Draw character image
                character.data.image?.let { imagePath ->
                    val options =
                        BitmapFactory.Options().apply {
                            inJustDecodeBounds = true
                        }
                    BitmapFactory.decodeFile(imagePath, options)
                    options.inSampleSize =
                        calculateInSampleSize(options, itemSize.toInt(), itemSize.toInt())
                    options.inJustDecodeBounds = false

                    val bitmap = BitmapFactory.decodeFile(imagePath, options)
                    if (bitmap != null) {
                        val bWidth = bitmap.width
                        val bHeight = bitmap.height
                        val side = minOf(bWidth, bHeight)
                        val srcLeft = (bWidth - side) / 2
                        val srcTop = (bHeight - side) / 2
                        val srcRect = Rect(srcLeft, srcTop, srcLeft + side, srcTop + side)

                        canvas.drawBitmap(
                            bitmap,
                            srcRect,
                            RectF(x, y, x + itemSize, y + itemSize),
                            null,
                        )
                        bitmap.recycle()
                    } else {
                        // Draw placeholder
                        val placeholderPaint =
                            Paint().apply {
                                color = Color.LTGRAY
                                style = Paint.Style.FILL
                            }
                        canvas.drawRect(x, y, x + itemSize, y + itemSize, placeholderPaint)
                    }
                } ?: run {
                    // Draw placeholder
                    val placeholderPaint =
                        Paint().apply {
                            color = Color.LTGRAY
                        style = Paint.Style.FILL
                    }
                canvas.drawRect(x, y, x + itemSize, y + itemSize, placeholderPaint)
            }

            // Draw name
            val name = character.data.name
            canvas.drawText(name, x + (itemSize / 2), y + itemSize + 20f, charNamePaint)
            }
        }

        private fun drawIllustrationPage(
            canvas: Canvas,
            imagePath: String,
        ) {
            val options =
                BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
            BitmapFactory.decodeFile(imagePath, options)

            // Validate if image is valid
            if (options.outWidth <= 0 || options.outHeight <= 0) {
                // Draw a blank page with a subtle note or just skip
                canvas.drawColor(Color.WHITE)
                return
            }

            options.inSampleSize = calculateInSampleSize(options, 595, 842)
            options.inJustDecodeBounds = false

            val bitmap = BitmapFactory.decodeFile(imagePath, options) ?: return
            val bWidth = bitmap.width.toFloat()
            val bHeight = bitmap.height.toFloat()
            val pWidth = 595f
            val pHeight = 842f

            val scale = maxOf(pWidth / bWidth, pHeight / bHeight)
            val finalWidth = bWidth * scale
            val finalHeight = bHeight * scale
            val left = (pWidth - finalWidth) / 2
            val top = (pHeight - finalHeight) / 2

            canvas.drawBitmap(
                bitmap,
                null,
                RectF(left, top, left + finalWidth, top + finalHeight),
                null,
            )
            bitmap.recycle()
        }

        private fun calculateInSampleSize(
            options: BitmapFactory.Options,
            reqWidth: Int,
            reqHeight: Int,
        ): Int {
            val (height: Int, width: Int) = options.outHeight to options.outWidth
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {
                val halfHeight: Int = height / 2
                val halfWidth: Int = width / 2
                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2
                }
            }
            return inSampleSize
        }
    }
