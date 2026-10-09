package com.ilustris.sagai.features.characters.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.CharacterStatus
import com.ilustris.sagai.features.newsaga.ui.glassSurface
import com.ilustris.sagai.features.progression.data.model.CharacterFeat
import com.ilustris.sagai.features.progression.data.model.CharacterHonor
import com.ilustris.sagai.features.progression.domain.CharacterProgress
import com.ilustris.sagai.features.progression.ui.LegacyBadge3D
import com.ilustris.sagai.features.progression.ui.honorFill
import com.ilustris.sagai.features.progression.ui.honorFillBottom
import com.ilustris.sagai.features.progression.ui.MedalCoin
import com.ilustris.sagai.features.newsaga.data.model.Genre

private val CardShape = RoundedCornerShape(24.dp)

/** The level as a gold pill under the name (the genre's coin colors), with the character's situation beside it when it is not simply "around". */
@Composable
fun CharacterLevelRow(
    progress: CharacterProgress?,
    status: CharacterStatus?,
    statusNote: String?,
    contentColor: Color,
    genre: Genre,
    modifier: Modifier = Modifier,
) {
    val showStatus = status != null && status != CharacterStatus.ACTIVE
    if (progress == null && !showStatus) return
    Row(
        modifier = modifier.padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        progress?.let {
            val (top, engrave) = genre.honorFill()
            Text(
                text = stringResource(R.string.progression_level_pill, it.level),
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                fontWeight = FontWeight.ExtraBold,
                color = engrave,
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .background(Brush.verticalGradient(listOf(top, genre.honorFillBottom())))
                        .padding(horizontal = 12.dp, vertical = 2.dp),
            )
        }
        if (showStatus) {
            val label =
                stringResource(
                    when (status) {
                        CharacterStatus.DEAD -> R.string.progression_status_dead
                        CharacterStatus.MISSING -> R.string.progression_status_missing
                        else -> R.string.progression_status_absent
                    },
                )
            Text(
                text = label + (statusNote?.let { " · $it" }.orEmpty()),
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = .8f),
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .background(contentColor.copy(alpha = .1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
}

/**
 * The experience block as in the prototype: three sections in one glass card, no title. Medals (coins and "Ver mais"),
 * feats (level, XP bar and the latest feat with its XP) and messages. The legacy is the emblem above the name, not a row here.
 */
@Composable
fun CharacterExperienceCard(
    progress: CharacterProgress,
    messageCount: Int,
    accent: Color,
    contentColor: Color,
    genre: Genre,
    pendingGenerations: Int,
    isRetrying: Boolean,
    onRetry: () -> Unit,
    onSeeMore: () -> Unit,
    onOpenHonor: (honors: List<com.ilustris.sagai.features.progression.data.model.CharacterHonor>, index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gold = androidx.compose.ui.graphics.lerp(accent, Color.White, .5f)
    val latest = progress.feats.maxByOrNull { it.id }
    GlassRowsCard(accent, modifier) {
        RowSection(first = true, onClick = onSeeMore) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionLabel(stringResource(R.string.progression_medals).lowercase())
                val medals = progress.medals
                if (medals.isEmpty()) {
                    Text(stringResource(R.string.progression_no_medals), style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = .6f))
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy((-4).dp), verticalAlignment = Alignment.CenterVertically) {
                        medals.indices.reversed().take(3).forEach { index ->
                            MedalCoin(
                                emoji = medals[index].emoji,
                                genre = genre,
                                size = 40.dp,
                                modifier = Modifier.clickable { onOpenHonor(medals, index) },
                            )
                        }
                    }
                }
            }
            Text(stringResource(R.string.see_more), style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp), color = contentColor.copy(alpha = .75f))
            Chevron()
        }

        RowSection(first = false, onClick = onSeeMore) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionLabel(stringResource(R.string.progression_feats_level, progress.level))
                if (progress.levelSpan > 0) {
                    LinearProgressIndicator(
                        progress = { (progress.xpIntoLevel.toFloat() / progress.levelSpan).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = gold,
                        trackColor = Color.White.copy(alpha = .14f),
                        gapSize = 0.dp,
                        drawStopIndicator = {},
                    )
                }
                latest?.let {
                    Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionValue(it.title, Modifier.weight(1f), size = 16)
                        Text(stringResource(R.string.character_card_xp_chip, it.xp), style = MaterialTheme.typography.labelMedium, color = gold, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Chevron()
        }

        RowSection(first = false) {
            Column {
                SectionLabel(stringResource(R.string.progression_messages).lowercase())
                SectionValue(messageCount.toString())
            }
        }

        if (pendingGenerations > 0) {
            RowSection(first = false, onClick = if (isRetrying) null else onRetry) {
                Text(
                    if (isRetrying) stringResource(R.string.progression_retrying) else stringResource(R.string.progression_pending_retry, pendingGenerations),
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = .75f),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** Skills as chips, for sagas where the abilities text was replaced by a list. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CharacterSkillChips(
    skills: List<String>,
    accent: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    if (skills.isEmpty()) return
    CharacterDetailSection(
        title = stringResource(R.string.progression_skills_title),
        contentColor = contentColor,
        modifier = modifier,
    ) {
        FlowRow(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            skills.forEach {
                Text(
                    it,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                    modifier =
                        Modifier
                            .clip(CircleShape)
                            .background(accent.copy(alpha = .18f))
                            .border(1.dp, accent.copy(alpha = .4f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}

/** Every honor and the feat behind the level, newest first, in the same glass sheet as the rest of the screen. */
@Composable
fun CharacterFeatLogSheet(
    progress: CharacterProgress,
    accent: Color,
    genre: Genre,
    onDismiss: () -> Unit,
) {
    val gold = androidx.compose.ui.graphics.lerp(accent, Color.White, .5f)
    GlassSheet(stringResource(R.string.progression_feat_log_title), accent, onDismiss) {
        var first = true
        progress.rationale?.let {
            RowSection(first = true) { SectionLabel(it) }
            first = false
        }
        progress.honors.sortedByDescending { it.createdAt }.forEach { honor ->
            RowSection(first = first) {
                if (honor.kind.name == "MEDAL") MedalCoin(honor.emoji, genre, size = 40.dp) else LegacyBadge3D(genre, size = 40.dp, levitate = false)
                Column(Modifier.weight(1f)) {
                    SectionValue(honor.name, size = 16)
                    SectionLabel(honor.description)
                    if (honor.posthumous) Text(stringResource(R.string.progression_in_memory), style = MaterialTheme.typography.labelSmall, color = gold)
                }
            }
            first = false
        }
        progress.feats.sortedByDescending { it.id }.forEach { feat ->
            RowSection(first = first) {
                Text("T${feat.tier}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = gold, modifier = Modifier.width(26.dp))
                Column(Modifier.weight(1f)) {
                    SectionValue(feat.title, size = 16)
                    SectionLabel(feat.changed)
                }
                Text("+${feat.xp}", style = MaterialTheme.typography.labelMedium, color = gold)
            }
            first = false
        }
    }
}
