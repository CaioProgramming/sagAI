package com.ilustris.sagai.features.premium.ui

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.MainActivity
import com.ilustris.sagai.R
import com.ilustris.sagai.features.premium.PremiumTitle
import com.ilustris.sagai.features.premium.data.PremiumPlan
import com.ilustris.sagai.features.premium.data.PremiumPlansState

/** Tall enough for three plans; more than that scrolls rather than pushing the button off screen. */
private val PLAN_LIST_MAX_HEIGHT = 320.dp

/**
 * The plan list, as the last page of the premium onboarding.
 *
 * Cards are selected and then confirmed, rather than bought on tap. Tapping a plan directly would
 * open Google's payment sheet on a single touch, which is a lot to hand to a mis-tap, and the
 * confirm step is also what lets the user see what they are about to be charged for.
 */
@Composable
fun PremiumPlansContent(
    activity: MainActivity? = null,
    modifier: Modifier = Modifier,
) {
    val viewModel: PremiumPlansViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selectedKey by viewModel.selectedKey.collectAsStateWithLifecycle()
    val isPurchasing by viewModel.isPurchasing.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (val current = state) {
            PremiumPlansState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally),
                    strokeWidth = 2.dp,
                )
            }

            PremiumPlansState.Unavailable -> {
                Text(
                    stringResource(R.string.plans_load_error),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            is PremiumPlansState.Available -> {
                // Falls back to the featured plan rather than the first, so the one the app puts
                // forward is also the one already selected when the screen opens.
                val selected =
                    current.plans.firstOrNull { it.key == selectedKey }
                        ?: current.plans.firstOrNull { it.isFeatured }
                        ?: current.plans.first()

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = PLAN_LIST_MAX_HEIGHT),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item(key = "premium-title") {
                        PremiumTitle(
                            titleStyle = MaterialTheme.typography.titleMedium,
                            isAnimated = true,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }

                    items(current.plans, key = { it.key }) { plan ->
                        PlanCard(
                            plan = plan,
                            isSelected = plan.key == selected.key,
                            onSelect = { viewModel.select(plan) },
                        )
                    }
                }

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
        }
    }
}

@Composable
private fun PlanCard(
    plan: PremiumPlan,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    val borderColor by animateColorAsState(
        if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = .12f)
        },
        label = "plan-border",
    )

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
                .clickable(onClick = onSelect)
                .padding(16.dp),
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
            if (plan.description.isNotBlank()) {
                Text(
                    plan.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .7f),
                )
            }
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
