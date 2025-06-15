package com.ilustris.sagai.features.newsaga.ui.pages

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.ai.type.content
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.IllustrationVisuals
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.ui.components.GenreAvatar
import com.ilustris.sagai.features.newsaga.ui.components.SagaCard
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.components.SparkLoader
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.genresGradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

enum class NewSagaPages(
    @StringRes val title: Int? = null,
    @StringRes val subtitle: Int? = null,
    val content: @Composable (((Any?) -> Unit), Any?) -> Unit = { _, _ -> },
) {
    INTRO(
        R.string.lets_create_saga,
        R.string.lets_create_saga_subtitle,
        content = { sendContent, _ ->
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {

                SparkIcon(
                    brush = gradientAnimation(genresGradient(), targetValue = 500f),
                    modifier = Modifier.align(Alignment.CenterHorizontally).size(200.dp),
                    tint = MaterialTheme.colorScheme.background.copy(alpha = .7f),
                )
            }
            LaunchedEffect(Unit) {
                delay(8.seconds)
                sendContent(Unit)
            }
        },
    ),
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
            GenresPageView(data as Genre) {
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

    GENERATING(
        content = { _, _ ->
            Box(Modifier.fillMaxSize()) {
                SparkLoader(
                    brush = gradientAnimation(genresGradient(), targetValue = 500f),
                    modifier = Modifier.size(100.dp).align(Alignment.Center),
                )
            }
        },
    ),
    RESULT(
        content = { onSend, data ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                (data as? SagaData)?.let {
                    val brush = gradientAnimation(it.genre.color.darkerPalette())

                    SagaCard(it, Modifier.padding(16.dp).fillMaxWidth().weight(1f))

                    Button(
                        onClick = { onSend(data) },
                        shape = RoundedCornerShape(15.dp),
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                                .border(
                                    width = 2.dp,
                                    brush = brush,
                                    shape = RoundedCornerShape(15.dp),
                                ),
                        colors =
                            ButtonDefaults.buttonColors().copy(
                                containerColor = MaterialTheme.colorScheme.background,
                            ),
                    ) {
                        Text(
                            text = stringResource(R.string.next),
                            style =
                                MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onBackground,
                                ),
                            modifier =
                                Modifier
                                    .padding(16.dp)
                                    .gradientFill(brush),
                        )

                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "Next",
                            modifier = Modifier.gradientFill(brush).size(24.dp),
                            tint = MaterialTheme.colorScheme.background,
                        )
                    }

                    Button(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        onClick = {
                            onSend(null)
                        },
                        colors =
                            ButtonDefaults.buttonColors().copy(
                                containerColor = Color.Transparent,
                            ),
                        shape = RoundedCornerShape(15.dp),
                    ) {
                        Text(text = "Voltar")
                    }
                } ?: run {
                    Box(Modifier.fillMaxSize()) {
                        SparkIcon(
                            Modifier.size(100.dp).align(Alignment.Center),
                            brush = gradientAnimation(genresGradient()),
                        )
                    }
                }
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
        val isValidTitle = title.isNotEmpty() && title.length <= 30
        val brush = Brush.linearGradient(holographicGradient)
        var input by remember { mutableStateOf(title) }

        TextField(
            value = input,
            onValueChange = {
                input = it
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
                    imeAction = ImeAction.Send,
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
                ),
            modifier = Modifier.wrapContentSize(),
        )

        Button(
            onClick = { onSendTitle(input) },
            shape = RoundedCornerShape(15.dp),
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .border(2.dp, brush, RoundedCornerShape(15.dp)),
            colors =
                ButtonDefaults.buttonColors().copy(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
        ) {
            Text(
                text = stringResource(R.string.next),
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.background,
                    ),
                modifier =
                    Modifier
                        .padding(16.dp)
                        .gradientFill(brush),
            )

            Icon(
                Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "Next",
                modifier = Modifier.gradientFill(brush).size(24.dp),
                tint = MaterialTheme.colorScheme.background,
            )
        }
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
        GenresPageView(genre = Genre.FANTASY) {
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

@Composable
fun GenresPageView(
    genre: Genre,
    onSelectGenre: (Genre) -> Unit,
) {
    val genres = Genre.entries

    LazyVerticalGrid(
        modifier =
            Modifier
                .padding(24.dp)
                .border(
                    2.dp,
                    gradientAnimation(holographicGradient),
                    RoundedCornerShape(15.dp),
                ).background(
                    MaterialTheme.colorScheme.surfaceContainer,
                    RoundedCornerShape(15.dp),
                ).padding(16.dp),
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.Center,
    ) {
        items(genres) {
            GenreAvatar(it, true, it == genre) { g ->
                onSelectGenre(g)
            }
        }
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
        val isValidTitle = input.isNotEmpty() && input.length <= 300
        val brush = Brush.linearGradient(holographicGradient)

        TextField(
            value = input,
            onValueChange = {
                input = it
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
                        if (isValidTitle) onSendDescription(input)
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
                    color = MaterialTheme.colorScheme.onBackground,
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

        Button(
            onClick = { onSendDescription(input) },
            shape = RoundedCornerShape(15.dp),
            modifier =
                Modifier
                    .padding(vertical = 12.dp)
                    .fillMaxWidth()
                    .border(2.dp, brush, RoundedCornerShape(15.dp)),
            colors =
                ButtonDefaults.buttonColors().copy(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
        ) {
            Text(
                text = stringResource(R.string.next),
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.background,
                    ),
                modifier =
                    Modifier
                        .padding(16.dp)
                        .gradientFill(brush),
            )

            Icon(
                Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "Next",
                modifier = Modifier.gradientFill(brush).size(24.dp),
                tint = MaterialTheme.colorScheme.background,
            )
        }
    }
}
