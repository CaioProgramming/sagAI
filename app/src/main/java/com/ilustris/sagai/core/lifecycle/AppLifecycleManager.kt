package com.ilustris.sagai.core.lifecycle

import kotlinx.coroutines.flow.StateFlow

interface AppLifecycleManager {
    val isAppInForeground: StateFlow<Boolean>
}
