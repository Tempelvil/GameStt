package com.example.gamest.ui.screens.search

import com.example.gamest.model.GameUiModel

data class SearchUiState(
    val searchQuery: String = "",
    val games: List<GameUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedGenre: String? = null,
)
