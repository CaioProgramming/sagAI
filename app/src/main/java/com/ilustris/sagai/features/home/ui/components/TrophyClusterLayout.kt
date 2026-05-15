package com.ilustris.sagai.features.home.ui.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal const val TROPHY_COLUMNS = 3
internal const val TROPHY_MAX_VISIBLE = 9

private val yJitterPattern =
    listOf(
        0.dp,
        6.dp,
        2.dp,
        8.dp,
        4.dp,
        10.dp,
    )

internal fun yJitterForIndex(index: Int): Dp = yJitterPattern[index % yJitterPattern.size]
