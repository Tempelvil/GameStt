package com.example.gamest.ui.screens.search

import android.adservices.adid.AdId
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.gamest.model.GameUiModel

class SearchViewModel: ViewModel() {
    var uiState by mutableStateOf(
        SearchUiState(
            games = fakeGames
        )
    )
        private set

    fun onSearchQueryChange(query: String){
        uiState = uiState.copy(searchQuery = query)
    }
    fun onSaveGameClick(gameId: Int){
        uiState = uiState.copy(
            games = uiState.games.map { game ->
                if (game.id==gameId){
                    game.copy(isSaved = !game.isSaved)
                }else{
                    game
                }
            }
        )
    }
    fun onGenreClick(genre: String) {
        uiState = uiState.copy(selectedGenre = genre)
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