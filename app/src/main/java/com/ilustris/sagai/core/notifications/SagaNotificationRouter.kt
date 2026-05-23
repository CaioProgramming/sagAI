package com.ilustris.sagai.core.notifications

import com.ilustris.sagai.core.lifecycle.AppLifecycleManager
import com.ilustris.sagai.core.navigation.SagaNavigationTracker
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.saga.chat.data.manager.ChatNotificationManager
import com.ilustris.sagai.features.saga.chat.data.manager.SagaContentManager
import com.ilustris.sagai.features.settings.domain.SettingsUseCase
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
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

/**
 * Routes saga events to in-app heads-up (foreground, outside chat) or system notifications (background).
 */
@Singleton
class SagaNotificationRouter
    @Inject
    constructor(
        private val sagaContentManager: SagaContentManager,
        private val chatNotificationManager: ChatNotificationManager,
        private val appLifecycleManager: AppLifecycleManager,
        private val navigationTracker: SagaNavigationTracker,
        private val settingsUseCase: SettingsUseCase,
    ) {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

        private val _inAppNotification = MutableStateFlow<SagaInAppNotification?>(null)
        val inAppNotification: StateFlow<SagaInAppNotification?> = _inAppNotification.asStateFlow()

        private var collectJob: Job? = null
        private var dismissJob: Job? = null
        private var started = false

        fun start() {
            if (started) return
            started = true
            collectJob =
                scope.launch {
                    sagaContentManager.notificationUpdate.collect { event ->
                        val payload = event ?: return@collect
                        if (!settingsUseCase.getNotificationsEnabled().first()) return@collect
                        deliver(payload)
                    }
                }
        }

        fun dismissInApp() {
            dismissJob?.cancel()
            _inAppNotification.value = null
        }

        private fun deliver(event: SagaNotificationEvent) {
            if (!shouldNotify(event.sagaId)) {
                Timber.d("SagaNotificationRouter: suppressed (user on chat for saga ${event.sagaId})")
                return
            }

            val deepLink = chatDeepLink(event.sagaId)
            val inForeground = appLifecycleManager.isAppInForeground.value

            if (inForeground) {
                showInApp(
                    SagaInAppNotification(
                        sagaId = event.sagaId,
                        sagaTitle = event.sagaTitle,
                        genre = event.genre,
                        message = event.message,
                        deepLink = deepLink,
                        icon = event.icon ?: event.largeIcon,
                    ),
                )
            } else {
                dismissInApp()
                val sagaMeta =
                    SagaMetadata(
                        data =
                            com.ilustris.sagai.features.home.data.model.Saga(
                                id = event.sagaId,
                                title = event.sagaTitle,
                                genre = event.genre,
                            ),
                    )
                chatNotificationManager.deliverSystemNotification(sagaMeta, event)
            }
        }

        private fun shouldNotify(sagaId: Int): Boolean {
            if (!appLifecycleManager.isAppInForeground.value) return true
            return !navigationTracker.isOnChatForSaga(sagaId)
        }

        private fun showInApp(notification: SagaInAppNotification) {
            _inAppNotification.value = notification
            dismissJob?.cancel()
            dismissJob =
                scope.launch {
                    delay(6.seconds)
                    if (_inAppNotification.value?.sagaId == notification.sagaId &&
                        _inAppNotification.value?.message == notification.message
                    ) {
                        _inAppNotification.value = null
                    }
                }
        }

        private fun chatDeepLink(sagaId: Int): String = "saga://chat/$sagaId/false"
    }
