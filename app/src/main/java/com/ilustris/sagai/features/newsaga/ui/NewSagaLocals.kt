package com.ilustris.sagai.features.newsaga.ui

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.staticCompositionLocalOf
import com.ilustris.sagai.features.newsaga.data.model.GenderPlaceholderMap

val LocalSharedTransitionScope =
    staticCompositionLocalOf<SharedTransitionScope?> {
        null
    }

val LocalGenderPlaceholders =
    staticCompositionLocalOf<GenderPlaceholderMap> {
        emptyMap()
    }
