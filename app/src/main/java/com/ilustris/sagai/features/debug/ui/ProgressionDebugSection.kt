package com.ilustris.sagai.features.debug.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.debug.presentation.ProgressionRun
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.progression.domain.CharacterLedger
import com.ilustris.sagai.ui.animations.genreVfx
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill

@Composable
private fun tierColor(tier: Int): Color =
    when (tier) {
        0, 1 -> MaterialTheme.colorScheme.outline
        2 -> MaterialTheme.colorScheme.secondary
        3 -> MaterialTheme.colorScheme.primary
        4 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

/** The progression tools of the lore debug screen: what the ledger holds and the four actions on it. */
@Composable
fun ProgressionSection(
    ledger: List<CharacterLedger>,
    enabled: Boolean,
    onGenerate: () -> Unit,
    onRecompute: () -> Unit,
    onShowLedger: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val feats = ledger.flatMap { it.feats }
    val placed = ledger.count { it.baseLevel != null }
    val strongest = ledger.filter { it.level != null }.maxByOrNull { it.level ?: 0 }
    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .35f),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Progressão de personagens",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                if (feats.isEmpty() && placed == 0) {
                    "Nada gerado ainda nesta saga."
                } else {
                    "${feats.size} feitos · $placed de ${ledger.size} personagens colocados" +
                        (strongest?.let { " · mais forte: ${it.name} (nível ${it.level})" } ?: "")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .7f),
            )
            if (feats.isNotEmpty()) TierDistribution(feats.map { it.tier })

            Button(onClick = onGenerate, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                Text("Gerar progressão")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onRecompute, enabled = enabled && feats.isNotEmpty(), modifier = Modifier.weight(1f)) {
                    Text("Recalcular XP")
                }
                OutlinedButton(onClick = onShowLedger, modifier = Modifier.weight(1f)) { Text("Ver ledger") }
            }
            TextButton(
                onClick = onReset,
                enabled = enabled && (feats.isNotEmpty() || placed > 0),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.align(Alignment.End),
            ) { Text("Zerar progressão") }
        }
    }
}

/** One bar per tier, so an inflated or empty scale is visible at a glance. */
@Composable
private fun TierDistribution(tiers: List<Int>) {
    val counts = (0..5).map { tier -> tiers.count { it == tier } }
    val max = counts.max().coerceAtLeast(1)
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        counts.forEachIndexed { tier, count ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$count", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .8f))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height((4 + 30 * count / max).dp)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(tierColor(tier).copy(alpha = if (count == 0) .2f else .9f)),
                )
                Text("T$tier", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f))
            }
        }
    }
}

/** Full-screen loading of a progression run: the step it is on and, below, what it has changed so far. */
@Composable
fun ProgressionRunOverlay(
    run: ProgressionRun,
    genre: Genre,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = .94f))
            .clickable(enabled = false) {}
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(horizontal = 28.dp),
        ) {
            Icon(
                painterResource(genre.icon),
                contentDescription = null,
                modifier = Modifier.size(56.dp).gradientFill(genre.gradient()).genreVfx(genre),
            )
            Text(
                run.stage,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            if (run.item.isNotBlank()) {
                Text(
                    run.item,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            LinearProgressIndicator(
                progress = { if (run.total == 0) 0f else run.done.toFloat() / run.total },
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = .2f),
            )
            Text(
                "${run.done} de ${run.total}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .6f),
            )
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                run.feed.forEachIndexed { index, line ->
                    Row(
                        modifier = Modifier.alpha((1f - index * .11f).coerceAtLeast(.25f)),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(Modifier.padding(top = 6.dp).size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                        Text(line, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
        }
    }
}

/** The ledger by character, strongest first; tap a character for the feats and honors behind the level. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerSheet(
    ledger: List<CharacterLedger>,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        val sorted = remember(ledger) { ledger.sortedByDescending { it.level ?: 0 } }
        LazyColumn(
            modifier = Modifier.navigationBarsPadding().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text("Ledger de progressão", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
            }
            items(sorted, key = { it.name }) { LedgerCharacter(it) }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun LedgerCharacter(entry: CharacterLedger) {
    var expanded by rememberSaveable(entry.name) { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .4f),
    ) {
        Column(Modifier.padding(14.dp).animateContentSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(entry.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(
                    if (entry.baseLevel == null) "sem colocação" else "${entry.baseLevel} → ${entry.level}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                "${entry.earnedXp} XP · ${entry.feats.size} feitos · ${entry.honors.size} honras",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .65f),
            )
            if (expanded) {
                entry.honors.forEach {
                    Text(
                        "${if (it.kind.name == "MEDAL") "🏅" else "👑"} ${it.emoji.orEmpty()} ${it.name}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                entry.feats.forEach { feat ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                        Text(
                            "T${feat.tier}",
                            style = MaterialTheme.typography.labelSmall,
                            color = tierColor(feat.tier),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(22.dp),
                        )
                        Text(
                            "${feat.title} · ${feat.kind.lowercase().replace('_', ' ')} · +${feat.xp}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}
