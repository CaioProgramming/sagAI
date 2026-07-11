package com.ilustris.sagai.core.globalshell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface OverlayVisibilityTrackerEntryPoint {
    fun overlayVisibilityTracker(): OverlayVisibilityTracker
}

@Composable
fun rememberOverlayVisibilityTracker(): OverlayVisibilityTracker {
    val context = LocalContext.current
    return remember(context) {
        val entryPoint =
            EntryPointAccessors.fromApplication(context, OverlayVisibilityTrackerEntryPoint::class.java)
        entryPoint.overlayVisibilityTracker()
    }
}

