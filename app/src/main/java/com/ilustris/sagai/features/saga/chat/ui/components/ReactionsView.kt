package com.ilustris.sagai.features.saga.chat.ui.components

import ReactionContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.dashedBorder
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.shape

@OptIn(ExperimentalSharedTransitionApi::class)
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
                val scale by animateFloatAsState(
                    targetValue = if (isPeeking) 1.2f else 1.0f,
                    label = "scale",
                )

                AnimatedContent(
                    targetState = isPeeking,
                    label = "reactionAnimation",
                    transitionSpec = {
                        fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                    },
                ) { peeking ->
                    if (peeking) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier =
                                Modifier
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                tryAwaitRelease()
                                                peekedReactionId = null
                                            },
                                        )
                                    }.zIndex(100f),
                        ) {
                            Text(
                                text = reaction.data.thought ?: "",
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = genre.bodyFont()),
                                modifier =
                                    Modifier
                                        .padding(8.dp)
                                        .dashedBorder(
                                            1.dp,
                                            MaterialTheme.colorScheme.onBackground,
                                            genre.shape(),
                                            dashLength = 10.dp,
                                            gapLength = 10.dp,
                                        ).background(
                                            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = .3f),
                                            genre.shape(),
                                        ).padding(8.dp),
                            )
                            CharacterAvatar(
                                character = reaction.character,
                                genre = genre,
                                modifier =
                                    Modifier
                                        .size(32.dp)
                                        .sharedElement(
                                            rememberSharedContentState(key = "reaction_avatar_${reaction.data.id}"),
                                            animatedVisibilityScope = this@AnimatedContent,
                                        ),
                            )
                        }
                    } else {
                        Box(
                            modifier =
                                Modifier
                                    .zIndex(reactions.size - index.toFloat())
                                    .scale(scale)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.background, CircleShape)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onTap = { onReactionsClick() },
                                            onLongPress = {
                                                peekedReactionId = reaction.data.id
                                            },
                                        )
                                    }.padding(2.dp),
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
