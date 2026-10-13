package com.ilustris.sagai.ui.components.island

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.globalshell.ProgressionEffect
import com.ilustris.sagai.core.globalshell.ProgressionEffectKind
import com.ilustris.sagai.core.globalshell.ProgressionRow
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.progression.ui.LegacyBadge3D
import com.ilustris.sagai.features.progression.ui.MedalCoin
import com.ilustris.sagai.features.progression.ui.desaturated
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.hexToColor

private const val REVEAL_AUTO_EXPAND_MS = 1_000L

/**
 * The top island for level-ups, medals and legacies, after the prototype: a capsule with the character's avatar, a short
 * label and an indicator at the end (an arrow, the medal coin, the mini legacy badge); several characters at once share a
 * generic header with no avatar and nobody highlighted. Level-ups stay light and wait for a tap; medals and legacies are
 * reveals of their own and open by themselves.
 */
class ProgressionIslandContent(
    private val effect: ProgressionEffect,
    context: Context,
    private val onNavigate: (String) -> Unit,
    private val onDismiss: () -> Unit,
) : IslandContent {
    private val genre = effect.genre
    private val rows = effect.rows
    private val single = rows.singleOrNull()

    override val compact: CompactIslandData =
        CompactIslandData(
            label = compactLabel(context),
            genre = genre,
            backgroundColor = IslandBackgroundColor.ThemeBackground,
            leading = {
                if (single != null) {
                    CharacterAvatar(
                        character = single.character,
                        genre = genre,
                        innerPadding = 0.dp,
                        borderSize = 1.dp,
                        modifier = Modifier.size(26.dp),
                    )
                } else {
                    Icon(painterResource(R.drawable.ic_spark), null, Modifier.size(18.dp).gradientFill(genre.gradient()))
                }
            },
            trailing = {
                when (effect.kind) {
                    ProgressionEffectKind.LEVEL_UP ->
                        Icon(painterResource(R.drawable.ic_arrow_up), null, Modifier.size(20.dp).gradientFill(genre.gradient()))

                    ProgressionEffectKind.MEDAL -> rows.firstOrNull()?.let { MedalCoin(it.emoji, genre, size = 26.dp) }

                    ProgressionEffectKind.LEGACY -> LegacyBadge3D(genre, size = 28.dp, levitate = false)
                }
            },
        )

    override val autoExpandAfterMs: Long? = if (effect.kind == ProgressionEffectKind.LEVEL_UP) null else REVEAL_AUTO_EXPAND_MS

    private fun compactLabel(context: Context): String =
        when (effect.kind) {
            ProgressionEffectKind.LEVEL_UP ->
                single?.let { "${it.character.name} · ${context.getString(R.string.progression_level_pill, it.level ?: 0)}" }
                    ?: context.getString(R.string.progression_new_level)

            ProgressionEffectKind.MEDAL ->
                single?.let {
                    if (it.extra > 0) {
                        context.getString(R.string.progression_medals_n, it.character.name, it.extra + 1)
                    } else {
                        context.getString(R.string.progression_medal_single, it.character.name)
                    }
                } ?: context.getString(R.string.progression_medals_gained)

            ProgressionEffectKind.LEGACY ->
                if (single != null) context.getString(R.string.progression_legacy_gained) else context.getString(R.string.progression_legacies_gained)
        }

    @Composable
    override fun Expanded(scope: IslandScope) {
        SagAITheme(genre) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                when (effect.kind) {
                    ProgressionEffectKind.LEVEL_UP -> LevelUpBody()
                    ProgressionEffectKind.MEDAL -> MedalsBody()
                    ProgressionEffectKind.LEGACY -> LegacyBody()
                }
                // Flat, in the theme's primary only, so it never competes with the medals and emblems.
                Button(
                    onClick = {
                        onNavigate(effect.moreDeepLink)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                ) { Text(stringResource(R.string.notification_progression_see_more)) }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.progression_close), color = MaterialTheme.colorScheme.onBackground.copy(alpha = .6f))
                }
            }
        }
    }

    @Composable
    private fun LevelUpBody() {
        if (single != null) {
            Text(
                "${single.level ?: ""}",
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold, brush = genre.gradient()),
            )
            Text(stringResource(R.string.progression_level_up_caption), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = .85f))
            if (single.title.isNotBlank()) FeatLine(single.title, single.xp)
            Explainer()
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.onBackground.copy(alpha = .06f)).padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        CharacterAvatar(row.character, genre = genre, innerPadding = 0.dp, borderSize = 1.dp, modifier = Modifier.size(30.dp))
                        Column(Modifier.weight(1f)) {
                            Text(row.character.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (row.title.isNotBlank()) {
                                Text(row.title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = .65f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        Text(
                            stringResource(R.string.progression_level_pill, row.level ?: 0),
                            style = MaterialTheme.typography.titleSmall.copy(brush = genre.gradient()),
                        )
                        Icon(painterResource(R.drawable.ic_arrow_up), null, Modifier.size(16.dp).gradientFill(genre.gradient()))
                    }
                }
                Explainer()
            }
        }
    }

    /** What levels are and where to read the details; shown once, with the first level-up the player ever sees. */
    @Composable
    private fun Explainer() {
        if (!effect.showExplainer) return
        Text(
            stringResource(R.string.progression_explainer),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .65f),
            modifier = Modifier.padding(top = 6.dp),
        )
    }

    @Composable
    private fun FeatLine(
        title: String,
        xp: Int,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.onBackground.copy(alpha = .07f)).padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Text(stringResource(R.string.character_card_xp_chip, xp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    }

    @Composable
    private fun MedalsBody() {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            rows.forEach { row ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = .06f))
                            .clickable {
                                onNavigate("saga://character_detail/${row.character.id}")
                                onDismiss()
                            }.padding(horizontal = 8.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column(Modifier.width(56.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        CharacterAvatar(row.character, genre = genre, innerPadding = 0.dp, borderSize = 1.dp, modifier = Modifier.size(36.dp))
                        Text(row.character.name, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    MedalCoin(row.emoji, genre, size = 34.dp)
                    Column(Modifier.weight(1f)) {
                        Text(row.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(row.detail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    if (row.extra > 0) Text("+${row.extra}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f))
                    Icon(painterResource(R.drawable.round_arrow_forward_ios_24), null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onBackground.copy(alpha = .4f))
                }
            }
        }
    }

    @Composable
    private fun LegacyBody() {
        val pager = rememberPagerState { rows.size }
        HorizontalPager(pager, modifier = Modifier.fillMaxWidth()) { page ->
            val row = rows[page]
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LegacyBadge3D(genre, size = 92.dp)
                // Who earned it, right under the emblem; a character who is gone is shown in silver.
                CharacterAvatar(row.character, genre = genre, innerPadding = 0.dp, borderSize = 1.dp, modifier = Modifier.size(40.dp).let { if (row.memorial) it.desaturated(.15f) else it })
                Text(row.character.name, style = MaterialTheme.typography.bodyMedium)
                Text(
                    row.title.uppercase(),
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = MaterialTheme.typography.headlineSmall.fontFamily, brush = genre.gradient()),
                    textAlign = TextAlign.Center,
                )
                Text(row.detail, style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground.copy(alpha = .88f))
            }
        }
        if (rows.size > 1) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(rows.size) { i ->
                    val current = i == pager.currentPage
                    Icon(
                        painterResource(R.drawable.ic_spark),
                        null,
                        Modifier.size(if (current) 17.dp else 12.dp).alpha(if (current) 1f else .35f).let { if (current) it.gradientFill(genre.gradient()) else it },
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
    }
}
