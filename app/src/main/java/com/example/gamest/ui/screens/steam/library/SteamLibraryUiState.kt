package com.example.gamest.ui.screens.steam.library

data class SteamLibraryUiState(
    val personaName: String = "Steam user",
    val avatarUrl: String? = null,
    val profileUrl: String = "",
    val games: List<SteamLibraryGameUiModel> = emptyList(),
    val totalGamesCount: Int = 0,
    val searchQuery: String = "",
    val selectedFilter: SteamLibraryFilter = SteamLibraryFilter.ALL,
    val selectedSort: SteamLibrarySort = SteamLibrarySort.PLAYTIME,
    val lastSyncAt: Long? = null,
    val isSyncing: Boolean = false,
    val openingGameAppId: Int? = null,
    val errorMessage: String? = null,
    val profiles: List<SteamProfileUiModel> = emptyList(),
    val activeSteamId: String? = null,
    val activeProfileStatus: String? = null
)

data class SteamProfileUiModel(
    val steamId: String,
    val profileUrl: String,
    val personaName: String,
    val avatarUrl: String?,
    val status: String,
    val lastSyncAt: Long?
)

data class SteamLibraryGameUiModel(
    val appId: Int,
    val name: String,
    val imageUrl: String,
    val totalPlaytimeMinutes: Long,
    val recentPlaytimeMinutes: Long?,
    val lastPlayedAt: Long?
)

enum class SteamLibraryFilter(val title: String) {
    ALL("All"),
    RECENT("Recent"),
    PLAYED("Played"),
    NEVER_PLAYED("Never played")
}

enum class SteamLibrarySort(val title: String) {
    PLAYTIME("Playtime"),
    RECENT("Recently played"),
    NAME("Name")
}
