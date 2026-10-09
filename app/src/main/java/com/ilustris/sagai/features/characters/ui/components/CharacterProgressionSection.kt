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
import com.ilustris.sagai.features.progression.ui.MedalCoin
import com.ilustris.sagai.features.newsaga.data.model.Genre

private val CardShape = RoundedCornerShape(24.dp)

/** Level and band next to the occupation, plus the character's situation when it is not simply "around". */
@Composable
fun CharacterLevelRow(
    progress: CharacterProgress?,
    status: CharacterStatus?,
    statusNote: String?,
    accent: Color,
    contentColor: Color,
    genre: Genre,
    onOpenLegacy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showStatus = status != null && status != CharacterStatus.ACTIVE
    if (progress == null && !showStatus) return
    Row(
        modifier = modifier.padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (progress?.titles?.isNotEmpty() == true) {
            LegacyBadge3D(genre = genre, size = 44.dp, modifier = Modifier.clickable(onClick = onOpenLegacy))
        }
        progress?.let {
            Text(
                text = stringResource(R.string.progression_level_pill, it.level) + if (it.bandLabel.isNotBlank()) " · ${it.bandLabel}" else "",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .background(accent.copy(alpha = .28f))
                        .border(1.dp, accent.copy(alpha = .6f), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
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

/** The experience card: medals, progress to the next level and the counters, with the feat log one tap away. */
@Composable
fun CharacterExperienceCard(
    progress: CharacterProgress,
    messageCount: Int,
    accent: Color,
    contentColor: Color,
    genre: Genre,
    onSeeMore: () -> Unit,
    onOpenHonor: (honors: List<com.ilustris.sagai.features.progression.data.model.CharacterHonor>, index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .glassSurface(CardShape, accent)
                .clickable(onClick = onSeeMore)
                .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            stringResource(R.string.progression_experience_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            val medals = progress.medals
            if (medals.isEmpty()) {
                Text(
                    stringResource(R.string.progression_no_medals),
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = .6f),
                )
            } else {
                medals.indices.reversed().take(3).forEach { index ->
                    MedalCoin(
                        emoji = medals[index].emoji,
                        genre = genre,
                        size = 44.dp,
                        modifier = Modifier.clickable { onOpenHonor(medals, index) },
                    )
                }
                if (medals.size > 3) {
                    Text("+${medals.size - 3}", style = MaterialTheme.typography.labelLarge, color = contentColor.copy(alpha = .8f))
                }
            }
        }

        if (progress.levelSpan > 0) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinearProgressIndicator(
                    progress = { (progress.xpIntoLevel.toFloat() / progress.levelSpan).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = accent,
                    trackColor = contentColor.copy(alpha = .12f),
                )
                Text(
                    stringResource(R.string.progression_xp_progress, progress.xpIntoLevel, progress.levelSpan),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = .65f),
                )
            }
        } else {
            Text(stringResource(R.string.progression_xp_max), style = MaterialTheme.typography.labelMedium, color = accent)
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Counter(progress.medals.size.toString(), stringResource(R.string.progression_medals), contentColor)
            Counter(progress.feats.size.toString(), stringResource(R.string.progression_feats), contentColor)
            Counter(messageCount.toString(), stringResource(R.string.progression_messages), contentColor)
        }

        progress.titles.forEachIndexed { index, title ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onOpenHonor(progress.titles, index) },
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LegacyBadge3D(genre = genre, size = 40.dp, levitate = false)
                Text(title.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = contentColor)
            }
        }

        Text(
            stringResource(R.string.progression_see_more_action),
            style = MaterialTheme.typography.labelLarge,
            color = accent,
            modifier = Modifier.align(Alignment.End),
        )
    }
}

@Composable
private fun Counter(
    value: String,
    label: String,
    contentColor: Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = contentColor)
        Text(label, style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = .6f))
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

/** Every honor and the feats behind the level, newest first: the answer to "why is this character this strong". */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterFeatLogSheet(
    progress: CharacterProgress,
    accent: Color,
    genre: Genre,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier.navigationBarsPadding().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(stringResource(R.string.progression_feat_log_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                progress.rationale?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .65f), modifier = Modifier.padding(top = 4.dp))
                }
            }
            items(progress.honors.sortedByDescending { it.createdAt }, key = { "h${it.id}" }) { HonorRow(it, accent, genre) }
            items(progress.feats.sortedByDescending { it.id }, key = { "f${it.id}" }) { FeatRow(it, accent) }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun HonorRow(
    honor: CharacterHonor,
    accent: Color,
    genre: Genre,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        if (honor.kind.name == "MEDAL") MedalCoin(honor.emoji, genre, size = 40.dp) else LegacyBadge3D(genre, size = 40.dp, levitate = false)
        Column {
            Text(honor.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(honor.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .7f))
            if (honor.posthumous) {
                Text(stringResource(R.string.progression_in_memory), style = MaterialTheme.typography.labelSmall, color = accent)
            }
        }
    }
}

@Composable
private fun FeatRow(
    feat: CharacterFeat,
    accent: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            "T${feat.tier}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = accent,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(28.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(feat.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(feat.changed, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .65f))
        }
        Text("+${feat.xp}", style = MaterialTheme.typography.labelMedium, color = accent)
    }
}
