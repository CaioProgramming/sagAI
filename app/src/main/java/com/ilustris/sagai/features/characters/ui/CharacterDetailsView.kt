package com.ilustris.sagai.features.characters.ui
import com.ilustris.sagai.features.characters.ui.components.CharacterStatusDialog
import com.ilustris.sagai.features.progression.data.model.CharacterHonor
import com.ilustris.sagai.features.progression.ui.HonorSheet
import androidx.compose.ui.draw.alpha
import com.ilustris.sagai.features.characters.ui.components.AboutCard
import com.ilustris.sagai.features.characters.ui.components.ArcsCard
import com.ilustris.sagai.features.characters.ui.components.EventsCard
import com.ilustris.sagai.features.characters.ui.components.KnowledgeCard
import com.ilustris.sagai.features.characters.ui.components.ProfileCard
import com.ilustris.sagai.features.characters.ui.components.RelationsCard
import com.ilustris.sagai.features.characters.ui.components.SpecialAction
import com.ilustris.sagai.features.characters.ui.components.SpecialActionsCard
import com.ilustris.sagai.ui.components.IosStyleMenuHost
import com.ilustris.sagai.ui.components.IosStyleMenuItem
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.R
import com.ilustris.sagai.core.data.model.ImagePalette
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.CharacterDetailData
import com.ilustris.sagai.features.characters.data.model.fullName
import com.ilustris.sagai.features.characters.relations.ui.SingleRelationShipCard
import com.ilustris.sagai.features.characters.ui.components.CharacterAbilitiesSection
import com.ilustris.sagai.features.characters.ui.components.CharacterExperienceCard
import com.ilustris.sagai.features.characters.ui.components.CharacterFeatLogSheet
import com.ilustris.sagai.features.characters.ui.components.CharacterLevelRow
import com.ilustris.sagai.features.characters.ui.components.CharacterSkillChips
import com.ilustris.sagai.features.characters.ui.components.CharacterAppearanceSection
import com.ilustris.sagai.features.characters.ui.components.CharacterArcTimeline
import com.ilustris.sagai.features.characters.ui.components.CharacterDetailDivider
import com.ilustris.sagai.features.characters.ui.components.CharacterDetailSection
import com.ilustris.sagai.features.characters.ui.components.CharacterDetailText
import com.ilustris.sagai.features.characters.ui.components.CharacterKnowledgeList
import com.ilustris.sagai.features.characters.ui.components.CharacterStats
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.onboarding.ui.OnboardingDialog
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.features.share.ui.ShareSheet
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.ui.components.TimelineCharacterAttachment
import com.ilustris.sagai.ui.animations.imageStroke
import com.ilustris.sagai.ui.animations.rememberStopMotionFrame
import com.ilustris.sagai.ui.components.views.DepthLayout
import com.ilustris.sagai.ui.components.views.HeroMenuAction
import com.ilustris.sagai.ui.components.views.HeroOverflowMenu
import com.ilustris.sagai.ui.components.views.heroBottomCluster
import com.ilustris.sagai.ui.theme.PaletteTheme
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.characterDetailsTitleGradient
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.filters.genreCrtScreen
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.sagaShape
import com.ilustris.sagai.ui.theme.shimmerize

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CharacterDetailsView(
    characterId: Int? = null,
    onBack: () -> Unit = {},
    onOpenCharacterBrain: (sagaId: Int, characterId: Int) -> Unit = { _, _ -> },
    onOpenEpilogueChat: (sagaId: Int, characterId: Int) -> Unit = { _, _ -> },
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
    viewModel: CharacterDetailsViewModel = hiltViewModel(),
) {
    val detailData by viewModel.characterDetailData.collectAsStateWithLifecycle()
    val genre = detailData?.sagaInfo?.genre

    LaunchedEffect(characterId) {
        viewModel.loadCharacterDetails(characterId)
    }

    DisposableEffect(characterId, detailData?.sagaInfo?.id, detailData?.sagaInfo?.genre) {
        detailData?.sagaInfo?.let { sagaInfo ->
            viewModel.onCharacterScreenVisible(sagaInfo.id)
            viewModel.ensureGenreTheme(sagaInfo.genre)
        }
        onDispose { viewModel.onCharacterScreenHidden() }
    }

    SagAITheme(genre = genre) {
        AnimatedContent(detailData, transitionSpec = {
            fadeIn() togetherWith fadeOut()
        }, label = "CharacterDetailsTransition", modifier = Modifier.fillMaxSize()) {
            if (it != null) {
                CharacterDetailsContent(
                    it,
                    onBack = onBack,
                    onOpenCharacterBrain = onOpenCharacterBrain,
                    onOpenEpilogueChat = onOpenEpilogueChat,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                )
            }
        }
    }
}

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalSharedTransitionApi::class,
)
@Composable
fun CharacterDetailsContent(
    detailData: CharacterDetailData,
    onBack: () -> Unit = {},
    onOpenCharacterBrain: (sagaId: Int, characterId: Int) -> Unit = { _, _ -> },
    onOpenEpilogueChat: (sagaId: Int, characterId: Int) -> Unit = { _, _ -> },
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
    openEvent: (Timeline?) -> Unit = {},
) {
    val viewModel: CharacterDetailsViewModel = hiltViewModel()
    val imagePalette by viewModel.imagePalette.collectAsStateWithLifecycle()

    CharacterDetailsLoaded(
        detailData = detailData,
        openEvent = openEvent,
        onBack = onBack,
        onOpenCharacterBrain = onOpenCharacterBrain,
        onOpenEpilogueChat = onOpenEpilogueChat,
        viewModel = viewModel,
        imagePalette = imagePalette,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
    )

    val showPremiumSheet by viewModel.showPremiumSheet.collectAsStateWithLifecycle()
    if (showPremiumSheet) {
        OnboardingDialog(
            type = OnboardingType.PREMIUM_GUIDE,
            force = true,
            onDismiss = { viewModel.togglePremiumSheet() },
        )
    }
}

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalSharedTransitionApi::class,
)
@Composable
private fun CharacterDetailsLoaded(
    detailData: CharacterDetailData,
    viewModel: CharacterDetailsViewModel,
    onBack: () -> Unit = {},
    onOpenCharacterBrain: (sagaId: Int, characterId: Int) -> Unit = { _, _ -> },
    onOpenEpilogueChat: (sagaId: Int, characterId: Int) -> Unit = { _, _ -> },
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
    imagePalette: ImagePalette? = null,
    openEvent: (Timeline?) -> Unit = {},
) {
    PaletteTheme(imagePalette = imagePalette) {
        val sagaInfo = detailData.sagaInfo
        val character = detailData.character
        val genre = sagaInfo.genre
        val resolvedColor = MaterialTheme.colorScheme.primary

        val adaptiveColor = MaterialTheme.colorScheme.background
        val adaptiveTextColor = MaterialTheme.colorScheme.onBackground

        val listState = rememberLazyListState()
        val characterEvents = detailData.events
        val characterRelations = detailData.relationships
        val messageCount by viewModel.messageCount.collectAsStateWithLifecycle()
        val progress by viewModel.progress.collectAsStateWithLifecycle()
        val pendingGenerations by viewModel.pendingGenerations.collectAsStateWithLifecycle()
        val isRetrying by viewModel.isRetrying.collectAsStateWithLifecycle()
        var showFeatLog by remember { mutableStateOf(false) }
        var showStatusDialog by remember { mutableStateOf(false) }
        var honorSheet by remember { mutableStateOf<Pair<List<CharacterHonor>, Int>?>(null) }
        val characterArcs by viewModel.characterArcs.collectAsStateWithLifecycle()
        var showCharacterShare by remember { mutableStateOf(false) }

        // Lite wrapper to satisfy legacy components that still need SagaContent
        val liteSagaContent =
            remember(sagaInfo) {
                SagaContent(data = sagaInfo.toSaga())
            }

        AnimatedContent(
            character,
            transitionSpec = {
                fadeIn(tween(700)) togetherWith fadeOut(tween(200))
            },
        ) { characterData ->
            var imageError by remember { mutableStateOf(false) }
            val characterColor = characterData.hexColor.hexToColor() ?: resolvedColor
            val titleGradient =
                remember(adaptiveTextColor, characterColor) {
                    characterDetailsTitleGradient(adaptiveTextColor, characterColor)
                }

            // Fixed fraction of the screen, not a measured value: the LazyColumn's first item
            // (the spacer below) needs this before the background photo below it ever reports a
            // size, or the whole list would jump on first frame.
            val screenHeightDp = LocalConfiguration.current.screenHeightDp
            val heroHeightDp = remember(screenHeightDp) { (screenHeightDp * 0.55f).dp }
            val heroHeightPx = with(LocalDensity.current) { heroHeightDp.toPx() }

            // The background photo blurs progressively as the name/actions scroll up past it,
            // echoing the iOS Contacts card. Independent of effectForGenre's own static fade —
            // that one still handles the soft edge of the photo itself.
            val scrollFraction by remember(heroHeightPx) {
                derivedStateOf {
                    val offsetPx =
                        if (listState.firstVisibleItemIndex == 0) {
                            listState.firstVisibleItemScrollOffset.toFloat()
                        } else {
                            heroHeightPx
                        }
                    (offsetPx / heroHeightPx).coerceIn(0f, 1f)
                }
            }
            val headerBlurRadius by animateDpAsState(
                targetValue = (scrollFraction * 24).dp,
                label = "headerBlur",
            )
            // Blur alone doesn't guarantee contrast on a busy or light photo — a rising tint of
            // the palette's own background colour does, and keeps the transition read as "the
            // card settling" rather than "the text struggling against the art".
            val headerOverlayAlpha by animateFloatAsState(
                targetValue = scrollFraction * 0.7f,
                label = "headerOverlayAlpha",
            )

            with(sharedTransitionScope) {
                Box(
                    modifier =
                        Modifier.background(
                            adaptiveColor,
                        ),
                ) {
                    if (characterData.image.isNotBlank() && !imageError) {
                        // The hero art gets the Punk Rock poster's cutout treatment: an
                        // outline traced around the segmented subject itself rather than
                        // its bounding box, which is what lifts the character off the
                        // scene behind them. The accent is the character's own colour, so
                        // the halo identifies *this* character instead of restating the
                        // genre the whole screen is already themed by.
                        val heroImageModifier =
                            Modifier
                                .clipToBounds()
                                .fillMaxSize()
                                .effectForGenre(
                                    genre = genre,
                                    progressiveBlurRadius = 160f,
                                    progressiveBlurRange = 0.6f to 0.98f,
                                    enableSelectiveHighlight = true,
                                )
                                // Space Opera's portraits arrive as if read off a console
                                // panel. No-ops for every other genre, which keep theirs.
                                .genreCrtScreen(genre)
                        val strokeWidthPx = with(LocalDensity.current) { 4.dp.toPx() }
                        // Only ticks for the genre that draws a trembling outline; the
                        // rest would be paying a redraw every 240ms for nothing.
                        val strokeFrame =
                            rememberStopMotionFrame(isActive = genre == Genre.PUNK_ROCK)
                        val strokeJitterPx = with(LocalDensity.current) { 1.6.dp.toPx() }

                        // Purely decorative background layer, fixed behind the whole screen —
                        // the LazyColumn below is a sibling, not nested inside DepthLayout's
                        // content slot (that slot sits *between* background and the segmented
                        // foreground cutout, so screen content would render underneath it).
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .blur(headerBlurRadius),
                        ) {
                            DepthLayout(
                                imagePath = characterData.image,
                                onLoadError = { imageError = true },
                                modifier =
                                    Modifier
                                        .sharedElement(
                                            rememberSharedContentState(key = "character_${character.id}_icon"),
                                            animatedVisibilityScope,
                                            renderInOverlayDuringTransition = false,
                                        )
                                        .fillMaxSize()
                                        .clickable(
                                            enabled =
                                                BuildConfig.DEBUG ||
                                                        characterData.emojified ||
                                                        characterData.image.isEmpty(),
                                        ) {
                                            viewModel.regenerate(
                                                sagaInfo,
                                                characterData,
                                            )
                                        },
                                imageModifier = heroImageModifier,
                                // Punk Rock only. The trembling hand-cut outline is that
                                // template's own signature — every other genre keeps the
                                // plain portrait it had before.
                                //
                                // Only the cut-out foreground is stroked: the raw photo
                                // behind it has no silhouette to trace. The strokes sit
                                // innermost so they hug the unblurred subject and the
                                // genre effect then washes over outline and art alike.
                                foregroundImageModifier =
                                    if (genre == Genre.PUNK_ROCK) {
                                        heroImageModifier
                                            .imageStroke(
                                                color = adaptiveColor,
                                                widthPx = strokeWidthPx,
                                                jitterFrame = strokeFrame,
                                                jitterAmountPx = strokeJitterPx,
                                            )
                                            .imageStroke(
                                                color = characterColor,
                                                widthPx = strokeWidthPx * 1.3f,
                                                jitterFrame = strokeFrame,
                                                jitterAmountPx = strokeJitterPx,
                                            )
                                    } else {
                                        heroImageModifier
                                    },
                            ) {}

                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(adaptiveColor.copy(alpha = headerOverlayAlpha)),
                            )

                            Box(
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .background(fadeGradientBottom(adaptiveColor)),
                            )
                        }
                    }

                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxSize(),
                        state = listState,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        item { Spacer(Modifier.height(heroHeightDp)) }

                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                heroBottomCluster(
                                    title = character.fullName(),
                                    genre = genre,
                                    adaptiveColor = adaptiveColor,
                                    adaptiveTextColor = adaptiveTextColor,
                                    titleGradient = titleGradient,
                                    shimmerColors = characterColor.shimmerize(),
                                    accentColor = characterColor,
                                    onAccentColor = adaptiveTextColor,
                                )

                                CharacterLevelRow(
                                    progress = progress,
                                    status = characterData.status,
                                    statusNote = characterData.statusNote,
                                    accent = characterColor,
                                    contentColor = adaptiveTextColor,
                                    genre = genre,
                                    onOpenLegacy = { progress?.let { honorSheet = it.titles to 0 } },
                                )

                                Text(
                                    characterData.profile.occupation,
                                    style =
                                        MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                            color = adaptiveTextColor,
                                            textAlign = TextAlign.Center,
                                        ),
                                )

                                characterData.nicknames?.let {
                                    if (it.isNotEmpty()) {
                                        Text(
                                            text =
                                                stringResource(
                                                    id = R.string.character_details_aka,
                                                    it.joinToString(", "),
                                                ),
                                            style =
                                                MaterialTheme.typography.titleMedium.copy(
                                                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                                    color = adaptiveTextColor,
                                                    textAlign = TextAlign.Center,
                                                ),
                                        )
                                    }
                                }
                            }
                        }

                        progress?.let { current ->
                            item {
                                CharacterExperienceCard(
                                    progress = current,
                                    messageCount = messageCount,
                                    accent = characterColor,
                                    contentColor = adaptiveTextColor,
                                    genre = genre,
                                    pendingGenerations = pendingGenerations,
                                    isRetrying = isRetrying,
                                    onRetry = { viewModel.retryPending(sagaInfo.id) },
                                    onSeeMore = { showFeatLog = true },
                                    onOpenHonor = { honors, index -> honorSheet = honors to index },
                                )
                            }
                        }

                        item {
                            SpecialActionsCard(
                                accent = characterColor,
                                actions =
                                    buildList {
                                        if (sagaInfo.isEnded && characterData.id != sagaInfo.mainCharacterId) {
                                            add(
                                                SpecialAction(
                                                    iconRes = R.drawable.talk_bubble,
                                                    label = stringResource(R.string.talk_to_character_again, characterData.name),
                                                    onClick = { onOpenEpilogueChat(sagaInfo.id, characterData.id) },
                                                ),
                                            )
                                        }
                                        add(
                                            SpecialAction(
                                                iconRes = R.drawable.ic_cosmos,
                                                label = stringResource(R.string.character_brain_open),
                                                onClick = { onOpenCharacterBrain(sagaInfo.id, characterData.id) },
                                            ),
                                        )
                                    },
                            )
                        }

                        item {
                            val characterResume by viewModel.characterResume.collectAsStateWithLifecycle()
                            val isSummarizing by viewModel.isSummarizing.collectAsStateWithLifecycle()
                            val backstoryColor by animateColorAsState(
                                if (isSummarizing.not()) adaptiveTextColor else adaptiveColor,
                            )
                            AboutCard(
                                backstory = characterResume ?: characterData.backstory,
                                personality = characterData.profile.personality,
                                accent = characterColor,
                                contentColor = adaptiveTextColor,
                                backstoryColor = backstoryColor,
                            )
                        }

                        item { ProfileCard(characterData, characterColor, adaptiveTextColor) }

                        item {
                            RelationsCard(
                                relations = characterRelations,
                                character = characterData,
                                saga = liteSagaContent.data,
                                genre = genre,
                                accent = characterColor,
                                contentColor = adaptiveTextColor,
                            )
                        }

                        item { ArcsCard(characterArcs, characterColor, adaptiveTextColor) }

                        item {
                            KnowledgeCard(
                                knowledge = characterData.knowledge.orEmpty().filter { it.isNotBlank() },
                                accent = characterColor,
                                contentColor = adaptiveTextColor,
                            )
                        }

                        item {
                            EventsCard(
                                events = characterEvents,
                                progress = progress,
                                accent = characterColor,
                                contentColor = adaptiveTextColor,
                                onOpenEvent = openEvent,
                            )
                        }

                        item { Spacer(Modifier.height(48.dp)) }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(8.dp),
                    ) {
                        IconButton(
                            onClick = onBack,
                            colors =
                                IconButtonDefaults.iconButtonColors().copy(
                                    containerColor = adaptiveColor.copy(alpha = .5f),
                                ),
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_back_left),
                                contentDescription = stringResource(R.string.back_button_description),
                                tint = adaptiveTextColor,
                            )
                        }

                        Text(
                            text = characterData.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = adaptiveTextColor,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .alpha(((scrollFraction - .75f) * 4f).coerceIn(0f, 1f)),
                        )

                        var menuOpen by remember { mutableStateOf(false) }
                        IosStyleMenuHost(
                            expanded = menuOpen,
                            onDismissRequest = { menuOpen = false },
                            anchor = Alignment.TopEnd,
                            menu = {
                                IosStyleMenuItem(
                                    text = stringResource(R.string.character_card_share),
                                    icon = painterResource(R.drawable.ic_share),
                                    onClick = {
                                        menuOpen = false
                                        showCharacterShare = true
                                    },
                                )
                                if (progress != null) {
                                    IosStyleMenuItem(
                                        text = stringResource(R.string.character_card_change_status),
                                        onClick = {
                                            menuOpen = false
                                            showStatusDialog = true
                                        },
                                    )
                                }
                                if (BuildConfig.DEBUG) {
                                    IosStyleMenuItem(
                                        text = stringResource(R.string.debug_regenerate_image),
                                        onClick = {
                                            menuOpen = false
                                            viewModel.regenerate(sagaInfo, characterData)
                                        },
                                    )
                                }
                            },
                        ) {
                            IconButton(
                                onClick = { menuOpen = true },
                                colors =
                                    IconButtonDefaults.iconButtonColors().copy(
                                        containerColor = adaptiveColor.copy(alpha = .5f),
                                    ),
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_more_vert),
                                    contentDescription = stringResource(R.string.character_card_options),
                                    tint = adaptiveTextColor,
                                )
                            }
                        }
                    }
                }
            }

            val shareCharacterContent =
                remember(characterData, characterEvents) {
                    CharacterContent(data = characterData, events = characterEvents)
                }

            ShareSheet(
                saga = sagaInfo.toSaga(),
                isVisible = showCharacterShare,
                shareType = ShareType.CHARACTER,
                character = shareCharacterContent,
                onDismiss = { showCharacterShare = false },
            )

            progress?.takeIf { showFeatLog }?.let {
                CharacterFeatLogSheet(
                    progress = it,
                    accent = characterColor,
                    genre = genre,
                    onDismiss = { showFeatLog = false },
                )
            }

            if (showStatusDialog) {
                CharacterStatusDialog(
                    current = characterData.status,
                    currentNote = characterData.statusNote,
                    onConfirm = { status, note ->
                        viewModel.updateStatus(characterData, status, note)
                        showStatusDialog = false
                    },
                    onDismiss = { showStatusDialog = false },
                )
            }

            honorSheet?.let { (honors, index) ->
                HonorSheet(
                    honors = honors,
                    startIndex = index,
                    feats = progress?.feats.orEmpty(),
                    genre = genre,
                    onDismiss = { honorSheet = null },
                )
            }
        }
    }
}
