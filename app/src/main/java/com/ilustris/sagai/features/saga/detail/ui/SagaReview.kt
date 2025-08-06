package com.ilustris.sagai.features.saga.detail.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.rankMentions
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.rankMessageTypes
import com.ilustris.sagai.features.saga.chat.ui.components.title
import com.ilustris.sagai.features.saga.detail.data.model.Review
import com.ilustris.sagai.ui.animations.AnimatedChapterGridBackground
import com.ilustris.sagai.ui.animations.PoppingAvatarsBackground
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.rememberAnimatedShuffledGradientBrush
import com.ilustris.sagai.ui.theme.zoomAnimation
import effectForGenre
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

enum class ReviewPages {
    INTRO,
    PLAYSTYLE,
    CHARACTERS,
    CHAPTERS,
    CONCLUSION,
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun SagaReview(
    content: SagaContent,
    generatingReview: Boolean,
    resetReview: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState { ReviewPages.entries.size }
    val genre = content.data.genre

    val animatedGradientBrush =
        rememberAnimatedShuffledGradientBrush(
            pagerState = pagerState,
            colorPalette = genre.colorPalette(),
            animationDurationMillis = 1500,
        )

    var currentProgress by remember { mutableFloatStateOf(0f) }
    var isPlaying by remember { mutableStateOf(true) }
    val animatedProgress by animateFloatAsState(
        targetValue = currentProgress,
        label = "progressAnimation",
        animationSpec = tween(durationMillis = if (isPlaying) 10000 else 0),
    )

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == ReviewPages.entries.size - 1) {
            isPlaying = false
        } else {
            isPlaying = true
            if (isPlaying) {
                currentProgress = 0f
                currentProgress = 1f
            }
        }
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
                val (reviewIndicators, contentView, topFade, bottomFade) = createRefs()

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
                            }
                        } else {
                            Box {}
                        }
                    }
                }

                Box(
                    Modifier
                        .constrainAs(topFade) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }.fillMaxWidth()
                        .fillMaxHeight(.3f)
                        .background(fadeGradientTop()),
                )

                Box(
                    Modifier
                        .constrainAs(bottomFade) {
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }.fillMaxWidth()
                        .fillMaxHeight(.3f)
                        .background(fadeGradientBottom()),
                )

                Column(
                    modifier =
                        Modifier
                            .constrainAs(reviewIndicators) {
                                top.linkTo(parent.top, margin = 50.dp)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            },
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
                chapters = emptyList(), // Added for preview consistency
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
                        imageFraction = .6f
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

    var counting by remember {
        mutableIntStateOf(0)
    }

    var showText by remember {
        mutableStateOf(false)
    }

    var showCards by remember {
        mutableStateOf(false)
    }

    val countAnimation by animateIntAsState(
        counting,
        animationSpec = tween(4.seconds.toInt(DurationUnit.MILLISECONDS), easing = EaseIn),
        label = "countAnimation",
        finishedListener = {
            showText = true
        },
    )

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            delay(1000)
            counting = content.messages.size
        }
    }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .padding(top = 100.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .animateContentSize(
                    animationSpec = tween(500, easing = EaseIn),
                ),
    ) {
        Text(
            countAnimation.toString(),
            style =
                MaterialTheme.typography.displayLarge.copy(
                    fontFamily = genre.headerFont(),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Black,
                ),
            modifier = Modifier.padding(2.dp),
        )

        Text(
            stringResource(R.string.review_page_messages_label),
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontFamily = genre.bodyFont(),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Justify,
                ),
        )

        val cardAlpha by animateFloatAsState(
            if (showCards) 1f else 0f,
            animationSpec = tween(500, easing = EaseIn),
            label = "cardAlphaAnimation",
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.background.copy(alpha = cardAlpha),
                    ).animateContentSize()
                    .padding(16.dp),
        ) {
            val messagesRanking = content.messages.rankMessageTypes().filter { it.second > 0 }

            AnimatedVisibility(showText, enter = fadeIn() + slideInVertically(), exit = fadeOut()) {
                content.data.review?.playstyle?.let {
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

            AnimatedVisibility(showCards) {
                Column {
                    Text(
                        stringResource(R.string.review_page_messages_rank_title),
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontFamily = genre.headerFont(),
                                color = genre.color,
                            ),
                        modifier =
                            Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth(),
                    )

                    messagesRanking.forEach {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val position = messagesRanking.indexOf(it)

                            Text(
                                "${position + 1}. ${it.first.title()}",
                                style =
                                    MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = genre.bodyFont(),
                                        fontWeight = FontWeight.Bold,
                                    ),
                            )

                            Text(
                                it.second.toString(),
                                style =
                                    MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = genre.bodyFont(),
                                        fontWeight = FontWeight.Normal,
                                    ),
                                modifier = Modifier.alpha(.5f),
                            )
                        }
                    }

                    Spacer(Modifier.height(50.dp))
                }
            }
        }
    }
}

@Composable
fun MentionsPage(content: SagaContent) {
    val genre = content.data.genre

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

    var ranking by remember {
        mutableStateOf<List<Pair<Character, Int>>>(emptyList())
    }

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

            val rankingList =
                content.messages
                    .rankMentions(content.characters.filter { it.id != content.mainCharacter?.id })
            ranking = rankingList
            counting =
                rankingList.sumOf {
                    it.second
                }
        }
    }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .padding(top = 100.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
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
            stringResource(R.string.review_page_character_mentions_label),
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontFamily = genre.bodyFont(),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Justify,
                ),
        )

        val cardAlpha by animateFloatAsState(
            if (showCards) 1f else 0f,
            animationSpec = tween(500, easing = EaseIn),
            label = "cardAlphaAnimation",
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.background.copy(cardAlpha),
                    ).padding(16.dp)
                    .animateContentSize(
                        animationSpec = tween(1000, easing = EaseIn),
                    ),
        ) {
            AnimatedVisibility(
                showText,
                enter = fadeIn(),
                exit = scaleOut(),
            ) {
                content.data.review?.topCharacters?.let {
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
                        modifier =
                            Modifier.padding(vertical = 8.dp),
                    )
                }
            }

            AnimatedVisibility(
                showCards,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Column {
                    Text(
                        stringResource(R.string.review_page_characters_rank_title),
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontFamily = genre.headerFont(),
                                fontWeight = FontWeight.Bold,
                            ),
                        modifier = Modifier.padding(vertical = 8.dp),
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier =
                            Modifier
                                .padding(16.dp),
                    ) {
                        val topCharacters = ranking.take(3)

                        if (topCharacters.size == 3) {
                            CharacterAvatar(
                                topCharacters.last().first,
                                genre = genre,
                                modifier =
                                    Modifier
                                        .size(80.dp)
                                        .effectForGenre(
                                            genre,
                                        ),
                            )

                            CharacterAvatar(
                                topCharacters.first().first,
                                genre = genre,
                                modifier =
                                    Modifier
                                        .size(100.dp)
                                        .effectForGenre(
                                            genre,
                                        ),
                            )

                            CharacterAvatar(
                                topCharacters[1].first,
                                genre = genre,
                                modifier =
                                    Modifier
                                        .size(80.dp)
                                        .effectForGenre(
                                            genre,
                                        ),
                            )
                        }
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(Modifier.fillMaxWidth(.5f)) {
                            Text(
                                stringResource(R.string.review_page_most_present_title),
                                style =
                                    MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = genre.headerFont(),
                                    ),
                                modifier =
                                    Modifier
                                        .padding(vertical = 8.dp)
                                        .fillMaxWidth(),
                            )
                            ranking.forEach {
                                val position = ranking.indexOf(it)
                                Text(
                                    "${position + 1}. ${it.first.name}",
                                    style =
                                        MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = genre.bodyFont(),
                                            color =
                                                it.first.hexColor.hexToColor()
                                                    ?: MaterialTheme.colorScheme.onBackground,
                                        ),
                                )
                            }

                            Column(Modifier.weight(1f)) {
                                val messagesRanking =
                                    content.messages
                                        .rankMentions(content.characters.filter { it.id != content.mainCharacter?.id })
                                        .filter { it.second > 0 }
                                Text(
                                    stringResource(R.string.review_page_most_mentioned_title),
                                    style =
                                        MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = genre.headerFont(),
                                        ),
                                    modifier =
                                        Modifier
                                            .padding(vertical = 8.dp)
                                            .fillMaxWidth(),
                                )

                                messagesRanking.forEach {
                                    val position = messagesRanking.indexOf(it)
                                    Text(
                                        "${position + 1}. ${it.first.name}",
                                        style =
                                            MaterialTheme.typography.bodyLarge.copy(
                                                fontFamily = genre.bodyFont(),
                                                color =
                                                    it.first.hexColor.hexToColor()
                                                        ?: MaterialTheme.colorScheme.onBackground,
                                            ),
                                    )
                                }
                            }
                        }
                        Column(Modifier.weight(1f)) {
                            val messagesRanking =
                                content.messages
                                    .rankMentions(content.characters.filter { it.id != content.mainCharacter?.id })
                                    .filter { it.second > 0 }
                            Text(
                                stringResource(R.string.review_page_most_mentioned_title),
                                style =
                                    MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = genre.headerFont(),
                                    ),
                                modifier =
                                    Modifier
                                        .padding(vertical = 8.dp)
                                        .fillMaxWidth(),
                            )

                            messagesRanking.forEach {
                                val position = messagesRanking.indexOf(it)
                                Text(
                                    "${position + 1}. ${it.first.name}",
                                    style =
                                        MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = genre.bodyFont(),
                                            color =
                                                it.first.hexColor.hexToColor()
                                                    ?: MaterialTheme.colorScheme.onBackground,
                                        ),
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(50.dp))
                }
            }
        }
    }
}

@Composable
fun ActsInsightPage(content: SagaContent) {
    val genre = content.data.genre
    val chapters = content.chapters

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
            chapters = chapters,
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
                modifier = Modifier.padding(vertical = 8.dp).reactiveShimmer(true),
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

            Spacer(Modifier.fillMaxWidth().height(50.dp))
        }
    }
}

@Composable
fun ConclusionPage(content: SagaContent) {
    val genre = content.data.genre
    val charactersToDisplay =
        remember(content.characters) {
            content.characters.filter { it.image.isBlank().not() }.ifEmpty {
                content.mainCharacter?.let {
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

    Box( // Overlay
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
