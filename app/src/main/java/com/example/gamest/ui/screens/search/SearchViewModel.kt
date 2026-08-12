package com.example.gamest.ui.screens.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gamest.GameStApplication
import com.example.gamest.data.repository.GamesRepository
import com.example.gamest.data.repository.LocalGamesRepository
import com.example.gamest.model.GameFilter
import com.example.gamest.model.GameUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repository: GamesRepository,
    private val localGamesRepository: LocalGamesRepository
): ViewModel() {


    var uiState by mutableStateOf(SearchUiState())
        private set
    private var savedGameIds: Set<Int> = emptySet()

    init {
        observeSavedGames()
        loadGames()
    }

    private fun observeSavedGames() {
        viewModelScope.launch {
            localGamesRepository.observeSavedGameIds().collect { ids ->
                savedGameIds = ids
                uiState = uiState.copy(
                    games = uiState.games.withSavedState(ids)
                )
            }
        }
    }

    private var searchJob: Job?=null

    fun loadGenres() {
        if (
            uiState.availableGenres.isNotEmpty() ||
            uiState.isGenresLoading
        ) {
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(
                isGenresLoading = true,
                genresErrorMessage = null
            )

            try {
                val genres = repository.getGenres()

                uiState = uiState.copy(
                    availableGenres = genres,
                    isGenresLoading = false
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isGenresLoading = false,
                    genresErrorMessage =
                        "Unable to load genres"
                )
            }
        }
    }
    private fun loadGames() {
        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                isLoadingMore = false,
                page = 1,
                canLoadMore = true,
                errorMessage = null
            )

            try {
                val games = repository.getGames(
                    searchQuery = uiState.searchQuery,
                    filter = uiState.selectedFilter,
                    page = 1
                )

                uiState = uiState.copy(
                    games = games.withSavedState(savedGameIds),
                    isLoading = false,
                    page = 1,
                    canLoadMore = games.isNotEmpty()
                )
            } catch (e: Exception) {
                e.printStackTrace()

                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "${e::class.simpleName}: ${e.message}"
                )
            }
        }
    }

    fun loadNextPage() {
        if (uiState.isLoading || uiState.isLoadingMore || !uiState.canLoadMore) {
            return
        }

        viewModelScope.launch {
            val nextPage = uiState.page + 1

            uiState = uiState.copy(
                isLoadingMore = true,
                errorMessage = null
            )

            try {
                val newGames = repository.getGames(
                    searchQuery = uiState.searchQuery,
                    filter = uiState.selectedFilter,
                    page = nextPage
                )

                uiState = uiState.copy(
                    games = (
                        uiState.games +
                            newGames.withSavedState(savedGameIds)
                        ),
                    isLoadingMore = false,
                    page = nextPage,
                    canLoadMore = newGames.isNotEmpty()
                )
            } catch (e: Exception) {
                e.printStackTrace()

                uiState = uiState.copy(
                    isLoadingMore = false,
                    errorMessage = "${e::class.simpleName}: ${e.message}"
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        uiState = uiState.copy(searchQuery = query)

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            delay(500)

            uiState = uiState.copy(
                games = emptyList(),
                page = 1,
                canLoadMore = true
            )

            loadGames()
        }
    }

    fun applyFilter(filter: GameFilter) {
        searchJob?.cancel()

        uiState = uiState.copy(
            searchQuery = "",
            selectedFilter = filter,
            games = emptyList(),
            page = 1,
            canLoadMore = true,
            errorMessage = null
        )

        loadGames()
    }


    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GameStApplication)

                val repository = application.container.gamesRepository

                SearchViewModel(
                    repository = repository,
                    localGamesRepository =
                        application.container.localGamesRepository
                )
            }
        }
    }

}

private fun List<GameUiModel>.withSavedState(
    savedGameIds: Set<Int>
): List<GameUiModel> {
    return map { game ->
        game.copy(isSaved = game.id in savedGameIds)
    }
}
