package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType.*

@Composable
fun SenderType.itemOption(
    iconSize: Dp = 40.dp,
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

    val borderColor by animateColorAsState(
        if (isSelected) selectedColor else MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
    )

    this@itemOption.icon()?.let {
        Box(
            modifier =
                Modifier
                    .padding(vertical = 8.dp)
                    .clip(CircleShape)
                    .border(1.dp, borderColor, CircleShape)
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(100))
                    .size(iconSize)
                    .clickable {
                        onSelect(this@itemOption)
                    },
        ) {
            Image(
                painter = painterResource(id = it),
                contentDescription = this@itemOption.title(),
                colorFilter =
                    ColorFilter
                        .tint(color),
                modifier =
                    Modifier
                        .padding(8.dp)
                        .align(Alignment.Center),
                contentScale = ContentScale.Fit,
            )
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
        SenderType.NEW_CHARACTER -> R.drawable.character_icon
        else -> null
    }

fun SenderType.title() =
    when (this) {
        SenderType.USER -> "Falar"
        SenderType.NARRATOR -> "Narrar"
        SenderType.THOUGHT -> "Pensar"
        SenderType.ACTION -> "Ação"
        SenderType.NEW_CHARACTER -> "Novo personagem"
        else -> emptyString()
    }

@Composable
fun SenderType.description() =

 when (this) {
        USER -> "Fala do seu personagem, o que ele diz na conversa atual."
        THOUGHT -> "O que seu personagem está pensando, quais seus planos e receios."
        ACTION -> "Uma ação que seu personagem está fazendo."
        NARRATOR -> "Complete a história com eventos externos ou acontecimentos no ambiente."
        NEW_CHAPTER -> "Inicie um novo capítulo na história, isso irá gerar um novo resumo para sua saga."
        NEW_CHARACTER -> "Adicione um novo personagem a sua história, ele poderá interagir com o personagem principal e demais personagens."
        CHARACTER -> "Faça uma pergunta para um personagem específico sobre a história."
    }
