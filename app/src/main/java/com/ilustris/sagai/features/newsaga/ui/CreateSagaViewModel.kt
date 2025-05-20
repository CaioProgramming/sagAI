package com.ilustris.sagai.features.newsaga.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.home.data.model.ChatData
import com.ilustris.sagai.features.newsaga.data.usecase.NewSagaUseCase
import com.ilustris.sagai.features.newsaga.ui.pages.NewSagaPages
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateSagaViewModel
    @Inject
    constructor(
        private val newSagaUseCase: NewSagaUseCase,
    ) : ViewModel() {
        val saga = MutableStateFlow<ChatData?>(null)
        val sagaPage = MutableStateFlow(NewSagaPages.TITLE)
        val genre = MutableStateFlow("")

        fun updateData(data: String) {
            when (sagaPage.value) {
                NewSagaPages.TITLE -> setSagaTitle(data)
                NewSagaPages.GENRE -> {
                    genre.value = data
                }
                NewSagaPages.DESCRIPTION -> setSagaDescription(data)
                NewSagaPages.SAVING -> saveSaga()
            }
        }

        private fun saveSaga() {
            viewModelScope.launch {
                saga.value?.let { newSagaUseCase.saveSaga(it) }
            }
        }

        fun setSagaTitle(title: String) {
            saga.value = saga.value?.copy(name = title)
        }

        fun setSagaDescription(description: String) {
            saga.value = saga.value?.copy(description = description)
        }
    }
