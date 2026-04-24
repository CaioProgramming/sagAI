package com.ilustris.sagai.features.saga.chat.ui.components

import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.wiki.data.model.Wiki

sealed class ItemsType {
    data class Characters(
        val filteredCharacters: List<CharacterContent>,
        val query: String = "",
    ) : ItemsType()

    data class Wikis(
        val filteredWikis: List<Wiki>,
        val query: String = "",
    ) : ItemsType()
}
