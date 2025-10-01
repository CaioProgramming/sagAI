@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.settings.ui

import ai.atick.material.MaterialColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
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
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.premium.PremiumView
import com.ilustris.sagai.features.timeline.ui.AvatarTimelineIcon
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.SagaTitle
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
                        modifier =
                            Modifier
                                .reactiveShimmer(true)
                                .gradientFill(Brush.horizontalGradient(holographicGradient)),
                    ) {
                        SagaTitle()
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Pro",
                            modifier = Modifier.alpha(.4f),
                            style = MaterialTheme.typography.labelSmall,
                        )
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
                            memoryUsage?.let { String.format("%.2f MB", it / (1024f * 1024f)) }
                                ?: "-- MB",
                        style =
                            MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                    )

                    val breakdown = viewModel.storageBreakdown.collectAsStateWithLifecycle().value
                    StorageBarChart(
                        cacheSize = breakdown.cacheSize,
                        sagaContentSize = breakdown.sagaContentSize,
                        otherSize = breakdown.otherSize,
                        totalSize = (memoryUsage ?: 0L),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    )
                }
            }
            item {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceContainer,
                                RoundedCornerShape(15.dp),
                            ).padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Notifications", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = notificationsEnabled,
                        colors =
                            SwitchDefaults.colors().copy(
                                uncheckedBorderColor = Color.Transparent,
                            ),
                        onCheckedChange = { viewModel.setNotificationsEnabled(notificationsEnabled.not()) },
                    )
                }
            }
            item {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceContainer,
                                RoundedCornerShape(15.dp),
                            ).padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Smart Fix", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = smartSuggestionsEnabled,
                        colors =
                            SwitchDefaults.colors().copy(
                                uncheckedBorderColor = Color.Transparent,
                            ),
                        onCheckedChange = {
                            viewModel.setSmartSuggestionsEnabled(
                                smartSuggestionsEnabled.not(),
                            )
                        },
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.sagas_storage),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            items(storageInfo) { saga ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceContainer,
                                RoundedCornerShape(15.dp),
                            ).padding(8.dp),
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
                                .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = .1f), CircleShape)
                                .background(
                                    saga.genre.color.gradientFade(),
                                    CircleShape,
                                ),
                    )

                    Text(saga.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))

                    Text(
                        String.format("%.2f MB", saga.sizeBytes / (1024f * 1024f)),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.alpha(.5f),
                    )
                }
            }

            if (isUserPro.not()) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier =
                            Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainer,
                                    RoundedCornerShape(15.dp),
                                ).padding(8.dp),
                    ) {
                        Image(
                            painterResource(R.drawable.ic_spark),
                            null,
                            Modifier
                                .size(24.dp),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                modifier =
                                    Modifier
                                        .gradientFill(Brush.horizontalGradient(holographicGradient)),
                            ) {
                                SagaTitle(
                                    textStyle =
                                        MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                        ),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Pro",
                                    modifier = Modifier.alpha(.4f),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }

                            Text(
                                stringResource(R.string.premium_first_title),
                                style =
                                    MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Light,
                                    ),
                            )

                            TextButton(onClick = {
                                premiumSheetVisible = true
                            }) {
                                Text(stringResource(R.string.premium_sign_up))
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                ) {
                    Text("Clear Data")
                }
            }
        }

        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text("Confirm Clear Data") },
                text = { Text("Are you sure you want to clear all app data? This action cannot be undone.") },
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
                        Text("Yes, clear data")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) {
                        Text("Cancel")
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

    if (premiumSheetVisible) {
        ModalBottomSheet(onDismissRequest = {
            premiumSheetVisible = false
        }, dragHandle = { Box {} }) {
            PremiumView()
        }
    }
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
    val otherColor = MaterialColor.Gray300
    val barHeight = 12.dp
    val minWidth = 2.dp
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
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LegendDot(cacheColor, "Cache")
        LegendDot(sagaColor, "Saga Content")
        LegendDot(otherColor, "Other")
    }
}

@Composable
fun LegendDot(
    color: Color,
    label: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .background(color, CircleShape),
        )
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Preview
@Composable
fun SettingsViewPreview() {
    SettingsView()
}
