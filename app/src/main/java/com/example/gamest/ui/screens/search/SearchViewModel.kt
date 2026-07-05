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
import com.example.gamest.model.GameUiModel
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

    private fun loadGames() {
        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val games = repository.getTopRatedGames()

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
    }

    fun onGenreClick(genre: String) {
        uiState = uiState.copy(
            selectedGenre = if (uiState.selectedGenre == genre) null else genre
        )
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
private val fakeGames = listOf(
    GameUiModel(
        id = 1,
        title = "Elden Ring",
        imageUrl = "",
        rating = 9.4,
        genres = listOf("RPG"),
        platforms = listOf("PC"),
        isSaved = false
    ),
    GameUiModel(
        id = 2,
        title = "Cyberpunk 2077",
        imageUrl = "",
        rating = 8.9,
        genres = listOf("Action"),
        platforms = listOf("PC"),
        isSaved = true
    ),
    GameUiModel(
        id = 3,
        title = "The Witcher 3",
        imageUrl = "",
        rating = 9.5,
        genres = listOf("RPG"),
        platforms = listOf("PC"),
        isSaved = false
    ),
    GameUiModel(
        id = 4,
        title = "Hades",
        imageUrl = "",
        rating = 9.1,
        genres = listOf("Action"),
        platforms = listOf("Switch"),
        isSaved = false
    )
)