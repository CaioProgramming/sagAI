package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.ui.theme.gradientFade

@Composable
fun SenderType.itemOption(
    selectedItem: SenderType? = null,
    iconTint: Color = MaterialTheme.colorScheme.onBackground,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    onSelect: (SenderType) -> Unit = {},
) {
    val isSelected = this == selectedItem
    val color by animateColorAsState(
        targetValue =
            if (isSelected) {
                selectedColor
            } else {
                iconTint
            },
    )
    Column(
        Modifier
            .padding(vertical = 4.dp)
            .border(1.dp, MaterialTheme.colorScheme.onBackground.gradientFade(), RoundedCornerShape(25.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = .8f), RoundedCornerShape(25.dp))
            .padding(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.Start,
            modifier =
                Modifier
                    .clip(
                        RoundedCornerShape(25.dp),
                    ).clickable {
                        onSelect(this@itemOption)
                    },
        ) {
            this@itemOption.icon()?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = this@itemOption.title(),
                    colorFilter =
                        ColorFilter
                            .tint(color),
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit,
                )

                Text(
                    text = this@itemOption.title() ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp),
                    color = color,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SenderTypePreview() {
    Column {
        SenderType.entries.forEach {
            it.itemOption(
                selectedItem = SenderType.THOUGHT,
            )
        }
    }
}

fun SenderType.icon() =
    when (this) {
        SenderType.USER -> R.drawable.talk_bubble
        SenderType.NARRATOR -> R.drawable.ic_feather
        SenderType.THOUGHT -> R.drawable.think_icon
        SenderType.ACTION -> R.drawable.action_icon
        else -> null
    }

fun SenderType.title() =
    when (this) {
        SenderType.USER -> "Falar"
        SenderType.NARRATOR -> "Narrar"
        SenderType.THOUGHT -> "Pensar"
        SenderType.ACTION -> "Ação"
        else -> null
    }
