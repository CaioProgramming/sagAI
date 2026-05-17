@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.settings.ui

import ai.atick.material.MaterialColor
import android.Manifest
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
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
import com.ilustris.sagai.features.premium.PremiumCard
import com.ilustris.sagai.features.premium.PremiumTitle
import com.ilustris.sagai.features.settings.ui.components.PreferencesContainer
import com.ilustris.sagai.features.timeline.ui.AvatarTimelineIcon
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.darker
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.sagaBrush

@Composable
fun SettingsView(
    onBack: () -> Unit = {},
    navToFAQ: () -> Unit = {},
    navToAuditLogs: () -> Unit = {},
    navToPlaythrough: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
    onOpenPremiumOnboarding: () -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
) {
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle(false)
    val smartSuggestionsEnabled by viewModel.smartSuggestionsEnabled.collectAsStateWithLifecycle(
        false,
    )
    val backupEnabled by
        viewModel.backupEnabled
            .collectAsStateWithLifecycle(false)

    val hasSagasWithChapters by viewModel.hasSagasWithChapters.collectAsStateWithLifecycle(null)

    val messageEffectsEnabled by viewModel.messageEffectsEnabled.collectAsStateWithLifecycle(true)
    val showTutorials by viewModel.showTutorials.collectAsStateWithLifecycle(true)
    val musicEnabled by viewModel.musicEnabled.collectAsStateWithLifecycle(true)

    val memoryUsage by viewModel.memoryUsage.collectAsStateWithLifecycle()
    val isUserPro by viewModel.isUserPro.collectAsState(false)
    val storageInfo by viewModel.sagaStorageInfo.collectAsStateWithLifecycle(emptyList())
    val breakdown by viewModel.storageBreakdown.collectAsStateWithLifecycle()

    var showClearDialog by remember { mutableStateOf(false) }
    val isWiping by viewModel.isLoading.collectAsStateWithLifecycle()
    val loadingMessage by viewModel.loadingMessage.collectAsStateWithLifecycle()
    var requestedPermission by remember {
        mutableStateOf<String?>(null)
    }
    val context = LocalActivity.current
    val permissionLauncher = rememberPermissionLauncher()

    val blurRadius = if (isWiping || showClearDialog) 16.dp else 0.dp
    var showBackupSheet by remember { mutableStateOf(false) }
    var showBackups by remember { mutableStateOf(true) }

    val exportLauncher =
        PermissionService.rememberDatabaseExportLauncher { uri ->
            uri?.let { viewModel.exportDatabase(it) }
        }

    val importLauncher =
        PermissionService.rememberDatabaseImportLauncher { uri ->
            uri?.let { viewModel.importDatabase(it) }
        }
    LazyColumn(
        modifier =
            Modifier
                .statusBarsPadding()
                .fillMaxSize()
                .padding(16.dp)
                .blur(blurRadius),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        stickyHeader {
            Text(
                text = stringResource(R.string.settings_title),
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                    ),
                modifier =
                    Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(),
            )
        }

        if (isUserPro) {
            item {
                with(sharedTransitionScope) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .reactiveShimmer(true)
                                .gradientFill(Brush.horizontalGradient(holographicGradient)),
                    ) {
                        PremiumTitle(
                            iconModifier =
                                Modifier.sharedElement(
                                    rememberSharedContentState("spark_icon"),
                                    animatedVisibilityScope,
                                ),
                        )
                    }
                }
            }
        }

        item {
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
                        ).clip(RoundedCornerShape(15.dp))
                        .border(
                            1.dp,
                            Brush.verticalGradient(holographicGradient),
                            RoundedCornerShape(15.dp),
                        ).background(
                            MaterialTheme.colorScheme.surfaceContainer,
                            RoundedCornerShape(15.dp),
                        ).clickable {
                            navToPlaythrough()
                        }.padding(16.dp),
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
                            text = stringResource(R.string.your_playthrough_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        )
                        Text(
                            text = stringResource(R.string.your_playthrough_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.alpha(0.7f),
                        )
                    }
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
                            ).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    storageInfo.forEach { info ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            val saga = remember(info.data.id) { info.data }
                            SagAITheme(genre = saga.genre) {
                                val visualConfig =
                                    com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig.current
                                val genreColor = saga.genre.color
                                val genreBrush = sagaBrush()
                                AvatarTimelineIcon(
                                    saga.icon,
                                    false,
                                    saga.genre,
                                    placeHolderChar = saga.title.first().uppercase(),
                                    visualConfig = visualConfig,
                                    borderWidth = 1.dp,
                                    modifier =
                                        Modifier
                                            .dropShadow(CircleShape) {
                                                radius = 5f
                                                color = genreColor
                                                brush = genreBrush
                                                spread = 5f
                                            }.size(32.dp)
                                            .selectiveColorHighlight(saga.genre),
                                )
                            }

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
                    ).padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
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
                            viewModel.disableBackup()
                        } else {
                            showBackupSheet = true
                        }
                    },
                )

                if (backupEnabled) {
                    Button(
                        onClick = {
                            showBackups = true
                            showBackupSheet = true
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        colors = ButtonDefaults.textButtonColors(),
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_restore),
                            null,
                            modifier =
                                Modifier
                                    .padding(horizontal = 8.dp)
                                    .size(24.dp),
                        )
                        Text(
                            stringResource(R.string.restore_sagas),
                            style =
                                MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Light,
                                ),
                        )
                    }
                }

                if (!backupEnabled && hasSagasWithChapters == true) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            stringResource(R.string.backup_disabled_warning_title),
                            style =
                                MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                            color = MaterialColor.Red600,
                        )

                        Text(
                            stringResource(R.string.backup_disabled_warning_message),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Light,
                        )
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

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                    thickness = 1.dp,
                )

                PreferencesContainer(
                    stringResource(R.string.story_guides),
                    stringResource(R.string.story_guides_description),
                    isActivated = showTutorials,
                    onClickSwitch = {
                        viewModel.setShowTutorials(!it)
                    },
                )

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                    thickness = 1.dp,
                )

                PreferencesContainer(
                    stringResource(R.string.settings_music_title),
                    stringResource(R.string.settings_music_description),
                    isActivated = musicEnabled,
                    onClickSwitch = {
                        viewModel.setMusicEnabled(!it)
                    },
                )
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
                    onOpenPremiumOnboarding()
                },
            )
        }

        item {
            PreferencesContainer(
                stringResource(R.string.settings_help_center_title),
                stringResource(R.string.settings_help_center_subtitle),
                true,
                showSwitch = false,
                onClickSwitch = {
                    navToFAQ()
                },
                modifier =
                    Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceContainer,
                            RoundedCornerShape(15.dp),
                        ).padding(8.dp),
            )
        }

        item {
            Button(
                onClick = { viewModel.clearPreferences() },
                modifier = Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                shape = RoundedCornerShape(15.dp),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                ) {
                    Text(
                        stringResource(R.string.clear_preferences),
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        stringResource(R.string.clear_preferences_explanation),
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.alpha(0.6f),
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    exportLauncher.launch("sagai_database_backup.db")
                },
                colors =
                    ButtonDefaults.buttonColors().copy(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = .2f),
                        contentColor = MaterialTheme.colorScheme.primary.darker(.3f),
                    ),
                shape = RoundedCornerShape(15.dp),
            ) {
                Text(
                    stringResource(R.string.export_database_button),
                    textAlign = TextAlign.Start,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                )
            }
        }

        item {
            Button(
                onClick = {
                    importLauncher.launch(PermissionService.SQLITE_MIME_TYPES)
                },
                colors = ButtonDefaults.textButtonColors(),
                shape = RoundedCornerShape(15.dp),
            ) {
                Text(
                    stringResource(R.string.import_database_button),
                    textAlign = TextAlign.Start,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                )
            }
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
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    textAlign = TextAlign.Start,
                )
            }
        }

        if (com.ilustris.sagai.BuildConfig.DEBUG) {
            item {
                Button(
                    onClick = { navToAuditLogs() },
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    shape = RoundedCornerShape(15.dp),
                ) {
                    Text(
                        stringResource(R.string.audit_logs_title),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                        textAlign = TextAlign.Start,
                    )
                }
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
