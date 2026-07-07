package com.example.gamest.ui.screens.search

import com.example.gamest.model.GameFilter
import com.example.gamest.model.GameUiModel

data class SearchUiState(
    val searchQuery: String = "",
    val selectedFilter: GameFilter = GameFilter.TopRated,
    val games: List<GameUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val page: Int = 1,
    val canLoadMore: Boolean = true,
    val errorMessage: String? = null
)
