package com.example.gamest.ui.screens.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gamest.GameStApplication
import com.example.gamest.data.local.GameStatus
import com.example.gamest.data.repository.LocalGamesRepository
import com.example.gamest.ui.screens.collections.CollectionFilter
import com.example.gamest.ui.screens.collections.CollectionUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CollectionViewModel(
    private val localGamesRepository: LocalGamesRepository
) : ViewModel() {

    private val selectedFilter =
        MutableStateFlow(
            CollectionFilter.ALL
        )

    private val selectedSort =
        MutableStateFlow(
            CollectionSort.RECENTLY_ADDED
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState =
        combine(
            selectedFilter,
            selectedSort
        ) { filter, sort ->

            filter to sort
        }
            .flatMapLatest { (filter, sort) ->

                localGamesRepository.observeGames(
                    status = filter.status,
                    sort = sort.gameSort
                )
                    .map { games ->

                        CollectionUiState(
                            games = games,
                            selectedFilter = filter,
                            selectedSort = sort,
                            isLoading = false
                        )
                    }
            }
            .stateIn(
                scope = viewModelScope,

                started =
                    SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = 5_000
                    ),

                initialValue =
                    CollectionUiState()
            )

    fun selectFilter(
        filter: CollectionFilter
    ) {
        selectedFilter.value = filter
    }

    fun selectSort(
        sort: CollectionSort
    ) {
        selectedSort.value = sort
    }

    fun updateGame(
        gameId: Int,
        status: GameStatus,
        userRating: Int?,
        hoursPlayed: Int
    ) {
        viewModelScope.launch {
            localGamesRepository.updatePersonalData(
                gameId = gameId,
                status = status,
                userRating = userRating,
                hoursPlayed = hoursPlayed
            )
        }
    }

    companion object {
        val Factory =
            viewModelFactory {
                initializer {
                    val application =
                        this[APPLICATION_KEY] as GameStApplication
                    CollectionViewModel(
                        localGamesRepository =
                            application
                                .container
                                .localGamesRepository
                    )
                }
            }
    }
}