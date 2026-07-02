package com.example.gamest.ui.screens.search

import android.adservices.adid.AdId
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SearchViewModel: ViewModel() {
    var uiState by mutableStateOf(SearchUiState())
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