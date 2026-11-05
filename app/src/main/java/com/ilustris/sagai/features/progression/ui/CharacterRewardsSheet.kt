package com.ilustris.sagai.features.progression.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.navigation.SagaNavigationTracker
import com.ilustris.sagai.core.navigation.belongsToSagaFlow
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.progression.domain.CharacterRewards
import com.ilustris.sagai.features.progression.domain.CharacterRewardsService
import com.ilustris.sagai.features.progression.domain.RewardStep
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.gradient
import kotlinx.coroutines.delay

private const val STEP_ENTER_MS = 450
private const val STEP_EXIT_MS = 260

/**
 * The flow that tells the player what their characters earned, one page per achievement. It comes up only while the
 * player is inside that saga's own screens; elsewhere the rewards wait untold. [blocked] holds it back while something
 * else owns the stage (a milestone reveal).
 */
@Composable
fun CharacterRewardsHost(
    service: CharacterRewardsService,
    navigationTracker: SagaNavigationTracker,
    blocked: Boolean,
    onOpenCharacter: (Int) -> Unit,
) {
    val rewards by service.pending.collectAsState()
    val key by navigationTracker.currentKey.collectAsState()

    // Kept after the flow is finished so the sheet can play its exit instead of vanishing.
    var shown by remember { mutableStateOf<CharacterRewards?>(null) }
    LaunchedEffect(rewards) { if (rewards != null) shown = rewards }

    val pending = rewards
    val visible = pending != null && !blocked && key.belongsToSagaFlow(pending.sagaId, pending.characters.keys)
    LaunchedEffect(visible) { if (visible) service.markStarted() }

    val current = shown ?: return
    RewardsOverlay(current, visible, onFinish = service::finish, onOpenCharacter = onOpenCharacter)
}

/** Scrim and sheet for one flow. Shared by the host and the design-system preview, which has no service behind it. */
@Composable
fun RewardsOverlay(
    current: CharacterRewards,
    visible: Boolean,
    onFinish: () -> Unit,
    onOpenCharacter: (Int) -> Unit,
) {
    var index by remember(current) { mutableIntStateOf(0) }
    val steps = current.steps
    val step = steps[index.coerceIn(0, steps.lastIndex)]

    BackHandler(enabled = visible) { onFinish() }

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(320)),
            exit = fadeOut(tween(260, delayMillis = 60)),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = .6f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
            )
        }
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(tween(STEP_ENTER_MS + 70, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(260)),
            exit = slideOutVertically(tween(320, easing = FastOutLinearInEasing)) { it } + fadeOut(tween(240, delayMillis = 80)),
        ) {
            SagAITheme(current.genre) {
                // No Surface here, so text without an explicit color would fall back to black.
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
                    RewardsSheet(
                        rewards = current,
                        index = index,
                        step = step,
                        onContinue = { if (index >= steps.lastIndex) onFinish() else index++ },
                        onClose = onFinish,
                        onOpenCharacter = { id ->
                            onFinish()
                            onOpenCharacter(id)
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun RewardsSheet(
    rewards: CharacterRewards,
    index: Int,
    step: RewardStep,
    onContinue: () -> Unit,
    onClose: () -> Unit,
    onOpenCharacter: (Int) -> Unit,
) {
    val genre = rewards.genre
    val steps = rewards.steps
    val last = index >= steps.lastIndex
    val shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(.94f)
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary
                                .copy(alpha = .28f)
                                .compositeOver(MaterialTheme.colorScheme.background),
                            MaterialTheme.colorScheme.background,
                        ),
                    ),
                ).windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProgressSegments(steps.size, index, genre)
        AnimatedContent(
            targetState = step,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center,
            transitionSpec = {
                (
                    fadeIn(tween(STEP_ENTER_MS, delayMillis = STEP_EXIT_MS / 2, easing = EaseOutCubic)) +
                        slideInVertically(tween(STEP_ENTER_MS, delayMillis = STEP_EXIT_MS / 2, easing = EaseOutCubic)) { it / 8 } +
                        scaleIn(tween(STEP_ENTER_MS, delayMillis = STEP_EXIT_MS / 2), initialScale = .94f)
                ).togetherWith(
                    fadeOut(tween(STEP_EXIT_MS, easing = EaseInCubic)) +
                        slideOutVertically(tween(STEP_EXIT_MS, easing = EaseInCubic)) { -it / 10 } +
                        scaleOut(tween(STEP_EXIT_MS), targetScale = .96f),
                ).using(SizeTransform(clip = false))
            },
            label = "reward-step",
        ) { target ->
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                StepBody(target, rewards, onOpenCharacter)
            }
        }
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
        ) {
            AnimatedContent(last, transitionSpec = { fadeIn(tween(220)).togetherWith(fadeOut(tween(160))) }, label = "continue-label") { isLast ->
                Text(stringResource(if (isLast) R.string.progression_rewards_done else R.string.progression_rewards_continue))
            }
        }
        AnimatedVisibility(
            visible = !last,
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(180)),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            TextButton(onClick = onClose) {
                Text(stringResource(R.string.progression_rewards_skip), color = MaterialTheme.colorScheme.onBackground.copy(alpha = .6f))
            }
        }
    }
}

@Composable
private fun ProgressSegments(
    count: Int,
    index: Int,
    genre: Genre,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
        repeat(count) { i ->
            val fill by animateFloatAsState(if (i <= index) 1f else 0f, tween(500, easing = FastOutSlowInEasing), label = "segment")
            Box(
                Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = .16f)),
            ) {
                Box(Modifier.fillMaxWidth(fill).height(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
            }
        }
    }
}

// Items enter in a short cascade and leave in the same order, so a page comes and goes as one gesture.
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedContentScope.Reveal(
    order: Int,
    modifier: Modifier = Modifier,
    bouncy: Boolean = false,
    content: @Composable () -> Unit,
) {
    val delay = 90 * order
    Box(
        modifier =
            modifier.animateEnterExit(
                enter =
                    fadeIn(tween(380, delayMillis = STEP_EXIT_MS / 2 + delay)) +
                        if (bouncy) {
                            scaleIn(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), initialScale = .3f)
                        } else {
                            slideInVertically(tween(420, delayMillis = STEP_EXIT_MS / 2 + delay, easing = EaseOutCubic)) { it / 3 }
                        },
                exit = fadeOut(tween(180, delayMillis = 30 * order)) + slideOutVertically(tween(220, delayMillis = 30 * order)) { -it / 5 },
            ),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedContentScope.StepBody(
    step: RewardStep,
    rewards: CharacterRewards,
    onOpenCharacter: (Int) -> Unit,
) {
    val genre = rewards.genre
    fun character(id: Int): Character? = rewards.characters[id]
    when (step) {
        is RewardStep.LevelUp -> {
            val who = character(step.characterId) ?: return
            Hero(who, genre, order = 0)
            Label(stringResource(R.string.progression_new_level), order = 2)
            Reveal(3) { LevelNumber(step.from, step.to, genre) }
            Reveal(4) {
                Text(
                    stringResource(R.string.progression_level_change, step.from, step.to),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = .65f),
                )
            }
            if (step.featTitle.isNotBlank()) {
                Reveal(5, Modifier.padding(top = 12.dp)) { FeatLine(step.featTitle, step.xp) }
            }
            if (step.explainer) {
                Reveal(6, Modifier.padding(top = 12.dp)) {
                    Text(
                        stringResource(R.string.progression_explainer),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = .65f),
                    )
                }
            }
        }

        is RewardStep.Medal -> {
            val who = character(step.characterId) ?: return
            Hero(who, genre, order = 0)
            Label(stringResource(R.string.progression_new_medal), order = 2)
            Reveal(3, bouncy = true) { MedalCoin(step.emoji, genre, size = 76.dp) }
            Reveal(4, Modifier.padding(top = 8.dp)) { RewardTitle(step.name, genre) }
            Reveal(5) { Detail(step.detail) }
        }

        is RewardStep.Legacy -> {
            val who = character(step.characterId) ?: return
            Hero(who, genre, order = 0, memorial = step.memorial)
            Label(stringResource(R.string.progression_legacy_gained), order = 2)
            Reveal(3, bouncy = true) { LegacyBadge3D(genre, size = 84.dp) }
            Reveal(4, Modifier.padding(top = 8.dp)) { RewardTitle(step.name, genre) }
            Reveal(5) { Detail(step.detail, italic = true) }
        }

        is RewardStep.Relationship -> {
            val one = character(step.characterOneId) ?: return
            val two = character(step.characterTwoId) ?: return
            Reveal(0) {
                Box(contentAlignment = Alignment.Center) {
                    Row {
                        RewardAvatar(one, genre, 112.dp, Modifier.offset(x = 14.dp))
                        RewardAvatar(two, genre, 112.dp, Modifier.offset(x = (-14).dp))
                    }
                    Text(
                        step.emoji,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.background(MaterialTheme.colorScheme.background, CircleShape).padding(8.dp),
                    )
                }
            }
            Reveal(1, Modifier.padding(top = 8.dp)) {
                Text("${one.name} & ${two.name}", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            }
            Label(stringResource(R.string.progression_new_bond), order = 2)
            Reveal(4, Modifier.padding(top = 8.dp)) { RewardTitle(step.title, genre) }
            Reveal(5) { Detail(step.description) }
        }

        is RewardStep.Summary -> {
            Label(stringResource(R.string.progression_rewards_caught_up), order = 0)
            Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                step.rows.forEachIndexed { i, row ->
                    val who = character(row.characterId) ?: return@forEachIndexed
                    Reveal(i + 1, Modifier.fillMaxWidth()) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = .07f))
                                    .clickable { onOpenCharacter(row.characterId) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CharacterAvatar(who, genre = genre, innerPadding = 0.dp, borderSize = 1.dp, modifier = Modifier.size(48.dp))
                            Column(Modifier.weight(1f)) {
                                Text(who.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(summaryLine(row), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = .65f))
                            }
                            Icon(painterResource(R.drawable.ic_chevron_right), null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onBackground.copy(alpha = .4f))
                        }
                    }
                }
            }
            Reveal(step.rows.size + 1, Modifier.padding(top = 12.dp)) {
                Text(
                    stringResource(R.string.progression_rewards_open_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = .55f),
                )
            }
        }
    }
}

@Composable
private fun summaryLine(row: com.ilustris.sagai.features.progression.domain.SummaryRow): String {
    val parts = mutableListOf<String>()
    row.level?.let { parts += stringResource(R.string.progression_level_pill, it) }
    if (row.medals > 0) parts += pluralStringResource(R.plurals.progression_summary_medals, row.medals, row.medals)
    if (row.legacies > 0) parts += stringResource(R.string.progression_legacy_title)
    if (row.relationships > 0) parts += pluralStringResource(R.plurals.progression_summary_bonds, row.relationships, row.relationships)
    return parts.joinToString(" · ")
}

/** The character comes first and is the largest thing on the page: the reward is what happened to them. */
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedContentScope.Hero(
    character: Character,
    genre: Genre,
    order: Int,
    memorial: Boolean = false,
) {
    Reveal(order, bouncy = true) { RewardAvatar(character, genre, 136.dp, memorial = memorial) }
    Reveal(order + 1, Modifier.padding(top = 8.dp)) {
        Text(character.name, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
    }
}

@Composable
private fun RewardAvatar(
    character: Character,
    genre: Genre,
    size: Dp,
    modifier: Modifier = Modifier,
    memorial: Boolean = false,
) {
    CharacterAvatar(
        character = character,
        genre = genre,
        innerPadding = 0.dp,
        borderSize = 2.dp,
        modifier = modifier.size(size).let { if (memorial) it.desaturated(.15f) else it },
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedContentScope.Label(
    text: String,
    order: Int,
) {
    Reveal(order, Modifier.padding(top = 10.dp)) {
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f),
        )
    }
}

/** The new level counts up from the old one a moment after the page settles. */
@Composable
private fun LevelNumber(
    from: Int,
    to: Int,
    genre: Genre,
) {
    var shown by remember { mutableIntStateOf(from) }
    LaunchedEffect(to) {
        delay(750)
        shown = to
    }
    AnimatedContent(
        targetState = shown,
        transitionSpec = {
            (slideInVertically(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow)) { it } + fadeIn(tween(200)) + scaleIn(initialScale = .6f))
                .togetherWith(slideOutVertically(tween(220)) { -it } + fadeOut(tween(160)))
        },
        label = "level-number",
    ) { value ->
        Text(
            "$value",
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold, brush = genre.gradient()),
        )
    }
}

@Composable
private fun RewardTitle(
    text: String,
    genre: Genre,
) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.titleLarge.copy(fontFamily = MaterialTheme.typography.headlineSmall.fontFamily, brush = genre.gradient()),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun Detail(
    text: String,
    italic: Boolean = false,
) {
    if (text.isBlank()) return
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = .85f),
    )
}

@Composable
private fun FeatLine(
    title: String,
    xp: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.onBackground.copy(alpha = .08f)).padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(stringResource(R.string.character_card_xp_chip, xp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
    }
}
