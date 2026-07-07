package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.MilestoneTransitions
import com.ilustris.sagai.ui.theme.themeBubble
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private const val MILESTONE_GRID_MAX_LINES = 5
private val MILESTONE_CARD_REVEAL_DELAY = 160.milliseconds

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

@Composable
fun MilestoneScrollableReceipt(
    label: String,
    title: String,
    genre: Genre,
    items: List<MilestoneDashboardItem>,
    showContent: Boolean,
    showContinue: Boolean,
    onContinue: () -> Unit,
    sparkContent: @Composable () -> Unit,
    onDetailAction: (MilestoneDetailAction) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var revealedCards by remember { mutableIntStateOf(0) }
    var selectedItem by remember { mutableStateOf<MilestoneDashboardItem?>(null) }

    LaunchedEffect(showContent, items.size) {
        if (!showContent) {
            revealedCards = 0
            return@LaunchedEffect
        }
        revealedCards = 0
        items.indices.forEach { index ->
            delay(MILESTONE_CARD_REVEAL_DELAY)
            revealedCards = index + 1
        }
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp,
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                sparkContent()
                AnimatedVisibility(
                    visible = showContent,
                    enter = MilestoneTransitions.revealEnter,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        )
                        MilestoneStylisedTitle(
                            genre = genre,
                            text = title,
                            visible = true,
                        )
                    }
                }
            }
        }

        if (showContent) {
            itemsIndexed(
                items = items,
                key = { index, item -> "${item.subtitle}_$index" },
                span = { _, item ->
                    when {
                        item.fullWidth || item.kind == MilestoneCardKind.Narrative -> StaggeredGridItemSpan.FullLine
                        else -> StaggeredGridItemSpan.SingleLane
                    }
                },
            ) { index, item ->
                val isRevealed = index < revealedCards
                AnimatedVisibility(
                    visible = isRevealed,
                    enter = MilestoneTransitions.cardEnter(0),
                    modifier = Modifier.padding(horizontal = 4.dp),
                ) {
                    MilestoneDashboardCard(
                        item = item,
                        genre = genre,
                        onClick = { selectedItem = item },
                    )
                }
            }
        }

        item(span = StaggeredGridItemSpan.FullLine) {
            MilestoneContinueButton(
                genre = genre,
                visible = showContinue,
                onDismiss = onContinue,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp),
            )
        }
    }

    selectedItem?.let { item ->
        MilestoneDetailSheet(
            title = title,
            item = item,
            onDismiss = { selectedItem = null },
            onDetailAction = { action ->
                selectedItem = null
                onDetailAction(action)
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MilestoneDashboardCard(
    item: MilestoneDashboardItem,
    genre: Genre,
    onClick: () -> Unit,
) {
    val textColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(themeBubble())
                .background(MaterialTheme.colorScheme.primary)
                .background(MaterialTheme.colorScheme.background.copy(alpha = .3f))
                .clickable(onClick = onClick)
                .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                painter = painterResource(item.iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = item.subtitle,
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        color = textColor.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                    ),
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Icon(
                painterResource(R.drawable.ic_arrow_diagonal),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (item.kind) {
            MilestoneCardKind.Emotional -> {
                if (item.emotionBreakdown.isNotEmpty()) {
                    MilestoneEmotionPieChart(
                        breakdown = item.emotionBreakdown,
                        chartSize = 80.dp,
                    )
                    MilestoneEmotionMiniLegend(
                        breakdown = item.emotionBreakdown,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                item.content?.takeIf { it.isNotBlank() }?.let { body ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.85f),
                        maxLines = MILESTONE_GRID_MAX_LINES,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            MilestoneCardKind.Stat -> {
                item.value?.let { value ->
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall,
                        color = textColor,
                        fontWeight = FontWeight.Black,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (item.chipCharacters.isEmpty()) {
                    item.content?.takeIf { it.isNotBlank() }?.let { preview ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = preview,
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor.copy(alpha = 0.75f),
                            maxLines = MILESTONE_GRID_MAX_LINES,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            MilestoneCardKind.Continuity,
            MilestoneCardKind.Narrative,
            -> {
                item.value?.let { value ->
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                item.content?.let { body ->
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.85f),
                        maxLines = MILESTONE_GRID_MAX_LINES,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        if (item.chipCharacters.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                item.chipCharacters.take(4).forEach { character ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                    ) {
                        CharacterAvatar(
                            character = character,
                            genre = genre,
                            modifier = Modifier.size(22.dp),
                            innerPadding = 1.dp,
                            borderSize = 1.dp,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = character.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        } else if (item.displayContent.isNotEmpty() && item.kind == MilestoneCardKind.Stat) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                item.displayContent.keys.take(3).forEach { chip ->
                    Text(
                        text = chip,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier =
                            Modifier
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilestoneDetailSheet(
    title: String,
    item: MilestoneDashboardItem,
    onDismiss: () -> Unit,
    onDetailAction: (MilestoneDetailAction) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            if (item.emotionBreakdown.isNotEmpty()) {
                MilestoneEmotionPieChart(
                    breakdown = item.emotionBreakdown,
                    chartSize = 120.dp,
                    showLegend = true,
                    animate = false,
                )
            }

            val primaryBody = item.content ?: item.value
            if (!primaryBody.isNullOrBlank()) {
                Text(
                    text = primaryBody,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            item.displayContent.forEach { (sectionTitle, sectionContent) ->
                if (sectionContent.isNotBlank()) {
                    Spacer(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)),
                    )
                    Text(
                        text = sectionTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = sectionContent,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            item.detailAction?.let { action ->
                ElevatedButton(
                    onClick = { onDetailAction(action) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(action.label())
                }
            }

            item.detailHint?.takeIf { item.detailAction == null }?.let { hint ->
                Text(
                    text = hint,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MilestoneDashboardGrid(
    items: List<MilestoneDashboardItem>,
    genre: Genre,
    modifier: Modifier = Modifier,
    mainTitle: String,
    onContinue: () -> Unit = {},
) {
    MilestoneScrollableReceipt(
        label = "",
        title = mainTitle,
        genre = genre,
        items = items,
        showContent = true,
        showContinue = true,
        onContinue = onContinue,
        sparkContent = {},
        modifier = modifier,
    )
}
