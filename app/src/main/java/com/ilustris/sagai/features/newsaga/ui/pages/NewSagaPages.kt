package com.ilustris.sagai.features.newsaga.ui.pages

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import com.ilustris.sagai.R

enum class NewSagaPages(
   @StringRes val title: Int,
   @StringRes val subtitle: Int,
   @StringRes val inputHint: Int
) {
    TITLE(
        R.string.start_saga,
        R.string.start_saga_subtitle,
        R.string.saga_title_hint,
    ),
    GENRE(
        R.string.saga_genre,
        R.string.saga_genre_subtitle,
        R.string.saga_genre_hint,
    ),
    DESCRIPTION(
        R.string.saga_description,
        R.string.saga_description_subtitle,
        R.string.saga_description_hint,
    ),
    SAVING(
        R.string.saga_saving,
        R.string.saga_saving_subtitle,
        R.string.saga_saving_hint,
    ),
}
