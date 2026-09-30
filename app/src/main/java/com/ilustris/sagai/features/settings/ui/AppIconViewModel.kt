package com.ilustris.sagai.features.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.icon.AppIconManager
import com.ilustris.sagai.core.icon.LauncherIcon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppIconViewModel
    @Inject
    constructor(
        private val appIconManager: AppIconManager,
    ) : ViewModel() {
        val selected: StateFlow<LauncherIcon> = appIconManager.selected

        val followsLastSaga: StateFlow<Boolean> =
            appIconManager.followsLastSaga.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

        /** Picking an icon by hand ends "follow the last saga": otherwise the next background would undo the choice. */
        fun select(icon: LauncherIcon) {
            viewModelScope.launch {
                appIconManager.setFollowsLastSaga(false)
                appIconManager.choose(icon)
            }
        }

        fun setFollowsLastSaga(enabled: Boolean) {
            viewModelScope.launch { appIconManager.setFollowsLastSaga(enabled) }
        }
    }
