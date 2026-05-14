package com.ilustris.sagai.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.features.newsaga.data.usecase.SagaBook
import com.ilustris.sagai.features.newsaga.ui.LocalGenderPlaceholders
import com.ilustris.sagai.features.newsaga.ui.LocalSharedTransitionScope
import com.ilustris.sagai.features.newsaga.ui.presentation.AgenticAction
import com.ilustris.sagai.ui.animations.genreVfx
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.sagaShape
import com.ilustris.sagai.ui.theme.shape
import com.ilustris.sagai.ui.theme.shimmerize
import kotlinx.coroutines.launch

@Composable
fun CosmicBook(
    book: SagaBook,
    visualConfig: GenreVisualConfig,
    isOpened: Boolean,
    lockedCharacter: CharacterInfo? = null,
    isLoading: Boolean = false,
    reasoning: String? = null,
    onToggle: () -> Unit,
    onAction: (AgenticAction) -> Unit,
    modifier: Modifier = Modifier,
    titleModifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isOpened || reasoning != null) -180f else 0f,
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
    val pagerState = rememberPagerState(pageCount = { 2 })
    var showEditor by remember { mutableStateOf(false) }
    var isCharacterExpanded by remember { mutableStateOf(lockedCharacter != null) }

    LaunchedEffect(lockedCharacter) {
        if (lockedCharacter != null) {
            isCharacterExpanded = true
        }
    }

    LaunchedEffect(isOpened) {
        if (!isOpened) {
            pagerState.scrollToPage(0)
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
                    .background(MaterialTheme.colorScheme.primary)
                    .background(MaterialTheme.colorScheme.background.copy(alpha = .6f))
                    .border(1.dp, Color.LightGray.copy(alpha = 0.2f), shape)
                    .reactiveShimmer(isLoading, color.shimmerize()),
        ) {
            val scope = rememberCoroutineScope()

            if (reasoning != null) {
                // Reasoning Page: Clean layout with just the AI thoughts
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            painter = painterResource(genre.icon),
                            contentDescription = null,
                            tint = genre.resolveColor(visualConfig),
                            modifier =
                                Modifier
                                    .size(40.dp)
                                    .genreVfx(genre),
                        )

                        Text(
                            text = reasoning,
                            style =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                    lineHeight = 22.sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center,
                                ),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .reactiveShimmer(true, genre.shimmerColors(visualConfig)),
                        )
                    }
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = isOpened && !isCharacterExpanded,
                ) { page ->
                    val pageOffset =
                        (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    // Subtle page transition effect
                                    val pageScale = 1f - (kotlin.math.abs(pageOffset) * 0.05f)
                                    scaleX = pageScale
                                    scaleY = pageScale
                                    alpha = 1f - kotlin.math.abs(pageOffset)
                                },
                    ) {
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
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            IconButton(
                                                onClick = onToggle,
                                                modifier =
                                                    Modifier
                                                        .size(24.dp),
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.round_close_24),
                                                    contentDescription = "Close",
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(24.dp),
                                                )
                                            }

                                            Spacer(modifier = Modifier.weight(1f))

                                            Button(
                                                {
                                                    showEditor = true
                                                },
                                                shape = MaterialTheme.shapes.extraLarge,
                                                colors =
                                                    ButtonDefaults.buttonColors(
                                                        contentColor =
                                                            genre.resolveIconColor(
                                                                visualConfig,
                                                            ),
                                                        containerColor = color,
                                                    ),
                                                contentPadding = PaddingValues(8.dp),
                                            ) {
                                                Text(
                                                    stringResource(R.string.edit),
                                                    style = MaterialTheme.typography.labelMedium,
                                                )
                                            }
                                        }

                                        Text(
                                            text = book.draft.title,
                                            style =
                                                MaterialTheme.typography.titleMedium.copy(
                                                    fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                                                    fontSize = 20.sp,
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                    textAlign = TextAlign.Center,
                                                ),
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .genreVfx(genre),
                                        )

                                        Text(
                                            text = book.draft.description,
                                            style =
                                                MaterialTheme.typography.bodyMedium.copy(
                                                    lineHeight = 20.sp,
                                                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                ),
                                            modifier =
                                                Modifier
                                                    .weight(1f)
                                                    .verticalScroll(rememberScrollState()),
                                        )

                                        Row(
                                            modifier =
                                                Modifier
                                                    .clip(sagaShape())
                                                    .clickable {
                                                        scope.launch {
                                                            pagerState.animateScrollToPage(1)
                                                        }
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
                                                        color = MaterialTheme.colorScheme.secondary,
                                                    ),
                                            )

                                            Icon(
                                                painterResource(R.drawable.round_arrow_forward_ios_24),
                                                null,
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(12.dp),
                                            )
                                        }
                                    }
                                }
                            }

                            else -> {
                                // Page 1: Characters
                                SharedTransitionLayout {
                                    AnimatedContent(
                                        targetState = lockedCharacter,
                                        label = "CharacterPageContent",
                                    ) { selected ->
                                        if (isCharacterExpanded && selected != null && book.characters.any { it.id == selected.id }) {
                                            ExpandedCharacterPage(
                                                character = selected,
                                                genre = genre,
                                                visualConfig = visualConfig,
                                                onBack = { isCharacterExpanded = false },
                                                onAction = onAction,
                                                modifier =
                                                    Modifier.sharedBounds(
                                                        rememberSharedContentState(key = "char_${selected.id}"),
                                                        animatedVisibilityScope = this,
                                                    ),
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
                                                        text = stringResource(R.string.the_legends),
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
                                                            tint = MaterialTheme.colorScheme.secondary,
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
                                                        CharacterPageEntry(
                                                            character = char,
                                                            genre = genre,
                                                            isSelected = char.id == lockedCharacter?.id,
                                                            onSelect = {
                                                                onAction(
                                                                    AgenticAction.SelectCharacter(
                                                                        char,
                                                                    ),
                                                                )
                                                                isCharacterExpanded = true
                                                            },
                                                            modifier =
                                                                Modifier.sharedBounds(
                                                                    rememberSharedContentState(key = "char_${char.id}"),
                                                                    animatedVisibilityScope = this@AnimatedContent,
                                                                ),
                                                        )
                                                    }
                                                }

                                                Row(
                                                    modifier =
                                                        Modifier
                                                            .clip(sagaShape())
                                                            .clickable {
                                                                scope.launch {
                                                                    pagerState.animateScrollToPage(0)
                                                                }
                                                            }.padding(4.dp)
                                                            .align(Alignment.End)
                                                            .alpha(.5f),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Icon(
                                                        painterResource(R.drawable.ic_back_left),
                                                        null,
                                                        tint = MaterialTheme.colorScheme.secondary,
                                                        modifier = Modifier.size(12.dp),
                                                    )

                                                    Text(
                                                        "See story",
                                                        style =
                                                            MaterialTheme.typography.labelMedium.copy(
                                                                fontWeight = FontWeight.SemiBold,
                                                                textAlign = TextAlign.End,
                                                                color = MaterialTheme.colorScheme.secondary,
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
                    modifier =
                        Modifier
                            .size(50.dp)
                            .genreVfx(genre),
                )
                Spacer(Modifier.height(16.dp))
                genre.stylisedText(
                    book.draft.title,
                    fontSize = MaterialTheme.typography.titleSmall.fontSize,
                    modifier = titleModifier,
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun CharacterPageEntry(
    character: CharacterInfo,
    genre: Genre,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit,
) {
    val placeholders = LocalGenderPlaceholders.current
    val silhouetteUrl = placeholders.resolveUrl(genre, character.gender)
    val color = MaterialTheme.colorScheme.primary
    val borderColor = if (isSelected) color else color.copy(alpha = 0.1f)
    val borderStroke = if (isSelected) 2.dp else 1.dp

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(if (isSelected) color.copy(alpha = 0.1f) else genre.color.copy(alpha = 0.05f))
                .border(borderStroke, borderColor, MaterialTheme.shapes.medium)
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
                        genre.color.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.medium,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            if (silhouetteUrl.isNotEmpty()) {
                AsyncImage(
                    model = silhouetteUrl,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .alpha(.4f),
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(genre.color.copy(alpha = 0.1f)),
                )
            } else {
                Icon(
                    painterResource(genre.icon),
                    null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
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
                        fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
            )
            Text(
                character.description,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    ),
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ExpandedCharacterPage(
    character: CharacterInfo,
    genre: Genre,
    visualConfig: GenreVisualConfig,
    modifier: Modifier,
    onBack: () -> Unit,
    onAction: (AgenticAction) -> Unit,
) {
    val placeholders = LocalGenderPlaceholders.current
    LocalSharedTransitionScope.current
    val silhouetteUrl = placeholders.resolveUrl(genre, character.gender)
    val color = genre.resolveColor(visualConfig)
    var showEditor by remember { mutableStateOf(false) }

    Box(modifier.fillMaxSize()) {
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
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .alpha(0.8f),
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.tint(color),
                    )
                } else {
                    Icon(
                        painterResource(genre.icon),
                        null,
                        modifier =
                            Modifier
                                .size(60.dp)
                                .align(Alignment.Center),
                        tint = color.copy(alpha = 0.2f),
                    )
                }
            }

            Text(
                text = character.name,
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    ),
                modifier =
                    Modifier
                        .genreVfx(genre)
                        .align(Alignment.CenterHorizontally),
            )

            Text(
                text = character.description,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f),
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth(),
            )
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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

            Button(
                {
                    showEditor = true
                },
                shape = MaterialTheme.shapes.extraLarge,
                colors =
                    ButtonDefaults.buttonColors(
                        contentColor = genre.resolveIconColor(visualConfig),
                        containerColor = color,
                    ),
                contentPadding = PaddingValues(8.dp),
            ) {
                Text(stringResource(R.string.edit), style = MaterialTheme.typography.labelMedium)
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
                        it["description"] ?: character.description,
                    ),
                )
            },
            onDismiss = { showEditor = false },
        )
    }
}
