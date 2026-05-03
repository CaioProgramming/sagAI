@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.act.ui

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.features.act.ui.components.BookReader
import com.ilustris.sagai.features.act.ui.components.BookShelf
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findAct
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.DetailSectionView
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.LargeHorizontalHeader
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.reactiveShimmer

@Composable
fun ChronicleView(
    section: DetailSectionView.ActSection,
    saga: SagaContent,
    initialActId: Int? = null,
    onClose: () -> Unit,
) {
    val viewModel: ChronicleViewModel = hiltViewModel()
    val selectedBook by viewModel.selectedBook.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
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

            AnimatedVisibility(isReading.not()) {
                LargeHorizontalHeader(
                    section.title,
                    section.subtitle,
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
                    if (isReading) genre.resolveColor().copy(alpha = .5f) else genre.resolveColor(),
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
                    if (book == null) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            BookShelf(
                                saga = saga,
                                acts = section.acts,
                                selectedBook = selectedBook,
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedContentScope = this@AnimatedContent,
                                onBookSelected = viewModel::selectBook,
                                isLoading = state is ChronicleState.Generating,
                            )
                        }
                    } else {
                        val acts = remember { saga.acts }
                        val isLast = saga.findAct(book.data.id) == saga.acts.last()

                        BookReader(
                            saga = saga,
                            act = book,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@AnimatedContent,
                            isLast = isLast,
                            onSelectNextVolume = {
                                saga.findAct(book.data.id)?.let {
                                    val index = acts.indexOf(it)
                                    if (index != acts.lastIndex) {
                                        viewModel.generateNextVolume(acts[index + 1])
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }

        AnimatedContent(state) {
            when (it) {
                is ChronicleState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        com.ilustris.sagai.ui.theme.components.SagaLoader(
                            modifier =
                                Modifier.size(
                                    100.dp,
                                ),
                        )
                    }
                }

                is ChronicleState.Generating -> {
                    it.message?.let { message ->
                        Text(
                            message,
                            style =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = genre.bodyFont(),
                                    shadow =
                                        Shadow(
                                            color = Color.White,
                                            blurRadius = 5f,
                                        ),
                                    textAlign = TextAlign.Center,
                                ),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .reactiveShimmer(
                                        true,
                                        genre.shimmerColors(),
                                        repeatMode = RepeatMode.Restart,
                                        targetValue = 800f,
                                    ),
                        )
                    }
                }

                else -> {}
            }
        }
    }
}
