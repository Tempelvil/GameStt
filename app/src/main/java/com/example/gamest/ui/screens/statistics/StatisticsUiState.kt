package com.example.gamest.ui.screens.statistics

data class StatisticsUiState(
    val playedGamesCount: Int = 0,
    val completedGamesCount: Int = 0,
    val averageRating: Double? = null,
    val ratedGamesCount: Int = 0,
    val totalHours: Int = 0,
    val gamesWithPlaytimeCount: Int = 0,
    val favoriteGenre: String? = null,
    val favoriteGenrePercent: Int? = null,

    val statusStatistics: List<StatusStatisticUiModel> = emptyList(),
    val completedGames: List<StatisticGameUiModel> = emptyList(),
    val ratingDistribution: List<RatingStatisticUiModel> = emptyList(),
    val highestRatedGames: List<StatisticGameUiModel> = emptyList(),
    val gamesByPlaytime: List<StatisticGameUiModel> = emptyList(),
    val genrePlaytime: List<GenrePlaytimeUiModel> = emptyList(),
    val favoriteGenreGames: List<StatisticGameUiModel> = emptyList(),

    val selectedSection: StatisticsSection =
        StatisticsSection.PLAYED_GAMES,

    val isLoading: Boolean = true,
    val isEmpty: Boolean = false
)
