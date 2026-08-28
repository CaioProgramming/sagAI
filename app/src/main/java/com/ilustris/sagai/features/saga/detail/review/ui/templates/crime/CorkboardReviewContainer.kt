package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.ui.genre.crime.CorkboardBackground
import com.ilustris.sagai.ui.theme.themeFilter

/**
 * Wires [CorkboardBoard] into [com.ilustris.sagai.features.saga.detail.ui.SagaReview] the same
 * way [com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.ComicBoardReviewContainer]
 * wires up Heroes' comic board — genre chrome (background, dismiss icon) around a board that owns
 * its own navigation, so `Continue`/page-sequence actions have nothing to bubble up to here.
 */
@Composable
fun CorkboardReviewContainer(
    pages: List<ReviewPage>,
    genre: Genre,
    onEnsureGeneration: () -> Unit,
    onDismiss: () -> Unit,
    onShare: (ShareType) -> Unit,
    onRegenerate: () -> Unit,
) {
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { onEnsureGeneration() }

    Box(
        Modifier
            .fillMaxSize()
            .themeFilter(),
    ) {
        CorkboardBackground(Modifier.fillMaxSize())

        CorkboardBoard(
            pages = pages,
            modifier = Modifier.fillMaxSize(),
            onFinished = { finished = true },
            onPanelAction = { action ->
                when (action) {
                    is ReviewAction.Share -> onShare(action.shareType)
                    ReviewAction.Finish -> onDismiss()
                    ReviewAction.Regenerate -> onRegenerate()
                    else -> Unit
                }
            },
        )

        IconButton(
            onClick = onDismiss,
            modifier =
                Modifier
                    .size(24.dp)
                    .alpha(if (finished) 0.9f else 0.6f)
                    .align(Alignment.TopCenter)
                    .statusBarsPadding(),
        ) {
            Icon(
                painter = painterResource(genre.icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
