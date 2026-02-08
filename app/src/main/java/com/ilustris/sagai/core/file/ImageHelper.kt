package com.ilustris.sagai.core.file

import android.content.Context
import android.graphics.Bitmap
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.toBitmap
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ImageHelper
    @Inject
    constructor(
        @ApplicationContext
        private val context: Context,
        private val imageLoader: ImageLoader,
    ) {
        suspend fun getImageBitmap(
            uri: String? = null,
            cropToCircle: Boolean = false,
        ): RequestResult<Bitmap> =
            executeRequest {
                if (uri.isNullOrEmpty()) {
                    error("Image uri invalid")
                }
                val request =
                    imageLoader.execute(
                        ImageRequest
                            .Builder(context)
                            .data(uri)
                            .build(),
                    )
                val bitmap = request.image!!.toBitmap()

                return@executeRequest bitmap
            }
    }
