package com.ilustris.sagai.features.saga.chat.ui.components

import ReactionContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterProfile
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.Reaction
import com.ilustris.sagai.ui.theme.BubbleTailAlignment
import com.ilustris.sagai.ui.theme.ThoughtBubbleShape
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.dashedBorder
import com.ilustris.sagai.ui.theme.headerFont

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReactionsView(
    reactions: List<ReactionContent>,
    genre: Genre,
    onReactionsClick: () -> Unit,
) {
    var peekedReactionId by remember { mutableStateOf<Int?>(null) }
    SharedTransitionLayout {
        Row(
            horizontalArrangement = Arrangement.spacedBy((-4).dp),
            modifier = Modifier.padding(2.dp),
        ) {
            reactions.take(3).forEachIndexed { index, reaction ->
                val isPeeking by remember(peekedReactionId) {
                    mutableStateOf(peekedReactionId == reaction.data.id)
                }

                val reactionToolTipState =
                    androidx.compose.material3.rememberTooltipState(
                        isPersistent = true,
                    )
                val tooltipPositionProvider =
                    androidx.compose.material3.TooltipDefaults.rememberPlainTooltipPositionProvider(
                        spacingBetweenTooltipAndAnchor = 4.dp,
                    )

                LaunchedEffect(isPeeking) {
                    if (isPeeking) {
                        reactionToolTipState.show()
                    } else {
                        reactionToolTipState.dismiss()
                    }
                }
                TooltipBox(
                    positionProvider = tooltipPositionProvider,
                    state = reactionToolTipState,
                    onDismissRequest = {
                        peekedReactionId = null
                    },
                    tooltip = {
                        val thoughtBubbleShape =
                            ThoughtBubbleShape(
                                cornerRadius = genre.cornerSize(),
                                tailAlignment = BubbleTailAlignment.BottomLeft,
                                tailHeight = 0.dp,
                                tailWidth = 0.dp,
                            )

                        Text(
                            text = reaction.data.thought ?: "",
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = genre.bodyFont()),
                            modifier =
                                Modifier
                                    .padding(8.dp)
                                    .dashedBorder(
                                        strokeWidth = 1.dp,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        shape = thoughtBubbleShape,
                                        dashLength = 10.dp,
                                        gapLength = 5.dp,
                                    ).background(
                                        Brush.verticalGradient(MaterialTheme.colorScheme.background.darkerPalette()),
                                        thoughtBubbleShape,
                                    ).padding(8.dp),
                        )
                    },
                ) {
                    val animatedSize by animateDpAsState(if (isPeeking) 32.dp else 24.dp)
                    Box(
                        Modifier
                            .size(animatedSize)
                            .clip(CircleShape)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        awaitRelease()
                                        peekedReactionId = null
                                    },
                                    onTap = { onReactionsClick() },
                                    onLongPress = {
                                        if (reaction.data.thought
                                                .isNullOrEmpty()
                                                .not()
                                        ) {
                                            peekedReactionId = reaction.data.id
                                        }
                                    },
                                )
                            }.padding(4.dp),
                    ) {
                        AnimatedContent(
                            targetState = isPeeking,
                            label = "reactionAnimation",
                            transitionSpec = {
                                fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                            },
                        ) { peeking ->
                            if (peeking) {
                                CharacterAvatar(
                                    character = reaction.character,
                                    genre = genre,
                                    borderSize = 1.dp,
                                    softFocusRadius = 0f,
                                    grainRadius = 0f,
                                    modifier =
                                        Modifier
                                            .size(32.dp)
                                            .sharedElement(
                                                rememberSharedContentState(key = "reaction_avatar_${reaction.data.id}"),
                                                animatedVisibilityScope = this@AnimatedContent,
                                            ),
                                )
                            } else {
                                Box(
                                    modifier =
                                        Modifier
                                            .zIndex(reactions.size - index.toFloat())
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(
                                                MaterialTheme.colorScheme.background,
                                                CircleShape,
                                            ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = reaction.data.emoji,
                                        style =
                                            MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = genre.headerFont(),
                                                textAlign = TextAlign.Center,
                                            ),
                                        modifier =
                                            Modifier
                                                .sharedElement(
                                                    rememberSharedContentState(key = "reaction_avatar_${reaction.data.id}"),
                                                    animatedVisibilityScope = this@AnimatedContent,
                                                ),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ReactionsViewPreview() {
    val reactions =
        listOf(
            ReactionContent(
                data =
                    Reaction(
                        messageId = 0,
                        characterId = 0,
                        emoji = "😂",
                        thought = "This is hilarious!",
                    ),
                character =
                    Character(
                        name = "John",
                        details = Details(),
                        profile = CharacterProfile(),
                    ),
            ),
            ReactionContent(
                data =
                    Reaction(
                        messageId = 0,
                        characterId = 1,
                        emoji = "❤️",
                        thought = "I love this!",
                    ),
                character =
                    Character(
                        name = "Mary",
                        details = Details(),
                        profile = CharacterProfile(),
                    ),
            ),
        )
    ReactionsView(reactions = reactions, genre = Genre.FANTASY, onReactionsClick = {})
}
