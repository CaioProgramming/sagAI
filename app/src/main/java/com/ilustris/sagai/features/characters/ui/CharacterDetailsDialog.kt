package com.ilustris.sagai.features.characters.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.toColorInt
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.ui.components.CharacterSection
import com.ilustris.sagai.features.characters.ui.components.CharacterStats
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.GradientType
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.headerFont
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CharacterDetailsDialog(
    character: Character,
    genre: Genre,
    onDismissRequest: () -> Unit,
) {
    val isExpanded =
        remember {
            mutableStateOf(false)
        }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = isExpanded.value.not(),
            ),
    ) {
        val cornerSize =
            animateDpAsState(
                targetValue = if (isExpanded.value) 0.dp else genre.cornerSize(),
                animationSpec = tween(durationMillis = 300),
                label = "cornerSize",
            )
        val cardHeight =
            animateFloatAsState(
                targetValue = if (isExpanded.value) 1f else .6f,
                animationSpec = tween(durationMillis = 300),
                label = "cardHeight",
            )
        val cardBorderColor by animateColorAsState(
            targetValue = if (isExpanded.value) Color.Transparent else genre.color,
            animationSpec = tween(durationMillis = 300),
            label = "cardBorderColor",
        )
        Card(
            modifier =
                Modifier
                    .border(
                        2.dp,
                        cardBorderColor.gradientFade(),
                        RoundedCornerShape(cornerSize.value),
                    ).fillMaxWidth()
                    .fillMaxHeight(cardHeight.value)
                    .animateContentSize(
                        animationSpec = tween(durationMillis = 100),
                    ),
            shape = RoundedCornerShape(cornerSize.value),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background, // Or any color you want
                ),
        ) {
            Column(
                modifier =
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clipToBounds(),
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "infinite zoom")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.5f,
                        animationSpec =
                            infiniteRepeatable(
                                animation = tween(durationMillis = 1.minutes.toInt(DurationUnit.MILLISECONDS)),
                                repeatMode = RepeatMode.Reverse,
                            ),
                        label = "imageZoom",
                    )

                    AsyncImage(
                        character.image,
                        contentDescription = character.name,
                        contentScale = ContentScale.Crop,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    transformOrigin = TransformOrigin.Center,
                                ).clipToBounds(),
                    )
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                fadeGradientBottom(),
                            ),
                    )

                    IconButton(
                        onClick = {
                            onDismissRequest()
                        },
                        modifier =
                            Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                                .size(32.dp)
                                .background(
                                    MaterialTheme.colorScheme.background.copy(alpha = .7f),
                                    CircleShape,
                                ).padding(8.dp),
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Fechar",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }

                    IconButton(
                        onClick = {
                            isExpanded.value = !isExpanded.value
                        },
                        modifier =
                            Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .size(32.dp)
                                .background(
                                    MaterialTheme.colorScheme.background.copy(alpha = .7f),
                                    CircleShape,
                                ).padding(8.dp),
                    ) {
                        val icon =
                            if (isExpanded.value) R.drawable.ic_shrink else R.drawable.ic_expand
                        val description = if (isExpanded.value) "Fechar" else "Expandir"
                        AnimatedContent(icon, transitionSpec = {
                            scaleIn() + fadeIn() with fadeOut() + scaleOut()
                        }) {
                            Icon(
                                painterResource(it),
                                description,
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }

                val characterColor = Color(character.hexColor.toColorInt())

                Text(
                    text = character.name,
                    style =
                        MaterialTheme.typography.displaySmall.copy(
                            fontFamily = genre.headerFont(),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Normal,
                            brush =
                                gradientAnimation(
                                    characterColor.darkerPalette(),
                                    gradientType = GradientType.VERTICAL,
                                ),
                        ),
                    modifier =
                        Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                )

                Text(
                    text = character.details.occupation,
                    style =
                        MaterialTheme.typography.titleSmall.copy(
                            fontFamily = genre.headerFont(),
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                        ),
                    modifier =
                        Modifier
                            .padding(vertical = 4.dp)
                            .fillMaxWidth(),
                )

                CharacterStats(character = character, genre = genre)

                if (isExpanded.value) {
                    CharacterSection(
                        title = "Backstory",
                        content = character.backstory,
                        genre = genre,
                    )

                    CharacterSection(
                        title = "Personality",
                        content = character.details.personality,
                        genre = genre,
                    )

                    CharacterSection(
                        title = "Appearance",
                        content = character.details.appearance,
                        genre = genre,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun CharacterDetailsDialogPreview() {
    val character =
        Character(
            name = "Character Name",
            backstory = "Character backstory",
            image = "https://www.example.com/image.jpg",
            details = Details(occupation = "Occupation", race = "Human"),
        )
    val genre = Genre.FANTASY
    CharacterDetailsDialog(
        character = character,
        genre = genre,
        onDismissRequest = {},
    )
}
