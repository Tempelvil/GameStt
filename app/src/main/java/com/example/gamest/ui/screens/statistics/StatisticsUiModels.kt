package com.example.gamest.ui.screens.statistics

import com.example.gamest.data.local.GameStatus

data class StatisticGameUiModel(
    val id: Int,
    val title: String,
    val imageUrl: String?,
    val status: GameStatus,
    val userRating: Int?,
    val hoursPlayed: Int
)

data class StatusStatisticUiModel(
    val status: GameStatus,
    val title: String,
    val count: Int
)

data class RatingStatisticUiModel(
    val rating: Int,
    val count: Int
)

data class GenrePlaytimeUiModel(
    val name: String,
    val hours: Int,
    val percent: Int
)
