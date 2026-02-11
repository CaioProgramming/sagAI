package com.ilustris.sagai.ui.components.views

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import timber.log.Timber
import com.ilustris.sagai.core.segmentation.ImageSegmentationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DepthLayoutViewModel
    @Inject
    constructor(
        private val imageSegmentationHelper: ImageSegmentationHelper,
    ) : ViewModel() {
        private val _originalBitmap = MutableStateFlow<Bitmap?>(null)
        val originalBitmap = _originalBitmap.asStateFlow()

        private val _segmentedBitmap = MutableStateFlow<Bitmap?>(null)
        val segmentedBitmap = _segmentedBitmap.asStateFlow()

        private val _isProcessing = MutableStateFlow(false)
        val isProcessing = _isProcessing.asStateFlow()

        fun processImage(imagePath: String) {
            if (imagePath.isBlank()) return

            viewModelScope.launch(Dispatchers.IO) {
                _isProcessing.emit(true)
                imageSegmentationHelper
                    .processImage(imagePath)
                    .onSuccessAsync {
                        _originalBitmap.emit(it.first)
                        _segmentedBitmap.emit(it.second)
                        _isProcessing.emit(false)
                    }.onFailureAsync {
                        Timber.e("Error processing image: ${it.message}")
                        _isProcessing.emit(false)
                    }
            }
        }
    }
