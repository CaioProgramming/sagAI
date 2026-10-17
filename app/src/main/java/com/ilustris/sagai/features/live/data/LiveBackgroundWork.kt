package com.ilustris.sagai.features.live.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Where live mode starts work that must outlive the screen: a reply's voicing that already began
 * keeps going after the player leaves, so the chat bubble still gets its audio. Playback never
 * runs here — it belongs to the live screen alone.
 */
@Singleton
class LiveBackgroundWork
    @Inject
    constructor() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
