@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.act.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.act.ui.components.BookReader
import com.ilustris.sagai.ui.theme.components.SagaTopBar

/**
 * Dedicated Book Reader screen.
 *
 * Design notes:
 *  - The composable itself is edge-to-edge; illustrations bleed under the status bar.
 *  - All overlay controls use [statusBarsPadding] so they sit just below the status bar.
 *  - Page building is fully delegated to [BookReaderViewModel] via [BookPageMapper].
 */
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BookReaderView(
    sagaId: Int,
    initialActId: Int,
    onBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
    viewModel: BookReaderViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    BackHandler { onBack() }

    LaunchedEffect(sagaId, initialActId) {
        viewModel.load(sagaId, initialActId)
    }

    // PDF share intent — emitted as a one-shot SharedFlow event
    LaunchedEffect(Unit) {
        viewModel.pdfEvent.collect { event ->
            val intent =
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, event.uri)
                    putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_book_pdf_subject, event.title))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            context.startActivity(
                Intent.createChooser(intent, context.getString(R.string.share_book_pdf_chooser)),
            )
        }
    }

    with(sharedTransitionScope) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = state,
                label = "BookReaderTransition",
                transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(300)) },
                modifier = Modifier.fillMaxSize(),
            ) { currentState ->
                when (currentState) {
                    is BookReaderState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                        }
                    }

                    is BookReaderState.Generating -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            androidx.compose.foundation.layout.Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                CircularProgressIndicator()
                                currentState.message?.let { msg ->
                                    Text(
                                        text = msg,
                                        style =
                                            MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = currentState.actTitle.let { MaterialTheme.typography.bodySmall.fontFamily },
                                                textAlign = TextAlign.Center,
                                            ),
                                        modifier = Modifier.padding(horizontal = 32.dp),
                                    )
                                }
                            }
                        }
                    }

                    is BookReaderState.Ready -> {
                        SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
                            BookReader(
                                saga = currentState.saga,
                                act = currentState.currentAct,
                                pages = currentState.pages,
                                isLast = currentState.isLastAct,
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedContentScope = this@AnimatedContent,
                                onSelectNextVolume = viewModel::goToNextVolume,
                            )
                        }
                    }

                    is BookReaderState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = currentState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp),
                            )
                        }
                    }

                    // PDFGenerated is dispatched as a SharedFlow event; nothing to render here
                    is BookReaderState.PDFGenerated -> {
                        Unit
                    }
                }
            }

            // Overlay controls — always visible on top of the reader content
            // statusBarsPadding ensures they never hide behind the transparent status bar

            SagaTopBar(
                title = (state as? BookReaderState.Ready)?.currentAct?.data?.title ?: "",
                subtitle = emptyString(),
                genre = (state as? BookReaderState.Ready)?.saga?.data?.genre,
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .fillMaxWidth(),
                titleModifier =
                    Modifier.sharedElement(
                        rememberSharedContentState(
                            key = "saga_${sagaId}_title",
                        ),
                        animatedVisibilityScope = animatedVisibilityScope,
                    ),
                onBackClick = onBack,
                actionContent = {
                    if (state is BookReaderState.Ready) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = viewModel::regenerateBook,
                                modifier = Modifier.clip(CircleShape),
                            ) {
                                Icon(
                                    painterResource(R.drawable.baseline_refresh_24),
                                    contentDescription = stringResource(R.string.regenerate_book_cd),
                                    tint = MaterialTheme.colorScheme.onBackground,
                                )
                            }

                            IconButton(
                                onClick = viewModel::shareCurrentBook,
                                modifier = Modifier.clip(CircleShape),
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_share),
                                    contentDescription = stringResource(R.string.share_pdf_cd),
                                    tint = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }
                    }
                },
            )
        }
    }
}
