package com.ilustris.sagai.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.resolveIconColor
import com.ilustris.sagai.features.newsaga.data.model.resolveUrl
import com.ilustris.sagai.features.newsaga.data.usecase.SagaBook
import com.ilustris.sagai.features.newsaga.ui.LocalGenderPlaceholders
import com.ilustris.sagai.features.newsaga.ui.presentation.AgenticAction
import com.ilustris.sagai.ui.animations.genreVfx
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import com.ilustris.sagai.ui.theme.shimmerize

@Composable
fun CosmicBook(
    book: SagaBook,
    visualConfig: GenreVisualConfig,
    isOpened: Boolean,
    lockedCharacter: CharacterInfo? = null,
    isLoading: Boolean = false,
    onToggle: () -> Unit,
    onAction: (AgenticAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isOpened) -180f else 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "BookRotation",
    )

    val scale by animateFloatAsState(
        targetValue = if (isOpened) 1.08f else 1f,
        animationSpec = tween(500),
        label = "BookScale",
    )

    val genre = book.draft.genre
    val shape =
        RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp)
    val color = genre.resolveColor(visualConfig)
    var currentPage by remember { mutableIntStateOf(0) }
    var showEditor by remember { mutableStateOf(false) }

    LaunchedEffect(isOpened) {
        if (!isOpened) {
            currentPage = 0
            showEditor = false
        }
    }

    Box(
        modifier =
            modifier
                .graphicsLayer {
                    this.scaleX = scale
                    this.scaleY = scale
                }.aspectRatio(3f / 4f)
                .dropShadow(shape) {
                    this.color = color.copy(alpha = 0.5f)
                    this.radius = 20f
                    this.spread = 5f
                },
    ) {
        // --- 1. Inside Pages (Always there, covered by Front Cover) ---
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(genre.resolveColor())
                    .background(MaterialTheme.colorScheme.background.copy(alpha = .6f))
                    .border(1.dp, Color.LightGray.copy(alpha = 0.2f), shape)
                    .reactiveShimmer(isLoading, color.shimmerize()),
        ) {
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    (fadeIn() + scaleIn(initialScale = 0.95f)).togetherWith(
                        fadeOut() +
                            scaleOut(
                                targetScale = 1.05f,
                            ),
                    )
                },
                label = "BookPageContent",
            ) { page ->
                when (page) {
                    0 -> {
                        // Page 0: Synopsis
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                IconButton(
                                    onClick = { showEditor = true },
                                    modifier =
                                        Modifier
                                            .padding(8.dp)
                                            .size(24.dp)
                                            .align(Alignment.End),
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_edit),
                                        contentDescription = "Edit",
                                        tint = genre.resolveIconColor(),
                                        modifier = Modifier.size(12.dp),
                                    )
                                }

                                Text(
                                    text = book.draft.title,
                                    style =
                                        MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = genre.headerFont(),
                                            fontSize = 20.sp,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            textAlign = TextAlign.Center,
                                        ),
                                    modifier = Modifier.fillMaxWidth().genreVfx(genre),
                                )

                                HorizontalDivider(modifier = Modifier.alpha(0.2f))

                                Text(
                                    text = book.draft.description,
                                    style =
                                        MaterialTheme.typography.bodyMedium.copy(
                                            lineHeight = 20.sp,
                                            fontFamily = genre.bodyFont(),
                                            color = MaterialTheme.colorScheme.onBackground,
                                        ),
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .verticalScroll(rememberScrollState()),
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    IconButton(
                                        onClick = { onAction(AgenticAction.EnhanceSaga(book.draft)) },
                                        modifier =
                                            Modifier
                                                .size(24.dp),
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_full_spark),
                                            contentDescription = "Enhance",
                                            tint = genre.resolveIconColor(),
                                            modifier =
                                                Modifier.size(24.dp).gradientFill(
                                                    Brush.linearGradient(holographicGradient),
                                                ),
                                        )
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                    IconButton(
                                        onClick = onToggle,
                                        modifier =
                                            Modifier
                                                .size(24.dp),
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.round_close_24),
                                            contentDescription = "Close",
                                            tint = genre.resolveIconColor(),
                                            modifier = Modifier.size(24.dp),
                                        )
                                    }
                                }

                                Row(
                                    modifier =
                                        Modifier
                                            .clip(genre.shape())
                                            .clickable {
                                                currentPage = 1
                                            }.padding(4.dp)
                                            .align(Alignment.End)
                                            .alpha(.5f),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        "See protagonists",
                                        style =
                                            MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                textAlign = TextAlign.End,
                                                color = genre.resolveIconColor(),
                                            ),
                                    )

                                    Icon(
                                        painterResource(R.drawable.round_arrow_forward_ios_24),
                                        null,
                                        tint = genre.resolveIconColor(),
                                        modifier = Modifier.size(12.dp),
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        // Page 1: Characters
                        AnimatedContent(
                            targetState = lockedCharacter,
                            label = "CharacterPageContent",
                        ) { selected ->
                            if (selected != null && book.characters.any { it.id == selected.id }) {
                                ExpandedCharacterPage(
                                    character = selected,
                                    genre = genre,
                                    visualConfig = visualConfig,
                                    onBack = { onAction(AgenticAction.UnlockCharacter) },
                                    onAction = onAction,
                                )
                            } else {
                                Column(
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = "THE LEGENDS",
                                            style =
                                                MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 2.sp,
                                                ),
                                            modifier = Modifier.alpha(0.6f),
                                        )

                                        IconButton(
                                            onClick = onToggle,
                                            modifier = Modifier.size(24.dp),
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.round_close_24),
                                                contentDescription = "Close",
                                                tint = genre.resolveIconColor(),
                                                modifier = Modifier.size(24.dp),
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(12.dp))

                                    LazyColumn(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        contentPadding = PaddingValues(bottom = 12.dp),
                                    ) {
                                        items(book.characters) { char ->
                                            CharacterPageEntry(char, genre, onSelect = {
                                                onAction(AgenticAction.SelectCharacter(char))
                                            })
                                        }
                                    }

                                    Row(
                                        modifier =
                                            Modifier
                                                .clip(genre.shape())
                                                .clickable {
                                                    currentPage = 0
                                                }.padding(4.dp)
                                                .align(Alignment.End)
                                                .alpha(.5f),
                                    ) {
                                        Icon(
                                            painterResource(R.drawable.ic_back_left),
                                            null,
                                            tint = genre.resolveIconColor(),
                                            modifier = Modifier.size(12.dp),
                                        )

                                        Text(
                                            "See story",
                                            style =
                                                MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    textAlign = TextAlign.End,
                                                    color = genre.resolveIconColor(),
                                                ),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Page Gutter Shadow (Middle)
            Box(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .width(20.dp)
                        .align(Alignment.CenterStart)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Black.copy(alpha = 0.15f), Color.Transparent),
                            ),
                        ),
            )
        }

        // --- 2. Front Cover ---
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.rotationY = rotation
                        this.transformOrigin = TransformOrigin(0f, 0.5f)
                        this.cameraDistance = 12f * density
                    }.clip(shape)
                    .background(Brush.verticalGradient(color.darkerPalette(factor = .35f)))
                    .clickable { onToggle() },
        ) {
            // Book Spine Text/Detail
            Box(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .width(12.dp)
                        .background(Color.Black.copy(alpha = 0.2f)),
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = painterResource(id = genre.icon),
                    contentDescription = null,
                    tint = genre.resolveColor(visualConfig),
                    modifier = Modifier.size(48.dp).genreVfx(genre),
                )
                Spacer(Modifier.height(16.dp))
                genre.stylisedText(
                    book.draft.title,
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                )
            }

            // Hide the cover details when it is fully rotated (Back of the cover)
            if (rotation < -90f) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(color.copy(alpha = 0.9f)),
                )
            }
        }
    }

    if (showEditor) {
        CosmicEditorSheet(
            title = "Editar Crônica",
            genre = genre,
            fields =
                listOf(
                    CosmicInputField(
                        "title",
                        "Título da Saga",
                        book.draft.title,
                        hint = "O nome da lenda",
                    ),
                    CosmicInputField(
                        "description",
                        "Sinopse",
                        book.draft.description,
                        isMultiline = true,
                        hint = "O que os pergaminhos dizem?",
                    ),
                ),
            onSave = {
                onAction(
                    AgenticAction.UpdateSaga(
                        book.draft.id,
                        it["title"] ?: book.draft.title,
                        it["description"] ?: book.draft.description,
                    ),
                )
            },
            onDismiss = { showEditor = false },
        )
    }
}

@Composable
private fun CharacterPageEntry(
    character: CharacterInfo,
    genre: Genre,
    onSelect: () -> Unit,
) {
    val placeholders = LocalGenderPlaceholders.current
    val silhouetteUrl = placeholders.resolveUrl(genre, character.gender)

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(genre.color.copy(alpha = 0.05f))
                .clickable { onSelect() }
                .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(50.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        genre.color.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.medium,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            if (silhouetteUrl.isNotEmpty()) {
                AsyncImage(
                    model = silhouetteUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Icon(
                    painterResource(genre.icon),
                    null,
                    tint = genre.resolveColor().copy(alpha = 0.3f),
                    modifier = Modifier.size(30.dp),
                )
            }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                character.name,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = genre.headerFont(),
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
            )
            Text(
                character.description,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontFamily = genre.bodyFont(),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    ),
            )
        }
    }
}

@Composable
private fun ExpandedCharacterPage(
    character: CharacterInfo,
    genre: Genre,
    visualConfig: GenreVisualConfig,
    onBack: () -> Unit,
    onAction: (AgenticAction) -> Unit,
) {
    val placeholders = LocalGenderPlaceholders.current
    val silhouetteUrl = placeholders.resolveUrl(genre, character.gender)
    val color = genre.resolveColor(visualConfig)
    var showEditor by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(genre.shape(visualConfig))
                        .background(color.copy(alpha = 0.05f)),
            ) {
                if (silhouetteUrl.isNotEmpty()) {
                    AsyncImage(
                        model = silhouetteUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().alpha(0.8f),
                        contentScale = ContentScale.Fit,
                    )
                } else {
                    Icon(
                        painterResource(genre.icon),
                        null,
                        modifier = Modifier.size(60.dp).align(Alignment.Center),
                        tint = color.copy(alpha = 0.2f),
                    )
                }
            }

            Text(
                text = character.name,
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontFamily = genre.headerFont(),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                modifier = Modifier.fillMaxWidth().genreVfx(genre),
            )

            Text(
                character.gender.name.lowercase(),
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontFamily = genre.bodyFont(),
                    ),
                color = color,
                modifier = Modifier.alpha(0.8f),
            )

            Text(
                text = character.description,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 20.sp,
                        fontFamily = genre.bodyFont(),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f),
                    ),
                modifier = Modifier.fillMaxWidth().weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    painterResource(R.drawable.round_close_24),
                    null,
                    tint = genre.resolveIconColor(visualConfig),
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = { showEditor = true },
                modifier = Modifier.size(24.dp).padding(8.dp),
            ) {
                Icon(
                    painterResource(R.drawable.ic_edit),
                    null,
                    modifier = Modifier.fillMaxSize(),
                    tint = genre.resolveIconColor(visualConfig),
                )
            }
        }
    }

    if (showEditor) {
        CosmicEditorSheet(
            title = "Editar Protagonista",
            genre = genre,
            fields =
                listOf(
                    CosmicInputField("name", "Nome", character.name, hint = "Como ele é chamado?"),
                    CosmicInputField(
                        "gender",
                        "Identidade",
                        character.gender.name,
                        hint = "Gênero ou Origem",
                    ),
                    CosmicInputField(
                        "description",
                        "Biografia",
                        character.description,
                        isMultiline = true,
                        hint = "Descreva a lenda",
                    ),
                ),
            onSave = {
                onAction(
                    AgenticAction.UpdateCharacter(
                        character.id,
                        it["name"] ?: character.name,
                        it["gender"] ?: character.gender.name,
                        it["description"] ?: character.description,
                    ),
                )
            },
            onDismiss = { showEditor = false },
        )
    }
}
