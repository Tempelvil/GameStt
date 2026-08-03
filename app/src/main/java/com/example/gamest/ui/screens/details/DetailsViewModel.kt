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
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

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
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                GameDetailsUiState.Error(
                    message = getHttpErrorMessage(e.code())
                )
            } catch (e: IOException) {
                GameDetailsUiState.Error(
                    message = "Unable to connect to RAWG. Check your internet connection and try again."
                )
            } catch (e: Exception) {
                GameDetailsUiState.Error(
                    message = "An unexpected error occurred. Please try again."
                )
            }
        }
    }
    fun retry() {
        loadGameDetails()
    }
    private fun getHttpErrorMessage(code: Int): String {
        return when (code) {
            401 -> "The RAWG API key is invalid."
            403 -> "Access to the RAWG API is denied."
            404 -> "Game information was not found."
            429 -> "The RAWG request limit has been exceeded."
            500, 502, 503, 504, 522 ->
                "RAWG is temporarily unavailable. Please try again later."

            else -> "Server error. HTTP code: $code"
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


