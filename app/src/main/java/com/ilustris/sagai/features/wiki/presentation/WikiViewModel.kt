package com.ilustris.sagai.features.wiki.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.features.wiki.data.mapper.WikiMapper
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.model.WikiGroup
import com.ilustris.sagai.features.wiki.data.usecase.WikiUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WikiViewModel
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val wikiMapper: WikiMapper,
        private val wikiUseCase: WikiUseCase,
    ) : ViewModel() {
        private val _saga = MutableStateFlow<SagaContent?>(null)
        val saga = _saga.asStateFlow()

        private val _wikiGroups = MutableStateFlow<List<WikiGroup>>(emptyList())
        val wikiGroups = _wikiGroups.asStateFlow()

        fun loadSaga(sagaId: Int) {
            viewModelScope.launch {
                sagaRepository.getSagaById(sagaId).collectLatest {
                    _saga.value = it
                    it?.let { sagaContent ->
                        _wikiGroups.value = wikiMapper.buildWikiGroups(sagaContent)
                    }
                }
            }
        }

        fun reviewWiki(wikis: List<Wiki>) {
            val currentSaga = saga.value ?: return
            viewModelScope.launch {
                wikiUseCase.mergeWikis(currentSaga, wikis)
            }
        }
    }
