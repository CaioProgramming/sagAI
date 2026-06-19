package com.ilustris.sagai.features.settings.ui.audit

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.SafeGuard
import com.ilustris.sagai.core.database.model.AIAuditLog
import com.ilustris.sagai.core.utils.DateFormatOption
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAuditLogView(
    onBack: () -> Unit,
    viewModel: AIAuditLogViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
) {
    val logs by viewModel.filteredLogs.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val dataTypeFilter by viewModel.dataTypeFilter.collectAsState()
    val modelFilter by viewModel.modelFilter.collectAsState()
    val availableDataTypes by viewModel.availableDataTypes.collectAsState()
    val availableModels by viewModel.availableModels.collectAsState()

    val loadingSuggestionId by viewModel.loadingSuggestionId.collectAsState()
    val pipelineInsight by viewModel.pipelineInsight.collectAsState()
    val isPipelineInsightLoading by viewModel.isPipelineInsightLoading.collectAsState()

    var showClearDialog by remember { mutableStateOf(false) }

    var selectedSectionContent by remember { mutableStateOf<AuditLogSectionData?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(logs) {
        if (pipelineInsight == null && logs.isNotEmpty()) viewModel.requestGlobalInsight()
    }

    var optionsExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier =
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        state = rememberLazyListState(),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onBack() },
                    modifier =
                        Modifier
                            .padding(top = 16.dp)
                            .clip(CircleShape)
                            .size(32.dp),
                    colors =
                        IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                ) {
                    Icon(
                        painterResource(R.drawable.ic_back_left),
                        null,
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .fillMaxSize(),
                    )
                }
                Box(Modifier.weight(1f))

                AnimatedVisibility(logs.isNotEmpty(), enter = scaleIn(), exit = scaleOut()) {
                    IconButton({
                        optionsExpanded = true
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            painterResource(R.drawable.ic_menu),
                            "Options",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier =
                                Modifier
                                    .padding(8.dp)
                                    .fillMaxSize(),
                        )

                        DropdownMenu(
                            optionsExpanded,
                            onDismissRequest = { optionsExpanded = false },
                        ) {
                            DropdownMenuItem({
                                Text(stringResource(R.string.clear_data_button))
                            }, leadingIcon = {
                                Icon(
                                    painterResource(R.drawable.ic_delete),
                                    null,
                                    modifier = Modifier.size(18.dp),
                                )
                            }, onClick = {
                                showClearDialog = true
                                optionsExpanded = false
                            })
                        }
                    }
                }
            }
        }

        stickyHeader {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp),
            ) {
                Text(
                    stringResource(R.string.audit_logs_title),
                    style =
                        MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                        ),
                    textAlign = TextAlign.Start,
                )
            }
        }

        item {
            PipelineInsightCard(
                insight = pipelineInsight,
                isLoading = isPipelineInsightLoading,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
        item {
            AnimatedVisibility(logs.isNotEmpty()) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(
                                rememberScrollState(),
                            ).padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilterChip(
                        selected = statusFilter == null,
                        onClick = { viewModel.updateStatusFilter(null) },
                        label = { Text(stringResource(R.string.audit_logs_all)) },
                    )
                    FilterChip(
                        selected = statusFilter == "SUCCESS",
                        onClick = { viewModel.updateStatusFilter("SUCCESS") },
                        label = { Text(stringResource(R.string.audit_logs_success)) },
                        colors =
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                    )
                    FilterChip(
                        selected = statusFilter == "ERROR",
                        onClick = { viewModel.updateStatusFilter("ERROR") },
                        label = { Text(stringResource(R.string.audit_logs_error)) },
                        colors =
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer,
                            ),
                    )

                    if (availableDataTypes.isNotEmpty()) {
                        VerticalDivider(modifier = Modifier.height(24.dp))
                        availableDataTypes.forEach { dt ->
                            FilterChip(
                                selected = dataTypeFilter == dt,
                                onClick = { viewModel.updateDataTypeFilter(if (dataTypeFilter == dt) null else dt) },
                                label = { Text(dt) },
                                colors =
                                    FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    ),
                            )
                        }
                    }

                    if (availableModels.isNotEmpty()) {
                        VerticalDivider(modifier = Modifier.height(24.dp))
                        availableModels.forEach { mod ->
                            FilterChip(
                                selected = modelFilter == mod,
                                onClick = { viewModel.updateModelFilter(if (modelFilter == mod) null else mod) },
                                label = { Text(mod) },
                                colors =
                                    FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                    ),
                            )
                        }
                    }
                }
            }
        }

        val groupedLogs =
            logs.groupBy {
                SimpleDateFormat(
                    "MMM dd, yyyy",
                    Locale.getDefault(),
                ).format(Date(it.timestamp))
            }

        if (logs.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.audit_logs_empty),
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Light,
                        ),
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
            }
        } else {
            groupedLogs.forEach { (dateStr, logList) ->
                item {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 4.dp),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                items(logList) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ),
                    ) {
                        AuditLogItem(
                            log = log,
                            isLast = true,
                            isLoadingSuggestion = loadingSuggestionId == log.id,
                            onRequestSuggestion = { viewModel.requestSuggestion(log) },
                            onShowSection = { selectedSectionContent = it },
                        )
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.audit_logs_clear_dialog_title)) },
            text = { Text(stringResource(R.string.audit_logs_clear_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearLogs()
                    showClearDialog = false
                }) {
                    Text(stringResource(R.string.audit_logs_clear_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    selectedSectionContent?.let { section ->
        ModalBottomSheet(
            onDismissRequest = { selectedSectionContent = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = {
                Box(
                    Modifier
                        .padding(vertical = 12.dp)
                        .size(32.dp, 4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            CircleShape,
                        ),
                )
            },
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                )

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .background(
                                MaterialTheme.colorScheme.surfaceContainer,
                                RoundedCornerShape(12.dp),
                            ).verticalScroll(rememberScrollState()),
                ) {
                    if (section.isJson) {
                        JsonCodeBlock(jsonString = section.content)
                    } else {
                        Text(
                            text = section.content,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }

                Button(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            selectedSectionContent = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(stringResource(R.string.next))
                }
            }
        }
    }
}

data class AuditLogSectionData(
    val title: String,
    val content: String,
    val isJson: Boolean = true,
)

@Composable
fun AuditLogItem(
    log: AIAuditLog,
    isLast: Boolean,
    isLoadingSuggestion: Boolean,
    onRequestSuggestion: () -> Unit,
    onShowSection: (AuditLogSectionData) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val statusColor = if (log.status == "SUCCESS") Color(0xFF4CAF50) else Color(0xFFE53935)
    val responseTimeColor =
        when {
            log.responseTime < 5000 -> Color(0xFF4CAF50)
            log.responseTime < 10000 -> Color(0xFFFFC107)
            else -> Color(0xFFE53935)
        }

    Column(
        modifier =
            Modifier
                .clickable { expanded = !expanded }
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = log.dataType,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = if (expanded) Int.MAX_VALUE else 1,
                    )

                    Text(
                        text = log.status,
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                            ),
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .border(1.dp, statusColor, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text =
                            buildString {
                                append(String.format("%.1fs", log.responseTime / 1000.0))
                                if (log.queueWaitMs > 0) {
                                    append(String.format(" (+%.1fs queue)", log.queueWaitMs / 1000.0))
                                }
                            },
                        style =
                            MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = responseTimeColor,
                            ),
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (!log.blueprintKey.isNullOrEmpty()) {
                        Text(
                            text = log.blueprintKey,
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium,
                                ),
                            modifier =
                                Modifier
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        RoundedCornerShape(4.dp),
                                    ).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .padding(8.dp),
                        )
                    }

                    Text(
                        text = "• ${log.timestamp.formatDate(DateFormatOption.HOUR_MINUTE_DAY_OF_MONTH_YEAR)}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Light),
                        modifier = Modifier.alpha(.7f),
                    )
                }

                val formattedModel = log.model.replace("models/", "")
                Text(
                    text = formattedModel,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Light),
                    modifier = Modifier.alpha(.7f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )

                if (log.totalTokens != null && log.totalTokens > 0) {
                    TokenUsageBarChart(
                        promptTokens = log.promptTokens ?: 0,
                        candidatesTokens = log.candidatesTokens ?: 0,
                        totalTokens = log.totalTokens,
                        modifier = Modifier.padding(8.dp),
                        showLegend = true,
                    )
                }

                if (!log.usedTools.isNullOrEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_settings),
                            contentDescription = "Tools used",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        )
                        log.usedTools.forEach { tool ->
                            Text(
                                text = tool,
                                style = MaterialTheme.typography.labelSmall,
                                modifier =
                                    Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }

                if (!log.safetyStatus.isNullOrEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        val safeguard =
                            SafeGuard.findValue(
                                log.safetyStatus,
                            )
                        val color =
                            safeguard?.color(MaterialTheme.colorScheme)
                                ?: MaterialTheme.colorScheme.primaryContainer
                        Icon(
                            painter = painterResource(safeguard?.iconRes ?: R.drawable.ic_spark),
                            contentDescription = "Safety Status",
                            modifier = Modifier.size(12.dp),
                            tint = color,
                        )
                        Text(
                            text = log.safetyStatus ?: "OK",
                            style = MaterialTheme.typography.labelSmall,
                            color = color,
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(color.copy(alpha = 0.1f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
            }

            val rotation by animateFloatAsState(
                targetValue = if (expanded) 90f else 0f,
            )
            Icon(
                painter = painterResource(R.drawable.round_arrow_forward_ios_24),
                contentDescription = null,
                modifier =
                    Modifier
                        .size(12.dp)
                        .rotate(rotation),
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!log.errorMessage.isNullOrEmpty()) {
                    Text(
                        text = stringResource(R.string.audit_logs_error_label),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.error,
                    )
                    Text(
                        text = log.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                if (!log.reasoning.isNullOrEmpty()) {
                    AuditLogSectionCard(
                        title = stringResource(R.string.audit_logs_reasoning),
                        content = log.reasoning,
                        onClick = {
                            onShowSection(
                                AuditLogSectionData(
                                    title = it,
                                    content = log.reasoning,
                                    isJson = false,
                                ),
                            )
                        },
                    )
                }

                if (!log.systemInstruction.isNullOrEmpty()) {
                    AuditLogSectionCard(
                        title = stringResource(R.string.audit_logs_system_instruction),
                        content = log.systemInstruction,
                        onClick = {
                            onShowSection(
                                AuditLogSectionData(
                                    title = it,
                                    content = log.systemInstruction,
                                ),
                            )
                        },
                    )
                }

                if (!log.sentVariables.isNullOrEmpty()) {
                    AuditLogSectionCard(
                        title = stringResource(R.string.audit_logs_sent_variables),
                        content = log.sentVariables,
                        onClick = {
                            onShowSection(
                                AuditLogSectionData(
                                    title = it,
                                    content = log.sentVariables,
                                ),
                            )
                        },
                    )
                }

                if (!log.rawResponse.isNullOrEmpty()) {
                    AuditLogSectionCard(
                        title = stringResource(R.string.audit_logs_raw_response),
                        content = log.rawResponse,
                        onClick = {
                            onShowSection(
                                AuditLogSectionData(
                                    title = it,
                                    content = log.rawResponse,
                                ),
                            )
                        },
                    )
                }

                if (log.totalTokens != null && log.totalTokens > 0) {
                    Text(
                        text = stringResource(R.string.audit_logs_token_usage),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                    ) {
                        TokenInfo(
                            label =
                                stringResource(
                                    R.string.audit_logs_tokens_prompt,
                                    log.promptTokens ?: 0,
                                ),
                            color = MaterialTheme.colorScheme.secondary,
                        )
                        TokenInfo(
                            label =
                                stringResource(
                                    R.string.audit_logs_tokens_candidates,
                                    log.candidatesTokens ?: 0,
                                ),
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                        TokenInfo(
                            label =
                                stringResource(
                                    R.string.audit_logs_tokens_total,
                                    log.totalTokens,
                                ),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                if (!log.blueprintKey.isNullOrEmpty()) {
                    if (log.suggestion != null) {
                        Text(
                            text = stringResource(R.string.audit_logs_improvement_suggestion),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                        )

                        Text(
                            text = log.suggestion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp),
                                    ).padding(12.dp),
                        )
                    } else {
                        Button(
                            onClick = onRequestSuggestion,
                            enabled = !isLoadingSuggestion,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .reactiveShimmer(isLoadingSuggestion)
                                    .gradientFill(
                                        Brush.horizontalGradient(
                                            holographicGradient,
                                        ),
                                    ),
                            colors =
                                ButtonDefaults.textButtonColors(),
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_spark),
                                contentDescription = null,
                                modifier =
                                    Modifier
                                        .size(24.dp)
                                        .padding(horizontal = 8.dp),
                            )
                            Text(
                                stringResource(R.string.audit_logs_suggest_improvements),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }

        if (!isLast) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                thickness = 1.dp,
            )
        }
    }
}

@Composable
fun AuditLogSectionCard(
    title: String,
    content: String,
    onClick: (String) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onClick(title) }
                .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = content.take(100).replace("\n", " ") + "...",
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.alpha(0.6f),
            )
        }

        Icon(
            painter = painterResource(R.drawable.round_arrow_forward_ios_24),
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
    }
}

@Composable
fun JsonCodeBlock(jsonString: String) {
    val annotatedString =
        buildAnnotatedString {
            val keyRegex = "\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"\\s*:".toRegex()
            val stringRegex = "\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"".toRegex()
            val numberRegex = "\\b(-?\\d+(\\.\\d+)?)\\b".toRegex()
            val booleanRegex = "\\b(true|false|null)\\b".toRegex()

            var lastIndex = 0

            // Combine all candidates and sort by match start
            val candidates =
                (
                    keyRegex.findAll(jsonString) +
                        stringRegex.findAll(jsonString) +
                        numberRegex.findAll(jsonString) +
                        booleanRegex.findAll(jsonString)
                ).sortedBy { it.range.first }

            candidates.forEach { match ->
                if (match.range.first >= lastIndex) {
                    // Append text before match
                    append(jsonString.substring(lastIndex, match.range.first))

                    val style =
                        when {
                            keyRegex.matches(match.value) -> SpanStyle(color = Color(0xFF9CDCFE))

                            // VSCode-like Light Blue for keys
                            stringRegex.matches(match.value) -> SpanStyle(color = Color(0xFFCE9178))

                            // VSCode-like Orange/Red for strings
                            numberRegex.matches(match.value) -> SpanStyle(color = Color(0xFFB5CEA8))

                            // VSCode-like Green for numbers
                            booleanRegex.matches(match.value) -> SpanStyle(color = Color(0xFF569CD6))

                            // VSCode-like Blue for booleans/null
                            else -> SpanStyle(color = Color.White)
                        }

                    withStyle(style) {
                        append(match.value)
                    }
                    lastIndex = match.range.last + 1
                }
            }
            append(jsonString.substring(lastIndex))
        }

    Text(
        text = annotatedString,
        style =
            MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified,
            ),
        modifier =
            Modifier
                .padding(8.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small,
                ).padding(12.dp),
    )
}

@Composable
fun TokenUsageBarChart(
    promptTokens: Int,
    candidatesTokens: Int,
    totalTokens: Int,
    modifier: Modifier = Modifier,
    limit: Int = 16000,
    showLegend: Boolean = false,
) {
    val promptColor = MaterialTheme.colorScheme.secondary
    val candidatesColor = MaterialTheme.colorScheme.tertiary
    val barHeight = 6.dp

    val promptRatio = promptTokens.toFloat() / limit
    val candidatesRatio = candidatesTokens.toFloat() / limit
    val remainingRatio = ((limit - totalTokens).toFloat() / limit).coerceAtLeast(0f)

    Column(modifier = modifier) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (promptRatio > 0) {
                Box(
                    modifier =
                        Modifier
                            .weight(promptRatio.coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(promptColor),
                )
            }
            if (candidatesRatio > 0) {
                Box(
                    modifier =
                        Modifier
                            .weight(candidatesRatio.coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(candidatesColor),
                )
            }
            if (remainingRatio > 0) {
                Box(
                    modifier =
                        Modifier
                            .weight(remainingRatio.coerceAtLeast(0.01f))
                            .fillMaxHeight(),
                )
            }
        }

        if (showLegend) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                TokenInfo(
                    label = stringResource(R.string.audit_logs_tokens_prompt, promptTokens),
                    color = promptColor,
                )
                TokenInfo(
                    label = stringResource(R.string.audit_logs_tokens_candidates, candidatesTokens),
                    color = candidatesColor,
                )
            }
        }
    }
}

@Composable
fun TokenInfo(
    label: String,
    color: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(8.dp)
                    .background(color, CircleShape),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun PipelineInsightCard(
    insight: String?,
    isLoading: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
) {
    with(sharedTransitionScope) {
        AnimatedContent(insight) {
            if (it == null) {
                Box(
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painterResource(R.drawable.ic_spark),
                        null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surfaceContainer),
                        modifier =
                            Modifier
                                .size(50.dp)
                                .reactiveShimmer(
                                    isLoading,
                                    repeatMode = RepeatMode.Restart,
                                ),
                    )
                }
            } else {
                insight?.let {
                    var expanded by remember { mutableStateOf(false) }
                    val alpha by animateFloatAsState(if (expanded) 1f else 0.7f)

                    Text(
                        text = if (expanded) insight else insight.take(100) + "...",
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                brush = Brush.verticalGradient(holographicGradient),
                                fontWeight = FontWeight.Light,
                            ),
                        fontWeight = FontWeight.SemiBold,
                        modifier =
                            Modifier
                                .animateContentSize()
                                .alpha(alpha)
                                .padding(
                                    16.dp,
                                ).clickable {
                                    expanded = !expanded
                                },
                    )
                }
            }
        }
    }
}
