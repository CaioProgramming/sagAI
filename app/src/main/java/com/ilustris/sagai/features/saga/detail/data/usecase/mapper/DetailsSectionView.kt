package com.ilustris.sagai.features.saga.detail.data.usecase.mapper

import coil3.Bitmap
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.domain.TimelineCardContent

sealed class DetailSectionView(
    open val title: String,
    open val subtitle: String,
) {
    data class InitialSection(
        override val title: String,
        override val subtitle: String,
        val saga: SagaContent,
        val segmentedImage: Pair<Bitmap, Bitmap>?,
        val emotionalCard: String?,
        val starring: CharacterContent?,
        val characters: List<CharacterContent>,
        val relationships: List<RelationshipContent>,
        val lastEvent: TimelineCardContent?,
        val chapters: List<ChapterContent>,
        val acts: List<ActContent>,
        val endMessage: String?,
        val readyToReview: Boolean,
    ) : DetailSectionView(
            title,
            subtitle,
        )
}
