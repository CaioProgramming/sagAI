package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.PanelSpan
import com.ilustris.sagai.ui.genre.crime.CorkPin
import com.ilustris.sagai.ui.genre.crime.CorkboardBackground
import com.ilustris.sagai.ui.theme.components.HandwrittenText

/**
 * One suspect photo per top character, pinned in a shared cluster ([PanelSpan.GRID], grouped
 * under [GROUP_KEY]) — replaces the single collapsed "shared group link" card the old chat
 * thread used to send.
 */
class CorkboardCharacterPinPage(
    override val content: SagaContent,
    private val character: Character,
    private val messageCount: Int,
) : ReviewPage, CorkboardPinPage {
    override val pageType: ReviewPageType = ReviewPageType.CHARACTERS
    override val panelSpan: PanelSpan = PanelSpan.GRID
    override val groupKey: String = GROUP_KEY

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val genre = content.data.genre

        CorkPin(
            modifier = modifier.padding(14.dp),
            seed = character.id,
        ) {
            Column {
                CharacterAvatar(
                    character,
                    genre = genre,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                )
                HandwrittenText(
                    text = "${character.name} ${character.lastName.orEmpty()}".trim(),
                    fontSize = 14.sp,
                    isBold = true,
                    centered = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                )
                HandwrittenText(
                    text = stringResource(R.string.messages_count_label, messageCount),
                    fontSize = 11.sp,
                    centered = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        CorkboardBackground(modifier)
    }

    private companion object {
        const val GROUP_KEY = "characters"
    }
}
