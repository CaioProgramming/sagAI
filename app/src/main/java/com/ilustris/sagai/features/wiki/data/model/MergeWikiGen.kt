package com.ilustris.sagai.features.wiki.data.model

data class MergeWikiGen(
    val mergedItems: List<MergeWiki>,
)

data class MergeWiki(
    val firstItem: String,
    val secondItem: String,
    val mergedItem: Wiki,
)
