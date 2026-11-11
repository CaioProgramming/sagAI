package com.ilustris.sagai.features.act.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shimmerize
import kotlin.time.Duration.Companion.seconds

/**
 * One sheet for the whole export: pick a format, wait while it is written, then send or keep the
 * finished file. The steps swap in place so the user always sees where the export stands.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookExportSheet(
    state: BookExportState,
    genre: Genre?,
    onChoose: (BookShareFormat) -> Unit,
    onSave: (android.net.Uri) -> Unit,
    onDismiss: () -> Unit,
) {
    SagAITheme(genre) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                )
            },
        ) {
            AnimatedContent(
                targetState = state,
                contentKey = { it::class },
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "bookExportStep",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp)
                        .navigationBarsPadding(),
            ) { step ->
                when (step) {
                    is BookExportState.Choosing -> ChooseFormat(onChoose = onChoose)
                    is BookExportState.Generating -> Generating(genre = genre)
                    is BookExportState.Ready -> Ready(state = step, onSave = onSave, onDone = onDismiss)
                    is BookExportState.Failed ->
                        Failed(message = step.message, onRetry = { onChoose(step.format) })
                }
            }
        }
    }
}

@Composable
private fun SheetTitle(
    title: String,
    subtitle: String? = null,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
        )
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ChooseFormat(onChoose: (BookShareFormat) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SheetTitle(
            title = stringResource(R.string.book_export_title),
            subtitle = stringResource(R.string.book_export_subtitle),
        )
        Spacer(Modifier.size(4.dp))
        FormatOption(
            title = stringResource(R.string.book_export_pdf_title),
            description = stringResource(R.string.book_export_pdf_description),
            onClick = { onChoose(BookShareFormat.PDF) },
        )
        FormatOption(
            title = stringResource(R.string.book_export_epub_title),
            description = stringResource(R.string.book_export_epub_description),
            onClick = { onChoose(BookShareFormat.EPUB) },
        )
    }
}

@Composable
private fun FormatOption(
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Generating(genre: Genre?) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp)
                .then(
                    if (genre != null) {
                        Modifier.reactiveShimmer(
                            isPlaying = true,
                            shimmerColors = genre.color.shimmerize(),
                            duration = 2.seconds,
                            targetValue = 1200f,
                            repeatMode = RepeatMode.Restart,
                        )
                    } else {
                        Modifier
                    },
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (genre != null) {
            Icon(
                painter = painterResource(genre.icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(56.dp),
            )
        }
        Text(
            text = stringResource(R.string.book_share_preparing),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Ready(
    state: BookExportState.Ready,
    onSave: (android.net.Uri) -> Unit,
    onDone: () -> Unit,
) {
    val context = LocalContext.current
    val saveLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument(state.format.mimeType),
        ) { destination -> destination?.let(onSave) }
    val chooserTitle =
        when (state.format) {
            BookShareFormat.PDF -> R.string.share_book_pdf_chooser
            BookShareFormat.EPUB -> R.string.share_book_epub_chooser
        }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SheetTitle(
            title = stringResource(R.string.book_export_ready_title),
            subtitle = state.file.name,
        )
        Spacer(Modifier.size(4.dp))
        Button(
            onClick = {
                val intent =
                    Intent(Intent.ACTION_SEND).apply {
                        type = state.format.mimeType
                        putExtra(Intent.EXTRA_STREAM, state.uri)
                        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_book_pdf_subject, state.title))
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                context.startActivity(Intent.createChooser(intent, context.getString(chooserTitle)))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(painterResource(R.drawable.ic_send), contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                text = stringResource(R.string.book_export_send),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        OutlinedButton(
            onClick = { saveLauncher.launch(state.file.name) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(painterResource(R.drawable.ic_download), contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                text = stringResource(R.string.book_export_save),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        when (state.saveStatus) {
            BookSaveStatus.Saved ->
                SaveFeedback(R.string.book_export_saved, MaterialTheme.colorScheme.primary)

            BookSaveStatus.Failed ->
                SaveFeedback(R.string.book_export_save_failed, MaterialTheme.colorScheme.error)

            BookSaveStatus.Idle -> Unit
        }
        TextButton(onClick = onDone, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(stringResource(R.string.book_export_done))
        }
    }
}

@Composable
private fun SaveFeedback(
    text: Int,
    color: androidx.compose.ui.graphics.Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(text),
            style = MaterialTheme.typography.labelLarge,
            color = color,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Failed(
    message: String,
    onRetry: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SheetTitle(title = stringResource(R.string.book_export_failed_title), subtitle = message)
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(stringResource(R.string.try_again))
        }
    }
}
