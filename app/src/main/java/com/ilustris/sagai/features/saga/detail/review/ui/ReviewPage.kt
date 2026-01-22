package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface ReviewPage {
    @Composable
    fun Show(
        modifier: Modifier,
        onAction: (ReviewAction) -> Unit = {},
    )
}
