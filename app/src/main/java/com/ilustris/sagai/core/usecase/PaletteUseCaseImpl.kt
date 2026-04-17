package com.ilustris.sagai.core.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.data.model.ImagePalette
import com.ilustris.sagai.core.file.ImageHelper
import javax.inject.Inject

class PaletteUseCaseImpl
    @Inject
    constructor(
        private val imageHelper: ImageHelper,
    ) : PaletteUseCase {
        override suspend fun extractPalette(imageUrl: String): RequestResult<ImagePalette> =
            executeRequest {
                val bitmapResult = imageHelper.getImageBitmap(imageUrl)
                if (bitmapResult is RequestResult.Success) {
                    val bitmap = bitmapResult.value
                    val paletteBitmap =
                        if (bitmap.config == android.graphics.Bitmap.Config.HARDWARE) {
                            bitmap.copy(android.graphics.Bitmap.Config.ARGB_8888, false)
                                ?: throw Exception("Failed to copy HARDWARE bitmap")
                        } else {
                            bitmap
                        }
                    ImagePalette.fromBitmap(paletteBitmap)
                } else {
                    throw Exception("Failed to load image bitmap")
                }
            }
    }
