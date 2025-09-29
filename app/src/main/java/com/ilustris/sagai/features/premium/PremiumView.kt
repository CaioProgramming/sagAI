package com.ilustris.sagai.features.premium

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.ui.components.GenreCard
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.genresGradient
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer

@Composable
fun PremiumView() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        val brush = Brush.verticalGradient(holographicGradient)

        Box(Modifier.fillMaxWidth().fillMaxHeight(.3f)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
            ) {
                val genres = Genre.entries
                items(genres) {
                    GenreCard(it, false, Modifier.reactiveShimmer(true).padding(8.dp).aspectRatio(1f), false) { }
                }
            }

            Image(
                painterResource(R.drawable.ic_spark),
                null,
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .size(64.dp)
                        .gradientFill(brush)
                        .reactiveShimmer(true),
            )
        }

        Text(
            "Desbloqueie o poder do Sagas Pro",
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                brush = brush
            ),
        )

        Text(
            "Com o Sagas Pro, você pode criar imagens exclusivas para cada capítulo da sua história, utilizando estilos artísticos específicos de cada gênero para uma experiência visual única e imersiva.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )

        Text(
            "Explore recursos premium que permitem gerar imagens diretamente dos capítulos da sua saga, tornando suas narrativas ainda mais envolventes e personalizadas. Dê vida aos seus personagens e cenários com arte criada sob medida para o universo da sua história.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )

        Button(onClick = {},
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background,
            ),
            modifier = Modifier.padding(16.dp).fillMaxWidth()) {

            Text("Continuar", modifier = Modifier.gradientFill(brush).padding(8.dp))
        }

        Text(
            "Restaurar compras",
            style =
                MaterialTheme.typography.labelMedium.copy(
                    textAlign = TextAlign.Center,
                ),
            modifier =
                Modifier.alpha(.4f).padding(16.dp).fillMaxWidth().clickable {
                },
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
fun PremiumViewPreview() {
    SagAIScaffold {
        PremiumView()
    }
}
