package com.ilustris.sagai.core.file.backup.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.file.backup.RestorableSaga
import com.ilustris.sagai.core.file.backup.toSaga
import com.ilustris.sagai.features.newsaga.ui.components.SagaCard
import com.ilustris.sagai.ui.theme.shape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoverBackupSheet(
    isVisible: Boolean,
    content: List<RestorableSaga>,
    onRequestRecover: (RestorableSaga) -> Unit,
    onRecoverAll: (List<RestorableSaga>) -> Unit,
    onDismiss: () -> Unit,
) {
    if (isVisible) {
        ModalBottomSheet(onDismissRequest = {
            onDismiss()
        }) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
            ) {
                stickyHeader {
                    Text(
                        stringResource(R.string.recover_backup_sheet_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                item(span = { GridItemSpan(2) }) {
                    Text(
                        stringResource(R.string.recover_backup_sheet_description),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                items(content) {
                    val saga = it.manifest.toSaga()
                    SagaCard(
                        saga,
                        bitmap = it.iconBitmap?.asImageBitmap(),
                        modifier =
                            Modifier
                                .clip(saga.genre.shape())
                                .aspectRatio(.8f)
                                .clickable {
                                    onRequestRecover(it)
                                },
                    )
                }

                item(span = { GridItemSpan(2) }) {
                    Button(onClick = {
                        onRecoverAll(content)
                    }) {
                        Icon(
                            painterResource(R.drawable.ic_restore),
                            null,
                            modifier = Modifier.size(24.dp),
                        )
                        Text(stringResource(R.string.recover_backup_sheet_button_recover_all))
                    }
                }
            }
        }
    }
}
