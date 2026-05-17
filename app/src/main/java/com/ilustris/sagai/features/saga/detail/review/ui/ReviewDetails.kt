package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.relations.ui.RelationShipCard
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.home.data.model.rankByHour
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.model.rankMentions
import com.ilustris.sagai.features.saga.chat.domain.model.rankMessageTypes
import com.ilustris.sagai.features.saga.chat.domain.model.rankTopCharacters
import com.ilustris.sagai.features.saga.chat.ui.components.title
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.darker
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.solidGradient
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

@Composable
fun ReviewDetails(saga: SagaContent) {
    val genre = saga.data.genre
    MaterialTheme.colorScheme.primary
    MaterialTheme.colorScheme.secondary
    val brush = genre.gradient()
    val bodyFont = MaterialTheme.typography.bodyLarge.fontFamily
    val headerFont = MaterialTheme.typography.headlineSmall.fontFamily
    val messageCount =
        remember {
            saga.flatMessages().count()
        }
    val messageTypeRanking =
        remember {
            saga.flatMessages().rankMessageTypes().filter { it.second > 0 }
        }
    val characters = saga.getCharacters(true)
    val charactersRanking =
        remember {
            saga
                .flatMessages()
                .rankTopCharacters(characters)
        }

    val mentionsRanking =
        remember {
            saga
                .flatMessages()
                .rankMentions(characters)
        }

    val mentionsCount =
        remember {
            mentionsRanking
                .sumOf {
                    it.second
                }
        }

    val hourRanking =
        remember {
            saga.rankByHour()
        }

    LazyColumn {
        stickyHeader {
            SagaTopBar(
                saga.data.title,
                "",
                genre,
                isLoading = true,
                modifier =
                    Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxWidth()
                        .padding(top = 25.dp),
            )
        }

        item {
            AsyncImage(
                model = saga.data.icon,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .fillParentMaxHeight(.4f)
                        .effectForGenre(genre)
                        .selectiveColorHighlight(genre.selectiveHighlight()),
            )
        }

        item {
            Text(
                saga.data.description,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                modifier = Modifier.padding(16.dp),
            )
        }

        saga.data.review?.let {
            item {
                Text(
                    it.introduction?.content?.title ?: emptyString(),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                            color = MaterialTheme.colorScheme.onBackground,
                        ),
                    modifier = Modifier.padding(16.dp),
                )
            }

            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                ) {
                    Text(
                        messageCount.toString(),
                        style =
                            MaterialTheme.typography.displaySmall.copy(
                                fontFamily = headerFont,
                                brush = brush,
                                textAlign = TextAlign.Center,
                            ),
                    )
                    Text(
                        stringResource(R.string.review_page_messages_label),
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                fontFamily = bodyFont,
                                textAlign = TextAlign.Center,
                            ),
                        modifier = Modifier.alpha(.4f),
                    )
                }
            }

            item {
                Text(
                    it.playstyle?.content?.title ?: emptyString(),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                            color = MaterialTheme.colorScheme.onBackground,
                        ),
                    modifier = Modifier.padding(16.dp),
                )
            }

            item {
                MessagesRankChart(
                    saga,
                    messageTypeRanking,
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .fillParentMaxHeight(.5f),
                )
            }

            item {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onBackground.copy(.1f),
                    modifier =
                        Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(),
                    thickness = 1.dp,
                )
            }

            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                ) {
                    Text(
                        mentionsCount.toString(),
                        style =
                            MaterialTheme.typography.displaySmall.copy(
                                fontFamily = headerFont,
                                brush = brush,
                                textAlign = TextAlign.Center,
                            ),
                    )
                    Text(
                        stringResource(R.string.review_page_character_mentions_label),
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                fontFamily = bodyFont,
                                textAlign = TextAlign.Center,
                            ),
                        modifier = Modifier.alpha(.4f),
                    )
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    items(charactersRanking) {
                        Column(
                            Modifier
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CharacterAvatar(
                                it.first,
                                borderSize = 2.dp,
                                genre = genre,
                                modifier =
                                    Modifier
                                        .padding(8.dp)
                                        .clip(CircleShape)
                                        .size(120.dp)
                                        .padding(8.dp),
                            )

                            Text(
                                it.first.name,
                                style =
                                    MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Light,
                                        textAlign = TextAlign.Center,
                                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                    ),
                            )
                        }
                    }
                }
            }

            item {
                CharactersChart(
                    charactersRanking,
                    mentionsRanking,
                    genre,
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .fillParentMaxHeight(.5f),
                )
            }

            item {
                Text(
                    stringResource(R.string.saga_detail_relationships_section_title),
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                            color = MaterialTheme.colorScheme.onBackground,
                        ),
                    modifier = Modifier.padding(16.dp),
                )
            }

            item {
                LazyRow {
                    items(saga.relationships) { relation ->
                        RelationShipCard(
                            content = relation,
                            saga = saga.data,
                            modifier =
                                Modifier
                                    .padding(16.dp)
                                    .requiredWidthIn(max = 300.dp),
                        )
                    }
                }
            }

            item {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onBackground.copy(.1f),
                    modifier =
                        Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(),
                    thickness = 1.dp,
                )
            }

            item {
                Text(
                    "Horário mais jogado",
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                            color = MaterialTheme.colorScheme.onBackground,
                        ),
                    modifier = Modifier.padding(16.dp),
                )
            }

            item {
                HourRankChart(
                    hourRanking,
                    genre,
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .fillParentMaxHeight(.5f),
                )
            }

            item {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onBackground.copy(.1f),
                    modifier =
                        Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth(),
                    thickness = 1.dp,
                )
            }

            item {
                Text(
                    it.actsInsight?.content?.title ?: emptyString(),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = bodyFont,
                            color = MaterialTheme.colorScheme.onBackground,
                        ),
                    modifier = Modifier.padding(16.dp),
                )
            }

            item {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onBackground.copy(.1f),
                    modifier =
                        Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(),
                    thickness = 1.dp,
                )
            }

            item {
                Text(
                    it.conclusion?.content?.title ?: emptyString(),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = bodyFont,
                            color = MaterialTheme.colorScheme.onBackground,
                        ),
                    modifier = Modifier.padding(16.dp),
                )
            }

            saga.data.emotionalReview?.let {
                item {
                    Text(
                        "Sobre você",
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                                color = MaterialTheme.colorScheme.onBackground,
                            ),
                        modifier = Modifier.padding(16.dp),
                    )
                }

                item {
                    Text(
                        it,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = bodyFont,
                                color = MaterialTheme.colorScheme.onBackground,
                            ),
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            item {
                Spacer(Modifier.height(50.dp))
            }
        }
    }
}

@Composable
private fun CharactersChart(
    charactersRanking: List<Pair<Character, Int>>,
    mentionsRanking: List<Pair<Character, Int>>,
    genre: Genre,
    modifier: Modifier,
) {
    val resolvedColor = MaterialTheme.colorScheme.primary
    val resolvedIconColor = MaterialTheme.colorScheme.secondary

    val data =
        remember(resolvedColor, resolvedIconColor) {
            listOf(
                Bars(
                    label = "Ranking de personagens",
                    values =
                        charactersRanking
                            .map {
                                List(2) { index ->
                                    val color = (
                                        it.first.hexColor.hexToColor()
                                            ?: resolvedColor
                                    )
                                    val colorEffect = if (index == 0) color else color.darker(.3f)
                                    val mentionsForCharacter =
                                        mentionsRanking.find { character -> it.first.id == character.first.id }?.second
                                            ?: 0

                                    val displayValue =
                                        if (index == 0) it.second else mentionsForCharacter

                                    if (index % 2 == 0) "messages" else "mentions"

                                    Bars.Data(
                                        label = "${it.first.name}",
                                        value = displayValue.toDouble(),
                                        color = colorEffect.solidGradient(),
                                    )
                                }
                            }.flatten(),
                ),
            )
        }

    val cornerSize = genre.cornerSize()
    Box(modifier = modifier) {
        ColumnChart(
            modifier = Modifier.fillMaxSize(),
            data = data,
            gridProperties =
                GridProperties(
                    enabled = false,
                ),
            dividerProperties =
                DividerProperties(
                    false,
                ),
            popupProperties =
                PopupProperties(
                    containerColor = resolvedColor,
                    cornerRadius = cornerSize,
                    textStyle =
                        MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                            color = resolvedIconColor,
                        ),
                    contentBuilder = { dataIndex, valueIndex, value ->
                        val suffix = if (valueIndex % 2 == 0) "messages" else "mentions"
                        "${value.toInt()} $suffix"
                    },
                ),
            labelHelperProperties =
                LabelHelperProperties(
                    true,
                    textStyle =
                        MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                            color = MaterialTheme.colorScheme.onBackground,
                        ),
                ),
            labelProperties =
                LabelProperties(
                    true,
                    textStyle =
                        MaterialTheme.typography.labelMedium.copy(
                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        ),
                ),
            indicatorProperties =
                HorizontalIndicatorProperties(
                    enabled = false,
                ),
            barProperties =
                BarProperties(
                    cornerRadius =
                        Bars.Data.Radius.Rectangle(
                            topRight = cornerSize,
                            topLeft = cornerSize,
                        ),
                    spacing = 8.dp,
                    thickness = 20.dp,
                ),
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
        )
    }
}

@Composable
private fun MessagesRankChart(
    saga: SagaContent,
    messagesRanking: List<Pair<SenderType, Int>>,
    modifier: Modifier,
) {
    val genre = saga.data.genre
    val typeTitles =
        messagesRanking.map {
            it.first.title()
        }
    val cornerSize = genre.cornerSize()
    val resolvedColor = MaterialTheme.colorScheme.primary
    val resolvedIconColor = MaterialTheme.colorScheme.secondary
    RowChart(
        modifier = modifier,
        data =
            remember {
                messagesRanking.mapIndexed { i, value ->
                    Bars(
                        typeTitles[i],
                        values =
                            listOf(
                                Bars.Data(
                                    label = typeTitles[i],
                                    value = value.second.toDouble(),
                                    color =
                                        if (i == 0) {
                                            resolvedColor.solidGradient()
                                        } else {
                                            Brush.verticalGradient(
                                                resolvedColor.darkerPalette(i + 2),
                                            )
                                        },
                                ),
                            ),
                    )
                }
            },
        dividerProperties =
            DividerProperties(
                false,
            ),
        labelHelperProperties =
            LabelHelperProperties(
                true,
                textStyle =
                    MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
            ),
        labelProperties =
            LabelProperties(
                textStyle =
                    MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                enabled = true,
            ),
        popupProperties =
            PopupProperties(
                containerColor = resolvedColor,
                cornerRadius = cornerSize,
                textStyle =
                    MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        color = resolvedIconColor,
                    ),
            ),
        gridProperties =
            GridProperties(
                enabled = false,
            ),
        barProperties =
            BarProperties(
                spacing = 4.dp,
                thickness = 24.dp,
                cornerRadius =
                    Bars.Data.Radius.Rectangle(
                        topRight = cornerSize,
                        bottomRight = cornerSize,
                    ),
            ),
    )
}

@Composable
private fun HourRankChart(
    hourRanking: Map<Int, List<MessageContent>>,
    genre: Genre,
    modifier: Modifier,
) {
    val label =
        MaterialTheme.typography.labelMedium.copy(
            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
        )
    val palette = genre.colorPalette()
    val cornerSize = genre.cornerSize()
    val resolvedColor = MaterialTheme.colorScheme.primary
    val resolvedIconColor = MaterialTheme.colorScheme.secondary
    LineChart(
        modifier = modifier,
        data =
            remember(palette, cornerSize) {
                listOf(
                    Line(
                        label = "Hora mais jogada",
                        values = hourRanking.map { it.value.size.toDouble() },
                        color = Brush.verticalGradient(palette),
                        firstGradientFillColor = resolvedColor,
                        secondGradientFillColor = Color.Transparent,
                        strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                        gradientAnimationDelay = 1000,
                        drawStyle = DrawStyle.Stroke(width = 2.dp),
                        popupProperties =
                            PopupProperties(
                                containerColor = resolvedColor,
                                cornerRadius = cornerSize,
                                textStyle =
                                    label.copy(
                                        color = resolvedIconColor,
                                    ),
                                contentBuilder = { dataIndex, valueIndex, value ->
                                    "${value.toInt()} messages"
                                },
                            ),
                    ),
                )
            },
        gridProperties =
            GridProperties(
                enabled = false,
            ),
        labelHelperProperties =
            LabelHelperProperties(
                enabled = true,
                textStyle =
                    MaterialTheme.typography.labelMedium.copy(
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
            ),
        indicatorProperties =
            HorizontalIndicatorProperties(
                enabled = false,
            ),
        dividerProperties =
            DividerProperties(
                false,
            ),
        labelProperties =
            LabelProperties(
                enabled = true,
                labels = hourRanking.map { "${it.key}h" },
                padding = 16.dp,
                textStyle =
                    MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f),
                    ),
            ),
        animationMode =
            AnimationMode.Together(delayBuilder = {
                it * 500L
            }),
    )
}
