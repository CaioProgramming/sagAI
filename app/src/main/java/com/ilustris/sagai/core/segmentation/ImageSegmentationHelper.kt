package com.ilustris.sagai.core.segmentation

import android.content.Context
import android.graphics.Bitmap
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.toBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenter
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await

class ImageSegmentationHelper(
    @ApplicationContext
    private val context: Context
) {

    private val segmenter: SubjectSegmenter

    init {
        val options = SubjectSegmenterOptions.Builder()
            .enableForegroundBitmap()
            .build()
        segmenter = SubjectSegmentation.getClient(options)
    }

    suspend fun processImage(url: String): RequestResult<Pair<Bitmap, Bitmap>> = executeRequest {
        val imageLoader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(url)
            .build()
        val result = imageLoader.execute(request)
        val bitmap = result.image?.toBitmap()!!
        val image = InputImage.fromBitmap(bitmap, 0)
        bitmap to segmenter.process(image).await()?.foregroundBitmap!!
    }

}