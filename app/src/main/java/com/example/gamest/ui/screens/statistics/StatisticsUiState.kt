package com.example.gamest.ui.screens.statistics

import com.example.gamest.data.local.GameEntity

data class StatisticsUiState(
    val games: List<GameEntity> = emptyList(),

    val playedGamesCount: Int = 0,
    val averageRating: Double? = null,
    val totalHours: Int = 0,
    val favoriteGenre: String? = null,

    val selectedSection: StatisticsSection =
        StatisticsSection.PLAYED_GAMES,

    val isLoading: Boolean = true
)