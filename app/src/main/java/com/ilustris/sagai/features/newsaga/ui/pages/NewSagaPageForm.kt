package com.ilustris.sagai.features.newsaga.ui.pages

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.features.newsaga.data.model.SagaForm

@Composable
fun NewSagaPagesView(
    pagerState: PagerState,
    currentData: SagaForm,
    modifier: Modifier = Modifier,
    onUpdateData: (NewSagaPages, Any?) -> Unit = { _, _ -> },
) {
    HorizontalPager(pagerState, modifier = modifier, userScrollEnabled = false) {
        val page = NewSagaPages.entries[it]
        val data =
            when (page) {
                NewSagaPages.TITLE -> currentData.title
                NewSagaPages.GENRE -> currentData.genre
                NewSagaPages.DESCRIPTION -> currentData.description
                NewSagaPages.CHARACTER -> currentData
            }
        page.content(
            { newData ->
                onUpdateData(page, newData)
            },
            data,
        )

        LaunchedEffect(page) {
            when (page) {
                NewSagaPages.GENRE -> {
                    onUpdateData(page, currentData.genre)
                }
                else -> doNothing()
            }
        }
    }
}
