package com.ilustris.sagai.core.globalshell

import com.ilustris.sagai.core.lifecycle.AppLifecycleManager
import com.ilustris.sagai.core.navigation.SagaNavigationTracker
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.saga.chat.data.manager.ChatNotificationManager
import com.ilustris.sagai.features.settings.domain.SettingsUseCase
import com.ilustris.sagai.ui.components.island.ChatIslandService
import com.ilustris.sagai.ui.components.island.IntroductionIslandContent
import com.ilustris.sagai.ui.components.island.IslandContent
import com.ilustris.sagai.ui.components.island.NarrativeMilestoneIslandContent
import com.ilustris.sagai.ui.components.SagaNotificationEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class GlobalShellService
    @Inject
    constructor(
        private val settingsUseCaseProvider: Provider<SettingsUseCase>,
        private val chatNotificationManager: ChatNotificationManager,
        private val appLifecycleManager: AppLifecycleManager,
        private val navigationTracker: SagaNavigationTracker,
        private val overlayVisibilityTracker: OverlayVisibilityTracker,
        private val chatIslandService: ChatIslandService,
    ) {
        companion object {
            private val AutoDismissSeconds = 15.seconds
            private const val MILESTONE_POLL_MS = 1_000L
            private const val MILESTONE_WAIT_MS = 90_000L
        }

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

        private val _uiState =
            MutableStateFlow<GlobalShellUiState>(GlobalShellUiState(effect = null))
        val uiState: StateFlow<GlobalShellUiState> = _uiState.asStateFlow()

        /**
         * Queues at most one transient effect while a persistent work is active.
         */
        private var pendingTransient: GlobalShellEffect? = null
        private var autoDismissJob: Job? = null

        fun setExpansion(expansion: GlobalShellExpansion) {
            _uiState.update { current ->
                if (current.effect == null) current else current.copy(expansion = expansion)
            }
        }

        fun post(effect: GlobalShellEffect) {
            scope.launch {
                handlePost(effect)
            }
        }

        private suspend fun handlePost(effect: GlobalShellEffect) {
            // Only effects that can turn into a system notification are tied to its permission; the progression
            // announcements live in the island and answer to the "Levels and experience" setting instead.
            if (effect.priority == GlobalShellPriority.Transient && effect.deliversSystemNotification) {
                if (!settingsUseCaseProvider.get().getNotificationsEnabled().first()) {
                    Timber.d("GlobalShellService: transient suppressed (notifications disabled)")
                    return
                }
            }

            if (effect is ProgressionEffect && !settingsUseCaseProvider.get().getShowProgression().first()) {
                Timber.d("GlobalShellService: progression effect suppressed (setting off)")
                return
            }

            // A level-up, medal or legacy must not pre-empt a milestone that is still open (a transient wins the top slot
            // over it): wait for the milestone to be dismissed, so the announcements queue behind it one at a time.
            if (effect is ProgressionEffect) {
                var waitedMs = 0L
                while (chatIslandService.top.value.isMilestoneReveal() && waitedMs < MILESTONE_WAIT_MS) {
                    delay(MILESTONE_POLL_MS)
                    waitedMs += MILESTONE_POLL_MS
                }
            }

            val ctx = VisibilityContext()
            if (effect.shouldSuppress(ctx)) {
                Timber.d("GlobalShellService: suppressed by visibility policy: ${effect.id}")
                return
            }

            val inForeground = appLifecycleManager.isAppInForeground.value

            when (effect.priority) {
                GlobalShellPriority.PersistentWork -> {
                    cancelAutoDismiss()
                    // If a transient is currently visible, keep it queued and hidden while persistent is active.
                    val current = _uiState.value.effect
                    pendingTransient =
                        if (current != null && current.priority == GlobalShellPriority.Transient) {
                            current
                        } else {
                            pendingTransient
                        }
                    _uiState.update {
                        GlobalShellUiState(
                            effect = effect,
                            expansion = effect.defaultExpansion,
                        )
                    }
                }

                GlobalShellPriority.Transient -> {
                    if (_uiState.value.isPersistentWorkActive) {
                        // Sticky persistent work has priority; keep only the most recent transient.
                        pendingTransient = effect
                        Timber.d("GlobalShellService: queued transient over persistent: ${effect.id}")
                        return
                    }

                    if (!inForeground) {
                        if (effect.deliversSystemNotification) deliverSystemNotification(effect)
                        // Keep in-app hidden while background; will re-trigger on next foreground entry if needed.
                        dismissInternal(clearPending = true)
                        return
                    }

                    showTransient(effect)
                }
            }
        }

        fun dismiss() {
            scope.launch {
                dismissInternal(clearPending = false)
            }
        }

        private fun cancelAutoDismiss() {
            autoDismissJob?.cancel()
            autoDismissJob = null
        }

        private suspend fun dismissInternal(clearPending: Boolean) {
            cancelAutoDismiss()
            val hadEffect = _uiState.value.effect != null
            _uiState.update { GlobalShellUiState(effect = null) }
            if (clearPending) pendingTransient = null

            // Promote queued transient only when we actually cleared something.
            if (hadEffect && pendingTransient != null) {
                val next = pendingTransient
                pendingTransient = null
                // Re-apply suppression rules with the latest UI context to avoid stale overlays.
                val ctx = VisibilityContext()
                if (next != null && !next.shouldSuppress(ctx)) {
                    showTransient(next)
                } else {
                    pendingTransient = null
                }
            }
        }

        private fun showTransient(effect: GlobalShellEffect) {
            cancelAutoDismiss()
            _uiState.update { GlobalShellUiState(effect = effect, expansion = effect.defaultExpansion) }

            autoDismissJob =
                scope.launch {
                    delay(AutoDismissSeconds)
                    // Only dismiss if it's still the same effect instance.
                    if (_uiState.value.effect?.id == effect.id) {
                        dismissInternal(clearPending = false)
                    }
                }
        }

        private fun deliverSystemNotification(effect: GlobalShellEffect) {
            val sagaMetadata =
                SagaMetadata(
                    data = Saga(id = effect.sagaId, title = effect.sagaTitle, genre = effect.genre),
                )
            val sagaEvent =
                SagaNotificationEvent(
                    sagaId = effect.sagaId,
                    sagaTitle = effect.sagaTitle,
                    genre = effect.genre,
                    message = effect.message,
                    icon = effect.icon,
                    largeIcon = effect.largeIcon,
                    style = effect.notificationStyle,
                    deepLink = effect.deepLink,
                )

            Timber.i("GlobalShellService: delivering system notification (${effect.notificationStyle}) for ${effect.id}")
            chatNotificationManager.deliverSystemNotification(sagaMetadata, sagaEvent)
        }

        /**
         * Local visibility context wrapper.
         *
         * (We build it on demand to always use the latest navigation/overlay state.)
         */
        private inner class VisibilityContext : GlobalShellVisibilityContext {
            // Live mode is the same conversation: its messages, chapters and generation are on screen there too.
            override fun isOnChatForSaga(sagaId: Int): Boolean = navigationTracker.isInConversation(sagaId)

            override fun isOnSagaDetail(sagaId: Int): Boolean = navigationTracker.isOnSagaDetail(sagaId)

            override fun isOnCharacterDetail(characterId: Int): Boolean = navigationTracker.isOnCharacterDetail(characterId)

            override fun isOnBookReader(
                sagaId: Int,
                actId: Int,
            ): Boolean = navigationTracker.isOnBookReader(sagaId, actId)

            override fun isOnChronicle(sagaId: Int): Boolean = navigationTracker.isOnChronicle(sagaId)

            override fun isReviewVisibleForSaga(sagaId: Int): Boolean = overlayVisibilityTracker.isReviewVisibleForSaga(sagaId)
        }
    }

private fun IslandContent?.isMilestoneReveal(): Boolean = this is NarrativeMilestoneIslandContent || this is IntroductionIslandContent
