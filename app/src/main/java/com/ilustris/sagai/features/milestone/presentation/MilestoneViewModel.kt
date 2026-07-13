package com.ilustris.sagai.features.milestone.presentation

import androidx.lifecycle.ViewModel
import com.ilustris.sagai.core.theme.SagaThemeManager
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.MilestoneDashboardItem
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.MilestoneDashboardMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MilestoneViewModel
    @Inject
    constructor(
        private val dashboardMapper: MilestoneDashboardMapper,
        private val sagaThemeManager: SagaThemeManager,
    ) : ViewModel() {
        private val _congratsMessage = MutableStateFlow<String?>(null)
        val congratsMessage: StateFlow<String?> = _congratsMessage.asStateFlow()

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

        private val _dashboardItems = MutableStateFlow<List<MilestoneDashboardItem>>(emptyList())
        val dashboardItems: StateFlow<List<MilestoneDashboardItem>> = _dashboardItems.asStateFlow()

        private var pendingRevealSfx = false

        fun loadDashboardItems(
            milestone: SagaMilestone,
            sagaId: Int,
        ) {
            pendingRevealSfx = milestone.shouldPlaySoundFx && milestone.playsRevealSfx
            _dashboardItems.value = dashboardMapper.toDashboardItems(milestone, sagaId)
        }

        fun onRevealStarted() {
            if (pendingRevealSfx) {
                sagaThemeManager.playVfx()
                pendingRevealSfx = false
        }
    }

        fun clear() {
            _congratsMessage.value = null
            _isLoading.value = false
            _dashboardItems.value = emptyList()
            pendingRevealSfx = false
        }
    }
