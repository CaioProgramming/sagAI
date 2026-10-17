@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.widgets.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.CharacterDetailData
import com.ilustris.sagai.features.newsaga.ui.GlassButton
import com.ilustris.sagai.features.widgets.domain.LinesType
import com.ilustris.sagai.features.widgets.domain.RotationRule
import com.ilustris.sagai.features.widgets.domain.WidgetLimits
import kotlinx.coroutines.delay

/**
 * The widget creation flow, as a bottom sheet over the character screen: style, generating, pick
 * lines, rotation, add to the home screen.
 */
@Composable
fun WidgetCreatorSheet(
    detail: CharacterDetailData,
    onDismiss: () -> Unit,
    viewModel: WidgetCreatorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(detail.character.id) { viewModel.start(detail) }
    LaunchedEffect(state.done) { if (state.done) onDismiss() }
    DisposableEffect(Unit) { onDispose { viewModel.reset() } }

    val name = detail.character.name
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AnimatedContent(
                targetState = state.step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "widget_creator_step",
            ) { step ->
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    when (step) {
                        CreatorStep.STYLE -> StyleStep(name, state, viewModel)
                        CreatorStep.GENERATING -> GeneratingStep(name, state, viewModel)
                        CreatorStep.PICK -> PickStep(name, state, viewModel)
                        CreatorStep.OPTIONS -> OptionsStep(state, viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepHeader(
    title: String,
    onBack: (() -> Unit)?,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_back_left),
                    contentDescription = stringResource(R.string.back_button_description),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun StyleStep(
    name: String,
    state: CreatorState,
    viewModel: WidgetCreatorViewModel,
) {
    StepHeader(stringResource(R.string.widget_create_style_title), onBack = null)
    Text(
        text = stringResource(R.string.widget_create_style_subtitle, name),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
    )
    StyleCard(
        selected = state.type == LinesType.QUOTE,
        title = stringResource(R.string.widget_style_quote),
        description = stringResource(R.string.widget_style_quote_desc),
        onClick = { viewModel.chooseType(LinesType.QUOTE) },
    )
    StyleCard(
        selected = state.type == LinesType.HOOK,
        title = stringResource(R.string.widget_style_hook),
        description = stringResource(R.string.widget_style_hook_desc),
        onClick = { viewModel.chooseType(LinesType.HOOK) },
    )
    if (state.limitReached) {
        Text(
            text = stringResource(R.string.widget_limit_reached, WidgetLimits.MAX_WIDGETS),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
    GlassButton(
        text = stringResource(R.string.widget_continue),
        onClick = viewModel::generate,
        enabled = !state.limitReached,
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
    )
}

@Composable
private fun StyleCard(
    selected: Boolean,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    val borderColor =
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.18f)
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                .border(BorderStroke(if (selected) 2.dp else 1.dp, borderColor), shape)
                .clickable(onClick = onClick)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun GeneratingStep(
    name: String,
    state: CreatorState,
    viewModel: WidgetCreatorViewModel,
) {
    StepHeader(stringResource(R.string.widget_generating_title, name), onBack = { viewModel.goTo(CreatorStep.STYLE) })

    // A few lines that take turns, so the wait never looks frozen.
    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(state.failed) {
        while (!state.failed) {
            delay(LINE_ROTATION_MS)
            index = (index + 1) % GENERATING_LINES
        }
    }
    val line =
        when (index) {
            0 -> stringResource(R.string.widget_generating_line_1, name)
            1 -> stringResource(R.string.widget_generating_line_2)
            2 -> stringResource(R.string.widget_generating_line_3)
            else -> stringResource(R.string.widget_generating_line_4)
        }
    if (state.failed) {
        Text(
            text = stringResource(R.string.widget_generation_failed),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
        GlassButton(
            text = stringResource(R.string.try_again),
            onClick = viewModel::generate,
            modifier = Modifier.fillMaxWidth(),
        )
    } else {
        AnimatedContent(
            targetState = line,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "widget_generating_line",
        ) { current ->
            Text(
                text = current,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
    Text(
        text = stringResource(R.string.widget_generating_privacy),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        modifier = Modifier.padding(bottom = 16.dp),
    )
}

@Composable
private fun PickStep(
    name: String,
    state: CreatorState,
    viewModel: WidgetCreatorViewModel,
) {
    var editing by remember { mutableStateOf<Int?>(null) }
    StepHeader(stringResource(R.string.widget_lines_title, name), onBack = { viewModel.goTo(CreatorStep.STYLE) })
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.widget_lines_selected, state.selected.size, state.lines.size),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
        TextButton(onClick = viewModel::generateMore, enabled = !state.generatingMore) {
            Text(stringResource(R.string.widget_lines_more))
        }
    }
    if (state.littleHistory) {
        Text(
            text = stringResource(R.string.widget_little_history),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )
    }
    LazyColumn(
        modifier = Modifier.heightIn(max = 340.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(state.lines) { lineIndex, line ->
            val selected = lineIndex in state.selected
            val shape = RoundedCornerShape(14.dp)
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                        .border(
                            BorderStroke(
                                1.5.dp,
                                if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            ),
                            shape,
                        ).clickable { viewModel.toggle(lineIndex) }
                        .padding(start = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = selected, onCheckedChange = null, modifier = Modifier.padding(12.dp))
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f).padding(vertical = 10.dp),
                )
                IconButton(onClick = { editing = lineIndex }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = stringResource(R.string.widget_lines_edit),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = viewModel::selectAllOrNone) {
            Text(
                stringResource(
                    if (state.selected.isEmpty()) R.string.widget_lines_all else R.string.widget_lines_clear,
                ),
            )
        }
        GlassButton(
            text = stringResource(R.string.widget_continue),
            onClick = { viewModel.goTo(CreatorStep.OPTIONS) },
            enabled = state.selected.isNotEmpty(),
            modifier = Modifier.weight(1f),
        )
    }

    editing?.let { lineIndex ->
        EditLineDialog(
            initial = state.lines.getOrNull(lineIndex).orEmpty(),
            onSave = {
                viewModel.editLine(lineIndex, it)
                editing = null
            },
            onDismiss = { editing = null },
        )
    }
}

@Composable
private fun EditLineDialog(
    initial: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.widget_lines_edit)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }, enabled = text.isNotBlank()) {
                Text(stringResource(R.string.widget_lines_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
private fun OptionsStep(
    state: CreatorState,
    viewModel: WidgetCreatorViewModel,
) {
    StepHeader(stringResource(R.string.widget_size_title), onBack = { viewModel.goTo(CreatorStep.PICK) })
    Text(
        text = stringResource(R.string.widget_rotation_label),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RotationChip(RotationRule.EACH_OPEN, R.string.widget_rotation_open, state, viewModel)
        RotationChip(RotationRule.DAILY, R.string.widget_rotation_daily, state, viewModel)
        RotationChip(RotationRule.FIXED, R.string.widget_rotation_fixed, state, viewModel)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.widget_discreet_mode),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.widget_discreet_mode_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
        }
        Spacer(Modifier.size(12.dp))
        Switch(checked = state.discreet, onCheckedChange = viewModel::setDiscreet)
    }
    Text(
        text = stringResource(R.string.widget_resize_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
    )
    if (state.pinUnsupported) {
        Text(
            text = stringResource(R.string.widget_pin_unsupported),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
    GlassButton(
        text = stringResource(R.string.widget_add_to_home),
        onClick = viewModel::addToHome,
        enabled = !state.limitReached && state.selected.isNotEmpty(),
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
    )
}

@Composable
private fun RotationChip(
    rule: RotationRule,
    label: Int,
    state: CreatorState,
    viewModel: WidgetCreatorViewModel,
) {
    FilterChip(
        selected = state.rotation == rule,
        onClick = { viewModel.setRotation(rule) },
        label = { Text(stringResource(label)) },
    )
}

private const val LINE_ROTATION_MS = 2500L
private const val GENERATING_LINES = 4
