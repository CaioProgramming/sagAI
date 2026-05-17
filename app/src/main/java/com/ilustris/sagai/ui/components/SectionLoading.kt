package com.ilustris.sagai.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.animations.genreVfx
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill

@Composable
fun SectionLoading(
    genre: Genre? = null,
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AnimatedContent(
            targetState = genre,
            transitionSpec = {
                fadeIn(tween(500)) togetherWith fadeOut(tween(500))
            },
            label = "SectionLoadingContent",
        ) { selectedGenre ->
            if (selectedGenre == null) {
                Image(
                    painter = painterResource(id = R.drawable.ic_spark),
                    contentDescription = "Loading...",
                    modifier = Modifier.size(50.dp),
                )
            } else {
                Image(
                    painter = painterResource(id = selectedGenre.icon),
                    contentDescription = selectedGenre.name,
                    modifier =
                        Modifier
                            .size(50.dp)
                            .gradientFill(selectedGenre.gradient())
                            .genreVfx(selectedGenre),
                )
            }
        }
    }
}
