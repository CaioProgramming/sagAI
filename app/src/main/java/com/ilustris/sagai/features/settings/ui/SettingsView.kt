@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.settings.ui

import ai.atick.material.MaterialColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.formatFileSize
import com.ilustris.sagai.features.premium.PremiumCard
import com.ilustris.sagai.features.premium.PremiumTitle
import com.ilustris.sagai.features.premium.PremiumView
import com.ilustris.sagai.features.settings.domain.StorageBreakdown
import com.ilustris.sagai.features.timeline.ui.AvatarTimelineIcon
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer

@Composable
fun SettingsView(viewModel: SettingsViewModel = hiltViewModel()) {
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle(false)
    val smartSuggestionsEnabled by viewModel.smartSuggestionsEnabled.collectAsStateWithLifecycle(
        false,
    )
    val memoryUsage by viewModel.memoryUsage.collectAsStateWithLifecycle()
    val isUserPro by viewModel.isUserPro.collectAsState(false)
    val storageInfo by viewModel.sagaStorageInfo.collectAsStateWithLifecycle()
    val breakdown = viewModel.storageBreakdown.collectAsStateWithLifecycle().value

    var showClearDialog by remember { mutableStateOf(false) }
    var isWiping by remember { mutableStateOf(false) }
    var wipeComplete by remember { mutableStateOf(false) }
    var premiumSheetVisible by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        val blurRadius = if (isWiping || showClearDialog) 16.dp else 0.dp
        LazyColumn(
            modifier =
                Modifier
                    .padding(top = 50.dp)
                    .fillMaxSize()
                    .padding(16.dp)
                    .blur(blurRadius),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.settings_title),
                    style =
                        MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                        ),
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            if (isUserPro) {
                item {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier =
                            Modifier
                                .reactiveShimmer(true)
                                .gradientFill(Brush.horizontalGradient(holographicGradient)),
                    ) {
                        PremiumTitle()
                    }
                }
            }

            item {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceContainer,
                                RoundedCornerShape(15.dp),
                            ).padding(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.memory_usage),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.alpha(.5f),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text =
                            memoryUsage?.formatFileSize() ?: "---",
                        style =
                            MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                    )

                    StorageBarChart(
                        cacheSize = breakdown.cacheSize,
                        sagaContentSize = breakdown.sagaContentSize,
                        otherSize = breakdown.otherSize,
                        totalSize = (memoryUsage ?: 0L),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                    )
                }
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                MaterialTheme.colorScheme.surfaceContainer,
                                RoundedCornerShape(15.dp),
                            ).clickable {
                                viewModel.clearCache()
                            }.padding(16.dp),
                ) {
                    Text(
                        stringResource(R.string.clear_cache),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )

                    Text(
                        breakdown.cacheSize.formatFileSize(),
                        style =
                            MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Normal,
                            ),
                        modifier = Modifier.alpha(.5f),
                    )

                    Icon(
                        painterResource(R.drawable.round_arrow_forward_ios_24),
                        null,
                        modifier = Modifier.alpha(.5f).size(24.dp),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.sagas_storage),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.alpha(.5f),
                )
            }
            item {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceContainer,
                                RoundedCornerShape(15.dp),
                            ).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    storageInfo.forEach { saga ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            AvatarTimelineIcon(
                                saga.icon,
                                false,
                                saga.genre,
                                placeHolderChar = saga.name.first().uppercase(),
                                modifier =
                                    Modifier
                                        .size(32.dp)
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.onBackground.gradientFade(),
                                            CircleShape,
                                        ),
                            )

                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(
                                    saga.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                )

                                Text(
                                    stringResource(R.string.saga_detail_status_created, saga.createdAt),
                                    style =
                                        MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Light,
                                        ),
                                    modifier = Modifier.alpha(.5f),
                                )
                            }

                            Text(
                                saga.sizeBytes.formatFileSize(),
                                style =
                                    MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Normal,
                                    ),
                                modifier = Modifier.alpha(.5f),
                            )
                        }

                        if (saga != storageInfo.last()) {
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                                thickness = 1.dp,
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.preferences),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.alpha(.5f),
                )
            }

            item {
                Column(
                    Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceContainer,
                            RoundedCornerShape(15.dp),
                        ),
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainer,
                                    RoundedCornerShape(15.dp),
                                ).padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier =
                                Modifier
                                    .padding(8.dp)
                                    .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                stringResource(R.string.notifications),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                stringResource(R.string.notifications_description),
                                style =
                                    MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Light,
                                    ),
                                modifier = Modifier.alpha(.7f),
                            )
                        }
                        Switch(
                            checked = notificationsEnabled,
                            colors =
                                SwitchDefaults.colors().copy(
                                    uncheckedBorderColor = Color.Transparent,
                                ),
                            modifier = Modifier.scale(.6f),
                            onCheckedChange = {
                                viewModel.setNotificationsEnabled(
                                    notificationsEnabled.not(),
                                )
                            },
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                        thickness = 1.dp,
                    )

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainer,
                                    RoundedCornerShape(15.dp),
                                ).padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            Modifier
                                .padding(8.dp)
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                stringResource(R.string.smart_fix),
                                style = MaterialTheme.typography.bodyMedium,
                            )

                            Text(
                                stringResource(R.string.smart_fix_description),
                                style =
                                    MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Light,
                                    ),
                                modifier = Modifier.alpha(.7f),
                            )
                        }
                        Switch(
                            checked = smartSuggestionsEnabled,
                            colors =
                                SwitchDefaults.colors().copy(
                                    uncheckedBorderColor = Color.Transparent,
                                ),
                            modifier = Modifier.scale(.6f),
                            onCheckedChange = {
                                viewModel.setSmartSuggestionsEnabled(
                                    smartSuggestionsEnabled.not(),
                                )
                            },
                        )
                    }
                }
            }

            item {
                Text(
                    "Assinaturas",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.alpha(.5f),
                )
            }

            item {
                PremiumCard(
                    isUserPro = isUserPro,
                    onClick = {
                        premiumSheetVisible = true
                    },
                )
            }

            item {
                Button(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialColor.RedA200,
                        ),
                    shape = RoundedCornerShape(15.dp),
                ) {
                    Text(stringResource(R.string.clear_data_button), modifier = Modifier.padding(8.dp))
                }
            }
        }

        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text(stringResource(R.string.clear_data_dialog_title)) },
                text = { Text(stringResource(R.string.clear_data_dialog_message)) },
                confirmButton = {
                    TextButton(onClick = {
                        showClearDialog = false
                        isWiping = true
                        wipeComplete = false
                        viewModel.wipeAppData {
                            isWiping = false
                            wipeComplete = true
                        }
                    }) {
                        Text(stringResource(R.string.clear_data_dialog_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) {
                        Text(stringResource(R.string.clear_data_dialog_cancel))
                    }
                },
            )
        }

        if (isWiping || wipeComplete) {
            Dialog(
                onDismissRequest = {
                    if (wipeComplete) wipeComplete = false
                },
                properties =
                    DialogProperties(
                        dismissOnBackPress = wipeComplete,
                        dismissOnClickOutside = wipeComplete,
                    ),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(holographicGradient)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        StarryTextPlaceholder(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(Brush.verticalGradient(holographicGradient)),
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = if (isWiping) "Wiping your universes" else "Your universe is empty again!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }
    }

    PremiumView(
        isVisible = premiumSheetVisible,
        onDismiss = {
            premiumSheetVisible = false
        },
    )
}

@Composable
fun StorageBarChart(
    cacheSize: Long,
    sagaContentSize: Long,
    otherSize: Long,
    totalSize: Long,
    modifier: Modifier = Modifier,
) {
    val cacheColor = MaterialColor.BlueA100
    val sagaColor = MaterialTheme.colorScheme.primary
    val otherColor = MaterialColor.BlueGray500
    val barHeight = 12.dp
    val cacheRatio = if (totalSize > 0) cacheSize.toFloat() / totalSize else 0f
    val sagaRatio = if (totalSize > 0) sagaContentSize.toFloat() / totalSize else 0f
    val otherRatio = if (totalSize > 0) otherSize.toFloat() / totalSize else 0f
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
        ) {
            Row(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .weight(cacheRatio.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(cacheColor, RectangleShape),
                )
                Box(
                    Modifier
                        .weight(sagaRatio.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(sagaColor, RectangleShape),
                )
                Box(
                    Modifier
                        .weight(otherRatio.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(otherColor, RectangleShape),
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier =
            Modifier.horizontalScroll(
                rememberScrollState(),
            ),
    ) {
        LegendDot(cacheColor, stringResource(R.string.cache), cacheSize.formatFileSize())
        LegendDot(
            sagaColor,
            stringResource(R.string.sagas_storage),
            sagaContentSize.formatFileSize(),
        )
        LegendDot(otherColor, stringResource(R.string.other), otherSize.formatFileSize())
    }
}

@Composable
fun LegendDot(
    color: Color,
    label: String,
    value: String,
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier
                .size(10.dp)
                .background(color, CircleShape),
        )
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall)

            Text(value, style = MaterialTheme.typography.labelSmall, modifier = Modifier.alpha(.5f))
        }
    }
}

@Preview
@Composable
fun SettingsViewPreview() {
    SettingsView()
}
