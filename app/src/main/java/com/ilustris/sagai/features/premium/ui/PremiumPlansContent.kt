package com.ilustris.sagai.features.premium.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Context
import android.content.ContextWrapper
import com.ilustris.sagai.MainActivity
import com.ilustris.sagai.R
import com.ilustris.sagai.features.premium.PremiumTitle
import com.ilustris.sagai.features.premium.data.PremiumPlan
import com.ilustris.sagai.features.premium.data.PremiumPlansState

/**
 * The plan list, as the last page of the premium onboarding.
 *
 * One list from the title down to the confirm button, rather than a capped list sitting between
 * fixed pieces. The capped version had to guess a height that fit every plan count, and guessed
 * wrong: three cards overflowed it and the first was drawn cut in half, which reads as a rendering
 * bug rather than as a list that scrolls.
 *
 * Cards are selected and then confirmed, rather than bought on tap. Tapping a plan directly would
 * open Google's payment sheet on a single touch, which is a lot to hand to a mis-tap, and the
 * confirm step is also what lets the user see what they are about to be charged for.
 */
@Composable
fun PremiumPlansContent(modifier: Modifier = Modifier) {
    val viewModel: PremiumPlansViewModel = hiltViewModel()
    val activity = LocalContext.current.findMainActivity()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selectedKey by viewModel.selectedKey.collectAsStateWithLifecycle()
    val isPurchasing by viewModel.isPurchasing.collectAsStateWithLifecycle()
    val localError by viewModel.localError.collectAsStateWithLifecycle()
    val current = state

    LazyColumn(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item(key = "premium-title") {
            PremiumTitle(
                titleStyle = MaterialTheme.typography.headlineLarge,
                isAnimated = true,
            )
        }

        when (current) {
            PremiumPlansState.Loading -> {
                item(key = "loading") {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                    )
                }
            }

            PremiumPlansState.Unavailable -> {
                item(key = "unavailable") {
                    Text(
                        stringResource(R.string.plans_load_error),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            is PremiumPlansState.Available -> {
                // Falls back to the featured plan rather than the first, so the one the app puts
                // forward is also the one already selected when the screen opens.
                val selected =
                    current.plans.firstOrNull { it.key == selectedKey }
                        ?: current.plans.firstOrNull { it.isFeatured }
                        ?: current.plans.first()

                items(current.plans, key = { it.key }) { plan ->
                    PlanCard(
                        plan = plan,
                        isSelected = plan.key == selected.key,
                        onSelect = { viewModel.select(plan) },
                    )
                }

                item(key = "confirm") {
                    Button(
                        onClick = { viewModel.purchase(selected, activity) },
                        enabled = !isPurchasing,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        if (isPurchasing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Text(stringResource(R.string.plans_confirm))
                        }
                    }
                }

                localError?.let { message ->
                    item(key = "purchase-error") {
                        Text(
                            message,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun PlanCard(
    plan: PremiumPlan,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    val borderColor by animateColorAsState(
        if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = .12f)
        },
        label = "plan-border",
    )
    // Unselected cards tint the page artwork rather than covering it: a solid surface read as a
    // panel dropped onto the screen instead of part of it. The selected one goes opaque, because
    // its shadow falls on whatever is behind the card, and through a translucent card you see the
    // shadow lying on the artwork inside its own bounds.
    val containerColor by animateColorAsState(
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = if (isSelected) 1f else .35f),
        label = "plan-container",
    )
    val elevation by animateDpAsState(
        if (isSelected) 16.dp else 0.dp,
        label = "plan-elevation",
    )

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = elevation,
                    shape = shape,
                    ambientColor = MaterialTheme.colorScheme.primary,
                    spotColor = MaterialTheme.colorScheme.primary,
                )
                .clip(shape)
                .background(containerColor)
                .border(1.5.dp, borderColor, shape)
                .clickable(onClick = onSelect)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    plan.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (plan.isFeatured) {
                    Text(
                        stringResource(R.string.plan_featured_badge).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
            Text(
                plan.priceLine,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (isSelected) {
            Icon(
                painterResource(R.drawable.ic_check_circle),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
        } else {
            Column(
                Modifier
                    .size(22.dp)
                    .border(1.5.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .4f), CircleShape),
            ) {}
        }
    }
}

/**
 * The [MainActivity] behind this composition, unwrapped rather than cast.
 *
 * `LocalContext.current as? MainActivity` is null inside a Compose `Dialog`, which is where this
 * screen lives: the dialog composes against a `ContextWrapper` around the activity, not the
 * activity itself. The cast failing meant the purchase silently did nothing at all, since
 * launching Play's flow needs a real Activity.
 */
private fun Context.findMainActivity(): MainActivity? {
    var context: Context? = this
    while (context is ContextWrapper) {
        if (context is MainActivity) return context
        context = context.baseContext
    }
    return null
}
