package com.example.gamest.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gamest.GameStApplication
import com.example.gamest.data.repository.LocalGamesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class StatisticsViewModel(
    private val localGamesRepository: LocalGamesRepository
) : ViewModel() {

    private val selectedSection = MutableStateFlow(
        StatisticsSection.PLAYED_GAMES
    )

    val uiState: StateFlow<StatisticsUiState> =
        combine(
            localGamesRepository.getAllGames(),
            selectedSection
        ) { games, section ->
            StatisticsCalculator.calculate(
                games = games,
                selectedSection = section
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(
                    stopTimeoutMillis = 5_000
                ),
                initialValue = StatisticsUiState()
            )

    fun selectSection(section: StatisticsSection) {
        selectedSection.value = section
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[APPLICATION_KEY] as GameStApplication

                StatisticsViewModel(
                    localGamesRepository =
                        application.container.localGamesRepository
                )
            }
        }
    }
}
