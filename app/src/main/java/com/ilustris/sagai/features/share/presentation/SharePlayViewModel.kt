package com.ilustris.sagai.features.share.presentation

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.SharePrompts
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.share.domain.SharePlayUseCase
import com.ilustris.sagai.features.share.domain.model.ShareText
import com.ilustris.sagai.features.share.domain.model.ShareType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SharePlayViewModel
    @Inject
    constructor(
        private val sharePlayUseCase: SharePlayUseCase,
    ) : ViewModel(),
        DefaultLifecycleObserver {
        val shareText = MutableStateFlow<ShareText?>(null)
        val isLoading = MutableStateFlow(true)
        val isSaving = MutableStateFlow(false)
        val savedFilePath = MutableStateFlow<Uri?>(null)

        var shareFile: File? = null

        fun generateShareText(
            sagaContent: SagaContent,
            shareType: ShareType,
        ) {
            deleteSavedFile()
            isLoading.value = true
            viewModelScope.launch(Dispatchers.IO) {
                sharePlayUseCase
                    .generateShareMessage(sagaContent, shareType)
                    .onSuccess {
                        shareText.value = it
                        isLoading.value = false
                    }.onFailure {
                        isLoading.value = false
                    }
            }
        }

        fun deleteSavedFile() {
            viewModelScope.launch(Dispatchers.IO) {
                sharePlayUseCase.clearShareFolder()
                shareText.value = null
                isLoading.value = true
                isSaving.value = false
                savedFilePath.value = null
            }
        }

        fun saveBitmap(
            bitmap: Bitmap?,
            fileName: String,
        ) {
            if (bitmap == null) return
            isSaving.value = true
            viewModelScope.launch(Dispatchers.IO) {
                sharePlayUseCase.saveBitmapToCache(bitmap, fileName).onSuccessAsync {
                    shareFile = it
                    savedFilePath.value = sharePlayUseCase.loadWithFileProvider(it).getSuccess()
                }
            }
        }
    }
