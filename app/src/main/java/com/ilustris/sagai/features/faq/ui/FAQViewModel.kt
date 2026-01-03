package com.ilustris.sagai.features.faq.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.faq.domain.usecase.GetFaqsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class FAQViewModel
    @Inject
    constructor(
        private val getFaqsUseCase: GetFaqsUseCase,
    ) : ViewModel() {
        private val _faqState = MutableStateFlow<FAQState>(FAQState.Loading)
        val faqState = _faqState.asStateFlow()

        init {
            fetchFaqs()
        }

        fun fetchFaqs() {
            viewModelScope.launch {
                Locale.getDefault().language
                _faqState.update { FAQState.Loading }
                getFaqsUseCase()
                    .onSuccess { faq ->
                        _faqState.update { FAQState.FaqsRetrieved(faq) }
                    }.onFailure { failure ->
                        _faqState.update { FAQState.FaqsError(failure.message) }
                    }
            }
        }
    }
