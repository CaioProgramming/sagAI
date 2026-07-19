package com.ilustris.sagai.features.player.ui

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.features.player.data.model.ProfileTopic
import com.ilustris.sagai.features.player.ui.onboarding.UserNamePromptDialog
import com.ilustris.sagai.features.playthrough.AnimatedPlaytimeCounter
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.sagaBrush
import com.ilustris.sagai.ui.theme.themeShimmer
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProfileView(
    viewModel: PlayerProfileViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showUserNameSheet by remember { mutableStateOf(false) }
    var showEnrichmentDialog by remember { mutableStateOf(false) }
    val playthroughs by viewModel.availableEndedSagas.collectAsStateWithLifecycle(emptyList())

    LaunchedEffect(Unit) {
        viewModel.loadJourneyReview()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
        ) {
            // Toolbar
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        painterResource(R.drawable.ic_back_left),
                        stringResource(R.string.back_button_description),
                    )
                }
                Text(
                    stringResource(R.string.player_profile_title),
                    style =
                        MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    modifier = Modifier.weight(1f),
                )
            }

            if (uiState.isEmpty) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            buildString {
                                append(stringResource(R.string.home_greeting_prefix))
                                append(" ")
                                append(uiState.userName)
                            },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            stringResource(R.string.player_profile_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .alpha(.5f),
                        )

                        if (uiState.canBuildProfile) {
                            Button(
                                onClick = { showEnrichmentDialog = true },
                                modifier =
                                    Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .padding(top = 16.dp),
                            ) {
                                Text("Build Profile ✨")
                            }
                        }
                    }
                }

                TextButton(
                    onClick = {
                        showUserNameSheet = true
                    },
                    modifier =
                        Modifier
                            .padding(horizontal = 32.dp)
                            .fillMaxWidth(),
                ) {
                    Text(
                        stringResource(R.string.player_name_prompt_title),
                    )
                }
            } else {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                ) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            AnimatedPlaytimeCounter(
                                uiState.totalPlaytime,
                                animationDuration = 10.seconds,
                                textStyle =
                                    MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Black,
                                    ),
                                labelStyle = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(8.dp),
                                label =
                                    stringResource(
                                        R.string.total_playtime_label,
                                    ),
                            )
                        }
                    }

                    item {
                        if (uiState.userName.isNotEmpty()) {
                            Text(
                                buildString {
                                    append(stringResource(R.string.home_greeting_prefix))
                                    append(" ")
                                    append(uiState.userName)
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    item {
                        AnimatedVisibility(
                            visible = uiState.isReviewLoading,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .reactiveShimmer(true, themeShimmer()),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "thinking about you...",
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.alpha(.5f),
                                )
                            }
                        }
                    }

                    items(uiState.topics) { topic ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { visible = true }
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn() + expandVertically(),
                        ) {
                            TopicCard(topic)
                        }
                    }

                    item {
                        AnimatedVisibility(
                            visible = uiState.journeyReview != null,
                            enter = fadeIn() + expandVertically(),
                        ) {
                            uiState.journeyReview?.let { review ->
                                Column(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        text = review.title,
                                        style =
                                            MaterialTheme.typography.headlineSmall.copy(
                                                shadow = Shadow(Color.White, blurRadius = 10f),
                                                brush = sagaBrush(),
                                            ),
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = review.review,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                }
                            }
                        }
                    }

                    item {
                        TextButton(
                            onClick = {},
                            modifier =
                                Modifier
                                    .padding(horizontal = 32.dp)
                                    .fillMaxWidth(),
                        ) {
                            Text(
                                stringResource(R.string.player_name_prompt_title),
                            )
                        }
                    }

                    if (uiState.canBuildProfile) {
                        item {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Button(
                                    { showEnrichmentDialog = true },
                                ) {
                                    Icon(
                                        painterResource(R.drawable.center_spark),
                                        null,
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                    )
                                    Text("Build Profile")
                                }
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(50.dp))
                    }
                }
            }
        }

        StarryLoader(
            isLoading = uiState.isEnrichingProfile,
            loadingMessage = uiState.enrichmentProgress ?: "Building your profile...",
        )
    }

    if (showUserNameSheet) {
        UserNamePromptDialog(
            onSaveName = {
                viewModel.saveName(it)
            },
            onDismiss = { showUserNameSheet = false },
        )
    }

    if (showEnrichmentDialog) {
        ModalBottomSheet(
            onDismissRequest = { showEnrichmentDialog = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Build your profile ✨",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                )

                Text(
                    text = "I'll analyze your finished sagas to build a deeper profile of your style and personality.",
                    style = MaterialTheme.typography.bodyMedium,
                )

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceContainer,
                                RoundedCornerShape(12.dp),
                            ).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Sagas to be analyzed:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.alpha(0.6f),
                    )

                    playthroughs.forEach { playthrough ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_spark),
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = playthrough.data.title,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        showEnrichmentDialog = false
                        viewModel.buildProfileFromHistory()
                    },
                    modifier =
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Start Analysis")
                }
            }
        }
    }
}

@Composable
private fun TopicCard(topic: ProfileTopic) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (topic.title.isNotBlank()) {
            Text(
                topic.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Text(
            topic.content,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )

        topic.comment?.let {
            if (it.isNotBlank()) {
                Text(
                    it,
                    style = MaterialTheme.typography.labelMedium,
                    modifier =
                        Modifier
                            .alpha(0.7f)
                            .fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )
            }
        }

        HorizontalDivider(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
            thickness = 1.dp,
        )
    }
}
