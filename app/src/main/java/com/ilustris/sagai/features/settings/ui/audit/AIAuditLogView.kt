package com.ilustris.sagai.features.settings.ui.audit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ilustris.sagai.R
import com.ilustris.sagai.core.database.model.AIAuditLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAuditLogView(
    navController: NavHostController,
    viewModel: AIAuditLogViewModel = hiltViewModel(),
) {
    val logs by viewModel.filteredLogs.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()

    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Audit Logs", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(painterResource(R.drawable.ic_back_left), contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(
                            painterResource(R.drawable.ic_delete),
                            contentDescription = "Clear Logs",
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = statusFilter == null,
                        onClick = { viewModel.updateFilter(null) },
                        label = { Text("All") },
                    )
                    FilterChip(
                        selected = statusFilter == "SUCCESS",
                        onClick = { viewModel.updateFilter("SUCCESS") },
                        label = { Text("Success") },
                        colors =
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                    )
                    FilterChip(
                        selected = statusFilter == "ERROR",
                        onClick = { viewModel.updateFilter("ERROR") },
                        label = { Text("Error") },
                        colors =
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer,
                            ),
                    )
                }
            }

            if (logs.isEmpty()) {
                item {
                    Text(
                        text = "No logs recorded yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    )
                }
            } else {
                item {
                    Column(
                        Modifier
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                MaterialTheme.colorScheme.surfaceContainer,
                                RoundedCornerShape(15.dp),
                            ),
                    ) {
                        logs.forEachIndexed { index, log ->
                            AuditLogItem(log, isLast = index == logs.lastIndex)
                        }
                    }
                }
            }
        }

        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text("Clear Audit Logs") },
                text = { Text("Are you sure you want to delete all AI Audit Logs? This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.clearLogs()
                        showClearDialog = false
                    }) {
                        Text("Clear")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) {
                        Text("Cancel")
                    }
                },
            )
        }
    }
}

@Composable
fun AuditLogItem(
    log: AIAuditLog,
    isLast: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }

    val statusColor = if (log.status == "SUCCESS") Color(0xFF4CAF50) else Color(0xFFE53935)
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())

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
                ) {
                    Text(
                        text = log.dataType,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = if (expanded) Int.MAX_VALUE else 1,
                    )
                    Box(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .border(1.dp, statusColor, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = log.status,
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                ),
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${log.model} • ${dateFormat.format(Date(log.timestamp))}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Light),
                        modifier = Modifier.alpha(.7f),
                    )

                    if (!log.blueprintKey.isNullOrEmpty()) {
                        Box(
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        RoundedCornerShape(4.dp),
                                    ).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = log.blueprintKey,
                                style =
                                    MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium,
                                    ),
                            )
                        }
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
                        text = "Error:",
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
                    Text(
                        text = "Reasoning (Chain-of-Thought):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp),
                                ).padding(8.dp),
                    ) {
                        Text(
                            text = log.reasoning,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (!log.rawResponse.isNullOrEmpty()) {
                    Text(
                        text = "Raw Response:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                    RoundedCornerShape(8.dp),
                                ).padding(12.dp),
                    ) {
                        Text(
                            text = log.rawResponse,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.alpha(0.8f),
                        )
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
