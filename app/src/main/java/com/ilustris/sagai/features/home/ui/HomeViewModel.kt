package com.ilustris.sagai.features.home.ui

import androidx.lifecycle.ViewModel
import com.ilustris.sagai.features.home.data.model.ChatData
import com.ilustris.sagai.features.home.data.usecase.ChatHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val chatHistoryUseCase: ChatHistoryUseCase) : ViewModel() {
    val chats = chatHistoryUseCase.getChats()
}
