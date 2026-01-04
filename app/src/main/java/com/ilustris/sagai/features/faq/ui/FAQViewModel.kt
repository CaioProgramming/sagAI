package com.ilustris.sagai.features.faq.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.faq.domain.usecase.GetFaqsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FAQViewModel
    @Inject
    constructor(
        private val getFaqsUseCase: GetFaqsUseCase,
    ) : ViewModel() {
        private val _faqState = MutableStateFlow<FAQState>(FAQState.Loading)
        val faqState = _faqState.asStateFlow()

        private val _query = MutableStateFlow("")
        val query = _query.asStateFlow()

        private var fullContent: com.ilustris.sagai.features.faq.data.model.FAQContent? = null

        init {
            fetchFaqs()
        }

        fun fetchFaqs() {
            viewModelScope.launch {
                _faqState.update { FAQState.Loading }
                getFaqsUseCase()
                    .onSuccessAsync { faq ->
                        fullContent = faq
                        _faqState.emit(
                            FAQState.FaqsRetrieved(faq),
                        )
                    }.onFailureAsync { failure ->
                        _faqState.emit(FAQState.FaqsError(failure.message))
                    }
            }
        }

        fun updateQuery(newQuery: String) {
            _query.value = newQuery
        }

        fun askAi() {
            val currentQuery = _query.value
            val context = fullContent ?: return
            if (currentQuery.isBlank()) return

            viewModelScope.launch {
                _faqState.emit(FAQState.AiLoading)
                getFaqsUseCase
                    .askAi(currentQuery, context)
                    .onSuccessAsync { reply ->
                        _faqState.emit(FAQState.AiReply(reply))
                    }.onFailureAsync { failure ->
                        _faqState.emit(FAQState.FaqsError(failure.message))
                    }
            }
        }

        fun clearAiReply() {
            fullContent?.let {
                viewModelScope.launch {
                    _faqState.emit(FAQState.FaqsRetrieved(it))
                }
            }
        }
    }
