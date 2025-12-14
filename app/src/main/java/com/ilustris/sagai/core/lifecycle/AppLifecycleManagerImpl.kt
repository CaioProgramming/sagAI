package com.ilustris.sagai.core.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLifecycleManagerImpl
    @Inject
    constructor() :
    AppLifecycleManager,
        DefaultLifecycleObserver {
        private val _isAppInForeground = MutableStateFlow(false)
        override val isAppInForeground: StateFlow<Boolean> = _isAppInForeground

        init {
            // This observer will be added to the ProcessLifecycleOwner's lifecycle.
            // It's important that ProcessLifecycleOwner.get().lifecycle.addObserver(this)
            // is called, which happens here in the init block.
            // Hilt will ensure this @Singleton is constructed appropriately.
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }

        override fun onStart(owner: LifecycleOwner) {
            _isAppInForeground.value = true
        }

        override fun onStop(owner: LifecycleOwner) {
            _isAppInForeground.value = false
        }

        override fun onPause(owner: LifecycleOwner) {
            _isAppInForeground.value = false
        }
    }
