package com.example.gamest.ui.screens.steam

data class SteamConnectionUiState(
    val profileUrl: String = "",
    val isLoading: Boolean = false,
    val result: SteamConnectionResultUiModel? = null,
    val errorMessage: String? = null,

    val isConnected: Boolean = false,
    val connectedSteamId: String? = null,
    val connectedPersonaName: String? = null,
    val connectedAvatarUrl: String? = null,
    val lastSyncAt: Long? = null,
    val isAddingProfile: Boolean = false
)

data class SteamConnectionResultUiModel(
    val steamId: String,
    val personaName: String,
    val avatarUrl: String?,
    val canonicalProfileUrl: String,
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
