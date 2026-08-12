package com.example.gamest.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gamest.GameStApplication
import com.example.gamest.data.local.preferences.SteamConnectionPreferences
import com.example.gamest.data.repository.LocalGamesRepository
import com.example.gamest.data.repository.SteamGame
import com.example.gamest.data.repository.SteamPlaytimeDelta
import com.example.gamest.data.repository.SteamProfile
import com.example.gamest.data.repository.SteamRepository
import com.example.gamest.data.repository.SteamSync
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class StatisticsViewModel(
    private val localGamesRepository: LocalGamesRepository,
    private val steamRepository: SteamRepository,
    private val steamConnectionPreferences: SteamConnectionPreferences
) : ViewModel() {

    private val selectedSection = MutableStateFlow(StatisticsSection.PLAYED_GAMES)
    private val selectedSteamPeriod = MutableStateFlow(SteamStatisticsPeriod.MONTH)
    private val selectedSteamId = MutableStateFlow<String?>(null)

    private val steamSource: Flow<SteamStatisticsSource> = combine(
        steamRepository.observeProfiles(),
        steamConnectionPreferences.connectionData,
        selectedSteamId
    ) { profiles, connection, manuallySelectedId ->
        val selectedProfile = profiles.firstOrNull {
            it.steamId == manuallySelectedId
        } ?: profiles.firstOrNull {
            it.steamId == connection.steamId
        } ?: profiles.maxByOrNull { it.lastSyncAt ?: it.createdAt }
        SteamProfileSelection(profiles, selectedProfile)
    }.flatMapLatest { selection ->
        val profile = selection.selectedProfile
        if (profile == null) {
            flowOf(SteamStatisticsSource(profiles = selection.profiles))
        } else {
            combine(
                steamRepository.observeLibrary(profile.steamId),
                steamRepository.observeDeltas(profile.steamId),
                steamRepository.observeSyncs(profile.steamId)
            ) { games, deltas, syncs ->
                SteamStatisticsSource(
                    profiles = selection.profiles,
                    selectedProfile = profile,
                    games = games,
                    deltas = deltas,
                    syncs = syncs
                )
            }
        }
    }

    val uiState: StateFlow<StatisticsUiState> = combine(
        localGamesRepository.getAllGames(),
        selectedSection,
        selectedSteamPeriod,
        steamSource
    ) { games, section, period, steam ->
        StatisticsCalculator.calculate(games, section).copy(
            steamStatistics = steam.toUiModel(period)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatisticsUiState()
    )

    fun selectSection(section: StatisticsSection) {
        selectedSection.value = section
    }

    fun selectSteamPeriod(period: SteamStatisticsPeriod) {
        selectedSteamPeriod.value = period
    }

    fun selectSteamProfile(steamId: String) {
        selectedSteamId.value = steamId
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as GameStApplication
                StatisticsViewModel(
                    localGamesRepository = application.container.localGamesRepository,
                    steamRepository = application.container.steamRepository,
                    steamConnectionPreferences =
                        application.container.steamConnectionPreferences
                )
            }
        }
    }
}

private data class SteamProfileSelection(
    val profiles: List<SteamProfile>,
    val selectedProfile: SteamProfile?
)

private data class SteamStatisticsSource(
    val profiles: List<SteamProfile> = emptyList(),
    val selectedProfile: SteamProfile? = null,
    val games: List<SteamGame> = emptyList(),
    val deltas: List<SteamPlaytimeDelta> = emptyList(),
    val syncs: List<SteamSync> = emptyList()
)

private fun SteamStatisticsSource.toUiModel(
    period: SteamStatisticsPeriod
): SteamStatisticsUiModel {
    val profile = selectedProfile
    val trackedDeltas = deltas.filterNot { it.isUntrackedPeriod }
    val activity = createActivityPoints(trackedDeltas, period)
    val recentGames = games.filter { (it.recentPlaytimeMinutes ?: 0) > 0 }
        .sortedByDescending { it.recentPlaytimeMinutes }
        .map { game ->
            SteamRecentGameUiModel(
                appId = game.appId,
                name = game.name,
                imageUrl = "https://cdn.cloudflare.steamstatic.com/steam/apps/" +
                    "${game.appId}/header.jpg",
                totalMinutes = game.totalPlaytimeMinutes,
                recentMinutes = game.recentPlaytimeMinutes ?: 0
            )
        }
    return SteamStatisticsUiModel(
        profiles = profiles.map { steamProfile ->
            SteamStatisticsProfileUiModel(
                steamId = steamProfile.steamId,
                personaName = steamProfile.personaName,
                avatarUrl = steamProfile.avatarUrl,
                status = steamProfile.status
            )
        },
        selectedSteamId = profile?.steamId,
        personaName = profile?.personaName.orEmpty(),
        avatarUrl = profile?.avatarUrl,
        selectedPeriod = period,
        activity = activity,
        periodMinutes = activity.sumOf { it.minutes },
        totalMinutes = games.sumOf { it.totalPlaytimeMinutes },
        recentTwoWeeksMinutes = recentGames.sumOf { it.recentMinutes },
        recentGames = recentGames,
        untrackedMinutes = deltas.filter { it.isUntrackedPeriod }
            .sumOf { it.deltaMinutes }
    )
}

private fun createActivityPoints(
    deltas: List<SteamPlaytimeDelta>,
    period: SteamStatisticsPeriod,
    now: Long = System.currentTimeMillis()
): List<SteamActivityPointUiModel> {
    val calendar = Calendar.getInstance()
    return when (period) {
        SteamStatisticsPeriod.WEEK -> createDailyPoints(
            deltas = deltas,
            days = 7,
            labelPattern = "EEE",
            now = now
        )
        SteamStatisticsPeriod.MONTH -> createDailyPoints(
            deltas = deltas,
            days = 30,
            labelPattern = "d",
            now = now
        )
        SteamStatisticsPeriod.YEAR -> {
            val formatter = SimpleDateFormat("MMM", Locale.getDefault())
            (11 downTo 0).map { monthsAgo ->
                calendar.timeInMillis = now
                calendar.add(Calendar.MONTH, -monthsAgo)
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val minutes = deltas.filter { delta ->
                    calendar.timeInMillis = delta.recordedAt
                    calendar.get(Calendar.YEAR) == year &&
                        calendar.get(Calendar.MONTH) == month
                }.sumOf { it.deltaMinutes }
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                SteamActivityPointUiModel(formatter.format(calendar.time), minutes)
            }
        }
    }
}

private fun createDailyPoints(
    deltas: List<SteamPlaytimeDelta>,
    days: Int,
    labelPattern: String,
    now: Long
): List<SteamActivityPointUiModel> {
    val formatter = SimpleDateFormat(labelPattern, Locale.getDefault())
    val calendar = Calendar.getInstance()
    return (days - 1 downTo 0).map { daysAgo ->
        calendar.timeInMillis = now
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        val year = calendar.get(Calendar.YEAR)
        val day = calendar.get(Calendar.DAY_OF_YEAR)
        val minutes = deltas.filter { delta ->
            calendar.timeInMillis = delta.recordedAt
            calendar.get(Calendar.YEAR) == year &&
                calendar.get(Calendar.DAY_OF_YEAR) == day
        }.sumOf { it.deltaMinutes }
        calendar.timeInMillis = now
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        SteamActivityPointUiModel(formatter.format(Date(calendar.timeInMillis)), minutes)
    }
}
