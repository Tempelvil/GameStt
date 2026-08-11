package com.example.gamest.ui.screens.steam

data class SteamConnectionUiState(
    val profileUrl: String = "",
    val isLoading: Boolean = false,
    val result: SteamConnectionResultUiModel? = null,
    val errorMessage: String? = null
)

data class SteamConnectionResultUiModel(
    val steamId: String,
    val ownedGamesCount: Int,
    val totalPlaytimeMinutes: Long,
    val recentlyPlayedCount: Int,
    val games: List<SteamGameUiModel>
)

data class SteamGameUiModel(
    val appId: Int,
    val name: String,
    val totalPlaytimeMinutes: Long,
    val recentPlaytimeMinutes: Long?
)
