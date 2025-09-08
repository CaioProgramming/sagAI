package com.ilustris.sagai.core.data

sealed class State {
    object Loading : State()
    data class Success(val data: Any) : State()
    data class Error(val message: String) : State()

}