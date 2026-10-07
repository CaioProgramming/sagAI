package com.ilustris.sagai.ui.genre.choice

import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.genre.GenreSurfaceStyle
import com.ilustris.sagai.ui.genre.collage.PAPER_INK
import com.ilustris.sagai.ui.genre.collage.PAPER_WHITE
import com.ilustris.sagai.ui.genre.collage.TornPaperScrap
import com.ilustris.sagai.ui.genre.comic.COMIC_INK
import com.ilustris.sagai.ui.genre.comic.COMIC_PAPER
import com.ilustris.sagai.ui.genre.crime.rememberCorkboardPalette
import com.ilustris.sagai.ui.genre.surfaceStyle
import com.ilustris.sagai.ui.genre.terminal.neonGlow
import com.ilustris.sagai.ui.theme.LocalSagaGenre
import com.ilustris.sagai.ui.theme.glassLight
import com.ilustris.sagai.ui.theme.glassSheen
import kotlin.random.Random

/**
 * One option of a forced-choice dilemma, dealt face down. Turning it over reveals [text]; the
 * caller owns which card is up ([revealed]), so picking a second card simply lets the first fall
 * back to its back.
 *
 * Like every other genre surface, this is one entry point that resolves its material from the
 * theme: a book's leather and parchment, a terminal's locked plate, a corkboard's manila folder and
 * index card, torn photocopy, or a printed comic panel. Callers never branch on genre.
 *
 * [seed] gives each card its own stable tilt and tear, derived once so the table doesn't jitter.
 */
@Composable
fun GenreChoiceCard(
    text: String,
    revealed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    seed: Int = 0,
    genre: Genre? = LocalSagaGenre.current,
) {
    val style = genre?.surfaceStyle() ?: GenreSurfaceStyle.DEFAULT
    val hiddenDescription = stringResource(R.string.milestone_choices_card_hidden)
    val interactionSource = remember { MutableInteractionSource() }

    FlipCard(
        faceUp = revealed,
        modifier =
            modifier
                .semantics { contentDescription = if (revealed) text else hiddenDescription }
                .clickable(
                    interactionSource = interactionSource,
                    // A ripple over a card reads as a button; this is a card being turned over.
                    indication = null,
                    role = Role.Button,
                    enabled = !revealed,
                    onClick = onClick,
                ),
        back = { CardBack(style, genre, seed) },
        face = { CardFace(style, text, seed) },
    )
}

@Composable
private fun CardBack(
    style: GenreSurfaceStyle,
    genre: Genre?,
    seed: Int,
) {
    when (style) {
        GenreSurfaceStyle.BOOK -> BookBack(genre)
        GenreSurfaceStyle.TERMINAL -> TerminalBack()
        GenreSurfaceStyle.CRIME -> CrimeBack(seed)
        GenreSurfaceStyle.COLLAGE -> CollageBack(seed)
        GenreSurfaceStyle.COMIC -> ComicBack()
        GenreSurfaceStyle.DEFAULT -> PlainBack(genre)
    }
}

@Composable
private fun CardFace(
    style: GenreSurfaceStyle,
    text: String,
    seed: Int,
) {
    when (style) {
        GenreSurfaceStyle.BOOK -> BookFace(text)
        GenreSurfaceStyle.TERMINAL -> TerminalFace(text)
        GenreSurfaceStyle.CRIME -> CrimeFace(text, seed)
        GenreSurfaceStyle.COLLAGE -> CollageFace(text, seed)
        GenreSurfaceStyle.COMIC -> ComicFace(text)
        GenreSurfaceStyle.DEFAULT -> PlainFace(text)
    }
}

/** Long options step down a size so a sentence never overflows a card. */
@Composable
private fun optionStyle(text: String): TextStyle =
    if (text.length > 80) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge

@Composable
private fun GenreEmblem(
    genre: Genre?,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painterResource(genre?.icon ?: R.drawable.ic_spark),
        contentDescription = null,
        tint = tint,
        modifier = modifier,
    )
}

// region Plain (no genre yet: the saga-creation quiz)

private val CosmicBase = Color(0xFF100E1A)
private val CosmicFace = Color(0xFF15122A)
private val CosmicViolet = Color(0xFF8B6CFF)

@Composable
private fun PlainBack(genre: Genre?) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        Modifier
            .fillMaxSize()
            .shadow(8.dp, shape)
            .clip(shape)
            .background(CosmicBase)
            .background(Brush.radialGradient(listOf(CosmicViolet.copy(alpha = 0.3f), Color.Transparent)))
            .drawBehind {
                val center = Offset(size.width / 2f, size.height * 0.45f)
                var radius = 14.dp.toPx()
                while (radius < size.height) {
                    drawCircle(Color.White.copy(alpha = 0.04f), radius, center, style = Stroke(1.dp.toPx()))
                    radius += 14.dp.toPx()
                }
            }.glassSheen(shape, alpha = 0.3f, tint = Color(0xFFBEAAFF))
            .border(1.dp, Color.White.copy(alpha = 0.16f), shape),
        contentAlignment = Alignment.Center,
    ) {
        GenreEmblem(genre, Color(0xFFD6CAFF), Modifier.size(34.dp))
    }
}

@Composable
private fun PlainFace(text: String) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        Modifier
            .fillMaxSize()
            .shadow(8.dp, shape)
            .clip(shape)
            .background(CosmicFace)
            .background(Brush.verticalGradient(listOf(CosmicViolet.copy(alpha = 0.34f), Color.Transparent)))
            .glassSheen(shape, alpha = 0.22f, tint = Color(0xFFBEAAFF))
            .border(1.dp, Color(0xFFD6CAFF).copy(alpha = 0.6f), shape)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

// endregion

// region Book (Fantasy, Shinobi, Cowboy, Horror)

@Composable
private fun BookBack(genre: Genre?) {
    val primary = MaterialTheme.colorScheme.primary
    val leather = lerp(primary, Color.Black, .6f)
    val gilt = lerp(primary, Color.White, .35f)
    val shape = RoundedCornerShape(10.dp)
    Box(
        Modifier
            .fillMaxSize()
            .shadow(8.dp, shape)
            .background(leather, shape)
            // Lacquered leather: the gilt catches light along the bottom edge.
            .glassLight(shape, gilt, { 0.5f })
            .glassSheen(shape, alpha = 0.14f, tint = gilt)
            .padding(8.dp)
            .border(1.5.dp, gilt.copy(alpha = .7f), RoundedCornerShape(6.dp))
            .padding(5.dp)
            .border(0.75.dp, gilt.copy(alpha = .45f), RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center,
    ) {
        GenreEmblem(genre, gilt, Modifier.size(44.dp))
    }
}

@Composable
private fun BookFace(text: String) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(10.dp)
    Box(
        Modifier
            .fillMaxSize()
            .shadow(8.dp, shape)
            .background(scheme.surfaceContainerHigh, shape)
            .glassLight(shape, scheme.primary, { 0.4f })
            .glassSheen(shape, alpha = 0.1f)
            .padding(8.dp)
            .border(1.dp, scheme.primary.copy(alpha = .55f), RoundedCornerShape(6.dp))
            .padding(14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = optionStyle(text).copy(fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic),
            color = scheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

// endregion

// region Terminal (Cyberpunk, Space Opera)

private const val LOCKED_GLYPHS = "░▒▓█▓▒░ ▒▓░▓▒█░▒"

@Composable
private fun TerminalBack() {
    val accent = MaterialTheme.colorScheme.primary
    val plate = lerp(MaterialTheme.colorScheme.background, Color.Black, .5f)
    val mono = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace)
    val shape = RoundedCornerShape(2.dp)
    Box(
        Modifier
            .fillMaxSize()
            .background(plate, shape)
            .glassLight(shape, accent, { 0.55f })
            .glassSheen(shape, alpha = 0.1f, tint = accent)
            .border(1.dp, accent.copy(alpha = .8f), shape)
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(9) { row ->
                Text(
                    text = LOCKED_GLYPHS.drop(row % 5).take(11),
                    style = mono,
                    color = accent.copy(alpha = .22f),
                    maxLines = 1,
                )
            }
        }
        Text(
            text = "[ LOCKED ]",
            style = mono.copy(fontWeight = FontWeight.Bold).neonGlow(accent),
            color = accent,
            modifier = Modifier.background(plate).padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun TerminalFace(text: String) {
    val accent = MaterialTheme.colorScheme.primary
    val plate = lerp(MaterialTheme.colorScheme.background, Color.Black, .5f)
    val shape = RoundedCornerShape(2.dp)
    Box(
        Modifier
            .fillMaxSize()
            .background(plate, shape)
            .glassLight(shape, accent, { 0.55f })
            .glassSheen(shape, alpha = 0.1f, tint = accent)
            .border(1.dp, accent, shape)
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = "> $text",
            style =
                MaterialTheme.typography.bodyMedium
                    .copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    .neonGlow(accent, blurRadius = 8f),
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

// endregion

// region Crime (corkboard)

private val MANILA = Color(0xFFC9A66B)
private val MANILA_EDGE = Color(0xFF9C7B45)

@Composable
private fun PinHead(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(12.dp)
            .shadow(2.dp, CircleShape)
            .background(color, CircleShape)
            .border(1.dp, Color.White.copy(alpha = .4f), CircleShape),
    )
}

@Composable
private fun CrimeBack(seed: Int) {
    val palette = rememberCorkboardPalette()
    val tilt = remember(seed) { (Random(seed).nextFloat() - .5f) * 6f }
    val shape = RoundedCornerShape(3.dp)
    Box(Modifier.fillMaxSize().rotate(tilt)) {
        Box(
            Modifier
                .fillMaxSize()
                .shadow(6.dp, shape)
                .background(MANILA, shape)
                // The folder sits in a plastic sleeve: a rim and a passing gloss, no tinted light.
                .glassLight(shape, Color.White, { 0.3f }, light = Color.White)
                .glassSheen(shape, alpha = 0.16f)
                .border(1.dp, MANILA_EDGE, shape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.milestone_choices_classified),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp),
                color = palette.thread,
                modifier =
                    Modifier
                        .rotate(-12f)
                        .border(BorderStroke(2.5.dp, palette.thread), RoundedCornerShape(3.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
        PinHead(palette.pin, Modifier.align(Alignment.TopCenter).padding(top = 0.dp))
    }
}

@Composable
private fun CrimeFace(
    text: String,
    seed: Int,
) {
    val palette = rememberCorkboardPalette()
    val tilt = remember(seed) { (Random(seed).nextFloat() - .5f) * 6f }
    val shape = RoundedCornerShape(3.dp)
    Box(Modifier.fillMaxSize().rotate(tilt)) {
        Box(
            Modifier
                .fillMaxSize()
                .shadow(6.dp, shape)
                .background(palette.paper, shape)
                .glassLight(shape, Color.White, { 0.3f }, light = Color.White)
                .glassSheen(shape, alpha = 0.16f)
                .border(1.dp, Color.Black.copy(alpha = .08f), shape)
                .padding(horizontal = 12.dp, vertical = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = optionStyle(text).copy(fontStyle = FontStyle.Italic),
                // The paper is light stock in both themes, so the ink is fixed rather than themed.
                color = palette.ink,
                textAlign = TextAlign.Center,
            )
        }
        PinHead(palette.pin, Modifier.align(Alignment.TopCenter))
    }
}

// endregion

// region Collage (Punk Rock)

@Composable
private fun CollageBack(seed: Int) {
    val accent = MaterialTheme.colorScheme.primary
    TornPaperScrap(
        seed = seed + 1,
        paperColor = PAPER_INK,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
    ) {
        Text(
            text = "?",
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black),
            color = PAPER_WHITE,
            modifier = Modifier.align(Alignment.Center),
        )
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .rotate(-4f)
                .size(width = 54.dp, height = 14.dp)
                .background(accent.copy(alpha = .85f)),
        )
    }
}

@Composable
private fun CollageFace(
    text: String,
    seed: Int,
) {
    TornPaperScrap(
        seed = seed + 1,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
    ) {
        Text(
            text = text.uppercase(),
            style = optionStyle(text).copy(fontWeight = FontWeight.Black),
            color = PAPER_INK,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

// endregion

// region Comic (Heroes)

/** Hard offset, no blur: a printed card sitting a beat above the page. */
private fun Modifier.comicCardShadow(shape: Shape = RectangleShape): Modifier =
    dropShadow(
        shape = shape,
        shadow = Shadow(radius = 0.dp, spread = 0.dp, color = COMIC_INK, offset = DpOffset(5.dp, 5.dp)),
    )

@Composable
private fun ComicBack() {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        Modifier
            .fillMaxSize()
            .padding(end = 5.dp, bottom = 5.dp)
            .comicCardShadow()
            .background(primary)
            .border(3.dp, COMIC_INK)
            .drawHalftone(COMIC_INK.copy(alpha = .22f))
            // Spot varnish: a printed cover catches a passing gloss, and nothing else changes.
            .glassSheen(RectangleShape, alpha = 0.12f),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "?!",
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
            color = COMIC_PAPER,
        )
    }
}

@Composable
private fun ComicFace(text: String) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(end = 5.dp, bottom = 5.dp)
            .comicCardShadow()
            .background(COMIC_PAPER)
            .border(3.dp, COMIC_INK)
            .glassSheen(RectangleShape, alpha = 0.1f)
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = optionStyle(text).copy(fontWeight = FontWeight.Bold),
            color = COMIC_INK,
            textAlign = TextAlign.Center,
        )
    }
}

/** A field of print dots, the cheapest thing that makes a flat colour read as ink on paper. */
private fun Modifier.drawHalftone(color: Color): Modifier =
    drawBehind {
        val step = 12.dp.toPx()
        var y = step / 2
        var row = 0
        while (y < size.height) {
            var x = if (row % 2 == 0) step / 2 else step
            while (x < size.width) {
                drawCircle(color, radius = 2.2.dp.toPx(), center = Offset(x, y))
                x += step
            }
            y += step / 2
            row++
        }
    }

// endregion
