package com.ilustris.sagai.features.imagegeneration.ui

import androidx.lifecycle.ViewModel
import com.ilustris.sagai.features.imagegeneration.ImageGenerationService
import com.ilustris.sagai.features.imagegeneration.model.ImageTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Hands the singleton service's per-target state to composables, which have no other way to inject it. */
@HiltViewModel
class ImageTargetStateViewModel
    @Inject
    constructor(
        private val imageGenerationService: ImageGenerationService,
    ) : ViewModel() {
        fun stateOf(target: ImageTarget) = imageGenerationService.stateOf(target)
    }
