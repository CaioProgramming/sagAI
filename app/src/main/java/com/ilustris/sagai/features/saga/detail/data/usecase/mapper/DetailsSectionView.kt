package com.ilustris.sagai.features.saga.detail.data.usecase.mapper

import coil3.Bitmap
import com.ilustris.sagai.features.act.data.model.Book
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipContent
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.timeline.domain.TimelineCardContent
import com.ilustris.sagai.features.wiki.data.model.Wiki

sealed class DetailSectionView(
    open val title: String,
    open val subtitle: String,
) {
    data class InitialSection(
        override val title: String,
        override val subtitle: String,
        val saga: Saga,
        val segmentedImage: Pair<Bitmap, Bitmap>?,
        val emotionalCard: String?,
        val starring: CharacterContent?,
        val topCharacters: List<CharacterContent>,
        val relationships: List<RelationshipContent>,
        val lastEvent: TimelineCardContent?,
        val latestWikis: List<Wiki>,
        val books: List<Book>,
        val chaptersCount: Int,
        val hasActs: Boolean,
        val endMessage: String?,
        val readyToReview: Boolean,
    ) : DetailSectionView(
            title,
            subtitle,
        )
}
