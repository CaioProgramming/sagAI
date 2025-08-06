package com.ilustris.sagai.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.mikepenz.hypnoticcanvas.shaderBackground
import com.mikepenz.hypnoticcanvas.shaders.Heat
import com.mikepenz.hypnoticcanvas.shaders.MeshGradient
import com.mikepenz.hypnoticcanvas.shaders.PurpleLiquid
import effectForGenre

@Composable
fun Genre.shaderBackground() =
    when (this) {
        Genre.FANTASY -> Heat(
            .3f
        )
       else -> PurpleLiquid
    }

@Preview(showBackground = true, widthDp = 300, heightDp = 500)
@Composable
fun MorphingStarGradientPreview() {
    SagAITheme {
        val genre = Genre.entries.random()
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .shaderBackground(
                        genre.shaderBackground()
                    )
            ,
        )
    }
}
