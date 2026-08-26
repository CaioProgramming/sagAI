package com.ilustris.sagai.features.saga.detail.review.presentation

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.share.domain.SharePlayUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Review sharing is only ever "save the card we just drew and hand it to the system chooser" —
 * no prompt, no generated copy, no segmentation. The page already carries the story.
 */
@HiltViewModel
class ReviewShareViewModel
    @Inject
    constructor(
        private val sharePlayUseCase: SharePlayUseCase,
    ) : ViewModel() {
        private val _shareUri = MutableStateFlow<Uri?>(null)
        val shareUri: StateFlow<Uri?> = _shareUri.asStateFlow()

        private val _isSaving = MutableStateFlow(false)
        val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

        fun shareCard(
            bitmap: Bitmap,
            fileName: String,
        ) {
            if (_isSaving.value) return
            _isSaving.value = true
            viewModelScope.launch(Dispatchers.IO) {
                sharePlayUseCase
                    .saveBitmapToCache(bitmap, fileName)
                    .onSuccessAsync { file ->
                        _shareUri.value = sharePlayUseCase.loadWithFileProvider(file).getSuccess()
                    }
                _isSaving.value = false
            }
        }

        /**
         * Only drops the in-memory state — the cached file stays until the next save
         * clears the folder, so a chooser still reading the uri isn't pulled from under it.
         */
        fun clear() {
            _shareUri.value = null
            _isSaving.value = false
        }
    }
