package com.ilustris.sagai.features.home.ui.components
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.resolveIconColor
import com.ilustris.sagai.ui.theme.SagAITheme

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CreateSagaCard(
    modifier: Modifier = Modifier,
    dynamicNewSagaTexts: DynamicSagaPrompt?,
    onCreateNewChat: () -> Unit,
) {
    val genre = dynamicNewSagaTexts?.genre
    if (genre != null) {
        SagAITheme(genre = genre) {
            CreateSagaCardContent(
                modifier = modifier,
                dynamicNewSagaTexts = dynamicNewSagaTexts,
                onCreateNewChat = onCreateNewChat,
            )
        }
    } else {
        CreateSagaCardContent(
            modifier = modifier,
            dynamicNewSagaTexts = dynamicNewSagaTexts,
            onCreateNewChat = onCreateNewChat,
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun CreateSagaCardContent(
    modifier: Modifier = Modifier,
    dynamicNewSagaTexts: DynamicSagaPrompt?,
    onCreateNewChat: () -> Unit,
) {
    val shape = MaterialTheme.shapes.medium
    val genre = dynamicNewSagaTexts?.genre
    val visualConfig = LocalGenreVisualConfig.current
    val genreColor = genre?.resolveColor(visualConfig) ?: MaterialTheme.colorScheme.primary
    val contentColor = genre?.resolveIconColor(visualConfig) ?: MaterialTheme.colorScheme.onPrimary
    val containerColor = MaterialTheme.colorScheme.background
    val title =
        remember(dynamicNewSagaTexts) {
            dynamicNewSagaTexts?.title
        }
    val subtitle =
        remember(dynamicNewSagaTexts) {
            dynamicNewSagaTexts?.subtitle
        }
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .border(1.dp, genreColor, shape)
                .clip(shape)
                .background(containerColor)
                .clickable { onCreateNewChat() },
    ) {
        Row(
            modifier =
                Modifier
                    .background(genreColor)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = (title ?: stringResource(R.string.home_create_new_saga_title)).uppercase(),
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp,
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    ),
                color = contentColor,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .size(32.dp)
                        .padding(8.dp),
            ) {
                Icon(
                    painter = painterResource(genre?.icon ?: R.drawable.ic_spark),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    tint = genreColor,
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            1.dp,
            genreColor,
        )

        Text(
            text = subtitle ?: stringResource(R.string.home_create_new_saga_subtitle),
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    color = genreColor,
                ),
            color = genreColor,
            modifier =
                Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .alpha(.8f),
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun Float.toSp() =
    androidx.compose.ui.unit
        .TextUnit(this, androidx.compose.ui.unit.TextUnitType.Sp)
