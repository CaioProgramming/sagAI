import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import com.ilustris.sagai.R

@Composable
fun GrainOverlay(alpha: Float = .5f) {
    val grainPainter = painterResource(R.drawable.grain_overlay)

    Image(
        painter = grainPainter,
        null,
        colorFilter =
            ColorFilter.tint(
                Color.Black.copy(alpha = alpha),
                blendMode = BlendMode.Overlay,
            ),
        modifier =
            Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = alpha),
    )
}
