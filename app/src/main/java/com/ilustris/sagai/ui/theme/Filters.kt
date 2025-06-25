import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.brightness
import com.ilustris.sagai.ui.theme.contrast
import com.ilustris.sagai.ui.theme.grayScale

@Composable
fun Modifier.effectForGenre(genre: Genre): Modifier =
    this
        .grayScale(.5f)
        .brightness((.01f).unaryMinus())
        .contrast(0.85f)
