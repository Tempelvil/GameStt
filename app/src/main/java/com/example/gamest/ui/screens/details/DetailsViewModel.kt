package com.example.gamest.ui.screens.details

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
import com.example.gamest.model.GameDetailsUiModel
import kotlinx.coroutines.launch

class GameDetailsViewModel(
    private val gameId: Int,
    private val repository: GamesRepository
) : ViewModel() {

    var uiState by mutableStateOf<GameDetailsUiState>(
        GameDetailsUiState.Loading
    )
        private set

    init{
        loadGameDetails()
    }
    private fun loadGameDetails(){
        viewModelScope.launch{
            uiState = GameDetailsUiState.Loading

            uiState = try{
                val game = repository.getGameDetails(gameId)
                GameDetailsUiState.Success(
                    game=game
                )
            }catch (e: Exception){
                GameDetailsUiState.Error(
                    message = e.message?:"Unknown error"
                )
            }
        }
    }
    companion object{
        fun factory(gameId: Int): ViewModelProvider.Factory{
            return viewModelFactory {
                initializer {
                    val application = this [APPLICATION_KEY] as GameStApplication

                    val repository = application.container.gamesRepository

                    GameDetailsViewModel(
                        gameId = gameId,
                        repository = repository
                    )
                }
            }
        }
    }
}


