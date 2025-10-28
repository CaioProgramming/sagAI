package com.ilustris.sagai.features.saga.detail.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode.Reverse
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.relations.ui.SingleRelationShipCard
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.characters.ui.CharacterYearbookItem
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.features.saga.chat.data.model.AnimatedEmotionalShape
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.model.filterCharacterMessages
import com.ilustris.sagai.features.saga.chat.domain.model.rankEmotionalTone
import com.ilustris.sagai.features.saga.chat.ui.components.title
import com.ilustris.sagai.features.saga.detail.data.model.Review
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewDetails
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.features.share.presentation.SharePlayViewModel
import com.ilustris.sagai.features.share.ui.EmotionShareView
import com.ilustris.sagai.features.share.ui.HistoryShareView
import com.ilustris.sagai.features.share.ui.PlayStyleShareView
import com.ilustris.sagai.features.share.ui.RelationsShareView
import com.ilustris.sagai.ui.animations.AnimatedChapterGridBackground
import com.ilustris.sagai.ui.animations.PoppingAvatarsBackground
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.darker
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import com.ilustris.sagai.ui.theme.solidGradient
import com.ilustris.sagai.ui.theme.toEasing
import com.ilustris.sagai.ui.theme.zoomAnimation
import effectForGenre
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.RowChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DividerProperties
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.PopupProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

enum class ReviewPages(
    val shareType: ShareType? = null,
) {
    INTRO(ShareType.PLAYSTYLE),
    PLAYSTYLE(ShareType.EMOTIONS),
    CHARACTERS(ShareType.RELATIONS),
    CHAPTERS(ShareType.HISTORY),
    CONCLUSION(ShareType.EMOTIONS),
    DETAILS,
}

@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun SagaReview(
    content: SagaContent,
    generatingReview: Boolean,
    resetReview: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState { ReviewPages.entries.size }
    val genre = content.data.genre
    var showIndicators by remember { mutableStateOf(true) }

    val animatedGradientBrush =
        if (showIndicators) {
            genre.color.solidGradient()
        } else {
            MaterialTheme.colorScheme.background.solidGradient()
        }

    var currentProgress by remember { mutableFloatStateOf(0f) }
    var isPlaying by remember { mutableStateOf(true) }
    var shareSaga by remember { mutableStateOf(false) }
    val currentPage = ReviewPages.entries[pagerState.currentPage]
    val shareType = currentPage.shareType

    LaunchedEffect(pagerState.currentPage) {
        showIndicators = pagerState.currentPage != ReviewPages.entries.size - 1
    }

    Box(
        Modifier
            .background(animatedGradientBrush)
            .fillMaxSize(),
    ) {
        if (content.data.isEnded.not() || generatingReview) {
            Column(
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SparkIcon(
                    brush = genre.gradient(true),
                    tint = genre.color.copy(alpha = .4f),
                    modifier =
                        Modifier
                            .size(64.dp)
                            .align(Alignment.CenterHorizontally),
                )

                Text(
                    stringResource(R.string.review_page_surprise_awaits),
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontFamily = genre.bodyFont(),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                        ),
                )
            }
        } else {
            ConstraintLayout(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onLongPress = {
                            isPlaying = false
                        }, onPress = {
                            awaitRelease()
                            isPlaying = true
                        })
                    },
            ) {
                val (reviewIndicators, contentView, topFade, shareButton) = createRefs()

                HorizontalPager(
                    pagerState,
                    modifier =
                        Modifier.constrainAs(contentView) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                            height = Dimension.fillToConstraints
                            width = Dimension.fillToConstraints
                        },
                ) {
                    val page = ReviewPages.entries[it]
                    AnimatedContent(pagerState.currentPage, transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    }, modifier = Modifier.fillMaxSize()) { index ->
                        if (index == it) {
                            when (page) {
                                ReviewPages.INTRO ->
                                    ReviewIntroduction(
                                        content,
                                        content.data.review?.introduction,
                                    )

                                ReviewPages.PLAYSTYLE ->
                                    PlayStylePage(
                                        content,
                                    )

                                ReviewPages.CHARACTERS ->
                                    MentionsPage(
                                        content,
                                    )

                                ReviewPages.CHAPTERS ->
                                    ActsInsightPage(content)

                                ReviewPages.CONCLUSION ->
                                    ConclusionPage(content)

                                ReviewPages.DETAILS -> ReviewDetails(content)
                            }
                        } else {
                            Box {}
                        }
                    }
                }

                val indicatorsAlpha by animateFloatAsState(
                    if (showIndicators) 1f else 0f,
                )

                Column(
                    modifier =
                        Modifier
                            .constrainAs(reviewIndicators) {
                                top.linkTo(parent.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }.background(fadeGradientTop())
                            .alpha(indicatorsAlpha),
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                    ) {
                        ReviewPages.entries.forEachIndexed { index, _ ->
                            val isSelected = index <= pagerState.currentPage
                            val progress by animateFloatAsState(
                                targetValue = if (isSelected) 1f else 0.5f,
                                animationSpec = tween(500),
                                label = "progressAnimation",
                            )
                            Box(
                                modifier =
                                    Modifier
                                        .clickable {
                                            if (pagerState.currentPage != index) {
                                                coroutineScope.launch {
                                                    pagerState.animateScrollToPage(
                                                        index,
                                                        animationSpec = tween(500),
                                                    )
                                                }
                                            }
                                        }.weight(1f)
                                        .padding(horizontal = 2.dp)
                                        .height(3.dp)
                                        .background(
                                            color =
                                                MaterialTheme.colorScheme.onBackground.copy(
                                                    alpha = progress,
                                                ),
                                            shape = RoundedCornerShape(content.data.genre.cornerSize()),
                                        ),
                            )
                        }
                    }

                    Row(
                        modifier =
                            Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Image(
                            painterResource(R.drawable.ic_spark),
                            contentDescription = null,
                            modifier =
                                Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )

                        Text(
                            content.data.title,
                            style =
                                MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = genre.headerFont(),
                                    color = MaterialTheme.colorScheme.onBackground,
                                ),
                        )
                    }
                }

                Row(
                    Modifier
                        .constrainAs(shareButton) {
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }.clickable(enabled = shareType != null) {
                            shareSaga = true
                        }.fillMaxWidth()
                        .background(
                            fadeGradientBottom(),
                        ).padding(24.dp)
                        .gradientFill(genre.gradient(true)),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Compartilhar",
                        style =
                            MaterialTheme.typography.labelLarge.copy(
                                fontFamily = genre.bodyFont(),
                                fontWeight = FontWeight.SemiBold,
                            ),
                    )

                    Icon(
                        painterResource(R.drawable.ic_share),
                        contentDescription = null,
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .size(12.dp)
                                .gradientFill(genre.gradient()),
                    )
                }
            }
        }
    }

    if (shareSaga) {
        ModalBottomSheet(
            onDismissRequest = {
                shareSaga = false
            },
            sheetState = rememberModalBottomSheetState(true),
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            AnimatedContent(shareType, transitionSpec = { fadeIn() togetherWith fadeOut() }) {
                when (it) {
                    ShareType.PLAYSTYLE ->
                        PlayStyleShareView(
                            content,
                        )
                    ShareType.HISTORY -> HistoryShareView(content)
                    ShareType.EMOTIONS -> EmotionShareView(content)
                    ShareType.RELATIONS -> RelationsShareView(content)

                    else -> {
                        Text("Implementing..")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun SagaReviewPreview() {
    SagAIScaffold {
        val content =
            SagaContent(
                data =
                    Saga(
                        title = "Preview Saga",
                        isEnded = true,
                        review =
                            Review(
                                introduction = "Embark on an epic journey through the captivating world of this saga. Prepare to be immersed in a tale of adventure, mystery, and unforgettable characters.",
                                playstyle = "Experience a dynamic blend of strategic combat and intricate puzzle-solving. Master unique abilities and adapt your tactics to overcome formidable foes and unravel ancient secrets.",
                                topCharacters = "Meet a diverse cast of heroes and villains, each with their own compelling backstories and motivations. From valiant warriors to enigmatic sorcerers, these characters will leave a lasting impression.",
                                actsInsight = "Explore breathtaking landscapes and uncover hidden truths as you progress through a series of gripping chapters. Each stage presents new challenges and revelations, keeping you on the edge of your seat.",
                                conclusion = "As the saga reaches its climactic conclusion, prepare for an unforgettable finale that will tie together all the threads of this epic adventure. The fate of the world hangs in the balance.",
                            ),
                    ),
                acts = emptyList(),
            )
        SagaReview(content, false)
    }
}

@Composable
fun ReviewIntroduction(
    content: SagaContent,
    text: String?,
) {
    Box(
        modifier =
            Modifier
                .padding()
                .fillMaxSize(),
    ) {
        var imageFraction by remember {
            mutableFloatStateOf(1f)
        }
        val genre = content.data.genre
        val imageAnim by animateFloatAsState(
            imageFraction,
            animationSpec = tween(1500),
            label = "imageAnimation",
        )
        var showText by remember {
            mutableStateOf(false)
        }

        AsyncImage(
            model = content.data.icon,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .fillMaxSize()
                    .effectForGenre(genre)
                    .selectiveColorHighlight(genre.selectiveHighlight())
                    .zoomAnimation(),
        )

        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = imageAnim)),
        )

        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .padding(vertical = 100.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .animateContentSize(),
        ) {
            val titleAlpha by animateFloatAsState(
                if (showText) 1f else 0f,
                animationSpec = tween(500),
                label = "titleAlphaAnimation",
            )
            Text(
                content.data.title,
                style =
                    MaterialTheme.typography.displaySmall.copy(
                        fontFamily = genre.headerFont(),
                        brush = genre.gradient(true, duration = 2.seconds),
                    ),
                modifier = Modifier.alpha(titleAlpha),
            )
            text?.let {
                SimpleTypewriterText(
                    it,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = genre.bodyFont(),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                        ),
                    onAnimationFinished = {
                        showText = true
                        imageFraction = .4f
                    },
                    modifier =
                        Modifier
                            .padding(8.dp),
                )
            }
        }
    }
}

@Composable
fun PlayStylePage(content: SagaContent) {
    val genre = content.data.genre
    val coroutineScope = rememberCoroutineScope()

    var showText by remember {
        mutableStateOf(false)
    }

    var showCards by remember {
        mutableStateOf(false)
    }

    val topTone =
        remember {
            content
                .flatMessages()
                .filterCharacterMessages(content.mainCharacter?.data)
                .rankEmotionalTone()
        }
    val infiniteTransition = rememberInfiniteTransition()
    val firstTone = remember { topTone.first().first }
    val shapeA =
        remember {
            RoundedPolygon.star(
                4,
                rounding = CornerRounding(5f),
            )
        }
    val shapeB =
        remember {
            firstTone.starShape()
        }

    val morphProgress by
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    tween(
                        10.seconds.toInt(DurationUnit.MILLISECONDS),
                        easing = firstTone.toEasing(),
                    ),
                    repeatMode = Reverse,
                ),
            label = "morph",
        )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec =
            infiniteRepeatable(
                tween(
                    easing = firstTone.toEasing(),
                    durationMillis = 10.seconds.toInt(DurationUnit.MILLISECONDS),
                ),
            ),
    )

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            delay(2.seconds)
            showText = true
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.Center,
        horizontalArrangement = Arrangement.Center,
        modifier =
            Modifier
                .padding(16.dp)
                .fillMaxSize()
                .animateContentSize(),
    ) {
        item(span = { GridItemSpan(2) }) {
            Spacer(Modifier.size(50.dp))
        }

        item(span = { GridItemSpan(2) }) {
            AnimatedEmotionalShape(
                Modifier
                    .aspectRatio(1f)
                    .padding(16.dp)
                    .reactiveShimmer(true),
                firstTone,
                morphProgress,
                rotation,
                outlineBrush = firstTone.color.solidGradient(),
                backgroundBrush = Brush.verticalGradient(genre.shimmerColors()),
                glowColor = genre.iconColor,
            )
        }

        item(span = { GridItemSpan(2) }) {
            Text(
                topTone.first().first.getTitle(),
                style =
                    MaterialTheme.typography.displaySmall.copy(
                        fontFamily = genre.headerFont(),
                        color = genre.iconColor,
                        textAlign = TextAlign.Center,
                    ),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (showText) {
            content.data.review?.playstyle?.let {
                item(span = { GridItemSpan(2) }) {
                    SimpleTypewriterText(
                        it,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = genre.bodyFont(),
                                color = MaterialTheme.colorScheme.onBackground,
                            ),
                        onAnimationFinished = {
                            coroutineScope.launch {
                                delay(500)
                                showCards = true
                            }
                        },
                        modifier =
                            Modifier
                                .padding(vertical = 8.dp),
                    )
                }
            }
        }

        if (showCards) {
            item(span = { GridItemSpan(2) }) {
                Text(
                    stringResource(R.string.review_page_messages_rank_title),
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontFamily = genre.headerFont(),
                            color = genre.color,
                        ),
                    modifier =
                        Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(),
                )
            }

            items(topTone) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val position = topTone.indexOf(it)

                    Text(
                        "${position + 1}. ${it.first.getTitle()}",
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontFamily = genre.bodyFont(),
                                fontWeight = FontWeight.Bold,
                                color = it.first.color,
                            ),
                    )

                    Text(
                        it.second.size.toString(),
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = genre.bodyFont(),
                                fontWeight = FontWeight.Normal,
                            ),
                        modifier = Modifier.alpha(.5f),
                    )
                }
            }
        }

        item(span = { GridItemSpan(2) }) {
            Spacer(Modifier.size(50.dp))
        }
    }
}

@Composable
fun MentionsPage(content: SagaContent) {
    val genre = remember { content.data.genre }

    val coroutineScope = rememberCoroutineScope()

    var counting by remember {
        mutableIntStateOf(0)
    }

    var showText by remember {
        mutableStateOf(false)
    }

    var showCards by remember {
        mutableStateOf(false)
    }

    val mainCharacter = remember { content.mainCharacter }

    val ranking = remember { content.mainCharacter?.rankRelationships() ?: emptyList() }

    val countAnimation by animateIntAsState(
        counting,
        animationSpec =
            tween(
                4.seconds.toInt(DurationUnit.MILLISECONDS),
                easing = LinearOutSlowInEasing,
            ),
        label = "countAnimation",
        finishedListener = {
            showText = true
        },
    )

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            delay(1000)
            counting = content.relationships.size
        }
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalArrangement = Arrangement.Center,
    ) {
        item(span = { GridItemSpan(2) }) {
            Spacer(Modifier.height(50.dp))
        }

        item(span = { GridItemSpan(2) }) {
            Column(
                modifier =
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    countAnimation.toString(),
                    style =
                        MaterialTheme.typography.displayLarge.copy(
                            fontFamily = genre.headerFont(),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Black,
                        ),
                    modifier = Modifier.padding(4.dp),
                )

                Text(
                    stringResource(R.string.saga_detail_relationships_section_title),
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            fontFamily = genre.bodyFont(),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Justify,
                        ),
                )

                content.data.review?.topCharacters?.let {
                    AnimatedVisibility(showText) {
                        SimpleTypewriterText(
                            it,
                            style =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = genre.bodyFont(),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Justify,
                                ),
                            onAnimationFinished = {
                                coroutineScope.launch {
                                    delay(500)
                                    showCards = true
                                }
                            },
                            isAnimated = showCards.not(),
                            modifier =
                                Modifier.padding(vertical = 8.dp),
                        )
                    }
                }
            }
        }

        if (showCards) {
            item(span = { GridItemSpan(2) }) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .animateItem(),
                ) {
                    Column {
                        Text(
                            stringResource(R.string.review_page_characters_rank_title),
                            style =
                                MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = genre.headerFont(),
                                    fontWeight = FontWeight.Bold,
                                ),
                            modifier =
                                Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(vertical = 8.dp),
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier =
                                Modifier
                                    .padding(16.dp),
                        ) {
                            val topCharacters = ranking.take(3)

                            if (topCharacters.size == 3) {
                                CharacterYearbookItem(
                                    topCharacters.last().getCharacterExcluding(mainCharacter?.data),
                                    genre,
                                    imageModifier = Modifier.size(80.dp),
                                    modifier =
                                        Modifier
                                            .clip(genre.shape()),
                                )

                                CharacterYearbookItem(
                                    topCharacters
                                        .first()
                                        .getCharacterExcluding(mainCharacter?.data),
                                    genre = genre,
                                    imageModifier = Modifier.size(100.dp),
                                    modifier =
                                        Modifier
                                            .clip(genre.shape()),
                                )

                                CharacterYearbookItem(
                                    topCharacters[1].getCharacterExcluding(mainCharacter?.data),
                                    genre = genre,
                                    imageModifier = Modifier.size(80.dp),
                                    modifier =
                                        Modifier
                                            .clip(genre.shape()),
                                )
                            }
                        }

                        Spacer(Modifier.height(50.dp))
                    }
                }
            }
        }

        if (showCards) {
            mainCharacter?.let { character ->
                items(mainCharacter.relationships.sortedByDescending { it.relationshipEvents.size }) {
                    Column(
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .animateItem(),
                    ) {
                        SingleRelationShipCard(
                            content,
                            it.getCharacterExcluding(character.data),
                            it,
                            false,
                            showUpdates = true,
                            Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActsInsightPage(content: SagaContent) {
    val genre = content.data.genre
    val chapters = content.flatChapters()

    var showText by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        delay(3.seconds)
        showText = true
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        AnimatedChapterGridBackground(
            sagaIcon = content.data.icon,
            chapters = chapters.map { it.data },
            genre = genre,
        )

        val overlayAlpha by animateFloatAsState(
            targetValue = if (showText) .7f else 0f,
            animationSpec = tween(1500),
            label = "backgroundOverlayAlpha",
        )

        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = overlayAlpha)),
        )

        val coroutineScope = rememberCoroutineScope()
        val columnScroll = rememberScrollState()
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .padding(top = 100.dp)
                    .fillMaxSize()
                    .verticalScroll(columnScroll)
                    .padding(horizontal = 16.dp, vertical = 32.dp)
                    .animateContentSize(),
        ) {
            Text(
                stringResource(R.string.review_page_your_journey_title),
                style =
                    MaterialTheme.typography.displaySmall.copy(
                        fontFamily = genre.headerFont(),
                        brush = genre.gradient(),
                        textAlign = TextAlign.Center,
                    ),
                modifier =
                    Modifier
                        .padding(vertical = 8.dp)
                        .reactiveShimmer(true),
            )
            content.data.review?.actsInsight?.let {
                if (showText) {
                    SimpleTypewriterText(
                        it,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = genre.bodyFont(),
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Justify,
                            ),
                        duration = 10.seconds,
                        easing = EaseIn,
                    )
                }
            }

            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            )
        }
    }
}

@Composable
fun ConclusionPage(content: SagaContent) {
    val genre = content.data.genre
    val charactersToDisplay =
        remember(content.getCharacters()) {
            content.getCharacters().filter { it.image.isBlank().not() }.ifEmpty {
                content.mainCharacter?.data?.let {
                    listOf(
                        it,
                    )
                } ?: emptyList()
            }
        }
    var showTextParts by remember { mutableStateOf(false) }

    PoppingAvatarsBackground(
        characters = charactersToDisplay,
        genre = content.data.genre,
        avatarSize = 150.dp,
        popDuration = 2.seconds.toLong(DurationUnit.MILLISECONDS),
        moveDuration = 10.seconds.toInt(DurationUnit.MILLISECONDS),
        onCharacterPopped = {
            if (it >= charactersToDisplay.size / 2) {
                showTextParts = true
            }
        },
    )

    val overlayAlpha by animateFloatAsState(
        targetValue = if (showTextParts) .7f else 0f,
        animationSpec = tween(1500),
        label = "backgroundOverlayAlpha",
    )

    Box(
        // Overlay
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = overlayAlpha)),
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.review_page_thank_you),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                text = stringResource(R.string.review_page_for_playing),
                style =
                    MaterialTheme.typography.displaySmall.copy(
                        brush = genre.gradient(true, duration = 3.seconds),
                        textAlign = TextAlign.Center,
                        fontFamily = genre.headerFont(),
                    ),
                modifier = Modifier.reactiveShimmer(true),
            )

            Spacer(modifier = Modifier.height(24.dp))

            content.data.review?.conclusion?.let { conclusionText ->

                AnimatedVisibility(showTextParts) {
                    SimpleTypewriterText(
                        text = conclusionText,
                        style =
                            MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Justify,
                                fontFamily = genre.bodyFont(),
                            ),
                    )
                }
            }
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}
