package com.ilustris.sagai.features.share.presentation

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.SharePrompts
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.share.domain.SaveShareBitmapUseCase
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
        private val gemmaClient: GemmaClient,
        private val saveShareBitmapUseCase: SaveShareBitmapUseCase,
    ) : ViewModel() {
        val shareText = MutableStateFlow<String?>(null)
        val isLoading = MutableStateFlow(true)

        // bitmap saving flows
        val isSaving = MutableStateFlow(false)
        val savedFilePath = MutableStateFlow<Uri?>(null)

        fun generatePlayStyleText(
            character: Character,
            sagaContent: SagaContent,
        ) {
            isLoading.value = true
            viewModelScope.launch(Dispatchers.IO) {
                val prompt = SharePrompts.playStylePrompt(character, sagaContent)
                val generatedText = gemmaClient.generate<String>(prompt)
                shareText.value = generatedText
                isLoading.value = false
            }
        }

        fun saveBitmap(
            bitmap: Bitmap?,
            fileName: String = "saga_share",
        ) {
            if (bitmap == null) return
            isSaving.value = true
            viewModelScope.launch(Dispatchers.IO) {
                saveShareBitmapUseCase(bitmap, fileName).onSuccess {
                    savedFilePath.value = it
                }
            }
        }
    }
