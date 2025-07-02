package com.ilustris.sagai.features.newsaga.ui.pages

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.IllustrationVisuals
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.ui.components.GenreCard
import com.ilustris.sagai.features.newsaga.ui.components.SagaCard
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.holographicGradient
import kotlin.math.absoluteValue
import kotlinx.coroutines.launch

enum class NewSagaPages(
    @StringRes val title: Int? = null,
    @StringRes val subtitle: Int? = null,
    val content: @Composable (((Any?) -> Unit), Any?) -> Unit = { _, _ -> },
) {
    TITLE(
        R.string.start_saga,
        R.string.start_saga_subtitle,
        content = { onSendData, data ->
            TitlePageView(data as String) {
                onSendData(it)
            }
        },
    ),
    GENRE(
        R.string.saga_genre,
        R.string.saga_genre_subtitle,
        content = { onSendData, data ->
            GenresPageView(data as Genre) { // Parameter name is initialGenre in implementation
                onSendData(it)
            }
        },
    ),
    DESCRIPTION(
        R.string.saga_description,
        R.string.saga_description_subtitle,
        content = { onSendData, data ->
            DescriptionPageView(data as String, "Descreva sua saga...") {
                onSendData(it)
            }
        },
    ),

    CHARACTER(
        R.string.saga_character_description,
        R.string.saga_character_description_subtitle,
        content = { onSendData, data ->
            DescriptionPageView(data as String, "Descreva seu personagem...") {
                onSendData(it)
            }
        },
    ),
}

@Composable
fun TitlePageView(
    title: String,
    onSendTitle: (String) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
    ) {
        var input by remember { mutableStateOf(title) }

        val isValidTitle = input.isNotEmpty() && input.length <= 30
        val brush = Brush.linearGradient(holographicGradient)

        TextField(
            value = input,
            onValueChange = {
                input = it
                onSendTitle(input)
            },
            colors =
                TextFieldDefaults.colors(
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                ),
            maxLines = 3,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Sentences,
                    autoCorrect = true,
                ),
            keyboardActions =
                KeyboardActions(
                    onSend = {
                        if (isValidTitle) onSendTitle(input)
                    },
                ),
            placeholder = {
                Text(
                    style =
                        MaterialTheme.typography.displayMedium.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                        ),
                    text = stringResource(R.string.saga_title_hint),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .alpha(.5f),
                )
            },
            textStyle =
                MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    brush = brush,
                ),
            modifier = Modifier.wrapContentSize(),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TitlePageViewPreview() {
    SagAIScaffold {
        TitlePageView(title = "My Awesome Saga") {
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GenresPageViewPreview() {
    SagAIScaffold {
        GenresPageView(initialGenre = Genre.FANTASY) { // Updated parameter name
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DescriptionPageViewPreview() {
    SagAIScaffold {
        DescriptionPageView(
            description = "This is a great saga about a hero.",
            placeHolder = "Describe your saga...",
        ) {
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SagaCardPreview() {
    SagAIScaffold {
        Column {
            SagaCard(
                modifier =
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .fillMaxHeight(.8f),
                sagaData =
                    SagaData(
                        0,
                        "My Awesome Saga",
                        "This is a great saga about a hero.",
                        "",
                        0L,
                        Genre.FANTASY,
                        mainCharacterId = 0,
                        visuals = IllustrationVisuals(),
                    ),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GenresPageView(
    initialGenre: Genre,
    onSelectGenre: (Genre) -> Unit,
) {
    val genres = Genre.entries
    val pagerState = rememberPagerState(
        initialPage = genres.indexOf(initialGenre).coerceAtLeast(0),
        pageCount = { genres.size }
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.settledPage) {
        val selectedGenre = genres[pagerState.settledPage]
        onSelectGenre(selectedGenre)
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 64.dp),
        pageSpacing = 8.dp
    ) { pageIndex ->
        val genre = genres[pageIndex]
        val pageOffset = pagerState.currentPageOffsetFraction

        GenreCard(
            genre = genre,
            isSelected = pagerState.currentPage == pageIndex,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .graphicsLayer {
                    val scale = lerp(0.80f, 1f, 1f - pageOffset.absoluteValue.coerceIn(0f, 1f))
                    scaleX = scale
                    scaleY = scale
                    alpha = lerp(0.6f, 1f, 1f - pageOffset.absoluteValue.coerceIn(0f, 1f))
                },
            onClick = {
                if (pagerState.currentPage != pageIndex) {
                    scope.launch {
                        pagerState.animateScrollToPage(pageIndex)
                    }
                }
            }
        )
    }
}

@Composable
fun DescriptionPageView(
    description: String,
    placeHolder: String,
    onSendDescription: (String) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .padding(24.dp)
                .fillMaxWidth(),
    ) {
        var input by remember { mutableStateOf(description) }
        val isValidTitle = input.isNotEmpty() && input.length <= 300 // This seems to be a leftover from TitlePageView, should be description length
        val brush = Brush.linearGradient(holographicGradient)

        TextField(
            value = input,
            onValueChange = {
                input = it
                onSendDescription(it)
            },
            colors =
                TextFieldDefaults.colors(
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                ),
            maxLines = 10,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Send,
                    capitalization = KeyboardCapitalization.Sentences,
                    autoCorrect = true,
                ),
            keyboardActions =
                KeyboardActions(
                    onSend = {
                        if (isValidTitle) onSendDescription(input) // Same here, isValidTitle check
                    },
                ),
            placeholder = {
                Text(
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Start,
                            fontWeight = FontWeight.Medium,
                        ),
                    text = placeHolder,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .alpha(.5f),
                )
            },
            textStyle =
                MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Start,
                    brush = brush,
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainer,
                        RoundedCornerShape(15.dp),
                    ).border(
                        width = 2.dp,
                        shape = RoundedCornerShape(15.dp),
                        brush = Brush.verticalGradient(holographicGradient),
                    ),
        )
    }
}
