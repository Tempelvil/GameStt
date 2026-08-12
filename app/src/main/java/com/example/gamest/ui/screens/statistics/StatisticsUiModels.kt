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

enum class SteamStatisticsPeriod(val title: String) {
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year")
}

data class SteamActivityPointUiModel(
    val label: String,
    val minutes: Long
)

data class SteamRecentGameUiModel(
    val appId: Int,
    val name: String,
    val imageUrl: String,
    val totalMinutes: Long,
    val recentMinutes: Long
)

data class SteamStatisticsProfileUiModel(
    val steamId: String,
    val personaName: String,
    val avatarUrl: String?,
    val status: String
)

data class SteamStatisticsUiModel(
    val profiles: List<SteamStatisticsProfileUiModel> = emptyList(),
    val selectedSteamId: String? = null,
    val personaName: String = "",
    val avatarUrl: String? = null,
    val selectedPeriod: SteamStatisticsPeriod = SteamStatisticsPeriod.MONTH,
    val activity: List<SteamActivityPointUiModel> = emptyList(),
    val periodMinutes: Long = 0,
    val totalMinutes: Long = 0,
    val recentTwoWeeksMinutes: Long = 0,
    val recentGames: List<SteamRecentGameUiModel> = emptyList(),
    val untrackedMinutes: Long = 0
) {
    val isAvailable: Boolean
        get() = selectedSteamId != null
}
