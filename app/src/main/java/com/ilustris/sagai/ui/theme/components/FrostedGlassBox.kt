import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun GlassBox(content: @Composable (BoxScope, HazeState) -> Unit) {
    val haze = rememberHazeState()
    Box(modifier = Modifier.hazeSource(haze)) {
        content(this, haze)
    }
}

@Preview
@Composable
fun GlassBoxPreview() {
    val hazesource = rememberHazeState()
    Box {
        Image(
            painterResource(R.drawable.fantasy),
            null,
            modifier = Modifier.fillMaxSize().hazeSource(hazesource),
        )

        Text(
            "HazeBox",
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .size(200.dp)
                    .hazeEffect(state = hazesource),
        )
    }
}
