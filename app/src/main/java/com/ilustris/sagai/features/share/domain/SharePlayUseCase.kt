package com.ilustris.sagai.features.share.domain

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.SharePrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.utils.FileCacheService
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.share.domain.model.ShareText
import com.ilustris.sagai.features.share.domain.model.ShareType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

interface SharePlayUseCase {
    suspend fun saveBitmapToCache(
        bitmap: Bitmap,
        fileName: String,
    ): RequestResult<File>

    suspend fun loadWithFileProvider(file: File): RequestResult<Uri>

    suspend fun clearShareFolder(): RequestResult<Unit>

    suspend fun generateShareMessage(
        saga: SagaContent,
        shareType: ShareType,
        character: CharacterContent?,
    ): RequestResult<ShareText>
}

class SharePlayUseCaseImpl
    @Inject
    constructor(
        private val fileHelper: FileCacheService,
        @ApplicationContext
        private val context: Context,
        private val gemmaClient: GemmaClient,
    ) : SharePlayUseCase {
        override suspend fun saveBitmapToCache(
            bitmap: Bitmap,
            fileName: String,
        ): RequestResult<File> =
            executeRequest {
                clearShareFolder()
                fileHelper.saveFile("shares", fileName, bitmap)!!
            }

        override suspend fun loadWithFileProvider(file: File): RequestResult<Uri> =
            executeRequest {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file,
                )
            }

        override suspend fun clearShareFolder(): RequestResult<Unit> =
            executeRequest {
                val shareDir = File(context.cacheDir, "file_cache/shares")
                if (shareDir.exists() && shareDir.isDirectory) {
                    shareDir.listFiles()?.forEach { it.delete() }
                }
            }

        override suspend fun generateShareMessage(
            saga: SagaContent,
            shareType: ShareType,
            character: CharacterContent?,
        ): RequestResult<ShareText> =
            executeRequest {
                val prompt =
                    when (shareType) {
                        ShareType.PLAYSTYLE -> SharePrompts.playStylePrompt(saga.mainCharacter!!.data, saga)
                        ShareType.EMOTIONS -> SharePrompts.emotionalPrompt(saga)
                        ShareType.HISTORY -> SharePrompts.historyPrompt(saga)
                        ShareType.RELATIONS -> SharePrompts.relationsPrompt(saga)
                        ShareType.CHARACTER -> SharePrompts.characterPrompt(character!!, saga)
                    }

                gemmaClient.generate<ShareText>(prompt)!!
            }
    }
