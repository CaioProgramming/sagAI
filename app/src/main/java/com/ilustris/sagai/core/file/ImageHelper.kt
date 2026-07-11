package com.ilustris.sagai.core.file

import android.content.Context
import android.graphics.Bitmap
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.size.Size
import coil3.toBitmap
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Target resolution for small avatar/notification icons (chat message senders, new-character
 * reveals, etc.) — these only ever render at ~24-28dp in-app or as a small notification icon,
 * so there's no reason to decode the source portrait at full resolution for them.
 */
const val AVATAR_ICON_TARGET_PX = 192

class ImageHelper
    @Inject
    constructor(
        @ApplicationContext
        private val context: Context,
        private val imageLoader: ImageLoader,
    ) {
        /**
         * @param targetSizePx Caps the decoded resolution so Coil downsamples during decode
         * instead of allocating a full-resolution bitmap just to shrink/crop it afterwards.
         * Leave null only when the full-resolution pixels are actually needed (e.g. palette
         * extraction from a real photo) — every avatar/icon caller should pass a small size.
         */
        suspend fun getImageBitmap(
            uri: String? = null,
            cropToCircle: Boolean = false,
            targetSizePx: Int? = null,
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
                            .apply {
                                if (targetSizePx != null) {
                                    size(Size(targetSizePx, targetSizePx))
                                }
                            }.build(),
                    )
                val bitmap = request.image!!.toBitmap()

                return@executeRequest bitmap
            }
    }
