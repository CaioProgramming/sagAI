package com.ilustris.sagai.core.segmentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import androidx.core.graphics.get
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.toBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenter
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.characters.data.model.SmartZoom
import com.mikepenz.hypnoticcanvas.utils.round
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import kotlin.math.max
import kotlin.math.min

class ImageSegmentationHelper(
    @ApplicationContext
    private val context: Context,
) {
    private val segmenter: SubjectSegmenter

    init {
        val options =
            SubjectSegmenterOptions
                .Builder()
                .enableForegroundBitmap()
                .build()
        segmenter = SubjectSegmentation.getClient(options)
    }

    suspend fun processImage(url: String): RequestResult<Pair<Bitmap, Bitmap>> =
        executeRequest {
            val imageLoader = ImageLoader(context)
            val request =
                ImageRequest
                    .Builder(context)
                    .data(url)
                    .build()
            val result = imageLoader.execute(request)
            val bitmap = result.image?.toBitmap()!!
            val image = InputImage.fromBitmap(bitmap, 0)
            bitmap to segmenter.process(image).await()?.foregroundBitmap!!
        }

    suspend fun calculateSmartZoom(url: String): RequestResult<SmartZoom> =
        executeRequest {
            val imageLoader = ImageLoader(context)
            val request =
                ImageRequest
                    .Builder(context)
                    .data(url)
                    .build()
            val result = imageLoader.execute(request)
            val bitmap = result.image?.toBitmap()!!
            val image = InputImage.fromBitmap(bitmap, 0)
            val segmentationResult = segmenter.process(image).await()

            if (segmentationResult == null || segmentationResult.foregroundBitmap == null) {
                return@executeRequest SmartZoom(needsZoom = false)
            }

            val foregroundBitmap = segmentationResult.foregroundBitmap!!
            val imageWidth = bitmap.width.toFloat()
            val imageHeight = bitmap.height.toFloat()

            var minX = imageWidth
            var minY = imageHeight
            var maxX = 0f
            var maxY = 0f

            for (x in 0 until foregroundBitmap.width) {
                for (y in 0 until foregroundBitmap.height) {
                    val pixel = foregroundBitmap[x, y]
                    if (android.graphics.Color.alpha(pixel) > 0) { // If pixel is not transparent
                        minX = min(minX, x.toFloat())
                        minY = min(minY, y.toFloat())
                        maxX = max(maxX, x.toFloat())
                        maxY = max(maxY, y.toFloat())
                    }
                }
            }

            val subjectRect =
                if (minX < maxX && minY < maxY) {
                    RectF(minX, minY, maxX, maxY)
                } else {
                    // If no subject found or invalid rect, fallback to center or no zoom
                    RectF(0f, 0f, imageWidth, imageHeight)
                }

            val subjectWidthRatio = subjectRect.width() / imageWidth
            val subjectHeightRatio = subjectRect.height() / imageHeight

            val needsZoom = subjectWidthRatio < 0.65f || subjectHeightRatio < 0.65f

            val requiredZoom =
                if (needsZoom) {
                    val scale =
                        max(imageWidth / subjectRect.width(), imageHeight / subjectRect.height())
                    val finalScale = scale.coerceAtMost(2.0f)

                    val scaledSubjectWidth = subjectRect.width() * finalScale
                    val scaledSubjectHeight = subjectRect.height() * finalScale

                    val subjectCenterX = subjectRect.centerX()
                    // Focus on the upper body/face area (approx top 25%) rather than the geometric center (waist/torso)
                    // This is optimized for vertical character portraits (9:16)
                    val subjectFaceY = subjectRect.top + (subjectRect.height() * .05f)

                    // Calculate the distance from image center to subject center/face
                    val dx = (imageWidth / 2) - subjectCenterX
                    val dy = (imageHeight / 2) - subjectFaceY

                    // Convert to relative values (-0.5 to 0.5 range)
                    val relativeX = dx / imageWidth
                    val relativeY = dy / imageHeight

                    // Simplify values to steps of 0.01 to avoid noise and ensure perceivable changes
                    val step = 100f
                    val simpleScale = kotlin.math.round(finalScale * step) / step
                    var simpleX = kotlin.math.round(relativeX * step) / step
                    val simpleY = kotlin.math.round(relativeY * step) / step

                    // If the Horizontal shift is subtle, ignore it to keep the image stable
                    if (kotlin.math.abs(simpleX) < 0.1f) {
                        simpleX = 0f
                    }

                    SmartZoom(
                        scale = simpleScale.round(1),
                        translationX = simpleX.round(2),
                        translationY = simpleY.round(2),
                        needsZoom = true,
                    )
                } else {
                    SmartZoom(needsZoom = false)
                }
            Log.i(javaClass.simpleName, "calculateSmartZoom: Zoom result: ")
            Log.i(javaClass.simpleName, requiredZoom.toJsonFormat())
            requiredZoom
        }
}
