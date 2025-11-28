package com.ilustris.sagai.features.act.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.DateFormatOption
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.actNumber
import com.ilustris.sagai.features.home.data.model.chapterNumber
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.ui.components.EmotionalCard
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import effectForGenre

@Composable
fun ActReader(saga: SagaContent) {
    val pagerState = rememberPagerState { saga.acts.size + 1 }
    val genre = saga.data.genre

    LazyColumn {
        item {
            Spacer(Modifier.height(50.dp))
        }
        item {
            IntroductionPage(saga)
        }

        saga.acts.forEach { act ->
            stickyHeader {
                Text(
                    act.data.title,
                    style =
                        MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = genre.headerFont(),
                            textAlign = TextAlign.Center,
                            brush = Brush.verticalGradient(genre.color.darkerPalette()),
                        ),
                    modifier =
                        Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .padding(16.dp)
                            .fillMaxWidth()
                            .padding(8.dp),
                )
            }
            item {
                Text(
                    act.data.introduction,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = genre.bodyFont(),
                        ),
                    modifier = Modifier.padding(16.dp),
                )
            }

            items(act.chapters) {
                val shape = RoundedCornerShape(genre.cornerSize())
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp),
                ) {
                    AsyncImage(
                        model = it.data.coverImage,
                        contentDescription = it.data.title,
                        contentScale = ContentScale.Crop,
                        modifier =
                            Modifier
                                .clip(shape)
                                .selectiveColorHighlight(genre.selectiveHighlight())
                                .effectForGenre(genre)
                                .fillMaxWidth()
                                .fillParentMaxHeight(.4f),
                    )

                    Text(
                        "${saga.chapterNumber(it.data).toRoman()} - ${it.data.title}",
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontFamily = genre.headerFont(),
                                textAlign = TextAlign.Start,
                            ),
                        modifier =
                            Modifier
                                .padding(vertical = 12.dp)
                                .fillMaxWidth(),
                    )

                    Text(
                        it.data.introduction,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = genre.bodyFont(),
                            ),
                    )

                    Text(
                        it.data.overview,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = genre.bodyFont(),
                            ),
                    )

                    if (it != act.chapters.last()) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .1f),
                            modifier = Modifier.padding(vertical = 8.dp).height(1.dp),
                        )
                    }
                }
            }

            item {
                Text(
                    act.data.content,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = genre.bodyFont(),
                        ),
                    modifier = Modifier.padding(16.dp),
                )
            }

            act.data.emotionalReview?.let {
                item {
                    Text(
                        it,
                        style =
                            MaterialTheme.typography.labelLarge.copy(
                                fontFamily = genre.bodyFont(),
                                fontWeight = FontWeight.Normal,
                                fontStyle = FontStyle.Italic,
                            ),
                        modifier = Modifier.padding(16.dp).alpha(.4f),
                    )
                }
            }

            item {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = .1f),
                    modifier = Modifier.padding(vertical = 12.dp).height(1.dp),
                )
            }
        }
        item {
            Text(
                "Conclusão",
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontFamily = genre.headerFont(),
                        textAlign = TextAlign.Start,
                    ),
                modifier =
                    Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
            )
        }

        item {
            Text(
                saga.data.endMessage,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = genre.bodyFont(),
                        textAlign = TextAlign.Justify,
                    ),
                modifier = Modifier.padding(8.dp).fillMaxWidth(),
            )
        }

        saga.data.emotionalReview?.let {
            item {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = .1f),
                    modifier = Modifier.padding(vertical = 12.dp).height(1.dp),
                )
            }
            item {
                Text(
                    "Sobre você",
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontFamily = genre.headerFont(),
                            textAlign = TextAlign.Start,
                        ),
                    modifier =
                        Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                )
            }

            item {
                Text(
                    it,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = genre.bodyFont(),
                            textAlign = TextAlign.Justify,
                        ),
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                )
            }
        }

        item { Spacer(Modifier.height(50.dp)) }
    }
}

@Composable
fun ActReadingContent(
    act: ActContent,
    sagaContent: SagaContent,
) {
    val genre = remember { sagaContent.data.genre }
    LazyColumn(modifier = Modifier.padding(vertical = 16.dp)) {
        items(act.chapters) {
            val shape = RoundedCornerShape(genre.cornerSize())
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Text(
                    "${sagaContent.chapterNumber(it.data).toRoman()} - ${it.data.title}",
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontFamily = genre.headerFont(),
                            textAlign = TextAlign.Start,
                        ),
                    modifier =
                        Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth(),
                )

                AsyncImage(
                    model = it.data.coverImage,
                    contentDescription = it.data.title,
                    placeholder = painterResource(R.drawable.ic_spark),
                    error = painterResource(R.drawable.ic_spark),
                    fallback = painterResource(R.drawable.ic_spark),
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .selectiveColorHighlight(genre.selectiveHighlight())
                            .effectForGenre(genre)
                            .fillMaxWidth()
                            .fillParentMaxHeight(.4f)
                            .clip(shape),
                )

                Text(
                    it.data.overview,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = genre.bodyFont(),
                        ),
                )

                it.data.emotionalReview?.let {
                    EmotionalCard(
                        it,
                        genre,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = .1f),
                    modifier = Modifier.padding(vertical = 12.dp).height(1.dp),
                )

                Text(
                    act.data.content,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = genre.bodyFont(),
                            textAlign = TextAlign.Justify,
                        ),
                )
            }
        }

        if (act == sagaContent.acts.last()) {
            item {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = .1f),
                    modifier = Modifier.padding(vertical = 12.dp).height(1.dp),
                )
            }

            item {
                Text(
                    "Conclusão",
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontFamily = genre.headerFont(),
                            textAlign = TextAlign.Start,
                        ),
                    modifier =
                        Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                )
            }

            item {
                Text(
                    sagaContent.data.endMessage,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = genre.bodyFont(),
                            textAlign = TextAlign.Justify,
                        ),
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                )
            }

            sagaContent.data.emotionalReview?.let {
                item {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = .1f),
                        modifier = Modifier.padding(vertical = 12.dp).height(1.dp),
                    )
                }
                item {
                    Text(
                        "Sobre você",
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontFamily = genre.headerFont(),
                                textAlign = TextAlign.Start,
                            ),
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                    )
                }

                item {
                    Text(
                        it,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = genre.bodyFont(),
                                textAlign = TextAlign.Justify,
                            ),
                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    )
                }
            }
        }

        act.data.emotionalReview?.let {
            item {
                EmotionalCard(
                    it,
                    genre,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        item { Spacer(Modifier.height(50.dp)) }
    }
}

@Composable
fun IntroductionPage(saga: SagaContent) {
    val genre = saga.data.genre
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier =
            Modifier
                .padding(16.dp),
    ) {
        val genre = saga.data.genre

        Text(
            "Criado em ${saga.data.createdAt.formatDate(DateFormatOption.DAY_OF_WEEK_DD_MM_YYYY)}",
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontFamily = genre.bodyFont(),
                ),
            textAlign = TextAlign.Center,
        )

        Text(
            saga.data.title,
            style =
                MaterialTheme.typography.displaySmall.copy(
                    textAlign = TextAlign.Center,
                    fontFamily = genre.headerFont(),
                    brush = genre.gradient(false),
                ),
            modifier = Modifier.reactiveShimmer(true),
        )

        Text(
            saga.data.description,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Justify,
                    fontFamily = genre.bodyFont(),
                ),
        )
    }
}
