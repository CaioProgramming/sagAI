package com.ilustris.sagai.features.share.presentation

import android.graphics.Bitmap
import android.net.Uri
import android.util.LruCache
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.segmentation.ImageSegmentationHelper
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.share.domain.SharePlayUseCase
import com.ilustris.sagai.features.share.domain.model.ShareText
import com.ilustris.sagai.features.share.domain.model.ShareType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SharePlayViewModel
@Inject
constructor(
    private val sharePlayUseCase: SharePlayUseCase,
    private val imageSegmentationHelper: ImageSegmentationHelper,
) : ViewModel(),
    DefaultLifecycleObserver {
    val shareText = MutableStateFlow<ShareText?>(null)
    val isLoading = MutableStateFlow(true)
    val isSaving = MutableStateFlow(false)
    val savedFilePath = MutableStateFlow<Uri?>(null)

    private val _originalBitmap = MutableStateFlow<Bitmap?>(null)
    val originalBitmap = _originalBitmap.asStateFlow()
    private val _segmentedBitmap = MutableStateFlow<Bitmap?>(null)
    val segmentedBitmap = _segmentedBitmap.asStateFlow()
    private val segmentedImageCache =
        LruCache<String, Pair<Bitmap?, Bitmap?>>(5 * 1024 * 1024) // 5MB cache

    var shareFile: File? = null

    fun generateShareText(
        sagaContent: SagaContent,
        shareType: ShareType,
        character: CharacterContent? = null,
    ) {
        deleteSavedFile()
        isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            if (shareType == ShareType.CHARACTER && character == null) return@launch
            sharePlayUseCase
                .generateShareMessage(sagaContent, shareType, character)
                .onSuccess {
                    shareText.value = it
                    isLoading.value = false
                }.onFailure {
                    isLoading.value = false
                }
        }
    }

    fun segmentImage(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val cachedBitmaps = segmentedImageCache.get(url)
            if (cachedBitmaps != null) {
                _originalBitmap.value = cachedBitmaps.first
                _segmentedBitmap.value = cachedBitmaps.second
                return@launch
            }
            imageSegmentationHelper.processImage(url).onSuccess {
                _originalBitmap.value = it.first
                _segmentedBitmap.value = it.second
                segmentedImageCache.put(url, it)
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
            _originalBitmap.value = null
            _segmentedBitmap.value = null
        }
    }

    fun startSaving() {
        isSaving.value = true
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
                isSaving.emit(false)
            }
        }
    }
}
