package com.ilustris.sagai.ui.theme.components

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import kotlinx.coroutines.delay
import kotlin.collections.plusAssign
import kotlin.compareTo

@Composable
fun SparkIcon(
    modifier: Modifier,
    description: String,
    brush: Brush,
) {
    Box(modifier.clip(CircleShape)) {
        Image(
            painterResource(R.drawable.ic_spark),
            contentDescription = description,
            modifier =
                Modifier
                    .fillMaxSize()
                    .gradientFill(brush)
                    .blur(5.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
            alignment = Alignment.BottomCenter,
        )
        Image(
            painterResource(R.drawable.ic_spark),
            contentDescription = description,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.background),
            modifier =
                Modifier
                    .fillMaxSize()
                    .scale(.8f)
                    .alpha(.8f),
            alignment = Alignment.BottomCenter,
        )
    }
}

@Preview
@Composable
fun SparkIconPreview() {


    SparkIcon(
        modifier = Modifier.size(100.dp),
        description = "Spark Icon",
        brush = gradientAnimation(Genre.SCI_FI.gradient()),
    )
}