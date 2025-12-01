@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.settings.ui

import ai.atick.material.MaterialColor
import android.Manifest
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.core.file.backup.ui.BackupSheet
import com.ilustris.sagai.core.permissions.PermissionComponent
import com.ilustris.sagai.core.permissions.PermissionService
import com.ilustris.sagai.core.permissions.PermissionService.Companion.openAppSettings
import com.ilustris.sagai.core.permissions.PermissionService.Companion.rememberPermissionLauncher
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.core.utils.formatFileSize
import com.ilustris.sagai.features.playthrough.PlaythroughSheet
import com.ilustris.sagai.features.premium.PremiumCard
import com.ilustris.sagai.features.premium.PremiumTitle
import com.ilustris.sagai.features.premium.PremiumView
import com.ilustris.sagai.features.settings.ui.components.PreferencesContainer
import com.ilustris.sagai.features.timeline.ui.AvatarTimelineIcon
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SettingsView(viewModel: SettingsViewModel = hiltViewModel()) {
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle(false)
    val smartSuggestionsEnabled by viewModel.smartSuggestionsEnabled.collectAsStateWithLifecycle(
        false,
    )
    val backupEnabled by
        viewModel.backupEnabled
            .collectAsStateWithLifecycle(false)

    val messageEffectsEnabled by viewModel.messageEffectsEnabled.collectAsStateWithLifecycle(true)

    val memoryUsage by viewModel.memoryUsage.collectAsStateWithLifecycle()
    val isUserPro by viewModel.isUserPro.collectAsState(false)
    val storageInfo by viewModel.sagaStorageInfo.collectAsStateWithLifecycle(emptyList())
    val breakdown by viewModel.storageBreakdown.collectAsStateWithLifecycle()

    var showClearDialog by remember { mutableStateOf(false) }
    val isWiping by viewModel.isLoading.collectAsStateWithLifecycle()
    val loadingMessage by viewModel.loadingMessage.collectAsStateWithLifecycle()
    var premiumSheetVisible by remember { mutableStateOf(false) }
    var requestedPermission by remember {
        mutableStateOf<String?>(null)
    }
    val context = LocalActivity.current
    val permissionLauncher = rememberPermissionLauncher()

    val blurRadius = if (isWiping || showClearDialog) 16.dp else 0.dp
    var showBackupSheet by remember { mutableStateOf(false) }
    var showBackups by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()



    LazyColumn(
        state = listState,
        modifier =
            Modifier
                .statusBarsPadding()
                .fillMaxSize()
                .padding(16.dp)
                .blur(blurRadius),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        stickyHeader {
            SharedTransitionLayout {
                AnimatedContent(listState.canScrollBackward, modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.background
                    )
                    .fillMaxWidth()) {
                    if (it.not()) {
                        Text(
                            text = stringResource(R.string.settings_title),
                            style =
                                MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                ),
                            modifier = Modifier
                                .sharedElement(
                                    rememberSharedContentState("header-key"),
                                    this
                                )
                                .padding(vertical = 16.dp),
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.settings_title),
                            style =
                                MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                ),
                            modifier = Modifier
                                .sharedElement(
                                    rememberSharedContentState("header-key"),
                                    this
                                )
                                .padding(vertical = 16.dp),
                        )
                    }
                }
            }

        }

        if (isUserPro) {
            item {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier =
                        Modifier
                            .padding(8.dp)
                            .reactiveShimmer(true)
                            .gradientFill(Brush.horizontalGradient(holographicGradient)),
                ) {
                    PremiumTitle()
                }
            }
        }

        item {
            var showPlaythroughSheet by remember { mutableStateOf(false) }
            val playthroughCardPrompt by viewModel.playthroughCardPrompt.collectAsStateWithLifecycle()

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .dropShadow(
                            RoundedCornerShape(15.dp),
                            Shadow(
                                5.dp,
                                Brush.verticalGradient(holographicGradient),
                            ),
                        )
                        .clip(RoundedCornerShape(15.dp))
                        .border(
                            1.dp,
                            Brush.verticalGradient(holographicGradient),
                            RoundedCornerShape(15.dp),
                        )
                        .background(
                            MaterialTheme.colorScheme.surfaceContainer,
                            RoundedCornerShape(15.dp),
                        )
                        .clickable {
                            showPlaythroughSheet = true
                        }
                        .reactiveShimmer(playthroughCardPrompt == null)
                        .padding(16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        painterResource(R.drawable.ic_spark),
                        contentDescription = null,
                        modifier =
                            Modifier
                                .size(32.dp)
                                .gradientFill(Brush.verticalGradient(holographicGradient)),
                    )
                    Column {
                        Text(
                            text = playthroughCardPrompt?.title ?: stringResource(R.string.your_playthrough_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        )
                        Text(
                            text = playthroughCardPrompt?.subtitle ?: stringResource(R.string.your_playthrough_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.alpha(0.7f),
                        )
                    }
                }
            }

            if (showPlaythroughSheet) {
                PlaythroughSheet(
                    onDismiss = { showPlaythroughSheet = false },
                )
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
                        )
                        .padding(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.memory_usage),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alpha(.5f),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text =
                        memoryUsage?.formatFileSize() ?: stringResource(id = R.string.not_available),
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

        if (breakdown.cacheSize > 1) {
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
                            )
                            .clickable {
                                viewModel.clearCache()
                            }
                            .padding(16.dp),
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
                        modifier =
                            Modifier
                                .alpha(.5f)
                                .size(24.dp)
                                .padding(8.dp),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }

        if (storageInfo.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.sagas_storage),
                    style = MaterialTheme.typography.titleMedium,
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
                            )
                            .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    storageInfo.forEach { info ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            val saga = remember { info.data }
                            AvatarTimelineIcon(
                                saga.icon,
                                false,
                                saga.genre,
                                placeHolderChar = saga.title.first().uppercase(),
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
                                    saga.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                )

                                Text(
                                    stringResource(
                                        R.string.saga_detail_status_created,
                                        saga.createdAt.formatDate(),
                                    ),
                                    style =
                                        MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Light,
                                        ),
                                    modifier = Modifier.alpha(.5f),
                                )
                            }

                            Text(
                                info.sizeBytes.formatFileSize(),
                                style =
                                    MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Normal,
                                    ),
                                modifier = Modifier.alpha(.5f),
                            )
                        }

                        if (info != storageInfo.last()) {
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                                thickness = 1.dp,
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.preferences),
                style = MaterialTheme.typography.titleMedium,
                modifier =
                    Modifier
                        .alpha(.5f)
                        .padding(8.dp),
            )
        }

        item {
            Column(
                Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceContainer,
                        RoundedCornerShape(15.dp),
                    )
                    .padding(8.dp),
            ) {
                PreferencesContainer(
                    stringResource(R.string.notifications),
                    stringResource(R.string.notification_explanation),
                    isActivated = notificationsEnabled,
                    onClickSwitch = {
                        context?.let {
                            if (notificationsEnabled) {
                                openAppSettings(it)
                            } else {
                                PermissionService.requestPermission(
                                    it,
                                    Manifest.permission.POST_NOTIFICATIONS,
                                    permissionLauncher,
                                    onShowRationale = {
                                        requestedPermission =
                                            Manifest.permission.POST_NOTIFICATIONS
                                    },
                                )
                            }
                        }
                    },
                )

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                    thickness = 1.dp,
                )

                PreferencesContainer(
                    stringResource(R.string.backup),
                    stringResource(R.string.storage_permission_description),
                    isActivated = backupEnabled,
                    onClickSwitch = {
                        if (backupEnabled) {
                            // Backup cannot be disabled from here anymore
                        } else {
                            showBackupSheet = true
                        }
                    },
                )

                if (backupEnabled) {

                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(15.dp))
                        .clickable {
                            showBackups = true
                            showBackupSheet = true
                        }
                        .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,

                    ) {
                        Icon(
                            painterResource(R.drawable.ic_restore),
                            null,
                            tint = Color.White,
                            modifier =
                                Modifier
                                    .background(
                                        MaterialTheme.colorScheme.secondary,
                                        RoundedCornerShape(5.dp),
                                    )
                                    .size(32.dp)
                                    .padding(8.dp)
                                    ,
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.restore_sagas),
                                style =
                                    MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),

                            )

                            Text(
                                stringResource(R.string.settings_backup_restore_sagas_description),
                                modifier = Modifier.alpha(.7f),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Normal
                                )
                            )

                        }


                    }

                }



                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                    thickness = 1.dp,
                )

                PreferencesContainer(
                    stringResource(R.string.smart_fix),
                    stringResource(R.string.smart_fix_description),
                    isActivated = smartSuggestionsEnabled,
                    onClickSwitch = {
                        viewModel.setSmartSuggestionsEnabled(!it)
                    },
                )

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                    thickness = 1.dp,
                )

                PreferencesContainer(
                    stringResource(R.string.message_effects),
                    stringResource(R.string.message_effects_description),
                    isActivated = messageEffectsEnabled,
                    onClickSwitch = {
                        viewModel.setMessageEffectsEnabled(!it)
                    },
                )
            }
        }

        item {
            val exportLauncher =
                rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument(
                        "application/octet-stream"
                    )
                ) { uri ->
                    uri?.let {
                        viewModel.exportAllSagas(it)
                    }
                }
            val shape = RoundedCornerShape(15.dp)

            Row(
                modifier =
                Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceContainer, shape)
                    .clickable {
                        exportLauncher.launch("SagaAI_Full_Backup.sagas")
                    }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_folder),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(5.dp))
                        .size(32.dp)
                        .padding(8.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.export_all_sagas),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        text = stringResource(R.string.settings_export_sagas_description),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        item {
            val launcher =
                rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
                    uri?.let {
                        viewModel.importSaga(it)
                    }
                }

            val iconTint = MaterialTheme.colorScheme.surfaceContainer

            val shape = RoundedCornerShape(15.dp)

            Row(
                modifier =
                    Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth()
                        .clip(shape)
                        .background(iconTint, shape)
                        .clickable {
                            launcher.launch(arrayOf("application/zip"))
                        }
                        .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_import),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(5.dp))
                        .size(32.dp)
                        .padding(8.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.import_saga),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        text = stringResource(R.string.settings_import_sagas_description),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        item {
            Text(
                stringResource(R.string.signatures_title),
                style = MaterialTheme.typography.titleSmall,
                modifier =
                    Modifier
                        .alpha(.5f)
                        .padding(8.dp),
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
                Text(
                    stringResource(R.string.clear_data_button),
                    modifier = Modifier.padding(8.dp),
                )
            }
        }

        item {
            Spacer(Modifier.size(50.dp))
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
                    viewModel.wipeAppData()
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

    StarryLoader(isWiping, loadingMessage)

    PermissionComponent(requestedPermission, {
        openAppSettings(context)
    }, { requestedPermission = null })

    PremiumView(
        isVisible = premiumSheetVisible,
        onDismiss = {
            premiumSheetVisible = false
        },
    )

    if (showBackupSheet) {
        BackupSheet(showBackups, onDismiss = {
            showBackupSheet = false
            showBackups = false
        })
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
