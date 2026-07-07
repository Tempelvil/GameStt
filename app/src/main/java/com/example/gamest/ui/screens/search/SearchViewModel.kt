package com.example.gamest.ui.screens.search

import android.adservices.adid.AdId
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
import com.example.gamest.model.GameFilter
import com.example.gamest.model.GameUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call

class SearchViewModel(
    private val repository: GamesRepository
): ViewModel() {


    var uiState by mutableStateOf(SearchUiState())
        private set
    init{
        loadGames()
    }

    private var searchJob: Job?=null

    private fun loadGames() {
        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val games = repository.getGames(
                    searchQuery = uiState.searchQuery,
                    filter = uiState.selectedFilter,
                    page =uiState.
                )

                uiState = uiState.copy(
                    games = games,
                    isLoading = false
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        uiState = uiState.copy(searchQuery = query)

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            delay(500)
            loadGames()
        }
    }

    fun onGenreClick(filter: GameFilter) {
        uiState = uiState.copy(
            selectedFilter = filter,
            games = emptyList()
        )
        loadGames()
    }

    fun onSaveGameClick(gameId: Int) {
        uiState = uiState.copy(
            games = uiState.games.map { game ->
                if (game.id == gameId) {
                    game.copy(isSaved = !game.isSaved)
                } else {
                    game
                }
            }
        )
    }

    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GameStApplication)

                val repository = application.container.gamesRepository

                SearchViewModel(repository = repository)
            }
        }
    }

}