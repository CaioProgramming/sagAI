package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.saga.chat.domain.model.rankTopCharacters
import com.ilustris.sagai.features.saga.detail.data.model.ReviewStage
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.ui.theme.sagaBrush
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class ReviewCharactersPage(
    override val content: SagaContent,
    private val stage: ReviewStage,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.CHARACTERS

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val genre =
            remember {
                content.data.genre
            }
        var showTopCharacterName by remember {
            mutableStateOf(false)
        }

        var showCharacters by remember {
            mutableStateOf(false)
        }

        var shareCharactersButton by remember {
            mutableStateOf(false)
        }

        val topCharacters =
            remember {
                content
                    .flatMessages()
                    .rankTopCharacters(content.getCharacters(true))
                    .take(5)
            }

        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            coroutineScope.launch {
                delay(2.seconds)
                showTopCharacterName = true
            }
        }

        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            SharedTransitionLayout {
                AnimatedContent(showCharacters, transitionSpec = {
                    fadeIn(tween(1500)) togetherWith fadeOut(tween(600))
                }) { showList ->
                    if (!showList) {
                        topCharacters.firstOrNull()?.let {
                            ReviewTopCharacterContent(
                                it.first,
                                it.second,
                                genre,
                                stage.content?.subtitle,
                                imageModifier =
                                    Modifier
                                        .sharedElement(
                                            rememberSharedContentState("character-${it.first.id}"),
                                            this@AnimatedContent,
                                        ),
                                onAnimationFinished = {
                                    coroutineScope.launch {
                                        delay(2.seconds)
                                        showCharacters = true
                                    }
                                },
                            )
                        }
                    } else {
                        LazyColumn(
                            Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
                                .animateContentSize(
                                    tween(1000, easing = EaseIn),
                                ).animateEnterExit(
                                    enter = slideInVertically(tween(1500, easing = EaseIn)) { -it },
                                    exit = slideOutVertically { it },
                                ),
                            userScrollEnabled = false,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            item {
                                stage.content?.title?.let { title ->
                                    Text(
                                        title,
                                        style =
                                            MaterialTheme.typography.titleLarge.copy(
                                                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                                fontWeight = FontWeight.Bold,
                                                brush = sagaBrush(),
                                            ),
                                    )
                                }
                            }

                            items(topCharacters) { character ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier =
                                        Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                ) {
                                    val position = topCharacters.indexOf(character) + 1
                                    Text(
                                        position.toString(),
                                        style =
                                            MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                            ),
                                    )

                                    CharacterAvatar(
                                        character.first,
                                        genre = genre,
                                        borderSize = 2.dp,
                                        modifier =
                                            Modifier
                                                .size(64.dp)
                                                .sharedElement(
                                                    rememberSharedContentState("character-${character.first.id}"),
                                                    this@AnimatedContent,
                                                ),
                                    )

                                    Column {
                                        Text(
                                            character.first.name,
                                            style =
                                                MaterialTheme.typography.headlineSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                                ),
                                        )

                                        Text(
                                            stringResource(
                                                R.string.messages_count_label,
                                                character.second,
                                            ),
                                            style =
                                                MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                                ),
                                        )
                                    }
                                }
                            }

                            if (shareCharactersButton) {
                                item {
                                    Button(
                                        onClick = {
                                            onAction(ReviewAction.Share(ShareType.RELATIONS))
                                        },
                                        colors =
                                            ButtonDefaults.elevatedButtonColors().copy(
                                                containerColor = MaterialTheme.colorScheme.onBackground,
                                                contentColor = MaterialTheme.colorScheme.primary,
                                            ),
                                        modifier = Modifier.padding(16.dp),
                                    ) {
                                        Text(
                                            stringResource(R.string.share),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        LaunchedEffect(showTopCharacterName) {
            if (showTopCharacterName && canAnimate) {
                delay(10.seconds)
                showCharacters = true
                showTopCharacterName = false
            }
        }

        LaunchedEffect(showCharacters) {
            if (showCharacters) {
                delay(2.seconds)
                shareCharactersButton = true
            }
        }
    }
}
