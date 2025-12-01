package com.ilustris.sagai.core.file.backup.ui

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.ilustris.sagai.core.permissions.PermissionComponent
import com.ilustris.sagai.core.permissions.PermissionService
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.shape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSheet(
    displayBackups: Boolean = false,
    onDismiss: () -> Unit = {},
    importUri: Uri? = null,
    onConfirmImport: (Uri) -> Unit = {},
    viewModel: BackupViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val backupLauncher =
        PermissionService.rememberBackupLauncher {
            viewModel.saveBackupFolder(it, displayBackups)
        }

    LaunchedEffect(importUri) {
        importUri?.let {
            viewModel.setImportConfirmation(it)
        }
    }

    AnimatedContent(state) {
        when (it) {
            BackupUiState.BackupEnabled, BackupUiState.Dimissed, BackupUiState.CheckingUp -> Box {}
            is BackupUiState.Empty -> {
                ModalBottomSheet(onDismissRequest = {
                    viewModel.dismiss()
                    onDismiss()
                }) {
                    Text(
                        it.message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(32.dp)
                            .fillMaxWidth(),
                    )
                }
            }
            is BackupUiState.Loading -> StarryLoader(true, it.message, textStyle = MaterialTheme.typography.labelMedium)

            is BackupUiState.RequiresPermission ->
                PermissionComponent(
                    it.permission,
                    onConfirm = {
                        viewModel.dismiss()
                        backupLauncher.launch(null)
                    },
                    onDismiss = {
                        viewModel.dismiss()
                        onDismiss()
                    },
                )


            is BackupUiState.ImportConfirmation -> {
                val context = androidx.compose.ui.platform.LocalContext.current
                ModalBottomSheet(onDismissRequest = {
                    viewModel.dismiss()
                    onDismiss()
                }) {
                    val title = when (it.preview) {
                        is BackupPreview.Full -> "Import ${it.preview.sagas.size} sagas?"
                        is BackupPreview.Single -> "Import Saga?"
                        null -> "Import Backup"
                    }

                    LazyVerticalGrid(
                        columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        stickyHeader(key = "header") {
                            Text(
                                title,
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }

                        when (val preview = it.preview) {
                            is BackupPreview.Full -> {
                                items(preview.sagas) { info ->
                                    BackupSagaCard(
                                        title = info.title,
                                        genre = info.genre,
                                        description = info.description,
                                        icon = info.icon,
                                        modifier = Modifier.aspectRatio(.7f)
                                    )
                                }
                            }

                            is BackupPreview.Single -> {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    BackupSagaCard(
                                        title = preview.info.title,
                                        genre = preview.info.genre,
                                        description = preview.info.description,
                                        icon = preview.info.icon,
                                        modifier = Modifier.aspectRatio(.7f)
                                    )
                                }
                            }

                            null -> {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            it.uri.lastPathSegment ?: "Unknown file",
                                            style = MaterialTheme.typography.bodyLarge,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            "Size: ${
                                                android.text.format.Formatter.formatFileSize(
                                                    context,
                                                    it.size
                                                )
                                            }",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.dismiss()
                                        onDismiss()
                                    },
                                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    modifier = Modifier.fillMaxWidth(1f)
                                ) {
                                    Text("Cancel")
                                }

                                Button(
                                    onClick = {
                                        onConfirmImport(it.uri)
                                        viewModel.dismiss()
                                        onDismiss()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Import")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BackupSagaCard(
    title: String,
    genre: com.ilustris.sagai.features.newsaga.data.model.Genre,
    description: String,
    icon: android.graphics.Bitmap?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(genre.shape())
            .border(1.dp, genre.gradient(true), genre.shape())
            .background(MaterialTheme.colorScheme.background.gradientFade(), genre.shape())

    ) {
        if (icon != null) {
            AsyncImage(
                model = icon,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = genre.headerFont(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    brush = genre.gradient(true),
                    shadow = androidx.compose.ui.graphics.Shadow(Color.White, blurRadius = 5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )


            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = genre.bodyFont(),
                    textAlign = TextAlign.Start,
                    color = genre.iconColor
                ),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}
