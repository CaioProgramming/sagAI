package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 * A text-only pin — a case note tacked to the board — for a stage with nothing to photograph:
 * the closing send-off, or a [sender]'s farewell (portrait pinned beside their own words). Uses
 * [PanelSpan.BAND], the same "a beat that's only words and needs no room for art" strip
 * [com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.PanelSpan] documents.
 */
class CorkboardNotePinPage(
    override val content: SagaContent,
    override val pageType: ReviewPageType,
    private val body: String,
    private val sender: Character? = null,
) : ReviewPage, CorkboardPinPage {
    override val panelSpan: PanelSpan = PanelSpan.BAND

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val genre = content.data.genre

        CorkPin(
            modifier = modifier.padding(16.dp),
            seed = pageType.ordinal * 31 + body.length,
        ) {
            Row(
                Modifier.width(260.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (sender != null) {
                    CharacterAvatar(
                        sender,
                        genre = genre,
                        modifier = Modifier.size(36.dp),
                    )
                }
                Column(Modifier.padding(start = if (sender != null) 10.dp else 0.dp)) {
                    sender?.let {
                        HandwrittenText(
                            text = it.name,
                            fontSize = 14.sp,
                            isBold = true,
                        )
                    }
                    HandwrittenText(
                        text = body,
                        modifier = Modifier.fillMaxWidth().padding(top = if (sender != null) 2.dp else 0.dp),
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        CorkboardBackground(modifier)
    }
}
