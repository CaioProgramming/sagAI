package com.ilustris.sagai.features.settings.domain

data class StorageBreakdown(
    val cacheSize: Long,
    val sagaContentSize: Long,
    val otherSize: Long,
)
