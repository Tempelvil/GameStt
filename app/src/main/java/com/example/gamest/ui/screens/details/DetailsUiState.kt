package com.example.gamest.ui.screens.details

import com.example.gamest.model.GameDetailsUiModel

sealed interface GameDetailsUiState{
    data object Loading: GameDetailsUiState

    data class Success(
        val game: GameDetailsUiModel
    ): GameDetailsUiState
    data class Error(
        val message: String
    ): GameDetailsUiState
}