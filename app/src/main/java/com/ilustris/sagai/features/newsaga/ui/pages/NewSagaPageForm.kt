package com.ilustris.sagai.features.newsaga.ui.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.SagaForm

@Composable
fun NewSagaPagesView(
    pagerState: PagerState,
    currentData: SagaForm,
    saga: SagaData?,
    modifier: Modifier = Modifier,
    onSendData: (NewSagaPages, Any?) -> Unit = { _, _ -> },
) {
    HorizontalPager(pagerState, userScrollEnabled = saga != null, modifier = modifier) {
        val page = NewSagaPages.entries[it]
        val data =
            when (page) {
                NewSagaPages.INTRO -> null
                NewSagaPages.TITLE -> currentData.title
                NewSagaPages.GENRE -> currentData.genre
                NewSagaPages.DESCRIPTION -> currentData.description
                NewSagaPages.GENERATING -> null
                NewSagaPages.RESULT -> saga
                NewSagaPages.CHARACTER -> currentData.characterDescription
            }
        page.content(
            { newData ->
                onSendData(page, newData)
            },
            data,
        )
    }
}
