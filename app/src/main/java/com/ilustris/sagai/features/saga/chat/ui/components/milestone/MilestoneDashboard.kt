package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.annotation.DrawableRes
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone

enum class MilestoneCardKind {
    Stat,
    Narrative,
    Continuity,
    Emotional,
}

data class MilestoneDashboardItem(
    val title: String,
    val subtitle: String,
    @DrawableRes
    val iconRes: Int = R.drawable.ic_spark,
    val value: String? = null,
    val fullWidth: Boolean = false,
    val content: String? = null,
    val displayContent: Map<String, String> = emptyMap(),
    val kind: MilestoneCardKind = MilestoneCardKind.Stat,
    val detailHint: String? = null,
    val detailAction: MilestoneDetailAction? = null,
    val chipCharacters: List<Character> = emptyList(),
    val emotionBreakdown: List<Pair<EmotionalTone, Int>> = emptyList(),
)
