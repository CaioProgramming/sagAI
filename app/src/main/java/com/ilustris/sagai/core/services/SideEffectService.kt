package com.ilustris.sagai.core.services

import com.ilustris.sagai.core.data.SideEffect
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SideEffectService
    @Inject
    constructor() {
        private val _sideEffects = MutableSharedFlow<SideEffect>()
        val sideEffects = _sideEffects.asSharedFlow()

        suspend fun emit(sideEffect: SideEffect) {
            _sideEffects.emit(sideEffect)
        }
    }
