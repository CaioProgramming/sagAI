package com.ilustris.sagai.features.share.domain

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.utils.FileCacheService
import com.ilustris.sagai.core.utils.FileHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

interface SaveShareBitmapUseCase {
    suspend operator fun invoke(
        bitmap: Bitmap,
        fileName: String,
    ): RequestResult<Uri>
}

class SaveShareBitmapUseCaseImpl
    @Inject
    constructor(
        private val fileHelper: FileCacheService,
        @ApplicationContext
        private val context: Context,
    ) : SaveShareBitmapUseCase {
        override suspend fun invoke(
            bitmap: Bitmap,
            fileName: String,
        ): RequestResult<Uri> =
            executeRequest {
                val file = fileHelper.saveFile(fileName, bitmap)!!
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file,
                )
            }
    }
