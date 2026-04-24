package com.ilustris.sagai.ui.multiverse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.services.MultiverseAssetsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A shared ViewModel to access multiversal assets.
 * Can be used by backgrounds and other decorative components to self-manage data.
 */
@HiltViewModel
class MultiverseAssetsViewModel
    @Inject
    constructor(
        val provider: MultiverseAssetsProvider,
    ) : ViewModel() {
        val personas = provider.personas
        val storyAssets = provider.storyAssets

        fun fetchPersonas() {
            viewModelScope.launch {
                provider.fetchPersonas()
            }
        }

        fun fetchStoryAssets() {
            viewModelScope.launch {
                provider.fetchStoryAssets()
            }
        }
    }
