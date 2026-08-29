package com.example.gamest.ui.screens.steam.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gamest.GameStApplication
import com.example.gamest.data.local.preferences.SteamConnectionData
import com.example.gamest.data.local.preferences.SteamConnectionPreferences
import com.example.gamest.data.repository.SteamGame
import com.example.gamest.data.repository.SteamRepository
import com.example.gamest.data.repository.SteamProfile
import com.example.gamest.data.local.SteamProfileStatus
import com.example.gamest.data.local.SteamIgdbMatchStatus
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SteamLibraryViewModel(
    private val steamRepository: SteamRepository,
    private val preferences: SteamConnectionPreferences
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedFilter = MutableStateFlow(SteamLibraryFilter.ALL)
    private val selectedSort = MutableStateFlow(SteamLibrarySort.PLAYTIME)
    private val syncState = MutableStateFlow(SyncState())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val connectionAndGames = preferences.connectionData
        .flatMapLatest { connection ->
            if (connection.steamId.isBlank()) {
                flowOf(connection to emptyList())
            } else {
                steamRepository.observeLibrary(connection.steamId)
                    .map { games -> connection to games }
            }
        }

    private val profileData = combine(
        connectionAndGames,
        steamRepository.observeProfiles()
    ) { (connection, games), profiles ->
        ProfileData(connection, games, profiles)
    }

    val uiState = combine(
        profileData,
        searchQuery,
        selectedFilter,
        selectedSort,
        syncState
    ) { data, query, filter, sort, sync ->
        createUiState(
            data.connection,
            data.games,
            data.profiles,
            query,
            filter,
            sort,
            sync
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SteamLibraryUiState()
    )

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun selectFilter(filter: SteamLibraryFilter) {
        selectedFilter.value = filter
    }

    fun selectSort(sort: SteamLibrarySort) {
        selectedSort.value = sort
    }

    fun openGame(appId: Int, onResolved: (Int) -> Unit) {
        if (
            syncState.value.openingGameAppId != null ||
            syncState.value.isSyncing
        ) return
        viewModelScope.launch {
            syncState.value = syncState.value.copy(
                openingGameAppId = appId,
                errorMessage = null
            )
            try {
                val match = steamRepository.resolveIgdbGame(appId)
                val igdbGameId = match.igdbGameId
                syncState.value = syncState.value.copy(openingGameAppId = null)
                when {
                    match.status == SteamIgdbMatchStatus.EXACT &&
                        igdbGameId != null -> onResolved(igdbGameId)

                    match.status == SteamIgdbMatchStatus.AMBIGUOUS ->
                        showError(
                            "IGDB has several possible pages for this Steam game."
                        )

                    else -> showError(
                        "IGDB does not have a page linked to this Steam game yet."
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: IOException) {
                showError("Unable to load this game from IGDB.")
            } catch (exception: HttpException) {
                showError("Game lookup failed with HTTP ${exception.code()}.")
            } catch (_: Exception) {
                showError("Unable to open this Steam game.")
            }
        }
    }

    fun sync() {
        val state = uiState.value
        if (
            state.profileUrl.isBlank() ||
            state.isSyncing ||
            state.activeProfileStatus != SteamProfileStatus.LINKED
        ) return

        viewModelScope.launch {
            syncState.value = SyncState(isSyncing = true)
            try {
                val result = steamRepository.checkConnection(state.profileUrl)
                steamRepository.saveProfileAndLibrary(result)
                preferences.saveConnection(
                    profileUrl = result.canonicalProfileUrl,
                    steamId = result.steamId,
                    personaName = result.personaName,
                    avatarUrl = result.avatarUrl
                )
                syncState.value = SyncState()
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: IOException) {
                showError("Unable to connect to Steam.")
            } catch (exception: HttpException) {
                showError("Steam request failed with HTTP ${exception.code()}.")
            } catch (_: Exception) {
                showError("Unable to update the Steam library.")
            }
        }
    }

    fun pauseActiveProfile() {
        val steamId = uiState.value.activeSteamId ?: return
        viewModelScope.launch {
            steamRepository.updateProfileStatus(
                steamId,
                SteamProfileStatus.PAUSED
            )
        }
    }

    fun unlinkActiveProfile(onComplete: () -> Unit = {}) {
        val steamId = uiState.value.activeSteamId ?: return
        viewModelScope.launch {
            steamRepository.updateProfileStatus(
                steamId,
                SteamProfileStatus.UNLINKED
            )
            preferences.clearConnection()
            onComplete()
        }
    }

    fun activateProfile(profile: SteamProfileUiModel) {
        viewModelScope.launch {
            steamRepository.updateProfileStatus(
                profile.steamId,
                SteamProfileStatus.LINKED
            )
            preferences.selectProfile(
                profileUrl = profile.profileUrl,
                steamId = profile.steamId,
                personaName = profile.personaName,
                avatarUrl = profile.avatarUrl,
                lastSyncAt = profile.lastSyncAt
            )
        }
    }

    fun deleteProfileData(
        profile: SteamProfileUiModel,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            steamRepository.deleteProfileData(profile.steamId)
            if (profile.steamId == uiState.value.activeSteamId) {
                preferences.clearConnection()
            }
            onComplete()
        }
    }

    private fun showError(message: String) {
        syncState.value = SyncState(errorMessage = message)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as GameStApplication
                SteamLibraryViewModel(
                    steamRepository = application.container.steamRepository,
                    preferences =
                        application.container.steamConnectionPreferences
                )
            }
        }
    }
}

private fun createUiState(
    connection: SteamConnectionData,
    games: List<SteamGame>,
    profiles: List<SteamProfile>,
    query: String,
    filter: SteamLibraryFilter,
    sort: SteamLibrarySort,
    sync: SyncState
): SteamLibraryUiState {
    val filteredGames = games
        .asSequence()
        .filter { game ->
            query.isBlank() || game.name.contains(query, ignoreCase = true)
        }
        .filter { game ->
            when (filter) {
                SteamLibraryFilter.ALL -> true
                SteamLibraryFilter.RECENT ->
                    (game.recentPlaytimeMinutes ?: 0) > 0
                SteamLibraryFilter.PLAYED -> game.totalPlaytimeMinutes > 0
                SteamLibraryFilter.NEVER_PLAYED ->
                    game.totalPlaytimeMinutes == 0L
            }
        }
        .let { sequence ->
            when (sort) {
                SteamLibrarySort.PLAYTIME ->
                    sequence.sortedByDescending(SteamGame::totalPlaytimeMinutes)
                SteamLibrarySort.RECENT ->
                    sequence.sortedByDescending { game ->
                        game.lastPlayedAt ?: Long.MIN_VALUE
                    }
                SteamLibrarySort.NAME ->
                    sequence.sortedBy { game -> game.name.lowercase() }
            }
        }
        .map(SteamGame::toUiModel)
        .toList()

    return SteamLibraryUiState(
        personaName = connection.personaName.ifBlank { "Steam user" },
        avatarUrl = connection.avatarUrl.takeIf(String::isNotBlank),
        profileUrl = connection.profileUrl,
        games = filteredGames,
        totalGamesCount = games.size,
        searchQuery = query,
        selectedFilter = filter,
        selectedSort = sort,
        lastSyncAt = connection.lastSyncAt,
        isSyncing = sync.isSyncing,
        openingGameAppId = sync.openingGameAppId,
        errorMessage = sync.errorMessage,
        profiles = profiles.map(SteamProfile::toUiModel),
        activeSteamId = connection.steamId.takeIf(String::isNotBlank),
        activeProfileStatus = profiles
            .firstOrNull { profile -> profile.steamId == connection.steamId }
            ?.status
    )
}

private fun SteamGame.toUiModel(): SteamLibraryGameUiModel {
    return SteamLibraryGameUiModel(
        appId = appId,
        name = name,
        imageUrl =
            "https://shared.fastly.steamstatic.com/store_item_assets/" +
                "steam/apps/$appId/header.jpg",
        totalPlaytimeMinutes = totalPlaytimeMinutes,
        recentPlaytimeMinutes = recentPlaytimeMinutes,
        lastPlayedAt = lastPlayedAt
    )
}

private data class SyncState(
    val isSyncing: Boolean = false,
    val openingGameAppId: Int? = null,
    val errorMessage: String? = null
)

private data class ProfileData(
    val connection: SteamConnectionData,
    val games: List<SteamGame>,
    val profiles: List<SteamProfile>
)

private fun SteamProfile.toUiModel() = SteamProfileUiModel(
    steamId = steamId,
    profileUrl = profileUrl,
    personaName = personaName,
    avatarUrl = avatarUrl,
    status = status,
    lastSyncAt = lastSyncAt
)
