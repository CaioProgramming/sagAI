package com.ilustris.sagai.core.notifications

import android.graphics.Bitmap
import com.ilustris.sagai.features.newsaga.data.model.Genre

/** Heads-up style payload shown inside the app (WhatsApp-like), not a system notification. */
data class SagaInAppNotification(
    val sagaId: Int,
    val sagaTitle: String,
    val genre: Genre,
    val message: String,
    val deepLink: String,
    val icon: Bitmap? = null,
)
