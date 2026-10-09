package com.ilustris.sagai.features.progression.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.progression.data.model.CharacterFeat
import com.ilustris.sagai.features.progression.data.model.CharacterHonor
import com.ilustris.sagai.features.progression.data.model.HonorKind

/**
 * Explains an honor: the coin (or the turning badge for a legacy), what it is called, why it was granted and
 * the feats it cites. Several honors page sideways, with sparks as the indicators.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HonorSheet(
    honors: List<CharacterHonor>,
    startIndex: Int,
    feats: List<CharacterFeat>,
    genre: Genre,
    onDismiss: () -> Unit,
) {
    if (honors.isEmpty()) return
    val pagerState = rememberPagerState(initialPage = startIndex.coerceIn(0, honors.lastIndex)) { honors.size }
    val gold = genre.honorHighlight()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xF21A1224),
        contentColor = Color.White,
    ) {
        Column(
            modifier = Modifier.navigationBarsPadding().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
                val honor = honors[page]
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        stringResource(if (honor.kind == HonorKind.TITLE) R.string.progression_legacy_title else R.string.progression_medal_label).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.6.sp,
                        color = Color.White.copy(alpha = .65f),
                    )
                    if (honor.kind == HonorKind.TITLE) {
                        LegacyBadge3D(genre = genre, size = 120.dp)
                    } else {
                        MedalCoin(honor.emoji, genre, size = 96.dp)
                    }
                    Text(
                        honor.name.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = MaterialTheme.typography.headlineSmall.fontFamily),
                        color = gold,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        honor.description,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = Color.White.copy(alpha = .88f),
                        textAlign = TextAlign.Center,
                    )
                    if (honor.posthumous) {
                        Text(stringResource(R.string.progression_in_memory), style = MaterialTheme.typography.labelMedium, color = gold)
                    }
                    val cited = feats.filter { it.id in honor.featIds }
                    if (cited.isNotEmpty()) {
                        Column(Modifier.fillMaxWidth().padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                stringResource(R.string.progression_why).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = .55f),
                            )
                            cited.forEach {
                                Row(
                                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = .07f)).padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(it.title, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                    Text("T${it.tier}", style = MaterialTheme.typography.labelMedium, color = gold)
                                }
                            }
                        }
                    }
                }
            }
            if (honors.size > 1) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    repeat(honors.size) { i ->
                        Icon(
                            painterResource(R.drawable.ic_spark),
                            contentDescription = null,
                            tint = gold,
                            modifier = Modifier.size(if (i == pagerState.currentPage) 16.dp else 11.dp).alpha(if (i == pagerState.currentPage) 1f else .35f),
                        )
                    }
                }
            }
            Text(
                stringResource(R.string.progression_close),
                style = MaterialTheme.typography.labelLarge,
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = .12f))
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 24.dp, vertical = 10.dp),
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}
