@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.act.ui

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.ui.components.BookReader
import com.ilustris.sagai.features.act.ui.components.BookShelf
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findAct
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.LargeHorizontalHeader
import com.ilustris.sagai.ui.theme.headerFont

@Composable
fun ChronicleView(
    title: String,
    subtitle: String,
    saga: SagaContent,
    acts: List<ActContent>,
    initialActId: Int? = null,
    titleModifier: Modifier = Modifier,
    onClose: () -> Unit,
) {
    val viewModel: ChronicleViewModel = hiltViewModel()
    val selectedBook by viewModel.selectedBook.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val visualConfig by viewModel.visualConfig.collectAsStateWithLifecycle()
    val genre = saga.data.genre

    val context = LocalContext.current

    LaunchedEffect(initialActId) {
        viewModel.start(saga)
        if (initialActId != null) {
            viewModel.selectBookById(saga, initialActId)
        }
    }

    LaunchedEffect(state) {
        if (state is ChronicleState.PDFGenerated) {
            val shareState = state as ChronicleState.PDFGenerated
            val intent =
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, shareState.uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Sharing ${shareState.title} from Sagas")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            context.startActivity(Intent.createChooser(intent, "Share Book PDF"))
        }
    }

    // Void Prism: Hide UI elements when reading to maximize focus
    val isReading = selectedBook != null

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier
                .statusBarsPadding()
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(
                        onClick = {
                            if (selectedBook == null) {
                                onClose()
                            } else {
                                viewModel.selectBook(null)
                            }
                        },
                        modifier =
                            Modifier
                                .clip(CircleShape),
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_back_left),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }

                    if (isReading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    selectedBook?.let { viewModel.regenerateBook(it) }
                                },
                                modifier =
                                    Modifier
                                        .clip(CircleShape),
                            ) {
                                Icon(
                                    painterResource(R.drawable.baseline_refresh_24),
                                    contentDescription = "Regenerate Book",
                                    tint = MaterialTheme.colorScheme.onBackground,
                                )
                            }

                            IconButton(
                                onClick = {
                                    selectedBook?.let { viewModel.shareBook(it) }
                                },
                                modifier =
                                    Modifier
                                        .clip(CircleShape),
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_share),
                                    contentDescription = "Share PDF",
                                    tint = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(isReading.not()) {
                    LargeHorizontalHeader(
                        title,
                        subtitle,
                        titleStyle =
                            MaterialTheme.typography.displaySmall.copy(
                                fontFamily = genre.headerFont(),
                            ),
                        subtitleStyle =
                            MaterialTheme.typography.labelMedium.copy(
                                fontFamily = genre.bodyFont(),
                            ),
                        modifier =
                            Modifier
                                .fillMaxWidth(),
                        titleModifier = titleModifier,
                    )
                }
            }

            SharedTransitionLayout(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize(),
                ) {
                    val starColor by animateColorAsState(
                        if (isReading) {
                            genre
                                .resolveColor()
                                .copy(alpha = .5f)
                        } else {
                            genre.resolveColor()
                        },
                    )
                    StarryTextPlaceholder(
                        modifier = Modifier.fillMaxSize(),
                        starColor,
                    )
                    AnimatedContent(
                        targetState = selectedBook,
                        label = "ChronicleTransition",
                        transitionSpec = {
                            fadeIn(tween(500)) togetherWith fadeOut(tween(500))
                        },
                        modifier = Modifier.fillMaxSize(),
                    ) { book ->
                        if (book == null || state is ChronicleState.Generating) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                BookShelf(
                                    saga = saga,
                                    acts = acts,
                                    selectedBook = selectedBook,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedContentScope = this@AnimatedContent,
                                    onBookSelected = viewModel::selectBook,
                                    isLoading = state is ChronicleState.Generating,
                                    reasoning = (state as? ChronicleState.Generating)?.message,
                                    generatingActTitle = (state as? ChronicleState.Generating)?.actTitle,
                                    visualConfig = visualConfig,
                                )
                            }
                        } else {
                            val actsList = remember { saga.acts }
                            val isLast = saga.findAct(book.data.id) == saga.acts.last()

                            BookReader(
                                saga = saga,
                                act = book,
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedContentScope = this@AnimatedContent,
                                isLast = isLast,
                                onSelectNextVolume = {
                                    saga.findAct(book.data.id)?.let {
                                        val index = actsList.indexOf(it)
                                        if (index != actsList.lastIndex) {
                                            viewModel.generateNextVolume(actsList[index + 1])
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
