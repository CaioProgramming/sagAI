package com.ilustris.sagai.core.utils

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.core.graphics.scale
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.tasks.await

class ImageCropHelper {
    private val faceDetector: FaceDetector

    init {
        val highAccuracyOpts =
            FaceDetectorOptions
                .Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
                .setMinFaceSize(0.15f)
                .build()
        faceDetector = FaceDetection.getClient(highAccuracyOpts)
    }

    suspend fun cropToPortraitBitmap(
        sourceBitmap: Bitmap,
        targetWidth: Int = 512,
        targetHeight: Int = 512,
        centerCropZoomFactor: Float = 0.8f,
        // Default topBiasRatio for the public method if we want to expose it later,
        // but for now, it's used internally when calling performCenterZoomCrop.
        // We'll pass it explicitly in the fallback call.
    ): Bitmap {
        try {
            // --- Attempt Face Detection ---
            val inputImage = InputImage.fromBitmap(sourceBitmap, 0)
            val faces = faceDetector.process(inputImage).await()

            if (faces.isEmpty()) {
                throw IllegalArgumentException("No faces detected.")
            }

            val face = faces.first()
            val boundingBox = face.boundingBox

            val paddedBoundingBox =
                getPaddedAndSquaredBoundingBox(
                    originalBox = boundingBox,
                    imageWidth = sourceBitmap.width,
                    imageHeight = sourceBitmap.height,
                    paddingPercent = 0.35f,
                )

            if (paddedBoundingBox.width() <= 0 || paddedBoundingBox.height() <= 0) {
                throw Exception("Invalid bounding box for face crop: ${paddedBoundingBox.toShortString()}")
            }

            val faceCroppedBitmap =
                Bitmap.createBitmap(
                    sourceBitmap,
                    paddedBoundingBox.left,
                    paddedBoundingBox.top,
                    paddedBoundingBox.width(),
                    paddedBoundingBox.height(),
                )

            val finalBitmap = faceCroppedBitmap.scale(targetWidth, targetHeight, true)
            if (finalBitmap != faceCroppedBitmap) {
                faceCroppedBitmap.recycle()
            }
            println("ImageCropHelper: Successfully cropped image using face detection.")
            return finalBitmap
            // --- End of Face Detection Attempt ---
        } catch (e: Exception) {
            println("ImageCropHelper: Face detection failed (Reason: ${e.message}). Falling back to center zoom crop with top bias.")
            // --- Fallback to Center Zoom Crop by calling the extracted function ---
            return performCenterZoomCrop(
                sourceBitmap,
                targetWidth,
                targetHeight,
                centerCropZoomFactor,
                topBiasRatio = 0.25f, // Shift crop upwards
            )
        }
    }

    private fun performCenterZoomCrop(
        sourceBitmap: Bitmap,
        targetWidth: Int,
        targetHeight: Int,
        zoomFactor: Float,
        topBiasRatio: Float,
    ): Bitmap {
        try {
            val origWidth = sourceBitmap.width
            val origHeight = sourceBitmap.height

            if (origWidth <= 0 || origHeight <= 0) {
                throw IllegalArgumentException("Fallback crop: Source bitmap width and height must be positive.")
            }
            if (zoomFactor <= 0 || zoomFactor > 1.0f) {
                throw IllegalArgumentException("Fallback crop: Zoom factor must be between 0 (exclusive) and 1.0 (inclusive).")
            }
            if (topBiasRatio < 0.0f || topBiasRatio > 1.0f) {
                throw IllegalArgumentException("Fallback crop: Top bias ratio must be between 0.0 and 1.0.")
            }

            val cropWidth = (origWidth * zoomFactor).toInt()
            val cropHeight = (origHeight * zoomFactor).toInt()

            if (cropWidth <= 0 || cropHeight <= 0) {
                println("ImageCropHelper: Fallback crop calculated dimensions too small. Scaling original.")
                return sourceBitmap.scale(targetWidth, targetHeight, true)
            }

            val cropX = ((origWidth - cropWidth) / 2).takeIf { it >= 0 } ?: 0

            // Modified cropY calculation for top bias
            val verticalSlack = origHeight - cropHeight
            val cropY = (verticalSlack * topBiasRatio).toInt().coerceIn(0, maxOf(0, origHeight - cropHeight))

            val finalCropWidth = if (cropX + cropWidth > origWidth) origWidth - cropX else cropWidth
            val finalCropHeight = if (cropY + cropHeight > origHeight) origHeight - cropY else cropHeight

            if (finalCropWidth <= 0 || finalCropHeight <= 0) {
                println("ImageCropHelper: Fallback crop final dimensions invalid. Scaling original.")
                return sourceBitmap.scale(targetWidth, targetHeight, true)
            }

            val centerCroppedBitmap =
                Bitmap.createBitmap(
                    sourceBitmap,
                    cropX,
                    cropY,
                    finalCropWidth,
                    finalCropHeight,
                )

            val finalBitmap = centerCroppedBitmap.scale(targetWidth, targetHeight, true)
            if (finalBitmap != centerCroppedBitmap) {
                centerCroppedBitmap.recycle()
            }
            println("ImageCropHelper: Fallback to biased center zoom crop completed.")
            return finalBitmap
        } catch (fallbackException: Exception) {
            println("ImageCropHelper: Fallback biased center zoom crop also failed: ${fallbackException.message}")
            fallbackException.printStackTrace()
            throw fallbackException
        }
    }

    private fun getPaddedAndSquaredBoundingBox(
        originalBox: Rect,
        imageWidth: Int,
        imageHeight: Int,
        paddingPercent: Float,
    ): Rect {
        val paddedLeft = (originalBox.left - originalBox.width() * paddingPercent).toInt()
        val paddedTop = (originalBox.top - originalBox.height() * paddingPercent).toInt()
        val paddedRight = (originalBox.right + originalBox.width() * paddingPercent).toInt()
        val paddedBottom = (originalBox.bottom + originalBox.height() * paddingPercent).toInt()

        var newWidth = paddedRight - paddedLeft
        var newHeight = paddedBottom - paddedTop

        val centerX = originalBox.centerX()
        val centerY = originalBox.centerY()

        if (newWidth > newHeight) {
            newHeight = newWidth
        } else {
            newWidth = newHeight
        }

        var finalLeft = centerX - newWidth / 2
        var finalTop = centerY - newHeight / 2

        finalLeft = maxOf(0, finalLeft)
        finalTop = maxOf(0, finalTop)

        var finalRight = minOf(imageWidth, finalLeft + newWidth)
        var finalBottom = minOf(imageHeight, finalTop + newHeight)

        val currentWidth = finalRight - finalLeft
        val currentHeight = finalBottom - finalTop
        val size = minOf(currentWidth, currentHeight)

        finalLeft =
            if (centerX - size / 2 < 0) {
                0
            } else if (centerX + size / 2 > imageWidth) {
                imageWidth - size
            } else {
                centerX - size / 2
            }
        finalTop =
            if (centerY - size / 2 < 0) {
                0
            } else if (centerY + size / 2 > imageHeight) {
                imageHeight - size
            } else {
                centerY - size / 2
            }

        finalLeft = maxOf(0, finalLeft)
        finalTop = maxOf(0, finalTop)

        finalRight = minOf(imageWidth, finalLeft + size)
        finalBottom = minOf(imageHeight, finalTop + size)

        return Rect(finalLeft, finalTop, finalRight, finalBottom)
    }
}
